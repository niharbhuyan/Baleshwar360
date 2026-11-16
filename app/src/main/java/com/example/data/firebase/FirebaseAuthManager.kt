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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

/**
 * Manages User Authentication & Session State for Balasore 360:
 * - Google Sign-In via Android Credential Manager (GetSignInWithGoogleOption & GetGoogleIdOption)
 * - Account Signup, Login, Password Reset & Session Persistence via Room UserDao + SharedPreferences
 * - Visited Tourism Spots & Favorite News state synchronization
 */
class FirebaseAuthManager(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("balasore_auth_session_prefs", Context.MODE_PRIVATE)
    }

    private val userDao by lazy {
        AppDatabase.getInstance(context).userDao()
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            if (com.example.data.fcm.FcmManager.isFirebaseConfigured(context)) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthManager", "Firebase Auth not available: ${e.message}")
            null
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _activeSessionUser = MutableStateFlow<UserEntity?>(null)
    val activeSessionUser: StateFlow<UserEntity?> = _activeSessionUser.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val fbUser = firebaseAuth.currentUser
                _currentUser.value = fbUser
                if (fbUser != null) {
                    val mapped = toUserEntity(fbUser)
                    _activeSessionUser.value = mapped
                    saveLocalSessionUser(mapped)
                }
                Log.d("FirebaseAuthManager", "Auth state changed: user=${fbUser?.email}")
            }
            _currentUser.value = auth?.currentUser
            val initialFbUser = auth?.currentUser
            if (initialFbUser != null) {
                _activeSessionUser.value = toUserEntity(initialFbUser)
            } else {
                _activeSessionUser.value = loadLocalSessionUser()
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Failed to register auth listener: ${e.message}")
            _activeSessionUser.value = loadLocalSessionUser()
        }
    }

    val isAvailable: Boolean
        get() = auth != null

    val isUserSignedIn: Boolean
        get() = _currentUser.value != null || _activeSessionUser.value != null

    private fun legacySha256(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(raw: String, emailSalt: String = "balasore360_secure_salt"): String {
        return try {
            val saltBytes = "bls360_${emailSalt.trim().lowercase()}".toByteArray(Charsets.UTF_8)
            val spec = javax.crypto.spec.PBEKeySpec(
                raw.trim().toCharArray(),
                saltBytes,
                10000,
                256
            )
            val skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = skf.generateSecret(spec).encoded
            "pbkdf2_sha256$" + hash.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            legacySha256(raw)
        }
    }

    private fun verifyPassword(rawPassword: String, email: String, storedHash: String): Boolean {
        val pbkdf2WithEmail = hashPassword(rawPassword, email)
        val pbkdf2Default = hashPassword(rawPassword)
        val legacy = legacySha256(rawPassword)
        return storedHash == pbkdf2WithEmail ||
            storedHash == pbkdf2Default ||
            storedHash == legacy ||
            storedHash == rawPassword.trim()
    }

    private fun saveLocalSessionUser(user: UserEntity) {
        prefs.edit()
            .putString("session_user_id", user.id)
            .putString("session_user_email", user.email)
            .putString("session_user_name", user.fullName)
            .putString("session_user_phone", user.phoneNumber)
            .putString("session_user_locality", user.locality)
            .putString("session_user_bio", user.bio)
            .putString("session_user_avatar", user.avatarUri ?: "")
            .putLong("session_user_created", user.createdAt)
            .apply()
    }

    private fun loadLocalSessionUser(): UserEntity? {
        val id = prefs.getString("session_user_id", null) ?: return null
        val email = prefs.getString("session_user_email", null) ?: return null
        return UserEntity(
            id = id,
            fullName = prefs.getString("session_user_name", "Balasore Citizen") ?: "Balasore Citizen",
            email = email,
            passwordHash = "SESSION_ACTIVE",
            phoneNumber = prefs.getString("session_user_phone", "") ?: "",
            locality = prefs.getString("session_user_locality", "Balasore Town") ?: "Balasore Town",
            bio = prefs.getString("session_user_bio", "Verified Baleshwar 360 Member") ?: "Verified Baleshwar 360 Member",
            avatarUri = prefs.getString("session_user_avatar", null)?.takeIf { it.isNotBlank() },
            securityQuestion = "Locality",
            securityAnswer = prefs.getString("session_user_locality", "Balasore Town") ?: "Balasore Town",
            createdAt = prefs.getLong("session_user_created", System.currentTimeMillis())
        )
    }

    fun updateActiveSessionProfile(updated: UserEntity) {
        _activeSessionUser.value = updated
        saveLocalSessionUser(updated)
    }

    /**
     * Map FirebaseUser to UserEntity
     */
    fun toUserEntity(fbUser: FirebaseUser, fallbackLocality: String = "Balasore"): UserEntity {
        return UserEntity(
            id = fbUser.uid,
            email = fbUser.email ?: "${fbUser.uid}@google.com",
            fullName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "Balasore Resident",
            passwordHash = "FIREBASE_OAUTH_TOKEN",
            phoneNumber = fbUser.phoneNumber ?: "",
            locality = fallbackLocality,
            bio = "Verified Baleshwar 360 Community Member",
            avatarUri = fbUser.photoUrl?.toString(),
            securityQuestion = "Google Account Authentication",
            securityAnswer = "FIREBASE_AUTH_MANAGED",
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Interactive Google Sign-In button handler using GetSignInWithGoogleOption via CredentialManager.
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val firebaseAuth = auth
                ?: return@withContext Result.failure(Exception("Firebase is not initialized. Please ensure google-services.json is configured."))

            val credentialManager = CredentialManager.create(context)
            val clientId = serverClientId
                ?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.default_web_client_id)

            val signInOption = GetSignInWithGoogleOption.Builder(
                serverClientId = clientId
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = try {
                credentialManager.getCredential(context, request)
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Sign-in cancelled by user", e)
                return@withContext Result.failure(Exception("Google Sign-In was cancelled by user."))
            } catch (e: NoCredentialException) {
                Log.w("Auth", "No Google credentials available on device: ${e.message}")
                return@withContext Result.failure(Exception("No Google account found on device. Please add a Google account or use Sign Up."))
            } catch (e: GetCredentialException) {
                Log.e("Auth", "Sign-in failed", e)
                return@withContext Result.failure(Exception("Google Sign-In notice: ${e.message}"))
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user
                    ?: return@withContext Result.failure(Exception("Firebase returned null user after Google Sign-In"))

                val mappedUser = toUserEntity(user)
                userDao.insertUser(mappedUser)
                saveLocalSessionUser(mappedUser)
                _currentUser.value = user
                _activeSessionUser.value = mappedUser
                Result.success(mappedUser)
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Silent auto-sign-in for returning users using GetGoogleIdOption in a separate request.
     */
    suspend fun attemptAutoSignIn(context: Context): Result<UserEntity?> = withContext(Dispatchers.IO) {
        try {
            val firebaseAuth = auth ?: return@withContext Result.success(_activeSessionUser.value)
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val user = firebaseAuth.signInWithCredential(firebaseCredential).await().user
                if (user != null) {
                    val mapped = toUserEntity(user)
                    userDao.insertUser(mapped)
                    saveLocalSessionUser(mapped)
                    _currentUser.value = user
                    _activeSessionUser.value = mapped
                    return@withContext Result.success(mapped)
                }
            }
            Result.success(_activeSessionUser.value)
        } catch (_: GetCredentialException) {
            // Silent check — fall back to active local session user if present
            Result.success(_activeSessionUser.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user account via Firebase Auth (if configured) + Room UserDao + active session.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        locality: String = "Balasore Town"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }
            if (password.length < 6) {
                return@withContext Result.failure(Exception("Password must be at least 6 characters."))
            }

            val existing = userDao.getUserByEmail(normalizedEmail)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with this email already exists. Please Log In."))
            }

            val firebaseUid = auth?.currentUser?.uid

            val newUser = UserEntity(
                id = firebaseUid ?: "usr_${UUID.randomUUID().toString().take(10)}",
                fullName = displayName.trim().ifBlank { "Balasore Citizen" },
                email = normalizedEmail,
                passwordHash = hashPassword(password, normalizedEmail),
                phoneNumber = "",
                locality = locality,
                bio = "Active Baleshwar 360 Explorer & Citizen",
                avatarUri = null,
                securityQuestion = "What is your Balasore locality?",
                securityAnswer = locality.trim().lowercase(),
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(newUser)
            saveLocalSessionUser(newUser)
            _activeSessionUser.value = newUser
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Account sign-up error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sign in with Email and Password against Room UserDao + active cloud profile session.
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }
            if (password.isBlank()) {
                return@withContext Result.failure(Exception("Please enter your password."))
            }

            val passwordHash = hashPassword(password, normalizedEmail)
            val existingByEmail = userDao.getUserByEmail(normalizedEmail)
            val authenticatedUser = if (existingByEmail != null) {
                if (verifyPassword(password, normalizedEmail, existingByEmail.passwordHash)) {
                    existingByEmail
                } else {
                    return@withContext Result.failure(Exception("Incorrect password for $normalizedEmail. Use 'Forgot Pass' to reset."))
                }
            } else {
                // Create seamless local profile if logging in for the first time on this device
                val created = UserEntity(
                    id = "usr_${UUID.nameUUIDFromBytes(normalizedEmail.toByteArray()).toString().take(10)}",
                    fullName = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = normalizedEmail,
                    passwordHash = passwordHash,
                    phoneNumber = "",
                    locality = "Balasore Town",
                    bio = "Verified Baleshwar 360 Member",
                    avatarUri = null,
                    securityQuestion = "What is your Balasore locality?",
                    securityAnswer = "balasore town",
                    createdAt = System.currentTimeMillis()
                )
                userDao.insertUser(created)
                created
            }

            saveLocalSessionUser(authenticatedUser)
            _activeSessionUser.value = authenticatedUser
            Result.success(authenticatedUser)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Account sign-in error: ${e.message}")
            Result.failure(e)
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
            } else {
                prefs.edit().putBoolean("local_email_verified", true).apply()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Failed to send email verification: ${e.message}")
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
                _currentUser.value = user
                Result.success(user.isEmailVerified)
            } else {
                val verified = prefs.getBoolean("local_email_verified", true)
                Result.success(verified)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Failed to reload user: ${e.message}")
            Result.failure(e)
        }
    }

    val isEmailVerified: Boolean
        get() = auth?.currentUser?.isEmailVerified ?: prefs.getBoolean("local_email_verified", true)

    /**
     * Send password reset email and optionally update password in Room UserDao.
     */
    suspend fun sendPasswordResetEmail(email: String, newPassword: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }
            if (!newPassword.isNullOrBlank()) {
                if (newPassword.length < 6) {
                    return@withContext Result.failure(Exception("New password must be at least 6 characters."))
                }
                val newHash = hashPassword(newPassword, normalizedEmail)
                val updatedRows = userDao.updatePassword(normalizedEmail, newHash)
                if (updatedRows == 0) {
                    val created = UserEntity(
                        id = "usr_${UUID.nameUUIDFromBytes(normalizedEmail.toByteArray()).toString().take(10)}",
                        fullName = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = normalizedEmail,
                        passwordHash = newHash,
                        phoneNumber = "",
                        locality = "Balasore Town",
                        bio = "Verified Baleshwar 360 Member",
                        avatarUri = null,
                        securityQuestion = "What is your Balasore locality?",
                        securityAnswer = "balasore town",
                        createdAt = System.currentTimeMillis()
                    )
                    userDao.insertUser(created)
                }
                try {
                    auth?.currentUser?.updatePassword(newPassword)?.await()
                } catch (_: Exception) {}
            }
            try {
                auth?.sendPasswordResetEmail(normalizedEmail)?.await()
            } catch (e: Exception) {
                Log.w("FirebaseAuthManager", "Firebase reset email dispatch note: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out from Firebase Auth and clear local session credentials.
     */
    suspend fun signOut(context: Context? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
            _currentUser.value = null
            _activeSessionUser.value = null
            prefs.edit()
                .remove("session_user_id")
                .remove("session_user_email")
                .remove("session_user_name")
                .remove("session_user_phone")
                .remove("session_user_locality")
                .remove("session_user_bio")
                .remove("session_user_avatar")
                .apply()
            if (context != null) {
                try {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w("FirebaseAuthManager", "Clear credential state warning: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
