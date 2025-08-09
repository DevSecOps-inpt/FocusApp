package com.devsecopsinpt.focusapp.domain.policy

interface EnforcementGate {
    fun isActive(): Boolean
    fun onForeground(packageName: String)
}

object EnforcementGateRegistry {
    var current: EnforcementGate? = null
}
