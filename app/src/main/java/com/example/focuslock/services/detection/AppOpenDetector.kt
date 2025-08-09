package com.example.focuslock.services.detection

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.focuslock.di.ApplicationScope
import com.example.focuslock.domain.policy.EnforcementGateRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageStatsManager: UsageStatsManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    private var detectionJob: Job? = null

    fun start() {
        if (detectionJob?.isActive == true) return
        detectionJob = scope.launch(Dispatchers.Default) { 
            detectionLoop() 
        }
    }

    fun stop() {
        detectionJob?.cancel()
        detectionJob = null
    }

    private suspend fun detectionLoop() {
        var lastCheckTime = System.currentTimeMillis() - 5_000
        val event = UsageEvents.Event()
        
        while (isActive) {
            val enforcementGate = EnforcementGateRegistry.current
            if (enforcementGate?.isActive() != true) {
                delay(1000) // Wait if no active enforcement
                continue
            }
            val currentTime = System.currentTimeMillis()
            val usageEvents = usageStatsManager.queryEvents(lastCheckTime, currentTime)
            
            var foregroundPackage: String? = null
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundPackage = event.packageName
                }
            }
            
            foregroundPackage?.let { packageName ->
                enforcementGate.onForeground(packageName)
            }
            
            lastCheckTime = currentTime
            delay(300) // Check every 300ms during enforcement - low power impact
        }
    }
}
