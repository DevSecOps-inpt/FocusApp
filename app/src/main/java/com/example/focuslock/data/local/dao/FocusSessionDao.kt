package com.example.focuslock.data.local.dao

import androidx.room.*
import com.example.focuslock.data.local.entity.FocusSession
import com.example.focuslock.domain.model.FocusMode
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<FocusSession>>
    
    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL")
    fun observeActiveSessions(): Flow<List<FocusSession>>
    
    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getById(id: Long): FocusSession?
    
    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): FocusSession?
    
    @Query("SELECT * FROM focus_sessions WHERE mode = :mode ORDER BY startTime DESC")
    fun observeByMode(mode: FocusMode): Flow<List<FocusSession>>
    
    @Insert
    suspend fun insert(session: FocusSession): Long
    
    @Update
    suspend fun update(session: FocusSession)
    
    @Query("UPDATE focus_sessions SET endTime = :endTime WHERE id = :id")
    suspend fun endSession(id: Long, endTime: Long)
    
    @Query("UPDATE focus_sessions SET endTime = :endTime WHERE endTime IS NULL")
    suspend fun endAllActiveSessions(endTime: Long)
    
    @Delete
    suspend fun delete(session: FocusSession)
    
    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
} 