package com.devsecopsinpt.focusapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageNameEnc: String,  // Encrypted package name
    val labelEnc: String,                    // Encrypted app label
    val addedAt: Long = System.currentTimeMillis()
)
