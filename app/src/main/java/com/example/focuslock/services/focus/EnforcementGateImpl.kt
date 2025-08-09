package com.example.focuslock.services.focus

import android.content.Context
import com.example.focuslock.data.local.dao.BlockedAppDao
import com.example.focuslock.data.local.dao.BlockedAttemptDao
import com.example.focuslock.data.local.dao.FocusSessionDao
import com.example.focuslock.data.local.entity.BlockedAttempt
import com.example.focuslock.services.overlay.showLockOverlay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnforcementGateImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedAppDao: BlockedAppDao,
    private val focusSessionDao: FocusSessionDao,
    private val blockedAttemptDao: BlockedAttemptDao
) : EnforcementGate {
    
    private val _state = MutableStateFlow(EnforcementState())
    val state: StateFlow<EnforcementState> = _state.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val bypassedApps = mutableSetOf<String>()
    private var bypassJob: Job? = null
    
    init {
        EnforcementGate.instance = this
    }
    
    override fun isActive(): Boolean = _state.value.isActive
    
    override fun onForeground(packageName: String) {
        if (!isActive()) return
        if (packageName == context.packageName) return // Don't block ourselves
        if (bypassedApps.contains(packageName)) return // Temporarily bypassed
        
        scope.launch {
            val currentState = _state.value
            val shouldBlock = when {
                // Check if app is in focus session whitelist
                currentState.activeFocusSession != null && 
                !currentState.whitelistedApps.contains(packageName) -> true
                
                // Check if app is in blocked apps list
                currentState.blockedApps.contains(packageName) -> true
                
                else -> false
            }
            
            if (shouldBlock) {
                logBlockedAttempt(packageName, false)
                context.showLockOverlay(packageName)
            }
        }
    }
    
    fun startEnforcement() {
        scope.launch {
            // Load current blocked apps
            val blockedApps = blockedAppDao.getAll().map { it.packageName }.toSet()
            
            // Check for active focus session
            val activeSession = focusSessionDao.getActiveSession()
            val whitelistedApps = activeSession?.whitelist?.toSet() ?: emptySet()
            
            _state.value = _state.value.copy(
                isActive = true,
                blockedApps = blockedApps,
                whitelistedApps = whitelistedApps,
                activeFocusSession = activeSession?.id
            )
        }
    }
    
    fun stopEnforcement() {
        _state.value = _state.value.copy(
            isActive = false,
            activeFocusSession = null
        )
        bypassedApps.clear()
        bypassJob?.cancel()
    }
    
    fun updateBlockedApps(apps: Set<String>) {
        _state.value = _state.value.copy(blockedApps = apps)
    }
    
    fun updateFocusSession(sessionId: Long?, whitelistedApps: Set<String>) {
        _state.value = _state.value.copy(
            activeFocusSession = sessionId,
            whitelistedApps = whitelistedApps
        )
    }
    
    fun grantTemporaryBypass(packageName: String, durationMs: Long = 5 * 60 * 1000) {
        bypassedApps.add(packageName)
        
        // Remove bypass after duration
        bypassJob?.cancel()
        bypassJob = scope.launch {
            delay(durationMs)
            bypassedApps.remove(packageName)
        }
        
        scope.launch {
            logBlockedAttempt(packageName, true)
        }
    }
    
    private suspend fun logBlockedAttempt(packageName: String, successUnlock: Boolean) {
        try {
            // Get app label
            val appLabel = try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }
            
            val attempt = BlockedAttempt(
                packageName = packageName,
                timestamp = System.currentTimeMillis(),
                appLabel = appLabel,
                successUnlock = successUnlock,
                sessionId = _state.value.activeFocusSession
            )
            
            blockedAttemptDao.insert(attempt)
        } catch (e: Exception) {
            // Ignore logging errors
        }
    }
} 