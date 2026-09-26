package com.example.data.security

import android.util.Patterns
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityHelper {
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256 // bits
    private const val SALT_LENGTH = 16 // bytes

    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure random salt.
     */
    fun generateSalt(): String {
        val saltBytes = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    /**
     * Hashes a password using PBKDF2WithHmacSHA256 with the provided salt.
     * Never stores or compares plain-text passwords.
     */
    fun hashPassword(password: String, saltHex: String): String {
        val saltBytes = hexToBytes(saltHex)
        return try {
            val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hashBytes = skf.generateSecret(spec).encoded
            bytesToHex(hashBytes)
        } catch (e: Exception) {
            // Fallback to SHA-256 with multiple rounds if PBKDF2 is unavailable
            fallbackSha256(password, saltBytes)
        }
    }

    /**
     * Constant-time verification of password against stored hash and salt.
     */
    fun verifyPassword(password: String, saltHex: String, expectedHashHex: String): Boolean {
        val computedHashHex = hashPassword(password, saltHex)
        val computedBytes = hexToBytes(computedHashHex)
        val expectedBytes = hexToBytes(expectedHashHex)
        return MessageDigest.isEqual(computedBytes, expectedBytes)
    }

    /**
     * Generates a secure 6-digit numeric verification code for password reset.
     */
    fun generateResetCode(): String {
        val code = secureRandom.nextInt(900000) + 100000
        return code.toString()
    }

    /**
     * Normalizes email address (trimmed and lowercase).
     */
    fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    /**
     * Validates email format.
     */
    fun isValidEmail(email: String): Boolean {
        val normalized = normalizeEmail(email)
        return normalized.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(normalized).matches()
    }

    /**
     * Validates password strength (minimum 6 characters).
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Normalizes handle to ensure leading '@'.
     */
    fun normalizeHandle(handle: String): String {
        val cleaned = handle.trim().removePrefix("@")
        return "@$cleaned"
    }

    private fun fallbackSha256(password: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        var hash = md.digest(password.toByteArray(Charsets.UTF_8))
        for (i in 0 until 1000) {
            md.reset()
            md.update(salt)
            hash = md.digest(hash)
        }
        return bytesToHex(hash)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                    Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
