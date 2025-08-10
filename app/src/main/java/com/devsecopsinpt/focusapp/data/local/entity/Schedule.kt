package com.devsecopsinpt.focusapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val daysOfWeek: Set<Int>, // 1..7 (Mon..Sun)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val whitelist: List<String> = emptyList(),
    val vpnEnabled: Boolean = false
)
