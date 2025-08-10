package com.devsecopsinpt.focusapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.devsecopsinpt.focusapp.data.local.dao.*
import com.devsecopsinpt.focusapp.data.local.entity.*

@Database(
    entities = [
        BlockedApp::class,
        FocusSession::class,
        BlockedAttempt::class,
        Schedule::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusLockDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun blockedAttemptDao(): BlockedAttemptDao
    abstract fun scheduleDao(): ScheduleDao
}
