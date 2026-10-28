package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkAlarmDao {
  @Query("SELECT * FROM work_alarms ORDER BY hour ASC, minute ASC")
  fun observeAllAlarms(): Flow<List<WorkAlarm>>

  @Query("SELECT COUNT(*) FROM work_alarms")
  suspend fun getCount(): Int

  @Query("SELECT * FROM work_alarms WHERE id = :alarmId LIMIT 1")
  suspend fun getAlarmById(alarmId: String): WorkAlarm?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlarm(alarm: WorkAlarm)

  @Update
  suspend fun updateAlarm(alarm: WorkAlarm)

  @Query("DELETE FROM work_alarms WHERE id = :alarmId")
  suspend fun deleteAlarmById(alarmId: String)
}

@Database(entities = [WorkAlarm::class], version = 1, exportSchema = false)
@TypeConverters(DaysOfWeekConverters::class)
abstract class ChronoPulseDatabase : RoomDatabase() {
  abstract fun workAlarmDao(): WorkAlarmDao

  companion object {
    @Volatile
    private var INSTANCE: ChronoPulseDatabase? = null

    fun getDatabase(context: Context): ChronoPulseDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ChronoPulseDatabase::class.java,
          "chronopulse_alarms.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
