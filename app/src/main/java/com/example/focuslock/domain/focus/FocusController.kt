package com.example.focuslock.domain.focus

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.focuslock.data.local.FocusLockDatabase
import com.example.focuslock.data.local.entity.FocusSession
import com.example.focuslock.data.repo.PolicyRepository
import com.example.focuslock.domain.model.FocusMode
import com.example.focuslock.services.focus.EnforcementService
import com.example.focuslock.services.vpn.FocusVpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusController @Inject constructor(
    private val database: FocusLockDatabase,
    private val policyRepository: PolicyRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend fun startQuick(minutes: Int, whitelist: List<String>, vpnEnabled: Boolean): Long {
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + minutes * 60_000L
        
        val sessionId = database.focusSessionDao().insert(
            FocusSession(
                mode = FocusMode.QUICK,
                startTime = currentTime,
                endTime = endTime,
                whitelist = whitelist,
                vpnEnabled = vpnEnabled
            )
        )
        
        policyRepository.setFocusMode(whitelist, sessionId, vpnEnabled)
        
        // Start enforcement service
        ContextCompat.startForegroundService(
            context, 
            Intent(context, EnforcementService::class.java)
        )
        
        // Start VPN service if enabled
        if (vpnEnabled) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FocusVpnService::class.java)
            )
        }
        
        return sessionId
    }
    
    suspend fun stopCurrent(sessionId: Long?) {
        sessionId?.let { id ->
            database.focusSessionDao().end(id, System.currentTimeMillis())
        }
        
        policyRepository.stop()
        
        // Stop services
        context.stopService(Intent(context, EnforcementService::class.java))
        context.stopService(Intent(context, FocusVpnService::class.java))
    }
    
    suspend fun startScheduled(scheduleId: Long, whitelist: List<String>, vpnEnabled: Boolean): Long {
        val currentTime = System.currentTimeMillis()
        
        val sessionId = database.focusSessionDao().insert(
            FocusSession(
                mode = FocusMode.SCHEDULED,
                startTime = currentTime,
                endTime = null, // Will be set when schedule ends
                whitelist = whitelist,
                vpnEnabled = vpnEnabled,
                scheduleId = scheduleId
            )
        )
        
        policyRepository.setFocusMode(whitelist, sessionId, vpnEnabled)
        
        // Start enforcement service
        ContextCompat.startForegroundService(
            context,
            Intent(context, EnforcementService::class.java)
        )
        
        // Start VPN service if enabled
        if (vpnEnabled) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FocusVpnService::class.java)
            )
        }
        
        return sessionId
    }
}
