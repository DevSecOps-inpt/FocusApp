package com.devsecopsinpt.focusapp.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 1 to 2:
 * - Changes BlockedApp table to use encrypted columns
 * - This is a destructive migration since we can't decrypt old data
 * - In production, you might want to implement a more sophisticated migration
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop the old table and recreate with new schema
        // Note: This will lose existing data, but it's necessary for the encryption change
        database.execSQL("DROP TABLE IF EXISTS blocked_apps")
        
        // Create new table with encrypted columns
        database.execSQL("""
            CREATE TABLE blocked_apps (
                packageNameEnc TEXT NOT NULL PRIMARY KEY,
                labelEnc TEXT NOT NULL,
                addedAt INTEGER NOT NULL
            )
        """)
    }
}
