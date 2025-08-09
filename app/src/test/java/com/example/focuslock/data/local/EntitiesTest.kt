package com.example.focuslock.data.local

import com.example.focuslock.data.local.entity.BlockedApp
import com.example.focuslock.data.local.entity.FocusSession
import com.example.focuslock.data.local.entity.BlockedAttempt
import com.example.focuslock.data.local.entity.Schedule
import com.example.focuslock.domain.model.FocusMode
import org.junit.Test
import org.junit.Assert.*

class EntitiesTest {
    
    @Test
    fun `BlockedApp entity creation`() {
        val app = BlockedApp(
            packageName = "com.example.test",
            appLabel = "Test App",
            addedAt = System.currentTimeMillis()
        )
        
        assertEquals("com.example.test", app.packageName)
        assertEquals("Test App", app.appLabel)
        assertNull(app.iconBlob)
    }
    
    @Test
    fun `FocusSession entity creation`() {
        val session = FocusSession(
            mode = FocusMode.QUICK,
            startTime = System.currentTimeMillis(),
            endTime = null,
            whitelist = listOf("com.example.allowed"),
            vpnEnabled = true
        )
        
        assertEquals(FocusMode.QUICK, session.mode)
        assertEquals(1, session.whitelist.size)
        assertTrue(session.vpnEnabled)
    }
    
    @Test
    fun `BlockedAttempt entity creation`() {
        val attempt = BlockedAttempt(
            packageName = "com.example.blocked",
            appLabel = "Blocked App",
            successUnlock = false,
            sessionId = 1L
        )
        
        assertEquals("com.example.blocked", attempt.packageName)
        assertFalse(attempt.successUnlock)
        assertEquals(1L, attempt.sessionId)
    }
    
    @Test
    fun `Schedule entity creation`() {
        val schedule = Schedule(
            daysOfWeek = setOf(1, 2, 3, 4, 5), // Monday to Friday
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            whitelist = listOf("com.example.work"),
            vpnEnabled = false
        )
        
        assertEquals(5, schedule.daysOfWeek.size)
        assertEquals(9, schedule.startHour)
        assertEquals(17, schedule.endHour)
        assertEquals(1, schedule.whitelist.size)
    }
}
