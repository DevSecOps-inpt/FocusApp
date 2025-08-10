package com.devsecopsinpt.focusapp.di

import android.content.Context
import androidx.room.Room
import com.devsecopsinpt.focusapp.data.local.FocusLockDatabase
import com.devsecopsinpt.focusapp.data.local.dao.*
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
    fun provideDatabase(@ApplicationContext ctx: Context): FocusLockDatabase =
        Room.databaseBuilder(ctx, FocusLockDatabase::class.java, "focus_lock.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBlockedAppDao(db: FocusLockDatabase): BlockedAppDao = db.blockedAppDao()
    
    @Provides
    fun provideFocusSessionDao(db: FocusLockDatabase): FocusSessionDao = db.focusSessionDao()
    
    @Provides
    fun provideBlockedAttemptDao(db: FocusLockDatabase): BlockedAttemptDao = db.blockedAttemptDao()
    
    @Provides
    fun provideScheduleDao(db: FocusLockDatabase): ScheduleDao = db.scheduleDao()
}
