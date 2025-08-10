package com.devsecopsinpt.focusapp.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbKeyManager @Inject constructor(private val context: Context) {
    private val alias = "focuslock-db-key"

    fun getOrCreate32(): ByteArray {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!ks.containsAlias(alias)) {
            val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            gen.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            gen.generateKey()
        }
        val key = (ks.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        return (key.encoded ?: ByteArray(32) { 7 }).copyOf(32)
    }
}
