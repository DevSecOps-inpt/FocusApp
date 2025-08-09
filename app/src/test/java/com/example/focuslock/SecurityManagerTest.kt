package com.example.focuslock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.focuslock.core.security.SecurityManager
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecurityManagerTest {
    
    private lateinit var securityManager: SecurityManager
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        securityManager = SecurityManager(context)
    }
    
    @Test
    fun `PIN is not set initially`() {
        assertFalse(securityManager.isPinSet())
    }
    
    @Test
    fun `PIN can be set and verified`() {
        val testPin = "1234"
        
        securityManager.setUserPin(testPin)
        assertTrue(securityManager.isPinSet())
        assertTrue(securityManager.verifyPin(testPin))
        assertFalse(securityManager.verifyPin("wrong"))
    }
    
    @Test
    fun `biometric is disabled by default`() {
        assertFalse(securityManager.isBiometricEnabled())
    }
    
    @Test
    fun `biometric setting can be changed`() {
        securityManager.setBiometricEnabled(true)
        assertTrue(securityManager.isBiometricEnabled())
        
        securityManager.setBiometricEnabled(false)
        assertFalse(securityManager.isBiometricEnabled())
    }
    
    @Test
    fun `database passphrase is generated`() {
        val passphrase = securityManager.getDatabasePassphrase()
        assertTrue(passphrase.isNotEmpty())
        
        // Should return same passphrase on subsequent calls
        val passphrase2 = securityManager.getDatabasePassphrase()
        assertTrue(passphrase == passphrase2)
    }
} 