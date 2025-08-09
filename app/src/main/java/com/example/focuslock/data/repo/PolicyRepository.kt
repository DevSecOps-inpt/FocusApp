package com.example.focuslock.data.repo

import com.example.focuslock.data.local.dao.BlockedAppDao
import com.example.focuslock.data.local.dao.FocusSessionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolicyRepository @Inject constructor(
    private val blockedAppDao: BlockedAppDao,
    private val sessionDao: FocusSessionDao
) {
    private val _currentPolicy = MutableStateFlow(PolicyState.idle())
    val currentPolicy: StateFlow<PolicyState> = _currentPolicy

    suspend fun setFocusMode(whitelist: List<String>, sessionId: Long?, vpnEnabled: Boolean) {
        _currentPolicy.value = PolicyState(
            enforcementActive = true,
            mode = PolicyMode.FOCUS,
            whitelist = whitelist.toSet(),
            blocked = emptySet(),
            sessionId = sessionId,
            vpnEnabled = vpnEnabled
        )
    }

    suspend fun setAppLock(blocked: Set<String>) {
        _currentPolicy.value = PolicyState(
            enforcementActive = true,
            mode = PolicyMode.BLOCKLIST,
            whitelist = emptySet(),
            blocked = blocked,
            sessionId = null,
            vpnEnabled = false
        )
    }

    fun stop() {
        _currentPolicy.value = PolicyState.idle()
    }
}

data class PolicyState(
    val enforcementActive: Boolean,
    val mode: PolicyMode,
    val whitelist: Set<String>,
    val blocked: Set<String>,
    val sessionId: Long?,
    val vpnEnabled: Boolean
) {
    companion object {
        fun idle() = PolicyState(
            enforcementActive = false,
            mode = PolicyMode.BLOCKLIST,
            whitelist = emptySet(),
            blocked = emptySet(),
            sessionId = null,
            vpnEnabled = false
        )
    }
}

enum class PolicyMode { FOCUS, BLOCKLIST }
