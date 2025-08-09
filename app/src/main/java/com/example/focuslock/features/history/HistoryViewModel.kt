package com.example.focuslock.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslock.data.local.dao.BlockedAttemptDao
import com.example.focuslock.data.local.dao.FocusSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val blockedAttemptDao: BlockedAttemptDao,
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {
    
    val allAttempts = blockedAttemptDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val allSessions = focusSessionDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    // TODO: Add date filters, type filters, sorting functionality
}
