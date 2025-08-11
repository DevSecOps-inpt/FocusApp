package com.devsecopsinpt.focusapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsecopsinpt.focusapp.domain.model.FocusMode

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: FocusMode, // QUICK or SCHEDULED
    val startTime: Long,
    val endTime: Long?,
    val targetMinutes: Int = 25, // Default 25 minutes
    val completed: Boolean = false, // Whether session completed successfully
    val whitelist: List<String> = emptyList(), // package names
    val vpnEnabled: Boolean = false,
    val scheduleId: Long? = null // if scheduled
)
