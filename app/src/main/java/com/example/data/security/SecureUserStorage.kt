package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.data.local.UserEntity
import org.json.JSONObject
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/**
 * Cryptographic password hashing & verification utility using PBKDF2WithHmacSHA256
 * with per-user 16-byte SecureRandom salts and constant-time digest comparison.
 */
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val FORMAT_PREFIX_V2 = "pbkdf2_sha256_v2"
    private const val LEGACY_PREFIX_V1 = "pbkdf2_sha256$"
    const val DEFAULT_ITERATIONS = 120_000
    private const val LEGACY_ITERATIONS = 10_000
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256

    private val secureRandom = SecureRandom()

    /**
     * Generates a freshly salted PBKDF2-SHA256 hash in the format:
     * `pbkdf2_sha256_v2$<iterations>$<saltHex>$<hashHex>`
     */
    fun hash(rawSecret: String, iterations: Int = DEFAULT_ITERATIONS): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val derived = deriveKey(rawSecret.toCharArray(), salt, iterations, KEY_BITS)
        return "$FORMAT_PREFIX_V2$$iterations$${salt.toHex()}$${derived.toHex()}"
    }

    /**
     * Verifies a candidate password against a stored hash using constant-time comparison.
     * Supports v2 per-user salted hashes and legacy v1/SHA-256 hashes for seamless migration.
     * Never accepts plaintext password matches.
     */
    fun verify(rawSecret: String, storedHash: String, legacyEmailHint: String = ""): Boolean {
        if (rawSecret.isEmpty() || storedHash.isBlank()) return false

        // 1. Current format: pbkdf2_sha256_v2$<iterations>$<saltHex>$<hashHex>
        if (storedHash.startsWith("$FORMAT_PREFIX_V2$")) {
            val parts = storedHash.split('$')
            if (parts.size != 4) return false
            val iterations = parts[1].toIntOrNull() ?: return false
            if (iterations < 10_000 || iterations > 1_000_000) return false
            val salt = parts[2].hexToBytes() ?: return false
            val expectedHash = parts[3].hexToBytes() ?: return false
            val actualHash = deriveKey(rawSecret.toCharArray(), salt, iterations, expectedHash.size * 8)
            return MessageDigest.isEqual(expectedHash, actualHash)
        }

        // 2. Legacy v1 migration format: pbkdf2_sha256$<hashHex>
        if (storedHash.startsWith(LEGACY_PREFIX_V1)) {
            val expectedHex = storedHash.removePrefix(LEGACY_PREFIX_V1)
            val expectedBytes = expectedHex.hexToBytes() ?: return false
            val normalizedEmail = legacyEmailHint.trim().lowercase(Locale.ROOT)
            val emailSalt = "bls360_$normalizedEmail".toByteArray(Charsets.UTF_8)
            val defaultSalt = "bls360_balasore360_secure_salt".toByteArray(Charsets.UTF_8)

            val candidateTrimmed = rawSecret.trim().toCharArray()
            val candidateWithEmail = deriveKey(candidateTrimmed, emailSalt, LEGACY_ITERATIONS, KEY_BITS)
            if (MessageDigest.isEqual(expectedBytes, candidateWithEmail)) return true

            val candidateDefault = deriveKey(candidateTrimmed, defaultSalt, LEGACY_ITERATIONS, KEY_BITS)
            return MessageDigest.isEqual(expectedBytes, candidateDefault)
        }

        // 3. Legacy raw SHA-256 hex digest (64 hex chars) for pre-v1 migration only
        if (storedHash.length == 64 && storedHash.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            val expectedBytes = storedHash.hexToBytes() ?: return false
            val digest = MessageDigest.getInstance("SHA-256")
            val actualBytes = digest.digest(rawSecret.trim().toByteArray(Charsets.UTF_8))
            return MessageDigest.isEqual(expectedBytes, actualBytes)
        }

        // Plaintext fallback is strictly prohibited
        return false
    }

    /**
     * Returns true if the stored hash uses a legacy format or fewer iterations than current policy.
     */
    fun needsRehash(storedHash: String): Boolean {
        if (!storedHash.startsWith("$FORMAT_PREFIX_V2$")) return true
        val parts = storedHash.split('$')
        val iterations = parts.getOrNull(1)?.toIntOrNull() ?: return true
        return iterations < DEFAULT_ITERATIONS
    }

    /**
     * Verifies a normalized security answer (case-insensitive, trimmed).
     * Supports both hashed answers and legacy stored answers without exposing plaintext secrets.
     */
    fun hashSecurityAnswer(rawAnswer: String): String {
        val normalized = rawAnswer.trim().lowercase(Locale.ROOT)
        return hash(normalized)
    }

    fun verifySecurityAnswer(candidateAnswer: String, storedValue: String): Boolean {
        val normalizedCandidate = candidateAnswer.trim().lowercase(Locale.ROOT)
        if (normalizedCandidate.isEmpty() || storedValue.isBlank()) return false
        if (storedValue.startsWith("$FORMAT_PREFIX_V2$") || storedValue.startsWith(LEGACY_PREFIX_V1)) {
            return verify(normalizedCandidate, storedValue)
        }
        // Legacy locality answer stored in lower-case Room column prior to hashing upgrade
        val expectedBytes = storedValue.trim().lowercase(Locale.ROOT).toByteArray(Charsets.UTF_8)
        val actualBytes = normalizedCandidate.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(expectedBytes, actualBytes)
    }

    private fun deriveKey(chars: CharArray, salt: ByteArray, iterations: Int, keyBits: Int): ByteArray {
        val spec = PBEKeySpec(chars, salt, iterations, keyBits)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { i ->
                substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: NumberFormatException) {
            null
        }
    }
}

