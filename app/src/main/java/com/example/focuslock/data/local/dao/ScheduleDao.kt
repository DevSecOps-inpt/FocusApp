package com.example.focuslock.data.local.dao

import androidx.room.*
import com.example.focuslock.data.local.entity.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    
    @Query("SELECT * FROM schedules ORDER BY startHour, startMinute")
    fun observeAll(): Flow<List<Schedule>>
    
    @Query("SELECT * FROM schedules")
    suspend fun getAll(): List<Schedule>
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): Schedule?
    
    @Insert
    suspend fun insert(schedule: Schedule): Long
    
    @Update
    suspend fun update(schedule: Schedule)
    
    @Delete
    suspend fun delete(schedule: Schedule)
    
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
} 