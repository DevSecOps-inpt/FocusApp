package com.devsecopsinpt.focusapp

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

fun Context.hasUsageAccess(): Boolean {
    val appOps = getSystemService(AppOpsManager::class.java)
    val mode = appOps.unsafeCheckOpNoThrow("android:get_usage_stats", Process.myUid(), packageName)
    return mode == AppOpsManager.MODE_ALLOWED
}

fun Context.canDrawOverlays(): Boolean = Settings.canDrawOverlays(this)

fun Context.notificationsAllowed(): Boolean =
    NotificationManagerCompat.from(this).areNotificationsEnabled()

fun Context.hasDndAccess(): Boolean {
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.isNotificationPolicyAccessGranted
}

fun Context.openUsageAccessSettings() =
    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

fun Context.openOverlaySettings() =
    startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )

fun Context.openNotificationSettings() {
    if (Build.VERSION.SDK_INT >= 26) {
        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } else {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

fun Context.openDndSettings() =
    startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