enum class PasswordStrength {
    WEAK,
    FAIR,
    GOOD,
    STRONG
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val strength: PasswordStrength,
    val errorMessage: String? = null
)

/**
 * Input validation rules for email and password authentication.
 */
object AuthValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD_LENGTH = 8

    fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    fun isValidEmail(email: String): Boolean {
        val normalized = normalizeEmail(email)
        return normalized.length in 5..254 && EMAIL_REGEX.matches(normalized)
    }

    fun validatePasswordStrength(password: String): PasswordValidationResult {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return PasswordValidationResult(
                isValid = false,
                strength = PasswordStrength.WEAK,
                errorMessage = "Password must be at least $MIN_PASSWORD_LENGTH characters."
            )
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (!hasLetter || !hasDigit) {
            return PasswordValidationResult(
                isValid = false,
                strength = PasswordStrength.FAIR,
                errorMessage = "Password must include at least one letter and one number."
            )
        }

        val score = listOf(
            password.length >= 10,
            hasUpper && hasLower,
            hasDigit,
            hasSymbol
        ).count { it }

        val strength = when {
            score >= 3 -> PasswordStrength.STRONG
            score == 2 -> PasswordStrength.GOOD
            else -> PasswordStrength.FAIR
        }

        return PasswordValidationResult(isValid = true, strength = strength)
    }
}

/**
 * Hardware-backed Android Keystore AES-256-GCM encrypted session vault.
 * Encrypts active user profile session data before storing it on disk and wipes
 * legacy cleartext session preferences.
 */
class SecureSessionStore(context: Context) {

