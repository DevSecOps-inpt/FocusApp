// app/src/main/java/com/devsecopsinpt/focusapp/work/StartFocusWorker.kt
package com.devsecopsinpt.focusapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devsecopsinpt.focusapp.services.FocusSessionService

class StartFocusWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val minutes = inputData.getInt(KEY_MINUTES, 25)
        FocusSessionService.start(applicationContext, minutes)
        return Result.success()
    }

    companion object {
        const val KEY_MINUTES = "minutes"
    }
}
