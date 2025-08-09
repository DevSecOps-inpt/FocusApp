package com.example.focuslock.data.local.dao

import androidx.room.*
import com.example.focuslock.data.local.entity.BlockedAttempt
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAttemptDao {
    
    @Query("SELECT * FROM blocked_attempts ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BlockedAttempt>>
    
    @Query("SELECT * FROM blocked_attempts WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun observeBySession(sessionId: Long): Flow<List<BlockedAttempt>>
    
    @Query("SELECT * FROM blocked_attempts WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun observeByPackage(packageName: String): Flow<List<BlockedAttempt>>
    
    @Query("SELECT * FROM blocked_attempts WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun observeSince(since: Long): Flow<List<BlockedAttempt>>
    
    @Query("SELECT COUNT(*) FROM blocked_attempts WHERE timestamp >= :since")
    suspend fun countSince(since: Long): Int
    
    @Query("SELECT COUNT(*) FROM blocked_attempts WHERE packageName = :packageName AND timestamp >= :since")
    suspend fun countByPackageSince(packageName: String, since: Long): Int
    
    @Insert
    suspend fun insert(attempt: BlockedAttempt)
    
    @Insert
    suspend fun insertAll(attempts: List<BlockedAttempt>)
    
    @Delete
    suspend fun delete(attempt: BlockedAttempt)
    
    @Query("DELETE FROM blocked_attempts WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
    
    @Query("DELETE FROM blocked_attempts")
    suspend fun deleteAll()
} 