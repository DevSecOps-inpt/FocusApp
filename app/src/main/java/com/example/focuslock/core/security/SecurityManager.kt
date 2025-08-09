package com.example.focuslock.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val KEY_ALIAS = "focuslock_master_key"
        private const val PREFS_NAME = "focuslock_secure_prefs"
        private const val DB_PASSPHRASE_KEY = "db_passphrase"
        private const val USER_PIN_KEY = "user_pin"
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
    }
    
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Generate or retrieve the database passphrase
     */
    fun getDatabasePassphrase(): String {
        var passphrase = encryptedPrefs.getString(DB_PASSPHRASE_KEY, null)
        if (passphrase == null) {
            passphrase = generateSecurePassphrase()
            encryptedPrefs.edit().putString(DB_PASSPHRASE_KEY, passphrase).apply()
        }
        return passphrase
    }
    
    /**
     * Set user PIN for app unlock
     */
    fun setUserPin(pin: String) {
        val hashedPin = hashPin(pin)
        encryptedPrefs.edit().putString(USER_PIN_KEY, hashedPin).apply()
    }
    
    /**
     * Verify user PIN
     */
    fun verifyPin(pin: String): Boolean {
        val storedHash = encryptedPrefs.getString(USER_PIN_KEY, null) ?: return false
        return hashPin(pin) == storedHash
    }
    
    /**
     * Check if PIN is set
     */
    fun isPinSet(): Boolean {
        return encryptedPrefs.contains(USER_PIN_KEY)
    }
    
    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(BIOMETRIC_ENABLED_KEY, enabled).apply()
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(BIOMETRIC_ENABLED_KEY, false)
    }
    
    /**
     * Clear all security settings (for reset)
     */
    fun clearSecuritySettings() {
        encryptedPrefs.edit().clear().apply()
    }
    
    private fun generateSecurePassphrase(): String {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS + "_temp_${System.currentTimeMillis()}",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()
        
        // Use the key's encoded form as passphrase base
        return secretKey.encoded?.let { bytes ->
            bytes.joinToString("") { "%02x".format(it) }
        } ?: generateFallbackPassphrase()
    }
    
    private fun generateFallbackPassphrase(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { chars.random() }
            .joinToString("")
    }
    
    private fun hashPin(pin: String): String {
        // Simple hash - in production, use stronger hashing like bcrypt or scrypt
        return pin.hashCode().toString()
    }
} 