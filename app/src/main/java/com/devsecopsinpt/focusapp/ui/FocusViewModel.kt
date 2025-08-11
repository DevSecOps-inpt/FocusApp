package com.devsecopsinpt.focusapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devsecopsinpt.focusapp.data.local.dao.FocusSessionDao
import com.devsecopsinpt.focusapp.services.FocusSessionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    app: Application,
    sessionDao: FocusSessionDao
) : AndroidViewModel(app) {

    val sessions = sessionDao.sessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun start(minutes: Int) = FocusSessionService.start(getApplication(), minutes)
    fun stop() = FocusSessionService.stop(getApplication())
}
