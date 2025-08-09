package com.example.focuslock.services.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.focuslock.MainActivity
import com.example.focuslock.R

class FocusVpnService : VpnService() {
    
    private var vpnInterface: ParcelFileDescriptor? = null
    
    companion object {
        private const val VPN_NOTIFICATION_ID = 2
        private const val VPN_CHANNEL_ID = "focuslock.vpn"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupVpn()
    }
    
    override fun onDestroy() {
        vpnInterface?.close()
        super.onDestroy()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    private fun setupVpn() {
        val builder = Builder()
            .setSession("FocusLock VPN")
            .addAddress("10.0.0.2", 32)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
        
        // Add disallowed applications (blocked apps during focus)
        getBlockedPackages().forEach { packageName ->
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                // Package might not be installed
            }
        }
        
        vpnInterface = builder.establish()
        startForeground(VPN_NOTIFICATION_ID, createVpnNotification())
    }
    
    private fun getBlockedPackages(): List<String> {
        // TODO: Get blocked packages from enforcement gate or database
        // For now, return empty list
        return emptyList()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VPN_CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "FocusLock VPN for internet blocking"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createVpnNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, VPN_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_vpn_active))
            .setContentText("Internet blocking is active")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
} 