package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.WorkAlarm
import java.util.Calendar

class WorkAlarmScheduler(private val context: Context) {

  private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

  fun syncAllAlarms(alarms: List<WorkAlarm>) {
    alarms.forEach { alarm ->
      if (alarm.enabled) {
        scheduleAlarm(alarm)
      } else {
        cancelAlarm(alarm.id)
      }
    }
  }

  fun scheduleAlarm(alarm: WorkAlarm) {
    val manager = alarmManager ?: return
    if (alarm.id.isBlank()) return

    val triggerAtMillis = computeNextTriggerMillis(alarm.hour.toInt(), alarm.minute.toInt(), alarm.daysOfWeek.map { it.toInt() })
    val intent = Intent(context, WorkAlarmReceiver::class.java).apply {
      putExtra(WorkAlarmReceiver.EXTRA_ALARM_ID, alarm.id)
      putExtra(WorkAlarmReceiver.EXTRA_TITLE, alarm.title)
      putExtra(WorkAlarmReceiver.EXTRA_NOTES, alarm.notes)
      putExtra(WorkAlarmReceiver.EXTRA_CATEGORY, alarm.category)
      putExtra(WorkAlarmReceiver.EXTRA_BIO_TEMP, alarm.bioTempCelsius)
      putExtra(WorkAlarmReceiver.EXTRA_VIBRATE, alarm.vibrate)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      alarm.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        manager.canScheduleExactAlarms()
      } else {
        true
      }
      if (canScheduleExact) {
        manager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      } else {
        manager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      }
    } catch (_: SecurityException) {
      try {
        manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      } catch (_: Exception) {
      }
    }
  }

  fun triggerImmediateTestNotification(alarm: WorkAlarm) {
    val intent = Intent(context, WorkAlarmReceiver::class.java).apply {
      putExtra(WorkAlarmReceiver.EXTRA_ALARM_ID, alarm.id.ifBlank { "test_alarm" })
      putExtra(WorkAlarmReceiver.EXTRA_TITLE, alarm.title)
      putExtra(WorkAlarmReceiver.EXTRA_NOTES, alarm.notes)
      putExtra(WorkAlarmReceiver.EXTRA_CATEGORY, alarm.category)
      putExtra(WorkAlarmReceiver.EXTRA_BIO_TEMP, alarm.bioTempCelsius)
      putExtra(WorkAlarmReceiver.EXTRA_VIBRATE, alarm.vibrate)
    }
    WorkAlarmReceiver().onReceive(context, intent)
  }

  fun cancelAlarm(alarmId: String) {
    val manager = alarmManager ?: return
    if (alarmId.isBlank()) return
    val intent = Intent(context, WorkAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      alarmId.hashCode(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      manager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  companion object {
    fun computeNextTriggerMillis(
      hour: Int,
      minute: Int,
      daysOfWeek: List<Int>,
      nowMillis: Long = System.currentTimeMillis()
    ): Long {
      val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
      val candidate = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        set(Calendar.MINUTE, minute.coerceIn(0, 59))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

      if (daysOfWeek.isEmpty()) {
        if (candidate.timeInMillis <= now.timeInMillis) {
          candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return candidate.timeInMillis
      }

      for (offset in 0..7) {
        val check = (candidate.clone() as Calendar).apply {
          add(Calendar.DAY_OF_YEAR, offset)
        }
        // Convert Calendar.DAY_OF_WEEK (Sun=1..Sat=7) to ISO (Mon=1..Sun=7)
        val calDay = check.get(Calendar.DAY_OF_WEEK)
        val isoDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
        if (isoDay in daysOfWeek && check.timeInMillis > now.timeInMillis) {
          return check.timeInMillis
        }
      }
      candidate.add(Calendar.DAY_OF_YEAR, 1)
      return candidate.timeInMillis
    }

    fun formatCountdownUntil(
      hour: Int,
      minute: Int,
      daysOfWeek: List<Int>,
      nowMillis: Long = System.currentTimeMillis()
    ): String {
      val nextMillis = computeNextTriggerMillis(hour, minute, daysOfWeek, nowMillis)
      val diffMinutes = ((nextMillis - nowMillis) / 60_000L).coerceAtLeast(1L)
      val days = diffMinutes / (60 * 24)
      val hours = (diffMinutes % (60 * 24)) / 60
      val mins = diffMinutes % 60
      return buildString {
        append("in ")
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        append("${mins}m")
      }
    }
  }
}
