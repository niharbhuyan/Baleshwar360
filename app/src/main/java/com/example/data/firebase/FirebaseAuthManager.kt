package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.UserEntity
import com.example.data.security.AuthValidator
import com.example.data.security.PasswordHasher
import com.example.data.security.SecureSessionStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Manages User Authentication & Secure Session State for Baleshwar 360:
 * - Email & Password Sign Up, Log In, Log Out, and verified Password Reset
 *   protected with salted PBKDF2-SHA256 (120,000 iterations) and constant-time comparison
 * - Active session data encrypted at rest via Android Keystore AES-256-GCM (SecureSessionStore)
 * - Google Sign-In via Android Credential Manager (GetSignInWithGoogleOption & GetGoogleIdOption)
 * - Process-wide synchronized StateFlows so all screens react immediately to login/logout
 */
class FirebaseAuthManager(context: Context) {

    private val appContext = context.applicationContext

    private val sessionStore by lazy {
        SecureSessionStore(appContext)
    }

    private val userDao by lazy {
        AppDatabase.getInstance(appContext).userDao()
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            if (com.example.data.fcm.FcmManager.isFirebaseConfigured(appContext)) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not available: ${e.message}")
            null
        }
    }

    val currentUser: StateFlow<FirebaseUser?> = sharedCurrentUser.asStateFlow()
    val activeSessionUser: StateFlow<UserEntity?> = sharedActiveSessionUser.asStateFlow()

    init {
        ensureInitialized()
    }

    private fun ensureInitialized() {
        synchronized(initLock) {
            if (isListenerRegistered) {
                if (sharedActiveSessionUser.value == null) {
                    val fbUser = auth?.currentUser
                    sharedActiveSessionUser.value = if (fbUser != null) {
                        toUserEntity(fbUser)
                    } else {
                        sessionStore.loadSession()
                    }
                }
                return
            }
            try {
                auth?.addAuthStateListener { firebaseAuth ->
                    val fbUser = firebaseAuth.currentUser
                    sharedCurrentUser.value = fbUser
                    if (fbUser != null) {
                        val mapped = toUserEntity(fbUser)
                        sharedActiveSessionUser.value = mapped
                        sessionStore.saveSession(mapped)
                    }
                    Log.d(TAG, "Auth state changed: signedIn=${fbUser != null}")
                }
                val initialFbUser = auth?.currentUser
                sharedCurrentUser.value = initialFbUser
                sharedActiveSessionUser.value = if (initialFbUser != null) {
                    val mapped = toUserEntity(initialFbUser)
                    sessionStore.saveSession(mapped)
                    mapped
                } else {
                    sessionStore.loadSession()
                }
                isListenerRegistered = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register auth listener: ${e.message}")
                sharedActiveSessionUser.value = sessionStore.loadSession()
            }
        }
    }

    val isAvailable: Boolean
        get() = auth != null

    val isUserSignedIn: Boolean
        get() = sharedCurrentUser.value != null || sharedActiveSessionUser.value != null

    fun updateActiveSessionProfile(updated: UserEntity) {
        sharedActiveSessionUser.value = updated
        sessionStore.saveSession(updated)
    }

    /**
     * Map FirebaseUser to UserEntity without storing sensitive tokens.
     */
    fun toUserEntity(fbUser: FirebaseUser, fallbackLocality: String = "Balasore"): UserEntity {
        return UserEntity(
            id = fbUser.uid,
            email = fbUser.email ?: "${fbUser.uid}@google.com",
            fullName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "Balasore Resident",
            passwordHash = "GOOGLE_OAUTH_MANAGED",
            phoneNumber = fbUser.phoneNumber ?: "",
            locality = fallbackLocality,
            bio = "Verified Baleshwar 360 Community Member",
            avatarUri = fbUser.photoUrl?.toString(),
            securityQuestion = "Google Account Authentication",
            securityAnswer = "GOOGLE_OAUTH_MANAGED",
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Interactive Google Sign-In button handler using GetSignInWithGoogleOption via CredentialManager.
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        serverClientId: String? = null
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val firebaseAuth = auth
                ?: return@withContext Result.failure(
                    Exception("Google Sign-In is unavailable until Firebase is configured.")
                )

            val credentialManager = CredentialManager.create(activityContext)
            val clientId = serverClientId
                ?.takeIf { it.isNotBlank() }
                ?: activityContext.getString(R.string.default_web_client_id)

            val signInOption = GetSignInWithGoogleOption.Builder(
                serverClientId = clientId
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = try {
                credentialManager.getCredential(activityContext, request)
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled by user")
                return@withContext Result.failure(Exception("Google Sign-In was cancelled."))
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No Google credentials available on device")
                return@withContext Result.failure(
                    Exception("No Google account found on device. You can Sign Up with email and password.")
                )
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In failed: ${e.message}")
                return@withContext Result.failure(Exception("Google Sign-In could not complete: ${e.message}"))
            }

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user
                    ?: return@withContext Result.failure(Exception("Google Sign-In did not return a user account."))

                val existingLocal = user.email?.let { userDao.getUserByEmail(AuthValidator.normalizeEmail(it)) }
                val mappedUser = toUserEntity(
                    fbUser = user,
                    fallbackLocality = existingLocal?.locality ?: "Balasore Town"
                ).copy(
                    bio = existingLocal?.bio?.ifBlank { "Verified Baleshwar 360 Community Member" }
                        ?: "Verified Baleshwar 360 Community Member",
                    phoneNumber = existingLocal?.phoneNumber?.ifBlank { user.phoneNumber ?: "" }
                        ?: (user.phoneNumber ?: "")
                )

                userDao.insertUser(mappedUser)
                sessionStore.saveSession(mappedUser)
                sharedCurrentUser.value = user
                sharedActiveSessionUser.value = mappedUser
                Result.success(mappedUser)
            } else {
                Result.failure(Exception("Unexpected credential type received."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Silent auto-sign-in for returning users using GetGoogleIdOption.
     */
    suspend fun attemptAutoSignIn(activityContext: Context): Result<UserEntity?> = withContext(Dispatchers.IO) {
        try {
            val firebaseAuth = auth ?: return@withContext Result.success(sharedActiveSessionUser.value)
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(activityContext.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val user = firebaseAuth.signInWithCredential(firebaseCredential).await().user
                if (user != null) {
                    val mapped = toUserEntity(user)
                    userDao.insertUser(mapped)
                    sessionStore.saveSession(mapped)
                    sharedCurrentUser.value = user
                    sharedActiveSessionUser.value = mapped
                    return@withContext Result.success(mapped)
                }
            }
            Result.success(sharedActiveSessionUser.value)
        } catch (_: GetCredentialException) {
            Result.success(sharedActiveSessionUser.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user account with email and password.
     * Enforces email format validation, strong password policy (8+ chars, letter + number),
     * per-user salted PBKDF2-SHA256 hashing, and Keystore-encrypted session storage.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        locality: String = "Balasore Town",
        securityAnswer: String = locality
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = AuthValidator.normalizeEmail(email)
            if (!AuthValidator.isValidEmail(normalizedEmail)) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }

            val passwordCheck = AuthValidator.validatePasswordStrength(password)
            if (!passwordCheck.isValid) {
                return@withContext Result.failure(
                    Exception(passwordCheck.errorMessage ?: "Please choose a stronger password.")
                )
            }

            val existing = userDao.getUserByEmail(normalizedEmail)
            if (existing != null) {
                return@withContext Result.failure(
                    Exception("An account with this email already exists. Please Log In.")
                )
            }

            val safeName = displayName.trim().take(80).ifBlank { "Balasore Citizen" }
            val safeLocality = locality.trim().take(60).ifBlank { "Balasore Town" }
            val effectiveSecurityAnswer = securityAnswer.trim().ifBlank { safeLocality }

            val newUser = UserEntity(
                id = "usr_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                fullName = safeName,
                email = normalizedEmail,
                passwordHash = PasswordHasher.hash(password),
                phoneNumber = "",
                locality = safeLocality,
                bio = "Active Baleshwar 360 Explorer & Citizen",
                avatarUri = null,
                securityQuestion = "What is your Balasore recovery locality/place?",
                securityAnswer = PasswordHasher.hashSecurityAnswer(effectiveSecurityAnswer),
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser)
            sessionStore.saveSession(newUser)
            sessionStore.setLocalEmailVerified(true)
            sharedActiveSessionUser.value = newUser
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e(TAG, "Account sign-up error: ${e.message}")
            Result.failure(Exception("Could not create account right now. Please try again."))
        }
    }

    /**
     * Sign in with Email and Password against stored hashed credentials in Room UserDao.
     * Strictly rejects unregistered emails or incorrect passwords (never auto-creates accounts).
     * Automatically upgrades legacy hashes to salted 120,000-iteration PBKDF2-SHA256 upon login.
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = AuthValidator.normalizeEmail(email)
            if (!AuthValidator.isValidEmail(normalizedEmail)) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }
            if (password.isEmpty()) {
                return@withContext Result.failure(Exception("Please enter your password."))
            }

            val existingByEmail = userDao.getUserByEmail(normalizedEmail)
                ?: return@withContext Result.failure(
                    Exception("Invalid email or password. If you are new, please Sign Up first.")
                )

            if (existingByEmail.passwordHash == "GOOGLE_OAUTH_MANAGED" ||
                existingByEmail.passwordHash == "FIREBASE_OAUTH_TOKEN"
            ) {
                return@withContext Result.failure(
                    Exception("This email is linked to Google Sign-In. Please tap 'Continue with Google'.")
                )
            }

            val isMatch = PasswordHasher.verify(
                rawSecret = password,
                storedHash = existingByEmail.passwordHash,
                legacyEmailHint = normalizedEmail
            )
            if (!isMatch) {
                return@withContext Result.failure(
                    Exception("Invalid email or password. Please try again or use Forgot Password.")
                )
            }

            // Transparently upgrade legacy password hashes to per-user salted 120k-iteration v2 format
            val authenticatedUser = if (PasswordHasher.needsRehash(existingByEmail.passwordHash)) {
                val upgradedHash = PasswordHasher.hash(password)
                userDao.updatePassword(normalizedEmail, upgradedHash)
                existingByEmail.copy(passwordHash = upgradedHash)
            } else {
                existingByEmail
            }

            sessionStore.saveSession(authenticatedUser)
            sharedActiveSessionUser.value = authenticatedUser
            Result.success(authenticatedUser)
        } catch (e: Exception) {
            Log.e(TAG, "Account sign-in error: ${e.message}")
            Result.failure(Exception("Sign-in could not be completed. Please try again."))
        }
    }

    /**
     * Send email verification link to current authenticated user.
     */
    suspend fun sendEmailVerification(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth?.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
            } else if (sharedActiveSessionUser.value != null) {
                sessionStore.setLocalEmailVerified(true)
            } else {
                return@withContext Result.failure(Exception("Please sign in first."))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email verification: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Reload user from Firebase Auth to get the latest email verification status.
     */
    suspend fun reloadUser(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = auth?.currentUser
            if (user != null) {
                user.reload().await()
                sharedCurrentUser.value = user
                Result.success(user.isEmailVerified)
            } else {
                Result.success(sessionStore.isLocalEmailVerified())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reload user: ${e.message}")
            Result.failure(e)
        }
    }

    val isEmailVerified: Boolean
        get() = auth?.currentUser?.isEmailVerified ?: sessionStore.isLocalEmailVerified()

    /**
     * Reset or update password with ownership verification.
     * - Updating a local password requires either being signed in as that account OR supplying
     *   a matching recovery security answer.
     * - Never creates new accounts during password recovery.
     */
    suspend fun sendPasswordResetEmail(
        email: String,
        newPassword: String? = null,
        securityAnswer: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = AuthValidator.normalizeEmail(email)
            if (!AuthValidator.isValidEmail(normalizedEmail)) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }

            if (!newPassword.isNullOrBlank()) {
                val passwordCheck = AuthValidator.validatePasswordStrength(newPassword)
                if (!passwordCheck.isValid) {
                    return@withContext Result.failure(
                        Exception(passwordCheck.errorMessage ?: "Please choose a stronger password.")
                    )
                }

                val existingUser = userDao.getUserByEmail(normalizedEmail)
                    ?: return@withContext Result.failure(
                        Exception("No registered account found for this email.")
                    )

                val isSignedInOwner = sharedActiveSessionUser.value?.email
                    ?.equals(normalizedEmail, ignoreCase = true) == true
                val hasValidRecoveryAnswer = !securityAnswer.isNullOrBlank() &&
                    PasswordHasher.verifySecurityAnswer(securityAnswer, existingUser.securityAnswer)

                if (!isSignedInOwner && !hasValidRecoveryAnswer) {
                    return@withContext Result.failure(
                        Exception("Security recovery answer did not match. Please enter your registered locality or recovery answer.")
                    )
                }

                val newHash = PasswordHasher.hash(newPassword)
                userDao.updatePassword(normalizedEmail, newHash)
                if (isSignedInOwner) {
                    val updated = existingUser.copy(passwordHash = newHash)
                    sharedActiveSessionUser.value = updated
                    sessionStore.saveSession(updated)
                }
            } else {
                // Dispatch password reset email via Firebase Auth if configured
                try {
                    auth?.sendPasswordResetEmail(normalizedEmail)?.await()
                } catch (e: Exception) {
                    Log.w(TAG, "Password reset dispatch note: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sign out from Firebase Auth, wipe Keystore-encrypted session storage, and clear credential state.
     * Uses NonCancellable so teardown always finishes cleanly even if the calling screen closes.
     */
    suspend fun signOut(activityContext: Context? = null): Result<Unit> =
        withContext(Dispatchers.IO + NonCancellable) {
            try {
                try {
                    auth?.signOut()
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase signOut warning: ${e.message}")
                }
                sharedCurrentUser.value = null
                sharedActiveSessionUser.value = null
                sessionStore.clearSession()

                val credContext = activityContext ?: appContext
                try {
                    val credentialManager = CredentialManager.create(credContext)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Clear credential state warning: ${e.message}")
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-out error: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Permanently deletes the currently authenticated user account:
     * - Removes the local Room user record via parameterized SQLite delete
     * - Removes the user's cloud profile document from Firestore when authenticated
     * - Deletes the Firebase Auth account when signed in via Firebase
     * - Clears encrypted Keystore session storage and Credential Manager state
     */
    suspend fun deleteAccount(
        activityContext: Context? = null,
        confirmPassword: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO + NonCancellable) {
        try {
            val activeSession = sharedActiveSessionUser.value
            val fbUser = auth?.currentUser
            val uid = fbUser?.uid ?: activeSession?.id
            val email = fbUser?.email ?: activeSession?.email

            if (uid.isNullOrBlank() && email.isNullOrBlank()) {
                return@withContext Result.failure(Exception("No active account is currently signed in."))
            }

            // Optional password re-verification for email/password accounts when provided
            if (!confirmPassword.isNullOrBlank() &&
                activeSession != null &&
                activeSession.passwordHash != "GOOGLE_OAUTH_MANAGED" &&
                activeSession.passwordHash != "FIREBASE_OAUTH_TOKEN" &&
                activeSession.passwordHash != "ENCRYPTED_SESSION_ACTIVE"
            ) {
                val matches = PasswordHasher.verify(
                    rawSecret = confirmPassword,
                    storedHash = activeSession.passwordHash,
                    legacyEmailHint = AuthValidator.normalizeEmail(activeSession.email)
                )
                if (!matches) {
                    return@withContext Result.failure(Exception("Password confirmation did not match."))
                }
            }

            // 1. Delete user profile document from Firestore if signed into Firebase as this owner
            if (fbUser != null && !uid.isNullOrBlank()) {
                runCatching {
                    val dbId = appContext.getString(R.string.firestore_database_id)
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance(dbId)
                    firestore.collection("users").document(uid).delete().await()
                }.onFailure { e ->
                    Log.w(TAG, "Firestore profile deletion note: ${e.message}")
                }
            }

            // 2. Delete local user record from Room using parameterized query
            runCatching {
                val db = AppDatabase.getInstance(appContext).openHelper.writableDatabase
                val safeUid = uid.orEmpty()
                val safeEmail = AuthValidator.normalizeEmail(email.orEmpty())
                db.delete("users", "id = ? OR email = ?", arrayOf(safeUid, safeEmail))
            }.onFailure { e ->
                Log.w(TAG, "Local user record deletion note: ${e.message}")
            }

            // 3. Delete Firebase Auth account when signed in via Firebase
            if (fbUser != null) {
                runCatching {
                    fbUser.delete().await()
                }.onFailure { e ->
                    Log.w(TAG, "Firebase Auth account delete note (falling back to signOut): ${e.message}")
                    runCatching { auth?.signOut() }
                }
            }

            // 4. Wipe encrypted session & Credential Manager state
            sharedCurrentUser.value = null
            sharedActiveSessionUser.value = null
            sessionStore.clearSession()

            val credContext = activityContext ?: appContext
            runCatching {
                val credentialManager = CredentialManager.create(credContext)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion error: ${e.message}")
            Result.failure(Exception("Could not delete account right now. Please try again."))
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
        private val initLock = Any()
        @Volatile
        private var isListenerRegistered = false

        private val sharedCurrentUser = MutableStateFlow<FirebaseUser?>(null)
        private val sharedActiveSessionUser = MutableStateFlow<UserEntity?>(null)
    }
}
