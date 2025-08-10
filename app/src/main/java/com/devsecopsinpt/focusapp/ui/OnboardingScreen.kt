package com.devsecopsinpt.focusapp.ui

import androidx.compose.runtime.Composable

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    PermissionsGate(onAllGranted = onDone)
}
