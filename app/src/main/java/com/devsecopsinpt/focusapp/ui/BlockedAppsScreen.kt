// app/src/main/java/com/devsecopsinpt/focusapp/ui/BlockedAppsScreen.kt
package com.devsecopsinpt.focusapp

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext   // <-- import this
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BlockedAppsScreen(vm: FocusViewModel = hiltViewModel()) {
    val ctx = LocalContext.current                      // <-- use .current
    val blocked by vm.blocked.collectAsState()

    // Load launchable apps once (re-run if context changes)
    val apps by remember(ctx) {
        mutableStateOf(loadLaunchable(ctx.packageManager))
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage blocked apps", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(apps) { app ->
                val pkg = app.activityInfo.packageName
                val isBlocked = pkg in blocked
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(app.loadLabel(ctx.packageManager).toString())
                        Switch(
                            checked = isBlocked,
                            onCheckedChange = { vm.toggleBlocked(pkg) }
                        )
                    }
                }
            }
        }
    }
}

private fun loadLaunchable(pm: PackageManager): List<ResolveInfo> {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return pm.queryIntentActivities(intent, 0).sortedBy {
        it.loadLabel(pm).toString().lowercase()
    }
}
