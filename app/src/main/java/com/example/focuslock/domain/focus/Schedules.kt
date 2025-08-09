package com.example.focuslock.domain.focus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.focuslock.data.local.entity.Schedule
import java.util.*

object Schedules {
    
    fun arm(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            Intent("com.devsecopsinpt.focusapp.ACTION_START_FOCUS").apply {
                setPackage(context.packageName)
                putExtra("schedule_id", schedule.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val nextTriggerTime = nextTriggerFor(schedule)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerTime,
            pendingIntent
        )
    }
    
    fun rearmAll(context: Context) {
        // TODO: Query database for all active schedules and arm each one
        // This would be called from the boot receiver
    }
    
    fun nextTriggerFor(schedule: Schedule): Long {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        // Find the next occurrence of this schedule
        for (day in schedule.daysOfWeek.sorted()) {
            val targetCalendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, day)
                set(Calendar.HOUR_OF_DAY, schedule.startHour)
                set(Calendar.MINUTE, schedule.startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // If this time hasn't passed today, or it's a future day this week
            if (targetCalendar.timeInMillis > System.currentTimeMillis()) {
                return targetCalendar.timeInMillis
            }
        }
        
        // If no occurrence this week, find next week's first occurrence
        val nextWeekCalendar = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 1)
            set(Calendar.DAY_OF_WEEK, schedule.daysOfWeek.minOrNull() ?: Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, schedule.startHour)
            set(Calendar.MINUTE, schedule.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return nextWeekCalendar.timeInMillis
    }
}
