package com.example.focuslock.features.settings

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAppsScreen(
    viewModel: BlockedAppsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val blockedApps by viewModel.blockedApps.collectAsState()
    val userApps by viewModel.userApps.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Blocked Apps") }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(userApps) { app ->
                val isBlocked = viewModel.isAppBlocked(app.packageName)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleApp(app) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App icon
                        val appIcon = remember(app.packageName) {
                            try {
                                context.packageManager.getApplicationIcon(app)
                                    .toBitmap()
                                    .asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        appIcon?.let { icon ->
                            Image(
                                bitmap = icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // App name
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = context.packageManager.getApplicationLabel(app).toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Checkbox
                        Checkbox(
                            checked = isBlocked,
                            onCheckedChange = { viewModel.toggleApp(app) }
                        )
                    }
                }
            }
        }
    }
}
