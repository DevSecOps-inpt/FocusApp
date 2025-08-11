@file:OptIn(ExperimentalMaterial3Api::class)

package com.devsecopsinpt.focusapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { FocusLockApp() }
  }
}

@Composable
fun FocusLockApp() {
  val nav = rememberNavController()
  val items = listOf(
    Dest.FOCUS, Dest.BLOCKED, Dest.SCHEDULES, Dest.STATS, Dest.SETTINGS
  )

  Scaffold(
    topBar = {
      TopAppBar(title = { Text("Focus Lock") })
    },
    bottomBar = {
      NavigationBar {
        val currentDest by nav.currentBackStackEntryAsState()
        val dest = currentDest?.destination
        items.forEach { item ->
          NavigationBarItem(
            selected = dest.isInHierarchy(item.route),
            onClick = { nav.navigate(item.route) { launchSingleTop = true; popUpTo(Dest.FOCUS.route) } },
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) }
          )
        }
      }
    }
  ) { padding ->
    NavHost(
      navController = nav,
      startDestination = Dest.FOCUS.route,
      modifier = Modifier.fillMaxSize().padding(padding)
    ) {
      composable(Dest.FOCUS.route) { FocusConfigScreen() }
      composable(Dest.BLOCKED.route) { BlockedAppsScreen() }
      composable(Dest.SCHEDULES.route) { SchedulesScreen() }
      composable(Dest.STATS.route) { StatsScreen() }
      composable(Dest.SETTINGS.route) { SettingsScreen() }
    }
  }
}

private fun NavDestination?.isInHierarchy(route: String) =
  this?.hierarchy?.any { it.route == route } == true

// ---- Destinations model ----
private sealed class Dest(
  val route: String,
  val label: String,
  val icon: ImageVector
) {
  data object FOCUS     : Dest("focus",     "Focus",     Icons.Filled.PlayCircle)
  data object BLOCKED   : Dest("blocked",   "Blocked",   Icons.Filled.Block)
  data object SCHEDULES : Dest("schedules", "Schedules", Icons.Filled.Schedule)
  data object STATS     : Dest("stats",     "Stats",     Icons.Filled.Insights)
  data object SETTINGS  : Dest("settings",  "Settings",  Icons.Filled.Settings)
}

// ---- Screen implementations ----
@Composable
fun FocusConfigScreen(
  vm: FocusViewModel = hiltViewModel()
) {
  var duration by remember { mutableStateOf(25) }  // minutes
  var showTimePicker by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      "Start a distraction-free session", 
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center
    )
    
    Text(
      "Choose your focus duration and start blocking distracting apps",
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center
    )

    // Duration chooser
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf(15, 25, 45, 60).forEach { m ->
        FilterChip(
          selected = duration == m,
          onClick = { duration = m },
          label = { Text("$m m") }
        )
      }
    }

    Button(
      onClick = { vm.startNow(duration) },
      modifier = Modifier.fillMaxWidth()
    ) { 
      Text("Start focus session") 
    }
    
    OutlinedButton(
      onClick = { vm.stopNow() },
      modifier = Modifier.fillMaxWidth()
    ) { 
      Text("Stop") 
    }

    OutlinedButton(
      onClick = { showTimePicker = true },
      modifier = Modifier.fillMaxWidth()
    ) { 
      Text("Schedule…") 
    }
  }

  if (showTimePicker) {
    // Simple platform TimePicker dialog
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val now = java.util.Calendar.getInstance()
    android.app.TimePickerDialog(
      ctx,
      { _, hour, minute ->
        val startAt = java.time.LocalDateTime.now()
          .withHour(hour)
          .withMinute(minute)
          .withSecond(0)
          .withNano(0)
        // If selected time already passed today, schedule for tomorrow
        val fixed = if (startAt.isBefore(java.time.LocalDateTime.now()))
          startAt.plusDays(1) else startAt
        vm.schedule(duration, fixed)
        showTimePicker = false
        // (Optional) show a snackbar/toast
      },
      now.get(java.util.Calendar.HOUR_OF_DAY),
      now.get(java.util.Calendar.MINUTE),
      true
    ).apply {
      setOnDismissListener { showTimePicker = false }
    }.show()
  }
}

// BlockedAppsScreen is now implemented in ui/BlockedAppsScreen.kt

@Composable 
fun SchedulesScreen() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      "Focus Schedules", 
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center
    )
    
    Text(
      "Set up recurring focus sessions",
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center
    )
    
    // TODO: Add schedule list and creation form
  }
}

@Composable 
fun StatsScreen() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      "Focus Statistics", 
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center
    )
    
    Text(
      "Track your focus sessions and blocked attempts",
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center
    )
    
    // TODO: Add charts and statistics from your database
  }
}

@Composable 
fun SettingsScreen() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      "Settings", 
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center
    )
    
    Text(
      "Configure app preferences and permissions",
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center
    )
    
    // TODO: Add settings toggles and configuration options
  }
}
