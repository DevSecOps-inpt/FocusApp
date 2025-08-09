package com.example.focuslock.services.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.focuslock.services.focus.EnforcementGate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageStatsManager: UsageStatsManager,
    private val enforcementGate: EnforcementGate
) {
    private var detectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    fun start() {
        if (detectionJob?.isActive == true) return
        
        detectionJob = scope.launch {
            runDetectionLoop()
        }
    }
    
    fun stop() {
        detectionJob?.cancel()
        detectionJob = null
    }
    
    private suspend fun runDetectionLoop() {
        var lastTimestamp = System.currentTimeMillis() - 2000
        val event = UsageEvents.Event()
        
        while (isActive && enforcementGate.isActive()) {
            try {
                val currentTime = System.currentTimeMillis()
                val events = usageStatsManager.queryEvents(lastTimestamp, currentTime)
                var topPackage: String? = null
                
                // Find the most recent MOVE_TO_FOREGROUND event
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        topPackage = event.packageName
                    }
                }
                
                // Report the top package to enforcement gate
                topPackage?.let { packageName ->
                    if (packageName != context.packageName) { // Don't block ourselves
                        enforcementGate.onForeground(packageName)
                    }
                }
                
                lastTimestamp = currentTime
                delay(300) // Lightweight polling interval
                
            } catch (e: Exception) {
                // Handle SecurityException if usage access is revoked
                delay(1000) // Back off on errors
            }
        }
    }
    
    fun isUsageAccessGranted(): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(currentTime - 1000, currentTime)
            events.hasNextEvent()
        } catch (e: Exception) {
            false
        }
    }
} 