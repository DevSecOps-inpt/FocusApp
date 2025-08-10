package com.devsecopsinpt.focusapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devsecopsinpt.focusapp.data.local.dao.BlockedAppDao
import com.devsecopsinpt.focusapp.data.local.entity.BlockedApp

@Database(
    entities = [BlockedApp::class],
    version = 1,
    exportSchema = false
)
abstract class FocusLockDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
}
