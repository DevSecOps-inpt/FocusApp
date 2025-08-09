package com.example.focuslock.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.focuslock.data.local.dao.*
import com.example.focuslock.data.local.entity.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        BlockedApp::class,
        FocusSession::class,
        BlockedAttempt::class,
        Schedule::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FocusLockDatabase : RoomDatabase() {
    
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun blockedAttemptDao(): BlockedAttemptDao
    abstract fun scheduleDao(): ScheduleDao
    
    companion object {
        const val DATABASE_NAME = "focuslock_database"
        
        fun buildDatabase(context: Context, passphrase: String): FocusLockDatabase {
            val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
            
            return Room.databaseBuilder(
                context.applicationContext,
                FocusLockDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }
        
        // For debug/testing without encryption
        fun buildDatabaseDebug(context: Context): FocusLockDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FocusLockDatabase::class.java,
                "${DATABASE_NAME}_debug"
            )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Any initialization logic can go here
            }
        }
    }
} 