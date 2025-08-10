package com.devsecopsinpt.focusapp.services.focus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// --- Injected collaborators (stubs or real implementations) ---
class AppOpenDetector @Inject constructor()
class PolicyRepository @Inject constructor()
class AttemptLogger @Inject constructor()
class FocusController @Inject constructor()

@AndroidEntryPoint
class EnforcementService : Service() {

    @Inject lateinit var appOpenDetector: AppOpenDetector
    @Inject lateinit var policyRepository: PolicyRepository
    @Inject lateinit var attemptLogger: AttemptLogger
    @Inject lateinit var focusController: FocusController

    override fun onCreate() {
        super.onCreate()
        // TODO start foreground notification and begin enforcement loop
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO read commands (start/stop), observe app foreground changes, overlay lock, etc.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // TODO clean up observers
    }
}
