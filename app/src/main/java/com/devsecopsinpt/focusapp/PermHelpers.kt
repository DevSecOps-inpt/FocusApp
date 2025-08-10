package com.devsecopsinpt.focusapp

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings

fun Context.hasUsageAccess(): Boolean {
    val appOps = getSystemService(AppOpsManager::class.java)
    val mode = appOps.unsafeCheckOpNoThrow("android:get_usage_stats", Process.myUid(), packageName)
    return mode == AppOpsManager.MODE_ALLOWED
}

fun Context.canDrawOverlays(): Boolean = Settings.canDrawOverlays(this)

fun Context.openUsageAccessSettings() =
    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

fun Context.openOverlaySettings() =
    startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
