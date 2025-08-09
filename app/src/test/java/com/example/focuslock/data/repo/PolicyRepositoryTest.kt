package com.example.focuslock.data.repo

import com.example.focuslock.data.local.dao.BlockedAppDao
import com.example.focuslock.data.local.dao.FocusSessionDao
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class PolicyRepositoryTest {
    
    private val blockedAppDao: BlockedAppDao = mockk()
    private val sessionDao: FocusSessionDao = mockk()
    private val repository = PolicyRepository(blockedAppDao, sessionDao)
    
    @Test
    fun `initial policy state is idle`() {
        val initialState = repository.currentPolicy.value
        
        assertFalse(initialState.enforcementActive)
        assertEquals(PolicyMode.BLOCKLIST, initialState.mode)
        assertTrue(initialState.whitelist.isEmpty())
        assertTrue(initialState.blocked.isEmpty())
        assertNull(initialState.sessionId)
        assertFalse(initialState.vpnEnabled)
    }
    
    @Test
    fun `setFocusMode activates focus policy`() = runTest {
        val whitelist = listOf("com.example.allowed1", "com.example.allowed2")
        val sessionId = 123L
        
        repository.setFocusMode(whitelist, sessionId, vpnEnabled = true)
        
        val state = repository.currentPolicy.value
        assertTrue(state.enforcementActive)
        assertEquals(PolicyMode.FOCUS, state.mode)
        assertEquals(whitelist.toSet(), state.whitelist)
        assertTrue(state.blocked.isEmpty())
        assertEquals(sessionId, state.sessionId)
        assertTrue(state.vpnEnabled)
    }
    
    @Test
    fun `setAppLock activates blocklist policy`() = runTest {
        val blocked = setOf("com.example.blocked1", "com.example.blocked2")
        
        repository.setAppLock(blocked)
        
        val state = repository.currentPolicy.value
        assertTrue(state.enforcementActive)
        assertEquals(PolicyMode.BLOCKLIST, state.mode)
        assertTrue(state.whitelist.isEmpty())
        assertEquals(blocked, state.blocked)
        assertNull(state.sessionId)
        assertFalse(state.vpnEnabled)
    }
    
    @Test
    fun `stop deactivates policy`() = runTest {
        // First activate a policy
        repository.setFocusMode(listOf("com.example.test"), 1L, false)
        assertTrue(repository.currentPolicy.value.enforcementActive)
        
        // Then stop it
        repository.stop()
        
        val state = repository.currentPolicy.value
        assertFalse(state.enforcementActive)
        assertEquals(PolicyMode.BLOCKLIST, state.mode)
        assertTrue(state.whitelist.isEmpty())
        assertTrue(state.blocked.isEmpty())
        assertNull(state.sessionId)
        assertFalse(state.vpnEnabled)
    }
}
