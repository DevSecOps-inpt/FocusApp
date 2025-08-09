package com.example.focuslock.data.local.dao

import androidx.room.*
import com.example.focuslock.data.local.entity.BlockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    
    @Query("SELECT * FROM blocked_apps ORDER BY appLabel ASC")
    fun observeAll(): Flow<List<BlockedApp>>
    
    @Query("SELECT * FROM blocked_apps")
    suspend fun getAll(): List<BlockedApp>
    
    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getByPackage(packageName: String): BlockedApp?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BlockedApp)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BlockedApp>)
    
    @Delete
    suspend fun delete(item: BlockedApp)
    
    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
    
    @Query("DELETE FROM blocked_apps")
    suspend fun deleteAll()
} 