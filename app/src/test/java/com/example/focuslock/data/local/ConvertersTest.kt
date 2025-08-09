package com.example.focuslock.data.local

import com.example.focuslock.domain.model.FocusMode
import org.junit.Test
import org.junit.Assert.*

class ConvertersTest {
    
    private val converters = Converters()
    
    @Test
    fun `string list conversion`() {
        val originalList = listOf("app1", "app2", "app3")
        val converted = converters.fromStringList(originalList)
        val restored = converters.toStringList(converted)
        
        assertEquals(originalList, restored)
    }
    
    @Test
    fun `empty string list conversion`() {
        val emptyList = emptyList<String>()
        val converted = converters.fromStringList(emptyList)
        val restored = converters.toStringList(converted)
        
        assertEquals(emptyList, restored)
    }
    
    @Test
    fun `int set conversion`() {
        val originalSet = setOf(1, 2, 3, 4, 5)
        val converted = converters.fromIntSet(originalSet)
        val restored = converters.toIntSet(converted)
        
        assertEquals(originalSet, restored)
    }
    
    @Test
    fun `empty int set conversion`() {
        val emptySet = emptySet<Int>()
        val converted = converters.fromIntSet(emptySet)
        val restored = converters.toIntSet(converted)
        
        assertEquals(emptySet, restored)
    }
    
    @Test
    fun `focus mode conversion`() {
        val quickMode = FocusMode.QUICK
        val converted = converters.fromMode(quickMode)
        val restored = converters.toMode(converted)
        
        assertEquals(quickMode, restored)
        
        val scheduledMode = FocusMode.SCHEDULED
        val convertedScheduled = converters.fromMode(scheduledMode)
        val restoredScheduled = converters.toMode(convertedScheduled)
        
        assertEquals(scheduledMode, restoredScheduled)
    }
}
