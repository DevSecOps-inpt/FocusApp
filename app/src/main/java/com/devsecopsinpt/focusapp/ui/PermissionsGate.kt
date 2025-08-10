package com.devsecopsinpt.focusapp.ui

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat

private fun hasUsageAccess(ctx: Context): Boolean {
    val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        "android:get_usage_stats",
        android.os.Process.myUid(),
        ctx.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun hasOverlay(ctx: Context) = Settings.canDrawOverlays(ctx)

private fun notificationsAllowed(ctx: Context) =
    NotificationManagerCompat.from(ctx).areNotificationsEnabled()

private fun hasDndAccess(ctx: Context): Boolean {
    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.isNotificationPolicyAccessGranted
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsGate(
    onAllGranted: () -> Unit
) {
    val ctx = LocalContext.current
    var askedOnce by remember { mutableStateOf(false) }

    // POST_NOTIFICATIONS (runtime dialog)
    val requestNotif = rememberLauncherForActivityResult(RequestPermission()) { /* re-check below */ }

    LaunchedEffect(Unit) {
        if (!askedOnce) {
            askedOnce = true
            if (Build.VERSION.SDK_INT >= 33 && !notificationsAllowed(ctx)) {
                requestNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val allOk = notificationsAllowed(ctx) &&
            hasUsageAccess(ctx) &&
            hasOverlay(ctx) &&
            hasDndAccess(ctx)

    if (allOk) {
        onAllGranted()
        return
    }

    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Let's finish setup", style = MaterialTheme.typography.titleLarge)

            if (!notificationsAllowed(ctx)) {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        requestNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }) { Text("Allow notifications") }
            }

            if (!hasUsageAccess(ctx)) {
                Button(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }) { Text("Enable Usage Access") }
            }

            if (!hasOverlay(ctx)) {
                Button(onClick = {
                    ctx.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${ctx.packageName}")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }) { Text("Allow Draw over other apps") }
            }

            if (!hasDndAccess(ctx)) {
                Button(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }) { Text("Allow Do Not Disturb access") }
            }
        }
    }
}
