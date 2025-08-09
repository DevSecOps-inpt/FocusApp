package com.example.focuslock.data.local

import androidx.room.TypeConverter
import com.example.focuslock.domain.model.FocusMode

class Converters {
    
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString("|")
    
    @TypeConverter
    fun toStringList(s: String): List<String> = 
        if (s.isBlank()) emptyList() else s.split("|")
    
    @TypeConverter
    fun fromIntSet(set: Set<Int>): String = set.joinToString(",")
    
    @TypeConverter
    fun toIntSet(s: String): Set<Int> = 
        if (s.isBlank()) emptySet() else s.split(",").map { it.toInt() }.toSet()
    
    @TypeConverter
    fun fromMode(mode: FocusMode): String = mode.name
    
    @TypeConverter
    fun toMode(s: String): FocusMode = FocusMode.valueOf(s)
} 