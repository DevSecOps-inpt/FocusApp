package com.devsecopsinpt.focusapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.devsecopsinpt.focusapp.data.BlockedAppsStore
import com.devsecopsinpt.focusapp.services.FocusSessionService
import com.devsecopsinpt.focusapp.work.StartFocusWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    // Observe blocked packages for UI
    val blocked = BlockedAppsStore.blockedFlow(app)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleBlocked(pkg: String) = viewModelScope.launch {
        BlockedAppsStore.toggle(getApplication(), pkg)
    }

    fun startNow(minutes: Int) =
        FocusSessionService.start(getApplication(), minutes)

    fun stopNow() =
        FocusSessionService.stop(getApplication())

    /** Schedule a session to start at the given wall-clock time. */
    fun schedule(minutes: Int, startAt: LocalDateTime) {
        val ctx = getApplication<Application>()
        val now = LocalDateTime.now()
        val delayMs = Duration.between(now, startAt).toMillis().coerceAtLeast(0)

        val input = Data.Builder()
            .putInt(StartFocusWorker.KEY_MINUTES, minutes)
            .build()

        val req = OneTimeWorkRequestBuilder<StartFocusWorker>()
            .setInitialDelay(Duration.ofMillis(delayMs))
            .setInputData(input)
            .build()

        WorkManager.getInstance(ctx).enqueue(req)
    }
}
