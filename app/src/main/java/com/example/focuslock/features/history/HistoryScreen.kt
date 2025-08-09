package com.example.focuslock.features.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val attempts by viewModel.allAttempts.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("History") }
        )
        
        // Tab row
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Attempts") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Sessions") }
            )
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                // Blocked attempts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attempts) { attempt ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = attempt.appLabel,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = attempt.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                            .format(Date(attempt.timestamp)),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = if (attempt.successUnlock) "Unlocked" else "Blocked",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (attempt.successUnlock) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Focus sessions
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "${session.mode} Session",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Started: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                        .format(Date(session.startTime))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                session.endTime?.let { endTime ->
                                    Text(
                                        text = "Ended: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                            .format(Date(endTime))}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (session.whitelist.isNotEmpty()) {
                                    Text(
                                        text = "Whitelist: ${session.whitelist.size} apps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
