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
import com.example.focuslock.MainActivity
import com.example.focuslock.R
import com.example.focuslock.services.usage.AppOpenDetector
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EnforcementService : LifecycleService() {
    
    @Inject
    lateinit var appOpenDetector: AppOpenDetector
    
    @Inject
    lateinit var enforcementGate: EnforcementGateImpl
    
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
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Start enforcement
        enforcementGate.startEnforcement()
        appOpenDetector.start()
    }
    
    override fun onDestroy() {
        appOpenDetector.stop()
        enforcementGate.stopEnforcement()
        super.onDestroy()
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