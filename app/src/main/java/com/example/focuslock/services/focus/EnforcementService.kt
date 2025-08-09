package com.example.focuslock.services.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.focuslock.MainActivity
import com.example.focuslock.R
import com.example.focuslock.services.overlay.LockOverlayActivity
import kotlinx.coroutines.launch
import com.example.focuslock.services.detection.AppOpenDetector
import com.example.focuslock.domain.policy.EnforcementGate
import com.example.focuslock.domain.policy.EnforcementGateRegistry
import com.example.focuslock.data.repo.PolicyRepository
import com.example.focuslock.data.repo.AttemptLogger
import com.example.focuslock.data.repo.PolicyMode
import java.util.concurrent.ConcurrentHashMap
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EnforcementService : LifecycleService(), EnforcementGate {
    
    @Inject
    lateinit var appOpenDetector: AppOpenDetector
    
    @Inject
    lateinit var policyRepository: PolicyRepository
    
    @Inject
    lateinit var attemptLogger: AttemptLogger
    
    private val bypassMap = ConcurrentHashMap<String, Long>() // packageName -> untilEpochMs
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "focuslock.enforcement"
        
        fun start(context: Context) {
            val intent = Intent(context, EnforcementService::class.java)
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, EnforcementService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        EnforcementGateRegistry.current = this
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        appOpenDetector.start()
    }
    
    override fun onDestroy() {
        appOpenDetector.stop()
        if (EnforcementGateRegistry.current === this) {
            EnforcementGateRegistry.current = null
        }
        super.onDestroy()
    }

    override fun isActive(): Boolean = policyRepository.currentPolicy.value.enforcementActive

    override fun onForeground(packageName: String) {
        val policy = policyRepository.currentPolicy.value
        val currentTime = System.currentTimeMillis()
        val bypassUntil = bypassMap[packageName] ?: 0L
        
        if (currentTime < bypassUntil) return

        val shouldBlock = when {
            policy.mode == PolicyMode.FOCUS && !policy.whitelist.contains(packageName) -> true
            policy.mode == PolicyMode.BLOCKLIST && policy.blocked.contains(packageName) -> true
            else -> false
        }
        
        if (shouldBlock) {
            lifecycleScope.launch {
                attemptLogger.logAttempt(packageName, success = false, sessionId = policy.sessionId)
                showLockOverlay(packageName)
            }
        }
    }

    fun grantBypass(packageName: String, minutes: Int) {
        bypassMap[packageName] = System.currentTimeMillis() + minutes * 60_000L
    }

    private fun showLockOverlay(packageName: String) {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            putExtra("pkg", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_enforcement),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_enforcement_desc)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_focus_active))
            .setContentText("Tap to manage focus settings")
            .setSmallIcon(R.drawable.ic_notification) // We'll need to create this
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
} 