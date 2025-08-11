package com.devsecopsinpt.focusapp.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class CryptoStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Encrypted prefs – MasterKey protects these values at rest
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "crypto_store_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val appKey: SecretKey by lazy {
        val existing = prefs.getString("app_aes_key_b64", null)
        if (existing != null) {
            val raw = Base64.decode(existing, Base64.DEFAULT)
            SecretKeySpec(raw, "AES")
        } else {
            val raw = ByteArray(32) // 256-bit
            SecureRandom().nextBytes(raw)
            prefs.edit().putString("app_aes_key_b64", Base64.encodeToString(raw, Base64.DEFAULT)).apply()
            SecretKeySpec(raw, "AES")
        }
    }

    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return plaintext
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) } // 96-bit IV
        cipher.init(Cipher.ENCRYPT_MODE, appKey, GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        // Store iv || ciphertext as base64
        val out = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(ct, 0, out, iv.size, ct.size)
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    fun decrypt(cipherTextB64: String?): String {
        if (cipherTextB64.isNullOrEmpty()) return ""
        val all = Base64.decode(cipherTextB64, Base64.NO_WRAP)
        val iv = all.copyOfRange(0, 12)
        val ct = all.copyOfRange(12, all.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, appKey, GCMParameterSpec(128, iv))
        val pt = cipher.doFinal(ct)
        return String(pt, Charsets.UTF_8)
    }
}
