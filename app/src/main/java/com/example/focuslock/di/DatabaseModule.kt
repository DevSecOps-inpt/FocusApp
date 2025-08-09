package com.example.focuslock.di

import android.content.Context
import androidx.room.Room
import com.example.focuslock.core.security.DbKeyManager
import com.example.focuslock.data.local.FocusLockDatabase
import com.example.focuslock.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        dbKeyManager: DbKeyManager
    ): FocusLockDatabase {
        val passphrase = SQLiteDatabase.getBytes(dbKeyManager.getOrCreateKey())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context, FocusLockDatabase::class.java, "focuslock.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideBlockedAppDao(database: FocusLockDatabase): BlockedAppDao =
        database.blockedAppDao()
    
    @Provides
    fun provideFocusSessionDao(database: FocusLockDatabase): FocusSessionDao =
        database.focusSessionDao()
    
    @Provides
    fun provideBlockedAttemptDao(database: FocusLockDatabase): BlockedAttemptDao =
        database.blockedAttemptDao()
    
    @Provides
    fun provideScheduleDao(database: FocusLockDatabase): ScheduleDao =
        database.scheduleDao()
} 