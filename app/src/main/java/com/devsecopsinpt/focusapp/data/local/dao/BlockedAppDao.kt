package com.devsecopsinpt.focusapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devsecopsinpt.focusapp.data.local.entity.BlockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY label")
    fun observeAll(): Flow<List<BlockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BlockedApp>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BlockedApp)

    @Delete
    suspend fun delete(item: BlockedApp)

    @Query("DELETE FROM blocked_apps")
    suspend fun clear()
}
