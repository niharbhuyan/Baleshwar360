package com.example

import com.example.data.security.AuthValidator
import com.example.data.security.PasswordHasher
import com.example.data.security.PasswordStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAuthSecurityTest {

    @Test
    fun `hash generates unique salted v2 digests for identical passwords`() {
        val hash1 = PasswordHasher.hash("Chandipur2026!")
        val hash2 = PasswordHasher.hash("Chandipur2026!")

        assertTrue(hash1.startsWith("pbkdf2_sha256_v2$"))
        assertTrue(hash2.startsWith("pbkdf2_sha256_v2$"))
        assertNotEquals("Each user password must receive a unique random salt", hash1, hash2)
        assertFalse(PasswordHasher.needsRehash(hash1))
    }

    @Test
    fun `verify accepts matching password and rejects wrong password or plaintext`() {
        val storedHash = PasswordHasher.hash("BalasoreSea#42")

        assertTrue(PasswordHasher.verify("BalasoreSea#42", storedHash))
        assertFalse(PasswordHasher.verify("BalasoreSea#43", storedHash))
        assertFalse(PasswordHasher.verify("", storedHash))
        // Plaintext storage must NEVER verify as valid
        assertFalse(PasswordHasher.verify("PlaintextPass1", "PlaintextPass1"))
    }

    @Test
    fun `needsRehash flags legacy hashes and low iteration hashes`() {
        val lowIterHash = PasswordHasher.hash("TestPass123", iterations = 10_000)
        assertTrue(PasswordHasher.verify("TestPass123", lowIterHash))
        assertTrue(PasswordHasher.needsRehash(lowIterHash))
        assertTrue(PasswordHasher.needsRehash("pbkdf2_sha256\$deadbeef"))
    }

    @Test
    fun `security recovery answer hashing is case-insensitive and trimmed`() {
        val storedAnswerHash = PasswordHasher.hashSecurityAnswer("  Chandipur  ")

        assertTrue(storedAnswerHash.startsWith("pbkdf2_sha256_v2$"))
        assertTrue(PasswordHasher.verifySecurityAnswer("chandipur", storedAnswerHash))
        assertTrue(PasswordHasher.verifySecurityAnswer("CHANDIPUR ", storedAnswerHash))
        assertFalse(PasswordHasher.verifySecurityAnswer("Remuna", storedAnswerHash))
    }

    @Test
    fun `email validation accepts valid emails and rejects malformed ones`() {
        assertTrue(AuthValidator.isValidEmail("citizen.bls@example.com"))
        assertTrue(AuthValidator.isValidEmail("  USER+1@baleshwar360.in "))
        assertFalse(AuthValidator.isValidEmail(""))
        assertFalse(AuthValidator.isValidEmail("not-an-email"))
        assertFalse(AuthValidator.isValidEmail("a@b"))
        assertFalse(AuthValidator.isValidEmail("@example.com"))
        assertFalse(AuthValidator.isValidEmail("user@.com"))
    }

    @Test
    fun `password strength policy requires at least 8 chars with letter and digit`() {
        val tooShort = AuthValidator.validatePasswordStrength("Ab1234")
        assertFalse(tooShort.isValid)
        assertEquals(PasswordStrength.WEAK, tooShort.strength)

        val lettersOnly = AuthValidator.validatePasswordStrength("balasorecoast")
        assertFalse(lettersOnly.isValid)

        val digitsOnly = AuthValidator.validatePasswordStrength("12345678")
        assertFalse(digitsOnly.isValid)

        val validFair = AuthValidator.validatePasswordStrength("balasore1")
        assertTrue(validFair.isValid)

        val validStrong = AuthValidator.validatePasswordStrength("Chandipur#2026")
        assertTrue(validStrong.isValid)
        assertEquals(PasswordStrength.STRONG, validStrong.strength)
    }
}
