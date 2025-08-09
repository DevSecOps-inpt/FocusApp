package com.example.focuslock.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alias = "focuslock-db-key"

    fun getOrCreateKey(): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        
        val secretKey = (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        
        // Derive a raw passphrase for SQLCipher (store only wrapped in Keystore, not on disk)
        val material = secretKey.encoded ?: SecretKeySpec(secretKey.encoded, "AES").encoded
        return MessageDigest.getInstance("SHA-256").digest(material).copyOf(32)
    }
}
