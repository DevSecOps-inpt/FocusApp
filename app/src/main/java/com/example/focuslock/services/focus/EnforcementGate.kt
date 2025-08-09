package com.example.focuslock.services.focus

import kotlinx.coroutines.flow.StateFlow

interface EnforcementGate {
    fun isActive(): Boolean
    fun onForeground(packageName: String)
    
    companion object {
        @Volatile
        var instance: EnforcementGate? = null
    }
}

data class EnforcementState(
    val isActive: Boolean = false,
    val blockedApps: Set<String> = emptySet(),
    val whitelistedApps: Set<String> = emptySet(),
    val activeFocusSession: Long? = null
) 