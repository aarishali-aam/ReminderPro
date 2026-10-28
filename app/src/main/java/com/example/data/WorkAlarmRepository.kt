package com.example.data

import java.util.UUID
import kotlinx.coroutines.flow.Flow

class WorkAlarmRepository(private val alarmDao: WorkAlarmDao) {

  fun observeAlarms(): Flow<List<WorkAlarm>> = alarmDao.observeAllAlarms()

  suspend fun getCount(): Int = alarmDao.getCount()

  suspend fun createAlarm(
    title: String,
    notes: String = "",
    hour: Int = 9,
    minute: Int = 0,
    enabled: Boolean = true,
    category: AlarmCategory = AlarmCategory.DEEP_WORK,
    daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5),
    bioTempCelsius: String = category.defaultBioTemp,
    protocolDurationMin: Int = category.defaultDurationMin.toInt(),
    vibrate: Boolean = true,
    snoozeMinutes: Int = 10
  ): Result<String> {
    return try {
      val alarmId = "alarm_${UUID.randomUUID().toString().replace("-", "").take(20)}"
      val now = System.currentTimeMillis()
      val alarm = WorkAlarm(
        id = alarmId,
        title = title.trim().take(120),
        notes = notes.trim().take(500),
        hour = hour.coerceIn(0, 23).toLong(),
        minute = minute.coerceIn(0, 59).toLong(),
        enabled = enabled,
        category = category.key,
        daysOfWeek = daysOfWeek.take(7).map { it.toLong() },
        bioTempCelsius = bioTempCelsius.trim().take(32),
        protocolDurationMin = protocolDurationMin.coerceIn(0, 1440).toLong(),
        vibrate = vibrate,
        snoozeMinutes = snoozeMinutes.coerceIn(1, 60).toLong(),
        completedCount = 0L,
        createdAtMillis = now,
        updatedAtMillis = now
      )
      alarmDao.insertAlarm(alarm)
      Result.success(alarmId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateAlarm(alarm: WorkAlarm): Result<Unit> {
    return try {
      alarmDao.updateAlarm(
        alarm.copy(
          title = alarm.title.trim().take(120),
          notes = alarm.notes.trim().take(500),
          bioTempCelsius = alarm.bioTempCelsius.trim().take(32),
          updatedAtMillis = System.currentTimeMillis()
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun toggleAlarm(alarm: WorkAlarm, enabled: Boolean): Result<Unit> {
    return updateAlarm(alarm.copy(enabled = enabled))
  }

  suspend fun markCompleted(alarm: WorkAlarm): Result<Unit> {
    return updateAlarm(alarm.copy(completedCount = alarm.completedCount + 1L))
  }

  suspend fun deleteAlarm(alarmId: String): Result<Unit> {
    return try {
      alarmDao.deleteAlarmById(alarmId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
