package com.example.app_induztek.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 120000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun newSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashed = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hashed, Base64.NO_WRAP)
    }

    fun verify(password: String, salt: String, hash: String): Boolean {
        val computed = this.hash(password, salt)
        return MessageDigest.isEqual(computed.toByteArray(Charsets.UTF_8), hash.toByteArray(Charsets.UTF_8))
    }
}
