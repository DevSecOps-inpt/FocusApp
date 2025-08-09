package com.example.focuslock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_attempts")
data class BlockedAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestamp: Long,
    val appLabel: String,
    val successUnlock: Boolean,
    val sessionId: Long?
) 