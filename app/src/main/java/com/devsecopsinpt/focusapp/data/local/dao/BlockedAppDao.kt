package com.devsecopsinpt.focusapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devsecopsinpt.focusapp.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY labelEnc")
    fun observeAll(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps")
    suspend fun getAll(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BlockedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BlockedAppEntity)

    @Delete
    suspend fun delete(item: BlockedAppEntity)

    @Query("SELECT * FROM blocked_apps WHERE packageNameEnc = :packageNameEnc LIMIT 1")
    suspend fun getByPackageName(packageNameEnc: String): BlockedAppEntity?

    @Query("DELETE FROM blocked_apps")
    suspend fun clear()
}
