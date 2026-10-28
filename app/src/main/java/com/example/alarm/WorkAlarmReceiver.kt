package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class WorkAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val title = intent.getStringExtra(EXTRA_TITLE) ?: "Work Reminder"
    val notes = intent.getStringExtra(EXTRA_NOTES).orEmpty()
    val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "DEEP_WORK"
    val bioTemp = intent.getStringExtra(EXTRA_BIO_TEMP).orEmpty()
    val vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)
    val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: "default"

    if (vibrate) {
      triggerVibration(context)
    }

    showAlarmNotification(context, alarmId, title, notes, category, bioTemp)
  }

  private fun triggerVibration(context: Context) {
    try {
      val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(
          VibrationEffect.createWaveform(longArrayOf(0L, 400L, 200L, 400L, 200L, 600L), -1)
        )
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(800L)
      }
    } catch (_: Exception) {
    }
  }

  private fun showAlarmNotification(
    context: Context,
    alarmId: String,
    title: String,
    notes: String,
    category: String,
    bioTemp: String
  ) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "ChronoPulse Work & Lab Alarms",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "High-priority alerts for scheduled work blocks and bioscience protocols"
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(channel)
    }

    val openIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_ALARM_ID, alarmId)
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      alarmId.hashCode(),
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val subtitleBuilder = StringBuilder()
    if (bioTemp.isNotBlank()) {
      subtitleBuilder.append("[").append(bioTemp).append("] ")
    }
    if (notes.isNotBlank()) {
      subtitleBuilder.append(notes)
    } else {
      subtitleBuilder.append("Scheduled ").append(category.replace('_', ' ').lowercase()).append(" reminder")
    }

    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(subtitleBuilder.toString())
      .setStyle(NotificationCompat.BigTextStyle().bigText(subtitleBuilder.toString()))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setSound(alarmSound)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      notificationManager.notify(alarmId.hashCode(), notification)
    } catch (_: SecurityException) {
    }
  }

  companion object {
    const val CHANNEL_ID = "chronopulse_alarm_channel"
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_NOTES = "extra_notes"
    const val EXTRA_CATEGORY = "extra_category"
    const val EXTRA_BIO_TEMP = "extra_bio_temp"
    const val EXTRA_VIBRATE = "extra_vibrate"
  }
}
