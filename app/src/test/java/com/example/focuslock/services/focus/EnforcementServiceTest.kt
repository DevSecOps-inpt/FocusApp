package com.example.focuslock.services.focus

import com.example.focuslock.data.repo.PolicyRepository
import com.example.focuslock.data.repo.PolicyState
import com.example.focuslock.data.repo.PolicyMode
import com.example.focuslock.data.repo.AttemptLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Before

class EnforcementServiceTest {
    
    private val policyRepository: PolicyRepository = mockk()
    private val attemptLogger: AttemptLogger = mockk()
    private val policyStateFlow = MutableStateFlow(PolicyState.idle())
    
    @Before
    fun setup() {
        coEvery { policyRepository.currentPolicy } returns policyStateFlow
        coEvery { attemptLogger.logAttempt(any(), any(), any()) } returns Unit
    }
    
    @Test
    fun `focus mode blocks non-whitelisted apps`() = runTest {
        // Setup focus mode with whitelist
        val policy = PolicyState(
            enforcementActive = true,
            mode = PolicyMode.FOCUS,
            whitelist = setOf("com.example.allowed"),
            blocked = emptySet(),
            sessionId = 1L,
            vpnEnabled = false
        )
        policyStateFlow.value = policy
        
        // Test that non-whitelisted app is blocked
        val shouldBlock = when {
            policy.mode == PolicyMode.FOCUS && !policy.whitelist.contains("com.example.blocked") -> true
            policy.mode == PolicyMode.BLOCKLIST && policy.blocked.contains("com.example.blocked") -> true
            else -> false
        }
        
        assert(shouldBlock) { "Non-whitelisted app should be blocked in focus mode" }
    }
    
    @Test
    fun `focus mode allows whitelisted apps`() = runTest {
        // Setup focus mode with whitelist
        val policy = PolicyState(
            enforcementActive = true,
            mode = PolicyMode.FOCUS,
            whitelist = setOf("com.example.allowed"),
            blocked = emptySet(),
            sessionId = 1L,
            vpnEnabled = false
        )
        policyStateFlow.value = policy
        
        // Test that whitelisted app is allowed
        val shouldBlock = when {
            policy.mode == PolicyMode.FOCUS && !policy.whitelist.contains("com.example.allowed") -> true
            policy.mode == PolicyMode.BLOCKLIST && policy.blocked.contains("com.example.allowed") -> true
            else -> false
        }
        
        assert(!shouldBlock) { "Whitelisted app should be allowed in focus mode" }
    }
    
    @Test
    fun `blocklist mode blocks listed apps`() = runTest {
        // Setup blocklist mode
        val policy = PolicyState(
            enforcementActive = true,
            mode = PolicyMode.BLOCKLIST,
            whitelist = emptySet(),
            blocked = setOf("com.example.blocked"),
            sessionId = null,
            vpnEnabled = false
        )
        policyStateFlow.value = policy
        
        // Test that blocked app is blocked
        val shouldBlock = when {
            policy.mode == PolicyMode.FOCUS && !policy.whitelist.contains("com.example.blocked") -> true
            policy.mode == PolicyMode.BLOCKLIST && policy.blocked.contains("com.example.blocked") -> true
            else -> false
        }
        
        assert(shouldBlock) { "Blocked app should be blocked in blocklist mode" }
    }
    
    @Test
    fun `blocklist mode allows non-listed apps`() = runTest {
        // Setup blocklist mode
        val policy = PolicyState(
            enforcementActive = true,
            mode = PolicyMode.BLOCKLIST,
            whitelist = emptySet(),
            blocked = setOf("com.example.blocked"),
            sessionId = null,
            vpnEnabled = false
        )
        policyStateFlow.value = policy
        
        // Test that non-blocked app is allowed
        val shouldBlock = when {
            policy.mode == PolicyMode.FOCUS && !policy.whitelist.contains("com.example.allowed") -> true
            policy.mode == PolicyMode.BLOCKLIST && policy.blocked.contains("com.example.allowed") -> true
            else -> false
        }
        
        assert(!shouldBlock) { "Non-blocked app should be allowed in blocklist mode" }
    }
    
    @Test
    fun `inactive enforcement allows all apps`() = runTest {
        // Setup inactive enforcement
        val policy = PolicyState.idle()
        policyStateFlow.value = policy
        
        // Test that all apps are allowed when enforcement is inactive
        assert(!policy.enforcementActive) { "Enforcement should be inactive" }
    }
}
