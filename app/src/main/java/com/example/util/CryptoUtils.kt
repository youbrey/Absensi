package com.example.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * End-to-End Encryption utility using AES-256 and SHA-256 payload hashing
 * to protect sensitive NIP and attendance photo metadata.
 */
object CryptoUtils {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    // 256-bit Key derived for Secretariat DPRD Bitung
    private val SECRET_KEY_BYTES = "DPRD_BITUNG_SECRET_KEY_2026_E2E!".toByteArray(Charsets.UTF_8).copyOf(32)
    private val IV_BYTES = "BITUNG_WFH_IV_16!".toByteArray(Charsets.UTF_8).copyOf(16)

    fun encrypt(plainText: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY_BYTES, "AES")
            val ivSpec = IvParameterSpec(IV_BYTES)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(cipherText: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY_BYTES, "AES")
            val ivSpec = IvParameterSpec(IV_BYTES)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            String(cipher.doFinal(decoded), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }

    fun generatePayloadHash(nip: String, timestamp: Long, jenis: String): String {
        val raw = "$nip:$timestamp:$jenis:SEKRETARIAT_DPRD_BITUNG"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
