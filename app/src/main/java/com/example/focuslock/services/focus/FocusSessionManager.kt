package com.example.focuslock.services.focus

import android.content.Context
import com.example.focuslock.data.local.dao.FocusSessionDao
import com.example.focuslock.data.local.entity.FocusSession
import com.example.focuslock.domain.model.FocusMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusSessionDao: FocusSessionDao,
    private val enforcementGate: EnforcementGateImpl
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    suspend fun startQuickFocus(durationMinutes: Int, whitelist: List<String> = emptyList()): Long {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (durationMinutes * 60 * 1000L)
        
        val session = FocusSession(
            mode = FocusMode.QUICK,
            startTime = startTime,
            endTime = endTime,
            whitelist = whitelist,
            vpnEnabled = false
        )
        
        val sessionId = focusSessionDao.insert(session)
        
        // Update enforcement gate
        enforcementGate.updateFocusSession(sessionId, whitelist.toSet())
        
        // Start enforcement service
        EnforcementService.start(context)
        
        // Schedule auto-end
        scope.launch {
            kotlinx.coroutines.delay(durationMinutes * 60 * 1000L)
            endSession(sessionId)
        }
        
        return sessionId
    }
    
    suspend fun startScheduledFocus(scheduleId: Long, endTime: Long, whitelist: List<String> = emptyList()): Long {
        val session = FocusSession(
            mode = FocusMode.SCHEDULED,
            startTime = System.currentTimeMillis(),
            endTime = endTime,
            whitelist = whitelist,
            vpnEnabled = false,
            scheduleId = scheduleId
        )
        
        val sessionId = focusSessionDao.insert(session)
        
        // Update enforcement gate
        enforcementGate.updateFocusSession(sessionId, whitelist.toSet())
        
        // Start enforcement service
        EnforcementService.start(context)
        
        return sessionId
    }
    
    suspend fun endSession(sessionId: Long) {
        focusSessionDao.endSession(sessionId, System.currentTimeMillis())
        
        // Check if there are other active sessions
        val activeSessions = focusSessionDao.getActiveSession()
        if (activeSessions == null) {
            // No more active sessions, stop enforcement
            enforcementGate.updateFocusSession(null, emptySet())
            EnforcementService.stop(context)
        }
    }
    
    suspend fun endAllActiveSessions() {
        focusSessionDao.endAllActiveSessions(System.currentTimeMillis())
        enforcementGate.updateFocusSession(null, emptySet())
        EnforcementService.stop(context)
    }
    
    suspend fun getActiveSession(): FocusSession? {
        return focusSessionDao.getActiveSession()
    }
}

// Extension functions for easy access
fun Context.startQuickFocus(durationMinutes: Int, whitelist: List<String> = emptyList()) {
    EnforcementService.start(this)
}

fun Context.stopFocus() {
    EnforcementService.stop(this)
} 