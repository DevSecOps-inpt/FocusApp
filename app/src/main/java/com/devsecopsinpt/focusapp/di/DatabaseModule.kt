package com.devsecopsinpt.focusapp.di

import android.content.Context
import androidx.room.Room
import com.devsecopsinpt.focusapp.data.local.FocusLockDatabase
import com.devsecopsinpt.focusapp.data.local.dao.BlockedAppDao
import com.devsecopsinpt.focusapp.core.security.DbKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.sqlcipher.database.SupportFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext ctx: Context,
        keyMgr: DbKeyManager
    ): FocusLockDatabase {
        val factory = SupportFactory(keyMgr.getOrCreate32())
        return Room.databaseBuilder(ctx, FocusLockDatabase::class.java, "focuslock.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBlockedAppDao(db: FocusLockDatabase): BlockedAppDao = db.blockedAppDao()
}
