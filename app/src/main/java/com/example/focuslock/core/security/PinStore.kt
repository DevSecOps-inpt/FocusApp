package com.example.focuslock.core.security

import android.content.Context
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "pin_store",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun isSet(): Boolean = encryptedPrefs.contains("pin_hash")
    
    fun setPin(rawPin: CharArray) {
        val hash = hashPin(rawPin)
        encryptedPrefs.edit().putString("pin_hash", hash).apply()
        // Clear the raw pin from memory
        rawPin.fill('0')
    }
    
    fun verify(rawPin: CharArray): Boolean {
        val storedHash = encryptedPrefs.getString("pin_hash", null)
        val inputHash = hashPin(rawPin)
        // Clear the raw pin from memory
        rawPin.fill('0')
        return storedHash == inputHash
    }
    
    private fun hashPin(rawPin: CharArray): String {
        val pinBytes = String(rawPin).toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(pinBytes)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
    
    suspend fun promptAndVerify(activity: ComponentActivity): Boolean {
        // TODO: Show a Compose dialog to collect PIN securely, compare via verify()
        // For now, return true as placeholder
        return true
    }
}
