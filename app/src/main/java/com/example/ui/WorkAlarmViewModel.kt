package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AlarmCategory
import com.example.data.ChronoPulseDatabase
import com.example.data.WorkAlarm
import com.example.data.WorkAlarmRepository
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class NotificationLogItem(
  val id: String = UUID.randomUUID().toString(),
  val alarmId: String,
  val title: String,
  val message: String,
  val category: AlarmCategory,
  val bioCondition: String = "",
  val timestampMillis: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

data class ActiveProtocolTimerState(
  val title: String = "Cell Culture 37°C Incubation",
  val category: AlarmCategory = AlarmCategory.BIO_INCUBATION,
  val bioCondition: String = "37.0°C / 5% CO2",
  val totalSeconds: Long = 45 * 60L,
  val remainingSeconds: Long = 45 * 60L,
  val isRunning: Boolean = false,
  val linkedAlarmId: String? = null
) {
  val progressFraction: Float
    get() = if (totalSeconds > 0) {
      ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

  fun formattedRemaining(): String {
    val hrs = remainingSeconds / 3600
    val mins = (remainingSeconds % 3600) / 60
    val secs = remainingSeconds % 60
    return if (hrs > 0) {
      String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
      String.format("%02d:%02d", mins, secs)
    }
  }
}

class WorkAlarmViewModel(
  private val repository: WorkAlarmRepository
) : ViewModel() {

  val alarmsState: StateFlow<UiState<List<WorkAlarm>>> = repository.observeAlarms()
    .map<List<WorkAlarm>, UiState<List<WorkAlarm>>> { UiState.Success(it) }
    .catch { error ->
      Log.w(TAG, "Error observing work alarms", error)
      emit(UiState.Error(error.message ?: "Failed to load work alarms"))
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = UiState.Loading
    )

  private val _selectedCategoryFilter = MutableStateFlow<AlarmCategory?>(null)
  val selectedCategoryFilter: StateFlow<AlarmCategory?> = _selectedCategoryFilter.asStateFlow()

  private val _statusBannerMessage = MutableStateFlow<String?>(null)
  val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

  private val _protocolTimer = MutableStateFlow(ActiveProtocolTimerState())
  val protocolTimer: StateFlow<ActiveProtocolTimerState> = _protocolTimer.asStateFlow()

  private val _notifications = MutableStateFlow<List<NotificationLogItem>>(
    listOf(
      NotificationLogItem(
        alarmId = "welcome_alert",
        title = "ChronoPulse Notifications Ready",
        message = "Tap any task's pop-up menu (⋮) or the bell icon to trigger live work & bioscience alerts.",
        category = AlarmCategory.DEEP_WORK,
        bioCondition = "System Active",
        isRead = false
      )
    )
  )
  val notifications: StateFlow<List<NotificationLogItem>> = _notifications.asStateFlow()

  private val _livePopupNotification = MutableStateFlow<NotificationLogItem?>(null)
  val livePopupNotification: StateFlow<NotificationLogItem?> = _livePopupNotification.asStateFlow()

  private var timerJob: Job? = null
  private var popupAutoDismissJob: Job? = null

  init {
    viewModelScope.launch {
      if (repository.getCount() == 0) {
        seedStarterScheduleInternal(showBanner = false)
      }
    }
  }

  fun selectCategoryFilter(category: AlarmCategory?) {
    _selectedCategoryFilter.value = category
  }

  fun clearBannerMessage() {
    _statusBannerMessage.value = null
  }

  fun showBannerMessage(message: String) {
    _statusBannerMessage.value = message
  }

  fun pushNotificationEvent(
    alarm: WorkAlarm,
    customMessage: String? = null,
    showLivePopup: Boolean = true
  ) {
    val msg = customMessage ?: buildString {
      if (alarm.bioTempCelsius.isNotBlank()) {
        append("[${alarm.bioTempCelsius}] ")
      }
      if (alarm.notes.isNotBlank()) {
        append(alarm.notes)
      } else {
        append("Scheduled for ${alarm.formattedTime24()} (${alarm.protocolDurationMin}m session)")
      }
    }
    val item = NotificationLogItem(
      alarmId = alarm.id,
      title = alarm.title,
      message = msg,
      category = alarm.categoryEnum,
      bioCondition = alarm.bioTempCelsius,
      timestampMillis = System.currentTimeMillis(),
      isRead = false
    )
    _notifications.update { current ->
      listOf(item) + current.take(29)
    }
    if (showLivePopup) {
      showLivePopupBanner(item)
    }
  }

  fun showLivePopupBanner(item: NotificationLogItem) {
    popupAutoDismissJob?.cancel()
    _livePopupNotification.value = item
    popupAutoDismissJob = viewModelScope.launch {
      delay(6500L)
      if (_livePopupNotification.value?.id == item.id) {
        _livePopupNotification.value = null
      }
    }
  }

  fun dismissLivePopup() {
    popupAutoDismissJob?.cancel()
    _livePopupNotification.value = null
  }

  fun markNotificationRead(notificationId: String) {
    _notifications.update { list ->
      list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
    }
  }

  fun markAllNotificationsRead() {
    _notifications.update { list ->
      list.map { it.copy(isRead = true) }
    }
  }

  fun clearAllNotifications() {
    _notifications.value = emptyList()
    _livePopupNotification.value = null
  }

  fun createWorkAlarm(
    title: String,
    notes: String,
    hour: Int,
    minute: Int,
    category: AlarmCategory,
    daysOfWeek: List<Int>,
    bioTempCelsius: String,
    protocolDurationMin: Int,
    vibrate: Boolean,
    snoozeMinutes: Int
  ) {
    viewModelScope.launch {
      val result = repository.createAlarm(
        title = title,
        notes = notes,
        hour = hour,
        minute = minute,
        enabled = true,
        category = category,
        daysOfWeek = daysOfWeek,
        bioTempCelsius = bioTempCelsius,
        protocolDurationMin = protocolDurationMin,
        vibrate = vibrate,
        snoozeMinutes = snoozeMinutes
      )
      result.onSuccess { alarmId ->
        val created = WorkAlarm(
          id = alarmId,
          title = title.trim(),
          notes = notes.trim(),
          hour = hour.toLong(),
          minute = minute.toLong(),
          enabled = true,
          category = category.name,
          daysOfWeek = daysOfWeek.map { it.toLong() },
          bioTempCelsius = bioTempCelsius,
          protocolDurationMin = protocolDurationMin.toLong(),
          vibrate = vibrate,
          snoozeMinutes = snoozeMinutes.toLong()
        )
        pushNotificationEvent(
          alarm = created,
          customMessage = "Alarm armed for ${created.formattedTime24()} • ${category.displayName}",
          showLivePopup = true
        )
      }.onFailure { err ->
        _statusBannerMessage.value = err.localizedMessage ?: "Failed to create alarm"
      }
    }
  }

  fun updateExistingAlarm(alarm: WorkAlarm) {
    viewModelScope.launch {
      repository.updateAlarm(alarm).onSuccess {
        _statusBannerMessage.value = "Updated \"${alarm.title}\""
      }.onFailure { err ->
        _statusBannerMessage.value = err.localizedMessage ?: "Failed to update alarm"
      }
    }
  }

  fun toggleAlarmEnabled(alarm: WorkAlarm, enabled: Boolean) {
    viewModelScope.launch {
      repository.toggleAlarm(alarm, enabled).onSuccess {
        _statusBannerMessage.value = if (enabled) {
          "Armed \"${alarm.title}\" (${alarm.formattedTime24()})"
        } else {
          "Paused \"${alarm.title}\""
        }
      }.onFailure { err ->
        _statusBannerMessage.value = err.localizedMessage ?: "Could not toggle alarm"
      }
    }
  }

  fun snoozeAlarmByMinutes(alarm: WorkAlarm, minutesToAdd: Int) {
    viewModelScope.launch {
      val cal = Calendar.getInstance().apply {
        add(Calendar.MINUTE, minutesToAdd)
      }
      val newHour = cal.get(Calendar.HOUR_OF_DAY)
      val newMinute = cal.get(Calendar.MINUTE)
      val updated = alarm.copy(
        hour = newHour.toLong(),
        minute = newMinute.toLong(),
        enabled = true
      )
      repository.updateAlarm(updated).onSuccess {
        pushNotificationEvent(
          alarm = updated,
          customMessage = "Snoozed +${minutesToAdd}m → Next ring at ${updated.formattedTime24()}",
          showLivePopup = true
        )
      }
    }
  }

  fun duplicateAlarm(alarm: WorkAlarm) {
    viewModelScope.launch {
      val duplicateTitle = "${alarm.title} (Copy)"
      repository.createAlarm(
        title = duplicateTitle,
        notes = alarm.notes,
        hour = alarm.hour.toInt(),
        minute = alarm.minute.toInt(),
        enabled = true,
        category = alarm.categoryEnum,
        daysOfWeek = alarm.daysOfWeek.map { it.toInt() },
        bioTempCelsius = alarm.bioTempCelsius,
        protocolDurationMin = alarm.protocolDurationMin.toInt(),
        vibrate = alarm.vibrate,
        snoozeMinutes = alarm.snoozeMinutes.toInt()
      ).onSuccess {
        _statusBannerMessage.value = "Duplicated \"${alarm.title}\""
      }
    }
  }

  fun setAllAlarmsEnabled(enabled: Boolean) {
    viewModelScope.launch {
      val currentList = repository.observeAlarms().firstOrNull().orEmpty()
      currentList.forEach { alarm ->
        if (alarm.enabled != enabled) {
          repository.toggleAlarm(alarm, enabled)
        }
      }
      _statusBannerMessage.value = if (enabled) {
        "All ${currentList.size} reminders armed"
      } else {
        "All ${currentList.size} reminders paused"
      }
    }
  }

  fun markSessionCompleted(alarm: WorkAlarm) {
    viewModelScope.launch {
      repository.markCompleted(alarm).onSuccess {
        _statusBannerMessage.value = "Logged completed session for \"${alarm.title}\""
      }
    }
  }

  fun deleteAlarm(alarm: WorkAlarm) {
    viewModelScope.launch {
      repository.deleteAlarm(alarm.id).onSuccess {
        _statusBannerMessage.value = "Deleted \"${alarm.title}\""
      }.onFailure { err ->
        _statusBannerMessage.value = err.localizedMessage ?: "Could not delete alarm"
      }
    }
  }

  fun seedStarterSchedule() {
    viewModelScope.launch {
      seedStarterScheduleInternal(showBanner = true)
    }
  }

  private suspend fun seedStarterScheduleInternal(showBanner: Boolean) {
    repository.createAlarm(
      title = "Morning Deep Work Architecture Sprint",
      notes = "Zero-notification code & experimental design block",
      hour = 8,
      minute = 30,
      category = AlarmCategory.DEEP_WORK,
      daysOfWeek = listOf(1, 2, 3, 4, 5),
      bioTempCelsius = "",
      protocolDurationMin = 60
    )
    repository.createAlarm(
      title = "HEK293 Cell Culture Media Exchange",
      notes = "Pre-warm DMEM + 10% FBS in 37.0°C bath, check flask confluence",
      hour = 11,
      minute = 0,
      category = AlarmCategory.CELL_CULTURE,
      daysOfWeek = listOf(1, 3, 5),
      bioTempCelsius = "37.0°C / 5% CO2",
      protocolDurationMin = 35
    )
    repository.createAlarm(
      title = "qPCR Thermal Cycle & Gel Verification",
      notes = "Check amplification curves and prepare 1.5% agarose TAE buffer",
      hour = 14,
      minute = 15,
      category = AlarmCategory.PCR_ASSAY,
      daysOfWeek = listOf(2, 4),
      bioTempCelsius = "95°C → 60°C → 72°C",
      protocolDurationMin = 45
    )
    repository.createAlarm(
      title = "Circadian Ocular & Hydration Reset",
      notes = "Step away from screens, 250mL water + far-horizon visual rest",
      hour = 16,
      minute = 30,
      category = AlarmCategory.BREAK_CIRCADIAN,
      daysOfWeek = listOf(1, 2, 3, 4, 5),
      bioTempCelsius = "10,000 Lux Daylight",
      protocolDurationMin = 15
    )
    if (showBanner) {
      _statusBannerMessage.value = "Loaded 4 starter Work & Bioscience alarms"
    }
  }

  fun loadAlarmIntoProtocolTimer(alarm: WorkAlarm) {
    timerJob?.cancel()
    val durationSec = (alarm.protocolDurationMin.coerceAtLeast(1L)) * 60L
    _protocolTimer.value = ActiveProtocolTimerState(
      title = alarm.title,
      category = alarm.categoryEnum,
      bioCondition = alarm.bioTempCelsius.ifBlank { alarm.categoryEnum.defaultBioTemp.ifBlank { "Focus Protocol" } },
      totalSeconds = durationSec,
      remainingSeconds = durationSec,
      isRunning = false,
      linkedAlarmId = alarm.id
    )
    startOrResumeProtocolTimer()
  }

  fun configureCustomProtocolTimer(
    title: String,
    category: AlarmCategory,
    bioCondition: String,
    durationMinutes: Int
  ) {
    timerJob?.cancel()
    val durationSec = durationMinutes.coerceIn(1, 1440) * 60L
    _protocolTimer.value = ActiveProtocolTimerState(
      title = title,
      category = category,
      bioCondition = bioCondition,
      totalSeconds = durationSec,
      remainingSeconds = durationSec,
      isRunning = false,
      linkedAlarmId = null
    )
  }

  fun startOrResumeProtocolTimer() {
    if (_protocolTimer.value.isRunning) return
    _protocolTimer.update { it.copy(isRunning = true) }
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_protocolTimer.value.isRunning && _protocolTimer.value.remainingSeconds > 0) {
        delay(1000L)
        _protocolTimer.update { state ->
          val next = (state.remainingSeconds - 1L).coerceAtLeast(0L)
          state.copy(remainingSeconds = next, isRunning = next > 0L)
        }
      }
      if (_protocolTimer.value.remainingSeconds == 0L) {
        val finished = _protocolTimer.value
        val item = NotificationLogItem(
          alarmId = finished.linkedAlarmId ?: "protocol_done",
          title = "Protocol Complete: ${finished.title}",
          message = "Completed ${finished.totalSeconds / 60}m session [${finished.bioCondition}]",
          category = finished.category,
          bioCondition = finished.bioCondition
        )
        _notifications.update { listOf(item) + it.take(29) }
        showLivePopupBanner(item)
      }
    }
  }

  fun pauseProtocolTimer() {
    timerJob?.cancel()
    _protocolTimer.update { it.copy(isRunning = false) }
  }

  fun resetProtocolTimer() {
    timerJob?.cancel()
    _protocolTimer.update {
      it.copy(remainingSeconds = it.totalSeconds, isRunning = false)
    }
  }

  companion object {
    private const val TAG = "WorkAlarmVM"
    private const val STOP_TIMEOUT_MILLIS = 5000L

    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val database = ChronoPulseDatabase.getDatabase(app)
        WorkAlarmViewModel(WorkAlarmRepository(database.workAlarmDao()))
      }
    }
  }
}
