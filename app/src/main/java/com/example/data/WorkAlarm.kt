package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class AlarmCategory(
  val key: String,
  val displayName: String,
  val subtitle: String,
  val defaultDurationMin: Long,
  val defaultBioTemp: String,
  val isBioscience: Boolean
) {
  DEEP_WORK(
    key = "DEEP_WORK",
    displayName = "Deep Work Block",
    subtitle = "Uninterrupted cognitive sprint",
    defaultDurationMin = 50L,
    defaultBioTemp = "",
    isBioscience = false
  ),
  MEETING(
    key = "MEETING",
    displayName = "Sync & Review",
    subtitle = "Standup, milestone, or deliverable check",
    defaultDurationMin = 30L,
    defaultBioTemp = "",
    isBioscience = false
  ),
  BIO_INCUBATION(
    key = "BIO_INCUBATION",
    displayName = "Bio-Incubation",
    subtitle = "Thermal or CO2 chamber incubation alert",
    defaultDurationMin = 60L,
    defaultBioTemp = "37.0°C / 5% CO2",
    isBioscience = true
  ),
  PCR_ASSAY(
    key = "PCR_ASSAY",
    displayName = "PCR & Assay Cycle",
    subtitle = "Thermocycler hold, gel run, or ELISA wash",
    defaultDurationMin = 45L,
    defaultBioTemp = "95°C → 60°C → 72°C",
    isBioscience = true
  ),
  CELL_CULTURE(
    key = "CELL_CULTURE",
    displayName = "Cell Culture & Media",
    subtitle = "Passage, confluence check, or reagent feed",
    defaultDurationMin = 30L,
    defaultBioTemp = "37.0°C Biosafety",
    isBioscience = true
  ),
  BREAK_CIRCADIAN(
    key = "BREAK_CIRCADIAN",
    displayName = "Circadian Reset",
    subtitle = "Ocular rest, hydration & cortisol balance",
    defaultDurationMin = 15L,
    defaultBioTemp = "10,000 Lux Daylight",
    isBioscience = true
  );

  companion object {
    fun fromKey(key: String): AlarmCategory =
      entries.firstOrNull { it.key == key } ?: DEEP_WORK
  }
}

class DaysOfWeekConverters {
  @TypeConverter
  fun fromDaysList(days: List<Long>): String =
    days.joinToString(",")

  @TypeConverter
  fun toDaysList(csv: String): List<Long> =
    if (csv.isBlank()) emptyList()
    else csv.split(",").mapNotNull { it.trim().toLongOrNull() }
}

@Entity(tableName = "work_alarms")
data class WorkAlarm(
  @PrimaryKey val id: String = "",
  val title: String = "",
  val notes: String = "",
  val hour: Long = 9L,
  val minute: Long = 0L,
  val enabled: Boolean = true,
  val category: String = AlarmCategory.DEEP_WORK.key,
  val daysOfWeek: List<Long> = listOf(1L, 2L, 3L, 4L, 5L),
  val bioTempCelsius: String = "",
  val protocolDurationMin: Long = 45L,
  val vibrate: Boolean = true,
  val snoozeMinutes: Long = 10L,
  val completedCount: Long = 0L,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val updatedAtMillis: Long = System.currentTimeMillis()
) {
  val categoryEnum: AlarmCategory
    get() = AlarmCategory.fromKey(category)

  fun formattedTime24(): String = String.format("%02d:%02d", hour, minute)

  fun formattedTime12(): Pair<String, String> {
    val h = hour.toInt()
    val m = minute.toInt()
    val period = if (h < 12) "AM" else "PM"
    val h12 = when {
      h == 0 -> 12
      h > 12 -> h - 12
      else -> h
    }
    return String.format("%02d:%02d", h12, m) to period
  }
}
