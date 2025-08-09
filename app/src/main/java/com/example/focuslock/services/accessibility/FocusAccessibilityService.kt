package com.example.focuslock.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.focuslock.services.focus.EnforcementGate

class FocusAccessibilityService : AccessibilityService() {
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        // Only process window state changes
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }
        
        val packageName = event.packageName?.toString() ?: return
        
        // Ignore our own package
        if (packageName == this.packageName) return
        
        // Report to enforcement gate
        EnforcementGate.instance?.onForeground(packageName)
    }
    
    override fun onInterrupt() {
        // Called when the accessibility service is interrupted
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service is now connected and ready to receive accessibility events
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
} 