package com.example.focuslock.services.overlay

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.example.focuslock.R
import com.example.focuslock.core.auth.AuthHelper
import com.example.focuslock.core.security.SecurityManager
import com.example.focuslock.core.security.UnlockAuth
import com.example.focuslock.data.repo.AttemptLogger
import com.example.focuslock.services.focus.EnforcementGateImpl
import com.example.focuslock.services.focus.EnforcementService
import com.example.focuslock.ui.theme.FocusLockTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LockOverlayActivity : ComponentActivity() {
    
    @Inject
    lateinit var unlockAuth: UnlockAuth
    
    @Inject
    lateinit var attemptLogger: AttemptLogger
    
    private var blockedPackage: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        blockedPackage = intent.getStringExtra("pkg")
        
        setContent {
            FocusLockTheme {
                LockOverlayScreen(
                    packageName = blockedPackage ?: "",
                    onUnlock = { unlockMinutes -> attemptUnlock(unlockMinutes) },
                    onCloseApp = { closeApp() }
                )
            }
        }
    }
    
    private fun attemptUnlock(unlockMinutes: Int) {
        lifecycleScope.launch {
            val success = unlockAuth.authenticate(this@LockOverlayActivity)
            
            blockedPackage?.let { pkg ->
                attemptLogger.logAttempt(pkg, success = success, sessionId = null)
                
                if (success) {
                    // Grant temporary bypass for this app
                    val enforcementService = EnforcementService()
                    enforcementService.grantBypass(pkg, unlockMinutes)
                    finish()
                }
            }
        }
    }
    
    private fun closeApp() {
        // Send user to home screen
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back navigation to blocked app
        closeApp()
    }
    
    override fun onPause() {
        super.onPause()
        // If user switches away from overlay, close the blocked app
        closeApp()
    }
}

@Composable
fun LockOverlayScreen(
    packageName: String,
    onUnlock: (Int) -> Unit,
    onCloseApp: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    // Get app info
    val appInfo = remember(packageName) {
        try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    val appName = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: packageName
    val appIcon = appInfo?.let { packageManager.getApplicationIcon(it).toBitmap().asImageBitmap() }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon
            appIcon?.let { icon ->
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // App name
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Block message
            Text(
                text = stringResource(R.string.lock_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Duration selection
            var selectedDuration by remember { mutableStateOf(5) }
            Text(
                text = "Unlock for:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 5, 15).forEach { minutes ->
                    FilterChip(
                        onClick = { selectedDuration = minutes },
                        label = { Text("${minutes}m") },
                        selected = selectedDuration == minutes
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Unlock button
            Button(
                onClick = { onUnlock(selectedDuration) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_unlock),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Close app button
            OutlinedButton(
                onClick = onCloseApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_close_app),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// Extension function to show lock overlay
fun Context.showLockOverlay(packageName: String) {
    val intent = Intent(this, LockOverlayActivity::class.java).apply {
        putExtra("pkg", packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
    startActivity(intent)
} 