package com.example.focuslock.di

import android.content.Context
import com.example.focuslock.core.security.SecurityManager
import com.example.focuslock.data.local.FocusLockDatabase
import com.example.focuslock.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securityManager: SecurityManager
    ): FocusLockDatabase {
        return try {
            val passphrase = securityManager.getDatabasePassphrase()
            FocusLockDatabase.buildDatabase(context, passphrase)
        } catch (e: Exception) {
            // Fallback to debug database if encryption fails
            FocusLockDatabase.buildDatabaseDebug(context)
        }
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