    private val appContext = context.applicationContext
    private val vaultPrefs by lazy {
        appContext.getSharedPreferences(VAULT_PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val legacyPrefs by lazy {
        appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(user: UserEntity) {
        val payload = JSONObject().apply {
            put("id", user.id)
            put("fullName", user.fullName)
            put("email", user.email)
            put("phoneNumber", user.phoneNumber)
            put("locality", user.locality)
            put("bio", user.bio)
            put("avatarUri", user.avatarUri ?: "")
            put("securityQuestion", user.securityQuestion)
            put("createdAt", user.createdAt)
        }.toString()

        val encrypted = encryptWithKeystore(payload)
        if (encrypted != null) {
            vaultPrefs.edit()
                .putString(KEY_ENCRYPTED_SESSION, encrypted)
                .apply()
            clearLegacyPlaintextSession()
        } else {
            Log.e(TAG, "Refusing to persist session because Keystore encryption failed.")
        }
    }

    fun loadSession(): UserEntity? {
        val encrypted = vaultPrefs.getString(KEY_ENCRYPTED_SESSION, null)
        if (!encrypted.isNullOrBlank()) {
            val decrypted = decryptWithKeystore(encrypted)
            if (decrypted != null) {
                return parseSessionJson(decrypted)
            }
            // Corrupted or key-invalidated ciphertext: wipe cleanly
            vaultPrefs.edit().remove(KEY_ENCRYPTED_SESSION).apply()
        }

        // One-time migration from legacy cleartext session prefs if present
        val legacyUser = readLegacyPlaintextSession()
        if (legacyUser != null) {
            saveSession(legacyUser)
            clearLegacyPlaintextSession()
            return legacyUser
        }
        return null
    }

    fun clearSession() {
        vaultPrefs.edit()
            .remove(KEY_ENCRYPTED_SESSION)
            .remove(KEY_EMAIL_VERIFIED)
            .apply()
        clearLegacyPlaintextSession()
    }

    fun setLocalEmailVerified(verified: Boolean) {
        vaultPrefs.edit().putBoolean(KEY_EMAIL_VERIFIED, verified).apply()
    }

    fun isLocalEmailVerified(): Boolean {
        return vaultPrefs.getBoolean(KEY_EMAIL_VERIFIED, false)
    }

    private fun parseSessionJson(jsonStr: String): UserEntity? {
        return try {
            val json = JSONObject(jsonStr)
            val id = json.optString("id", "").takeIf { it.isNotBlank() } ?: return null
            val email = json.optString("email", "").takeIf { it.isNotBlank() } ?: return null
            UserEntity(
                id = id,
                fullName = json.optString("fullName", "Balasore Citizen").ifBlank { "Balasore Citizen" },
                email = email,
                passwordHash = SESSION_PLACEHOLDER_HASH,
                phoneNumber = json.optString("phoneNumber", ""),
                locality = json.optString("locality", "Balasore Town").ifBlank { "Balasore Town" },
                bio = json.optString("bio", "Verified Baleshwar 360 Member"),
                avatarUri = json.optString("avatarUri", "").takeIf { it.isNotBlank() },
                securityQuestion = json.optString("securityQuestion", "What is your Balasore locality?"),
                securityAnswer = SESSION_PLACEHOLDER_HASH,
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse decrypted session payload: ${e.message}")
            null
        }
    }

    private fun readLegacyPlaintextSession(): UserEntity? {
        val id = legacyPrefs.getString("session_user_id", null) ?: return null
        val email = legacyPrefs.getString("session_user_email", null) ?: return null
        return UserEntity(
            id = id,
            fullName = legacyPrefs.getString("session_user_name", "Balasore Citizen") ?: "Balasore Citizen",
            email = email,
            passwordHash = SESSION_PLACEHOLDER_HASH,
            phoneNumber = legacyPrefs.getString("session_user_phone", "") ?: "",
            locality = legacyPrefs.getString("session_user_locality", "Balasore Town") ?: "Balasore Town",
            bio = legacyPrefs.getString("session_user_bio", "Verified Baleshwar 360 Member") ?: "Verified Baleshwar 360 Member",
            avatarUri = legacyPrefs.getString("session_user_avatar", null)?.takeIf { it.isNotBlank() },
            securityQuestion = "What is your Balasore locality?",
            securityAnswer = SESSION_PLACEHOLDER_HASH,
            createdAt = legacyPrefs.getLong("session_user_created", System.currentTimeMillis())
        )
    }

    private fun clearLegacyPlaintextSession() {
        legacyPrefs.edit()
            .remove("session_user_id")
            .remove("session_user_email")
            .remove("session_user_name")
            .remove("session_user_phone")
            .remove("session_user_locality")
            .remove("session_user_bio")
            .remove("session_user_avatar")
            .remove("session_user_created")
            .remove("local_email_verified")
            .apply()
    }

    private fun getOrCreateAesKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptWithKeystore(plaintext: String): String? {
        return try {
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAesKey())
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val dataB64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            "$ENVELOPE_VERSION:$ivB64:$dataB64"
        } catch (e: Exception) {
            Log.e(TAG, "Keystore AES-GCM encryption failed: ${e.message}")
            null
        }
    }

    private fun decryptWithKeystore(envelope: String): String? {
        return try {
            val parts = envelope.split(':')
            if (parts.size != 3 || parts[0] != ENVELOPE_VERSION) return null
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[2], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), spec)
            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "Keystore AES-GCM decryption failed: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "SecureSessionStore"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "baleshwar360_session_aes256_gcm_v1"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val ENVELOPE_VERSION = "v1"
        private const val VAULT_PREFS_NAME = "balasore_secure_session_vault"
        private const val LEGACY_PREFS_NAME = "balasore_auth_session_prefs"
        private const val KEY_ENCRYPTED_SESSION = "encrypted_session_blob"
        private const val KEY_EMAIL_VERIFIED = "local_email_verified_v2"
        private const val SESSION_PLACEHOLDER_HASH = "ENCRYPTED_SESSION_ACTIVE"
    }
}
