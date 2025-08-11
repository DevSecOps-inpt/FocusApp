package com.devsecopsinpt.focusapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import com.devsecopsinpt.focusapp.R
import com.devsecopsinpt.focusapp.data.local.entity.FocusSession
import com.devsecopsinpt.focusapp.data.local.dao.FocusSessionDao
import com.devsecopsinpt.focusapp.domain.model.FocusMode

@AndroidEntryPoint
class FocusSessionService : Service() {

    @Inject lateinit var sessionDao: FocusSessionDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra(EXTRA_MINUTES, 25) ?: 25
        startForeground(NOTIF_ID, buildNotification(minutes, minutes * 60L))
        startTimer(minutes)
        return START_NOT_STICKY
    }

    private fun startTimer(minutes: Int) {
        val start = System.currentTimeMillis()
        val end = start + minutes * 60_000L
        createChannel()

        scope.launch(Dispatchers.Default) {
            var remaining = (end - System.currentTimeMillis()).coerceAtLeast(0)
            while (remaining > 0) {
                updateNotification(minutes, remaining / 1000L)
                delay(1_000)
                remaining = (end - System.currentTimeMillis()).coerceAtLeast(0)
            }
            // log completion
            sessionDao.insert(
                FocusSession(
                    mode = FocusMode.QUICK,
                    startTime = start,
                    endTime = System.currentTimeMillis(),
                    targetMinutes = minutes,
                    completed = true
                )
            )
            stopSelf()
        }
    }

    private fun buildNotification(targetMin: Int, secondsLeft: Long): Notification {
        createChannel()
        val mm = secondsLeft / 60
        val ss = secondsLeft % 60
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name) // add a vector in res/drawable
            .setContentTitle("Focus session running")
            .setContentText("Time left: %02d:%02d • Target %d min".format(mm, ss, targetMin))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(targetMin: Int, secondsLeft: Long) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(targetMin, secondsLeft))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Focus Session",
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "focus_session"
        private const val NOTIF_ID = 1337
        const val EXTRA_MINUTES = "minutes"

        fun start(ctx: Context, minutes: Int) {
            ctx.startForegroundService(Intent(ctx, FocusSessionService::class.java).apply {
                putExtra(EXTRA_MINUTES, minutes)
            })
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, FocusSessionService::class.java))
        }
    }
}
