package com.devsecopsinpt.focusapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.devsecopsinpt.focusapp.canDrawOverlays
import com.devsecopsinpt.focusapp.hasUsageAccess
import com.devsecopsinpt.focusapp.openOverlaySettings
import com.devsecopsinpt.focusapp.openUsageAccessSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var usageOk by remember { mutableStateOf(ctx.hasUsageAccess()) }
    var overlayOk by remember { mutableStateOf(ctx.canDrawOverlays()) }

    fun refresh() { usageOk = ctx.hasUsageAccess(); overlayOk = ctx.canDrawOverlays() }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar({ Text("Setup") }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            Text("We need a couple permissions to work fully. Nothing leaves your device.")
            Spacer(Modifier.height(16.dp))

            PermRow("Usage Access", usageOk, "Required to detect opened apps.") {
                ctx.openUsageAccessSettings()
            }
            PermRow("Draw over apps", overlayOk, "Needed to show the lock screen.") {
                ctx.openOverlaySettings()
            }

            Spacer(Modifier.height(16.dp))
            Button(
                enabled = usageOk && overlayOk,
                onClick = onDone
            ) { Text("Continue") }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { refresh() }) { Text("I granted them — Refresh") }
        }
    }
}

@Composable
private fun PermRow(title: String, granted: Boolean, desc: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
        if (granted) Text("Granted", color = Color(0xFF2E7D32))
        else Button(onClick = onClick) { Text("Grant") }
    }
}
