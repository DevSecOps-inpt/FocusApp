package com.example.focuslock.data.repo

import android.content.Context
import com.example.focuslock.data.local.dao.BlockedAttemptDao
import com.example.focuslock.data.local.entity.BlockedAttempt
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttemptLogger @Inject constructor(
    private val blockedAttemptDao: BlockedAttemptDao,
    @ApplicationContext private val context: Context
) {
    suspend fun logAttempt(packageName: String, success: Boolean, sessionId: Long?) {
        val appLabel = try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName // fallback to package name if label can't be resolved
        }
        
        blockedAttemptDao.insert(
            BlockedAttempt(
                packageName = packageName,
                appLabel = appLabel,
                timestamp = System.currentTimeMillis(),
                successUnlock = success,
                sessionId = sessionId
            )
        )
    }
}
