package com.devsecopsinpt.focusapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devsecopsinpt.focusapp.ui.OnboardingScreen
import com.devsecopsinpt.focusapp.canDrawOverlays
import com.devsecopsinpt.focusapp.hasUsageAccess
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusAppRoot()
        }
    }
}

@Composable
fun FocusAppRoot() {
    val start = if (permAllGranted()) "home" else "onboarding"
    val nav = rememberNavController()
    MaterialTheme {
        NavHost(navController = nav, startDestination = start) {
            composable("onboarding") {
                OnboardingScreen(onDone = {
                    nav.navigate("home") { popUpTo(0) }
                })
            }
            composable("home") {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Scaffold(topBar = { TopAppBar({ Text("FocusLock") }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            Text("Welcome. Start wiring features from here.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { /* start quick focus later */ }) { Text("Start Quick Focus") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* go to Blocked Apps */ }) { Text("Manage Blocked Apps") }
        }
    }
}

private fun permAllGranted(): Boolean {
    val ctx = FocusAppRef.appContext
    return ctx?.let { it.hasUsageAccess() && it.canDrawOverlays() } ?: false
}