package com.example.focuslock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.focuslock.data.local.dao.BlockedAppDao
import com.example.focuslock.data.local.dao.BlockedAttemptDao
import com.example.focuslock.data.local.dao.FocusSessionDao
import com.example.focuslock.data.local.entity.BlockedApp
import com.example.focuslock.services.focus.EnforcementGateImpl
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EnforcementGateTest {
    
    private lateinit var context: Context
    private lateinit var blockedAppDao: BlockedAppDao
    private lateinit var focusSessionDao: FocusSessionDao
    private lateinit var blockedAttemptDao: BlockedAttemptDao
    private lateinit var enforcementGate: EnforcementGateImpl
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        blockedAppDao = mockk(relaxed = true)
        focusSessionDao = mockk(relaxed = true)
        blockedAttemptDao = mockk(relaxed = true)
        
        enforcementGate = EnforcementGateImpl(
            context = context,
            blockedAppDao = blockedAppDao,
            focusSessionDao = focusSessionDao,
            blockedAttemptDao = blockedAttemptDao
        )
    }
    
    @Test
    fun `enforcement is inactive by default`() {
        assert(!enforcementGate.isActive())
    }
    
    @Test
    fun `starting enforcement loads blocked apps`() = runTest {
        // Given
        val blockedApps = listOf(
            BlockedApp("com.example.social", "Social App", System.currentTimeMillis()),
            BlockedApp("com.example.game", "Game App", System.currentTimeMillis())
        )
        coEvery { blockedAppDao.getAll() } returns blockedApps
        coEvery { focusSessionDao.getActiveSession() } returns null
        
        // When
        enforcementGate.startEnforcement()
        
        // Then
        assert(enforcementGate.isActive())
        assert(enforcementGate.state.value.blockedApps.contains("com.example.social"))
        assert(enforcementGate.state.value.blockedApps.contains("com.example.game"))
    }
    
    @Test
    fun `stopping enforcement deactivates blocking`() = runTest {
        // Given
        coEvery { blockedAppDao.getAll() } returns emptyList()
        coEvery { focusSessionDao.getActiveSession() } returns null
        enforcementGate.startEnforcement()
        
        // When
        enforcementGate.stopEnforcement()
        
        // Then
        assert(!enforcementGate.isActive())
    }
    
    @Test
    fun `onForeground does nothing when inactive`() {
        // Given enforcement is inactive
        assert(!enforcementGate.isActive())
        
        // When
        enforcementGate.onForeground("com.example.social")
        
        // Then - no exception should be thrown
        // and no attempt should be logged since enforcement is inactive
    }
} 