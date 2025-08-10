package com.devsecopsinpt.focusapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val label: String,
    val addedAt: Long = System.currentTimeMillis()
)
