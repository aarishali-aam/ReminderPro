package com.example.ui

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.alarm.WorkAlarmScheduler
import com.example.data.AlarmCategory
import com.example.data.WorkAlarm
import com.example.ui.theme.DomineFontFamily
import com.example.ui.theme.Gray100
import com.example.ui.theme.Gray200
import com.example.ui.theme.Gray300
import com.example.ui.theme.Gray400
import com.example.ui.theme.Gray50
import com.example.ui.theme.Gray500
import com.example.ui.theme.Gray600
import com.example.ui.theme.Gray700
import com.example.ui.theme.Gray800
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TaskViewScope(val label: String) {
  ALL("All Tasks"),
  ACTIVE("Active Only"),
  WORK("Work Focus"),
  BIOSCIENCE("Bio-Lab")
}

private data class QuickPromptTemplate(
  val label: String,
  val title: String,
  val promptNotes: String,
  val category: AlarmCategory,
  val hour: Int,
  val minute: Int,
  val durationMin: Int
)

private val quickPromptTemplates = listOf(
  QuickPromptTemplate(
    label = "Deep Work Sprint",
    title = "Deep Work Architecture Sprint",
    promptNotes = "Block notifications and focus on core engineering deliverables & system architecture for 60 minutes",
    category = AlarmCategory.DEEP_WORK,
    hour = 9,
    minute = 0,
    durationMin = 60
  ),
  QuickPromptTemplate(
    label = "Cell Culture 37°C",
    title = "HEK293 Cell Culture Media Exchange",
    promptNotes = "Warm DMEM + 10% FBS to 37.0°C, inspect flask confluence under phase-contrast microscope, and passage",
    category = AlarmCategory.CELL_CULTURE,
    hour = 11,
    minute = 0,
    durationMin = 35
  ),
  QuickPromptTemplate(
    label = "PCR Thermal Cycle",
    title = "qPCR Thermal Cycle & Gel Check",
    promptNotes = "Verify 95°C denaturation / 60°C annealing curve and cast 1.5% agarose gel in 1X TAE buffer",
    category = AlarmCategory.PCR_ASSAY,
    hour = 14,
    minute = 15,
    durationMin = 45
  ),
  QuickPromptTemplate(
    label = "Standup & Sync",
    title = "Daily Team Standup & Milestone Review",
    promptNotes = "Share yesterday's completed tasks, today's execution plan, and surface any lab or sprint blockers",
    category = AlarmCategory.MEETING,
    hour = 10,
    minute = 0,
    durationMin = 25
  ),
  QuickPromptTemplate(
    label = "Bio-Incubation Alert",
    title = "37.0°C / 5% CO2 Chamber Check",
    promptNotes = "Inspect incubator humidity pan, CO2 sensor readout, and sample shaker RPM stability",
    category = AlarmCategory.BIO_INCUBATION,
    hour = 15,
    minute = 30,
    durationMin = 60
  ),
  QuickPromptTemplate(
    label = "Circadian Eye Reset",
    title = "Circadian Ocular & Hydration Reset",
    promptNotes = "Step away from screens for 15 minutes, drink 250mL water, and get natural daylight exposure",
    category = AlarmCategory.BREAK_CIRCADIAN,
    hour = 16,
    minute = 30,
    durationMin = 15
  )
)

@Composable
fun ScheduledTasksScreen(
  alarmsState: UiState<List<WorkAlarm>>,
  selectedCategory: AlarmCategory?,
  hasNotificationPermission: Boolean,
  onRequestNotificationPermission: () -> Unit,
  onSelectCategory: (AlarmCategory?) -> Unit,
  onToggleAlarm: (WorkAlarm, Boolean) -> Unit,
  onEditAlarm: (WorkAlarm) -> Unit,
  onDeleteAlarm: (WorkAlarm) -> Unit,
  onMarkCompleted: (WorkAlarm) -> Unit,
  onTestRing: (WorkAlarm) -> Unit,
  onSnoozeAlarm: (WorkAlarm, Int) -> Unit,
  onDuplicateAlarm: (WorkAlarm) -> Unit,
  onStartProtocolTimer: (WorkAlarm) -> Unit,
  onCreateFromPrompt: (
    title: String,
    notes: String,
    hour: Int,
    minute: Int,
    category: AlarmCategory,
    durationMin: Int
  ) -> Unit,
  onOpenNewReminderSheet: () -> Unit,
  onSeedStarterSchedule: () -> Unit
) {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedScope by rememberSaveable { mutableStateOf(TaskViewScope.ALL) }

  LaunchedEffect(Unit) {
    while (true) {
      currentTimeMillis = System.currentTimeMillis()
      delay(10_000L)
    }
  }

  val allAlarms = (alarmsState as? UiState.Success)?.data.orEmpty()

  val filteredAlarms = remember(allAlarms, searchQuery, selectedScope, selectedCategory) {
    allAlarms.filter { alarm ->
      val matchesSearch = searchQuery.isBlank() ||
        alarm.title.contains(searchQuery, ignoreCase = true) ||
        alarm.notes.contains(searchQuery, ignoreCase = true) ||
        alarm.bioTempCelsius.contains(searchQuery, ignoreCase = true)

      val matchesScope = when (selectedScope) {
        TaskViewScope.ALL -> true
        TaskViewScope.ACTIVE -> alarm.enabled
        TaskViewScope.WORK -> !alarm.categoryEnum.isBioscience
        TaskViewScope.BIOSCIENCE -> alarm.categoryEnum.isBioscience
      }

      val matchesCategory = selectedCategory == null || alarm.categoryEnum == selectedCategory
      matchesSearch && matchesScope && matchesCategory
    }
  }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("scheduled_tasks_list"),
    contentPadding = PaddingValues(bottom = 112.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Prompt2App Hero Section (Typewriter Prompt Composer + Marquee Pills)
    item {
      Prompt2AppHeroSection(
        activeCount = allAlarms.count { it.enabled },
        totalCount = allAlarms.size,
        onCreateFromPrompt = onCreateFromPrompt,
        onOpenFullTimePicker = onOpenNewReminderSheet
      )
    }

    // 2. Notification Permission Banner (Android 13+)
    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
          NotificationPermissionBanner(onRequestPermission = onRequestNotificationPermission)
        }
      }
    }

    // 3. SCHEDULED TASKS Section Header (Domine Serif + Search & Scope Filter Pills)
    item {
      ScheduledTasksHeaderSection(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedScope = selectedScope,
        onSelectScope = { selectedScope = it },
        selectedCategory = selectedCategory,
        onSelectCategory = onSelectCategory,
        totalMatchCount = filteredAlarms.size,
        onOpenNewReminderSheet = onOpenNewReminderSheet
      )
    }

    // 4. Scheduled Tasks Cards (Featured "Most Popular" Dark Charcoal Card + Clean Bordered White Cards)
    when (alarmsState) {
      is UiState.Loading -> {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = Gray800)
          }
        }
      }

      is UiState.Error -> {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = Gray100),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .widthIn(max = 760.dp)
              .padding(horizontal = 20.dp)
          ) {
            Text(
              text = alarmsState.message,
              color = Gray800,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(20.dp)
            )
          }
        }
      }

      is UiState.Success -> {
        if (filteredAlarms.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
              EmptyScheduledTasksState(
                hasAnyAlarmsInDb = allAlarms.isNotEmpty(),
                onClearFilters = {
                  searchQuery = ""
                  selectedScope = TaskViewScope.ALL
                  onSelectCategory(null)
                },
                onOpenNewReminderSheet = onOpenNewReminderSheet,
                onSeedSchedule = onSeedStarterSchedule
              )
            }
          }
        } else {
          val nextActiveId = filteredAlarms
            .filter { it.enabled }
            .minByOrNull {
              WorkAlarmScheduler.computeNextTriggerMillis(
                it.hour.toInt(),
                it.minute.toInt(),
                it.daysOfWeek.map { d -> d.toInt() },
                currentTimeMillis
              )
            }?.id

          itemsIndexed(filteredAlarms, key = { _, item -> item.id }) { _, alarm ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
              Prompt2AppTaskCard(
                alarm = alarm,
                isFeaturedNext = alarm.id == nextActiveId,
                nowMillis = currentTimeMillis,
                onToggle = { enabled -> onToggleAlarm(alarm, enabled) },
                onEdit = { onEditAlarm(alarm) },
                onDelete = { onDeleteAlarm(alarm) },
                onMarkCompleted = { onMarkCompleted(alarm) },
                onTestRing = { onTestRing(alarm) },
                onSnooze10m = { onSnoozeAlarm(alarm, 10) },
                onSnooze30m = { onSnoozeAlarm(alarm, 30) },
                onDuplicate = { onDuplicateAlarm(alarm) },
                onStartProtocol = { onStartProtocolTimer(alarm) }
              )
            }
          }
        }
      }
    }

    // 5. Divided 3-Column / 3-Row Features Grid (matching features-section.tsx)
    item {
      Prompt2AppFeaturesGridSection()
    }

    // 6. Simple 4-Step Process Vertical Timeline (matching build-process.tsx with Domine underline titles)
    item {
      Prompt2AppBuildProcessSection()
    }

    // 7. Call To Action + Hand Asset + Footer (matching call-to-action.tsx & footer.tsx)
    item {
      Prompt2AppCallToActionAndFooter(
        onScrollToTopAndCreate = {
          coroutineScope.launch {
            listState.animateScrollToItem(0)
          }
          onOpenNewReminderSheet()
        }
      )
    }
  }
}

@Composable
private fun Prompt2AppHeroSection(
  activeCount: Int,
  totalCount: Int,
  onCreateFromPrompt: (
    title: String,
    notes: String,
    hour: Int,
    minute: Int,
    category: AlarmCategory,
    durationMin: Int
  ) -> Unit,
  onOpenFullTimePicker: () -> Unit
) {
  var promptText by rememberSaveable { mutableStateOf("") }
  var selectedTemplateLabel by rememberSaveable { mutableStateOf<String?>(null) }
  var selectedCategory by rememberSaveable { mutableStateOf(AlarmCategory.DEEP_WORK) }
  var selectedHour by rememberSaveable { mutableIntStateOf(9) }
  var selectedMinute by rememberSaveable { mutableIntStateOf(0) }
  var selectedDuration by rememberSaveable { mutableIntStateOf(45) }

  // Typewriter animated placeholder matching hero-section.tsx
  val placeholders = remember {
    listOf(
      "09:00 AM Deep Work Sprint...",
      "37°C Cell Culture Media Feed...",
      "14:15 PCR Thermal Cycle Check...",
      "10:00 AM Daily Standup Sync...",
      "16:30 Circadian Eye Reset..."
    )
  }
  var textIndex by remember { mutableIntStateOf(0) }
  var charIndex by remember { mutableIntStateOf(0) }
  var deleting by remember { mutableStateOf(false) }

  LaunchedEffect(textIndex, charIndex, deleting, promptText) {
    if (promptText.isNotEmpty()) return@LaunchedEffect
    val currentWord = placeholders[textIndex]
    if (!deleting && charIndex == currentWord.length) {
      delay(1800L)
      deleting = true
      return@LaunchedEffect
    }
    if (deleting && charIndex == 0) {
      deleting = false
      textIndex = (textIndex + 1) % placeholders.size
      return@LaunchedEffect
    }
    delay(45L)
    charIndex += if (deleting) -1 else 1
  }

  val animatedPlaceholder = placeholders[textIndex].take(charIndex.coerceAtLeast(0))

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 720.dp)
      .padding(horizontal = 20.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Trend Line: <TrendingUpIcon /> <span>Trusted by 2,000+ founders • X active alarms</span>
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
        contentDescription = null,
        tint = Gray500,
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = "Precision Work & Lab Rhythm • $activeCount of $totalCount Alarms Active",
        style = MaterialTheme.typography.bodyMedium,
        color = Gray500
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Hero H1: Build custom apps with AI -> Schedule work & lab reminders
    Text(
      text = "Remind your work with precision",
      style = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.8).sp
      ),
      color = Gray800,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "“No clutter. No missed steps. Just describe your work task or bioscience protocol and launch your alarm instantly.”",
      style = MaterialTheme.typography.bodyLarge,
      color = Gray500,
      textAlign = TextAlign.Center,
      modifier = Modifier.widthIn(max = 460.dp)
    )

    Spacer(modifier = Modifier.height(26.dp))

    // Prompt Box Form: border border-gray-200 rounded-xl max-w-2xl w-full
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, Gray200),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("hero_prompt_box")
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .padding(16.dp)
        ) {
          if (promptText.isEmpty()) {
            Text(
              text = "Schedule a $animatedPlaceholder",
              style = MaterialTheme.typography.bodyMedium,
              color = Gray400
            )
          }
          BasicTextField(
            value = promptText,
            onValueChange = { promptText = it },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray800),
            modifier = Modifier
              .fillMaxSize()
              .testTag("quick_prompt_input")
          )
        }

        // Bottom Toolbar inside Prompt Box
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left button: Time & Category Picker trigger (styled like the UploadCloudIcon box)
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              onClick = onOpenFullTimePicker,
              shape = RoundedCornerShape(8.dp),
              color = PureWhite,
              border = BorderStroke(1.dp, Gray200),
              modifier = Modifier.testTag("prompt_time_picker_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.AccessTime,
                  contentDescription = "Pick Alarm Time",
                  tint = Gray500,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = String.format(Locale.US, "%02d:%02d • %s", selectedHour, selectedMinute, selectedCategory.displayName),
                  style = MaterialTheme.typography.labelMedium,
                  color = Gray600
                )
              }
            }
          }

          // Right button: bg-linear-to-b from-gray-600 to-gray-800 px-4 h-9 text-white rounded-lg
          Box(
            modifier = Modifier
              .height(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
              .clickable {
                val raw = promptText.trim()
                val titleToSave = if (raw.isBlank()) {
                  selectedTemplateLabel ?: "Scheduled Work Reminder"
                } else {
                  raw.lineSequence().first().take(65)
                }
                val notesToSave = if (raw.length > 65) raw else "Scheduled via quick prompt composer"
                onCreateFromPrompt(
                  titleToSave,
                  notesToSave,
                  selectedHour,
                  selectedMinute,
                  selectedCategory,
                  selectedDuration
                )
                promptText = ""
                selectedTemplateLabel = null
              }
              .padding(horizontal = 16.dp)
              .testTag("prompt_create_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Create",
                style = MaterialTheme.typography.titleSmall,
                color = PureWhite
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Marquee / Horizontal Pill Selector matching hero-section.tsx
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      quickPromptTemplates.forEach { tpl ->
        val isSelected = selectedTemplateLabel == tpl.label
        Surface(
          onClick = {
            selectedTemplateLabel = tpl.label
            promptText = "${tpl.title} — ${tpl.promptNotes}"
            selectedCategory = tpl.category
            selectedHour = tpl.hour
            selectedMinute = tpl.minute
            selectedDuration = tpl.durationMin
          },
          shape = CircleShape,
          color = if (isSelected) Gray200 else Gray50,
          border = BorderStroke(1.dp, if (isSelected) Gray300 else Gray200),
          modifier = Modifier.testTag("template_pill_${tpl.label.lowercase().replace(' ', '_')}")
        ) {
          Text(
            text = tpl.label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Gray800 else Gray500,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ScheduledTasksHeaderSection(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedScope: TaskViewScope,
  onSelectScope: (TaskViewScope) -> Unit,
  selectedCategory: AlarmCategory?,
  onSelectCategory: (AlarmCategory?) -> Unit,
  totalMatchCount: Int,
  onOpenNewReminderSheet: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 760.dp)
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "YOUR SCHEDULED WORK",
          style = TextStyle(
            fontFamily = DomineFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 26.sp,
            lineHeight = 32.sp
          ),
          color = Gray800
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Manage daily work blocks, lab incubations, and recurring alarms ($totalMatchCount scheduled).",
          style = MaterialTheme.typography.bodyMedium,
          color = Gray500
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Secondary "Custom Time" button in Charcoal gradient
      Box(
        modifier = Modifier
          .height(38.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
          .clickable(onClick = onOpenNewReminderSheet)
          .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Add Alarm",
            style = MaterialTheme.typography.labelLarge,
            color = PureWhite
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Clean Search Box (bg-gray-50 border border-gray-200 rounded-lg)
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = { Text("Filter tasks by title, checklist notes, or lab temperature...", color = Gray400) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Filled.Search,
          contentDescription = null,
          tint = Gray500
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchQueryChange("") }) {
            Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = Gray500)
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(10.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = PureWhite,
        unfocusedContainerColor = Gray50,
        focusedBorderColor = Gray800,
        unfocusedBorderColor = Gray200
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("search_tasks_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Scope + Category Pills (rounded-full border border-gray-200)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TaskViewScope.entries.forEach { scope ->
        val selected = selectedScope == scope
        Surface(
          onClick = { onSelectScope(scope) },
          shape = CircleShape,
          color = if (selected) Gray800 else Gray50,
          border = BorderStroke(1.dp, if (selected) Gray800 else Gray200),
          modifier = Modifier.testTag("scope_${scope.name.lowercase()}")
        ) {
          Text(
            text = scope.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) PureWhite else Gray600,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
          )
        }
      }

      AlarmCategory.entries.forEach { cat ->
        val selected = selectedCategory == cat
        Surface(
          onClick = { onSelectCategory(if (selected) null else cat) },
          shape = CircleShape,
          color = if (selected) Gray200 else PureWhite,
          border = BorderStroke(1.dp, if (selected) Gray400 else Gray200),
          modifier = Modifier.testTag("filter_${cat.key.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = taskCategoryIcon(cat),
              contentDescription = null,
              tint = Gray700,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = cat.displayName,
              style = MaterialTheme.typography.labelLarge,
              color = Gray800
            )
          }
        }
      }
    }
  }
}

/**
 * Task Card styled directly after `pricing-section.tsx` in the uploaded ReactJS zip:
 * - Featured / Next Up alarm uses `bg-gray-800 text-white rounded-xl p-6`
 * - Standard alarms use `bg-white border border-slate-200 rounded-xl p-6`
 * - Feature checklist items use `flex items-center gap-2 border-b py-2.5` with circular `CheckIcon`
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Prompt2AppTaskCard(
  alarm: WorkAlarm,
  isFeaturedNext: Boolean,
  nowMillis: Long,
  onToggle: (Boolean) -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onMarkCompleted: () -> Unit,
  onTestRing: () -> Unit,
  onSnooze10m: () -> Unit,
  onSnooze30m: () -> Unit,
  onDuplicate: () -> Unit,
  onStartProtocol: () -> Unit
) {
  var showCardPopupMenu by remember { mutableStateOf(false) }
  val (time12, amPm) = alarm.formattedTime12()
  val cat = alarm.categoryEnum
  val darkCard = isFeaturedNext && alarm.enabled

  val containerColor = if (darkCard) Gray800 else PureWhite
  val borderColor = if (darkCard) Gray800 else Slate200
  val primaryTextColor = if (darkCard) PureWhite else Gray800
  val secondaryTextColor = if (darkCard) Gray400 else Gray500
  val dividerColor = if (darkCard) Gray700 else Gray200

  val countdownText = if (alarm.enabled) {
    WorkAlarmScheduler.formatCountdownUntil(
      alarm.hour.toInt(),
      alarm.minute.toInt(),
      alarm.daysOfWeek.map { it.toInt() },
      nowMillis
    )
  } else {
    "Paused"
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onEdit)
      .testTag("alarm_card_${alarm.id}")
  ) {
    Column(modifier = Modifier.padding(22.dp)) {
      // Top Row: Badge ("UP NEXT" or Category) + Enable/Disable Switch + Pop-up Menu (⋮)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (darkCard) {
            Surface(
              shape = CircleShape,
              color = PureWhite.copy(alpha = 0.14f)
            ) {
              Text(
                text = "UP NEXT REMINDER",
                style = MaterialTheme.typography.labelMedium,
                color = PureWhite,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (darkCard) PureWhite.copy(alpha = 0.1f) else Gray100
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = taskCategoryIcon(cat),
                contentDescription = null,
                tint = primaryTextColor,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = cat.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = primaryTextColor
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Switch(
            checked = alarm.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
              checkedThumbColor = if (darkCard) Gray800 else PureWhite,
              checkedTrackColor = if (darkCard) PureWhite else Gray800,
              uncheckedThumbColor = Gray500,
              uncheckedTrackColor = if (darkCard) Gray700 else Gray200
            ),
            modifier = Modifier.testTag("alarm_switch_${alarm.id}")
          )

          // Per-Task Pop-up Menu Button (`MoreVert` + `DropdownMenu`)
          Box {
            IconButton(
              onClick = { showCardPopupMenu = true },
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (darkCard) PureWhite.copy(alpha = 0.1f) else Gray50)
                .testTag("alarm_popup_menu_button_${alarm.id}")
            ) {
              Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Task Options Pop-up Menu",
                tint = primaryTextColor,
                modifier = Modifier.size(18.dp)
              )
            }

            DropdownMenu(
              expanded = showCardPopupMenu,
              onDismissRequest = { showCardPopupMenu = false },
              modifier = Modifier
                .width(235.dp)
                .background(PureWhite)
                .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                .testTag("alarm_popup_menu_${alarm.id}")
            ) {
              DropdownMenuItem(
                text = {
                  Text(
                    text = "Send Notification Now",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onTestRing()
                }
              )

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Start ${alarm.protocolDurationMin}m Timer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onStartProtocol()
                }
              )

              HorizontalDivider(color = Gray200)

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Snooze +10 Minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.Snooze,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onSnooze10m()
                }
              )

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Snooze +30 Minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onSnooze30m()
                }
              )

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Mark Session Done (+1)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onMarkCompleted()
                }
              )

              HorizontalDivider(color = Gray200)

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Duplicate Reminder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onDuplicate()
                }
              )

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Edit Reminder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onEdit()
                }
              )

              DropdownMenuItem(
                text = {
                  Text(
                    text = "Delete Reminder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                  )
                },
                leadingIcon = {
                  Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    tint = Gray800,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  showCardPopupMenu = false
                  onDelete()
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title & Notes + Big Time Display (matching pricing-section.tsx header)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = alarm.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = primaryTextColor
          )
          if (alarm.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = alarm.notes,
              style = MaterialTheme.typography.bodyMedium,
              color = secondaryTextColor
            )
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = time12,
              style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 32.sp
              ),
              color = primaryTextColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = amPm,
              style = MaterialTheme.typography.bodySmall,
              color = secondaryTextColor,
              modifier = Modifier.padding(bottom = 3.dp)
            )
          }
          Text(
            text = countdownText,
            style = MaterialTheme.typography.labelMedium,
            color = secondaryTextColor
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Primary Action Button inside Card (`mt-4 w-full rounded-lg bg-gray-100 py-2.5 font-medium text-gray-800`)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          onClick = onStartProtocol,
          shape = RoundedCornerShape(8.dp),
          color = if (darkCard) Gray100 else Gray800,
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .testTag("start_protocol_${alarm.id}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = null,
              tint = if (darkCard) Gray800 else PureWhite,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Start ${alarm.protocolDurationMin}m Timer",
              style = MaterialTheme.typography.titleSmall,
              color = if (darkCard) Gray800 else PureWhite
            )
          }
        }

        Surface(
          onClick = onTestRing,
          shape = RoundedCornerShape(8.dp),
          color = if (darkCard) PureWhite.copy(alpha = 0.12f) else Gray100,
          modifier = Modifier
            .height(40.dp)
            .testTag("test_ring_${alarm.id}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.NotificationsActive,
              contentDescription = null,
              tint = primaryTextColor,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Test Ring",
              style = MaterialTheme.typography.titleSmall,
              color = primaryTextColor
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Check-list rows matching `pricing-section.tsx` (`flex items-center gap-2 border-b py-2.5`)
      val dayNames = remember(alarm.daysOfWeek) {
        val map = mapOf(1L to "Mon", 2L to "Tue", 3L to "Wed", 4L to "Thu", 5L to "Fri", 6L to "Sat", 7L to "Sun")
        if (alarm.daysOfWeek.isEmpty()) "One-time reminder"
        else if (alarm.daysOfWeek.size == 7) "Every day"
        else if (alarm.daysOfWeek == listOf(1L, 2L, 3L, 4L, 5L)) "Weekdays (Mon – Fri)"
        else alarm.daysOfWeek.mapNotNull { map[it] }.joinToString(", ")
      }

      val detailRows = buildList {
        add("Schedule: $dayNames • Snooze ${alarm.snoozeMinutes}m")
        if (alarm.bioTempCelsius.isNotBlank()) {
          add("Bioscience / Env Condition: ${alarm.bioTempCelsius}")
        }
        add("Completed ${alarm.completedCount}× • ${if (alarm.vibrate) "Haptic vibration active" else "Silent alert"}")
      }

      detailRows.forEach { detail ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(if (darkCard) PureWhite.copy(alpha = 0.15f) else Gray800),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              tint = PureWhite,
              modifier = Modifier.size(11.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = primaryTextColor
          )
        }
        HorizontalDivider(color = dividerColor)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Footer Quick Actions (Done +1, Edit, Delete)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          onClick = onMarkCompleted,
          shape = RoundedCornerShape(6.dp),
          color = if (darkCard) PureWhite.copy(alpha = 0.1f) else Gray50,
          border = BorderStroke(1.dp, dividerColor),
          modifier = Modifier.testTag("complete_task_${alarm.id}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              tint = primaryTextColor,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Mark Done (+1)",
              style = MaterialTheme.typography.labelMedium,
              color = primaryTextColor
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier
              .size(34.dp)
              .testTag("edit_alarm_${alarm.id}")
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "Edit Reminder",
              tint = secondaryTextColor,
              modifier = Modifier.size(17.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(34.dp)
              .testTag("delete_alarm_${alarm.id}")
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Delete Reminder",
              tint = secondaryTextColor,
              modifier = Modifier.size(17.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Matches `features-section.tsx`:
 * `border rounded-lg border-gray-200/70 divide-y divide-gray-200/70`
 */
@Composable
private fun Prompt2AppFeaturesGridSection() {
  val features = listOf(
    Triple(
      Icons.Filled.Alarm,
      "Precision Work Scheduler",
      "Automatically syncs your recurring work sprints and standup reminders with Android system alarms."
    ),
    Triple(
      Icons.Filled.Biotech,
      "Bioscience Protocol Timers",
      "Built-in 37°C CO2 incubation alerts, qPCR thermal cycle holds, and reagent molarity calculators."
    ),
    Triple(
      Icons.Filled.Speed,
      "Zero-Friction Execution",
      "Launch focus countdowns, test ring alerts, and log completed work sessions with a single tap."
    )
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 760.dp)
      .padding(horizontal = 20.dp, vertical = 28.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, Gray200)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        features.forEachIndexed { idx, (icon, title, desc) ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Gray800
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = desc,
              style = MaterialTheme.typography.bodyMedium,
              color = Gray500
            )
          }
          if (idx < features.lastIndex) {
            HorizontalDivider(color = Gray200)
          }
        }
      }
    }
  }
}

/**
 * Matches `build-process.tsx`:
 * `Simple 4-Step Process` in `font-domine`, vertical timeline line with `size-4 bg-gray-800` squares,
 * and `text-xl underline font-domine` step titles.
 */
@Composable
private fun Prompt2AppBuildProcessSection() {
  val steps = listOf(
    "Describe Your Work or Lab Task" to
      "Type your task or select a quick template pill above. Our scheduler captures your target time, duration, and bioscience parameters.",
    "Smart Alarm & Haptic Sync" to
      "Your reminder is immediately persisted on-device and registered with the system alarm clock for reliable alerts.",
    "Launch Interactive Protocol Timers" to
      "Tap any scheduled card to start its linked countdown timer—ideal for 50-minute deep work blocks or 37°C incubations.",
    "Track Daily Rhythm & Completion" to
      "Log completed work blocks and calculate molarity dilutions seamlessly in one unified workspace."
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 760.dp)
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Simple 4-Step Process",
      style = TextStyle(
        fontFamily = DomineFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
      ),
      color = Gray800
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = "Master Your Work & Lab Schedule in Four Simple Steps",
      style = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp
      ),
      color = Gray500,
      textAlign = TextAlign.Center,
      modifier = Modifier.widthIn(max = 420.dp)
    )

    Spacer(modifier = Modifier.height(28.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
      steps.forEachIndexed { index, (title, description) ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          // Vertical Line + Square Node (`size-4 bg-gray-800`)
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp, end = 18.dp)
          ) {
            Box(
              modifier = Modifier
                .size(14.dp)
                .background(Gray800)
            )
            if (index < steps.lastIndex) {
              Box(
                modifier = Modifier
                  .width(2.dp)
                  .height(96.dp)
                  .background(Gray800)
              )
            }
          }

          Column(
            modifier = Modifier
              .weight(1f)
              .padding(bottom = 24.dp)
          ) {
            Text(
              text = title,
              style = TextStyle(
                fontFamily = DomineFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline
              ),
              color = Gray800
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = description,
              style = MaterialTheme.typography.bodyMedium,
              color = Gray500
            )
          }
        }
      }
    }
  }
}

/**
 * Matches `call-to-action.tsx` & `footer.tsx` from the uploaded ReactJS zip,
 * including the extracted `hand.png` (`R.drawable.img_prompt2app_hand`).
 */
@Composable
private fun Prompt2AppCallToActionAndFooter(
  onScrollToTopAndCreate: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 760.dp)
      .padding(horizontal = 20.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Your Next Productive Block Is Just One Alarm Away",
      style = TextStyle(
        fontFamily = DomineFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
      ),
      color = Gray800,
      textAlign = TextAlign.Center,
      modifier = Modifier.widthIn(max = 420.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Describe your work task or bioscience protocol in a sentence and let ChronoPulse handle your schedule and timers automatically.",
      style = MaterialTheme.typography.bodyMedium,
      color = Gray500,
      textAlign = TextAlign.Center,
      modifier = Modifier.widthIn(max = 420.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Charcoal gradient pill CTA button (`rounded-full bg-linear-to-b from-gray-600 to-gray-800 px-6 py-3 text-white`)
    Box(
      modifier = Modifier
        .clip(CircleShape)
        .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
        .clickable(onClick = onScrollToTopAndCreate)
        .padding(horizontal = 24.dp, vertical = 13.dp)
        .testTag("cta_schedule_button")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.AutoAwesome,
          contentDescription = null,
          tint = PureWhite,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Schedule My Work",
          style = MaterialTheme.typography.titleMedium,
          color = PureWhite
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Image(
      painter = painterResource(id = R.drawable.img_prompt2app_hand),
      contentDescription = "ChronoPulse Hand Illustration",
      modifier = Modifier.size(150.dp),
      contentScale = ContentScale.Fit
    )

    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider(color = Slate200)
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "ChronoPulse • Precision Work & Bioscience Alarm Workspace",
      style = MaterialTheme.typography.bodySmall,
      color = Gray500,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun NotificationPermissionBanner(
  onRequestPermission: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Gray50,
    border = BorderStroke(1.dp, Gray200),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Filled.NotificationsActive,
          contentDescription = null,
          tint = Gray800
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.notification_permission_banner),
          style = MaterialTheme.typography.bodySmall,
          color = Gray600
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Button(
        onClick = onRequestPermission,
        modifier = Modifier.testTag("grant_notification_permission_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Gray800,
          contentColor = PureWhite
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(stringResource(R.string.grant_permission))
      }
    }
  }
}

@Composable
private fun EmptyScheduledTasksState(
  hasAnyAlarmsInDb: Boolean,
  onClearFilters: () -> Unit,
  onOpenNewReminderSheet: () -> Unit,
  onSeedSchedule: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = Gray50,
    border = BorderStroke(1.dp, Gray200)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Filled.Schedule,
        contentDescription = null,
        tint = Gray800,
        modifier = Modifier.size(42.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = if (hasAnyAlarmsInDb) "No Matching Scheduled Tasks" else stringResource(R.string.empty_alarms_title),
        style = TextStyle(
          fontFamily = DomineFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 22.sp
        ),
        color = Gray800
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = if (hasAnyAlarmsInDb) {
          "Clear your search or filter pills above to see all scheduled work reminders."
        } else {
          stringResource(R.string.empty_alarms_subtitle)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = Gray500,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(20.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (hasAnyAlarmsInDb) {
          Button(
            onClick = onClearFilters,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gray800, contentColor = PureWhite)
          ) {
            Text("Reset Filters")
          }
        } else {
          Button(
            onClick = onOpenNewReminderSheet,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gray800, contentColor = PureWhite)
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Reminder")
          }
          OutlinedButton(
            onClick = onSeedSchedule,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("seed_starter_schedule_button")
          ) {
            Text("Load Starter Pack", color = Gray800)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduledTaskEditorSheet(
  initialAlarm: WorkAlarm?,
  prefilledTitle: String,
  prefilledNotes: String,
  prefilledCategory: AlarmCategory,
  onDismiss: () -> Unit,
  onSave: (
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
  ) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val nowCal = remember { Calendar.getInstance() }
  val initialHour = initialAlarm?.hour?.toInt() ?: ((nowCal.get(Calendar.HOUR_OF_DAY) + 1) % 24)
  val initialMinute = initialAlarm?.minute?.toInt() ?: 0

  val timePickerState = rememberTimePickerState(
    initialHour = initialHour,
    initialMinute = initialMinute,
    is24Hour = false
  )

  var useDialPicker by rememberSaveable { mutableStateOf(true) }
  var title by rememberSaveable { mutableStateOf(initialAlarm?.title ?: prefilledTitle) }
  var notes by rememberSaveable { mutableStateOf(initialAlarm?.notes ?: prefilledNotes) }
  var category by rememberSaveable { mutableStateOf(initialAlarm?.categoryEnum ?: prefilledCategory) }
  var bioTemp by rememberSaveable { mutableStateOf(initialAlarm?.bioTempCelsius ?: category.defaultBioTemp) }
  var durationStr by rememberSaveable {
    mutableStateOf((initialAlarm?.protocolDurationMin ?: category.defaultDurationMin).toString())
  }
  var snoozeStr by rememberSaveable {
    mutableStateOf((initialAlarm?.snoozeMinutes ?: 10L).toString())
  }
  var vibrate by rememberSaveable { mutableStateOf(initialAlarm?.vibrate ?: true) }

  val selectedDays = remember {
    mutableStateListOf<Int>().apply {
      addAll(initialAlarm?.daysOfWeek?.map { it.toInt() } ?: listOf(1, 2, 3, 4, 5))
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PureWhite,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (initialAlarm == null) "Schedule Work Reminder" else "Edit Scheduled Task",
            style = TextStyle(
              fontFamily = DomineFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 22.sp
            ),
            color = Gray800
          )
          Text(
            text = "Pick alarm time, task checklist, and recurring days",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
          )
        }
        IconButton(onClick = { useDialPicker = !useDialPicker }) {
          Icon(
            imageVector = if (useDialPicker) Icons.Filled.Keyboard else Icons.Filled.AccessTime,
            contentDescription = "Switch Time Input Mode",
            tint = Gray800
          )
        }
      }

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Gray50,
        border = BorderStroke(1.dp, Gray200)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          if (useDialPicker) {
            TimePicker(
              state = timePickerState,
              colors = TimePickerDefaults.colors(
                selectorColor = Gray800,
                clockDialColor = PureWhite,
                clockDialSelectedContentColor = PureWhite,
                clockDialUnselectedContentColor = Gray800,
                timeSelectorSelectedContainerColor = Gray800,
                timeSelectorSelectedContentColor = PureWhite,
                timeSelectorUnselectedContainerColor = Gray100,
                timeSelectorUnselectedContentColor = Gray800
              )
            )
          } else {
            TimeInput(state = timePickerState)
          }
        }
      }

      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Task or Reminder Title") },
        placeholder = { Text("e.g., Morning Deep Work Sprint or 37°C Cell Culture") },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("alarm_title_input")
      )

      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Checklist / Protocol Notes") },
        placeholder = { Text("Deliverables, reagent concentrations, or meeting agenda") },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("alarm_notes_input")
      )

      Text(
        text = "CATEGORY",
        style = MaterialTheme.typography.labelLarge,
        color = Gray800
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AlarmCategory.entries.forEach { cat ->
          FilterChip(
            selected = category == cat,
            onClick = {
              category = cat
              if (bioTemp.isBlank() || AlarmCategory.entries.any { it.defaultBioTemp == bioTemp }) {
                bioTemp = cat.defaultBioTemp
              }
            },
            label = { Text(cat.displayName) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Gray800,
              selectedLabelColor = PureWhite
            )
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = bioTemp,
          onValueChange = { bioTemp = it },
          label = { Text("Lab / Context Tag") },
          placeholder = { Text("37.0°C / Silent Focus") },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1.3f)
        )
        OutlinedTextField(
          value = durationStr,
          onValueChange = { durationStr = it },
          label = { Text("Duration (min)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(0.7f)
        )
      }

      Text(
        text = "REPEAT DAYS",
        style = MaterialTheme.typography.labelLarge,
        color = Gray800
      )

      val dayLabels = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
      FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        dayLabels.forEach { (d, name) ->
          FilterChip(
            selected = d in selectedDays,
            onClick = {
              if (d in selectedDays) selectedDays.remove(d) else selectedDays.add(d)
            },
            label = { Text(name) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Gray800,
              selectedLabelColor = PureWhite
            )
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Haptic Vibration Alert", style = MaterialTheme.typography.titleSmall, color = Gray800)
          Text(
            "Vibrate device when alarm triggers",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
          )
        }
        Switch(
          checked = vibrate,
          onCheckedChange = { vibrate = it },
          colors = SwitchDefaults.colors(
            checkedThumbColor = PureWhite,
            checkedTrackColor = Gray800
          )
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Text("Cancel", color = Gray800)
        }

        Box(
          modifier = Modifier
            .weight(1.4f)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
            .clickable {
              val cleanTitle = title.trim().ifBlank { category.displayName }
              val dur = durationStr.toIntOrNull()?.coerceIn(1, 1440) ?: 45
              val snz = snoozeStr.toIntOrNull()?.coerceIn(1, 60) ?: 10
              onSave(
                cleanTitle,
                notes,
                timePickerState.hour,
                timePickerState.minute,
                category,
                selectedDays.sorted(),
                bioTemp,
                dur,
                vibrate,
                snz
              )
            }
            .testTag("save_alarm_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AlarmAdd, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Reminder", style = MaterialTheme.typography.titleSmall, color = PureWhite)
          }
        }
      }
    }
  }
}

internal fun taskCategoryIcon(category: AlarmCategory): ImageVector = when (category) {
  AlarmCategory.DEEP_WORK -> Icons.Filled.WorkOutline
  AlarmCategory.MEETING -> Icons.Filled.Alarm
  AlarmCategory.BIO_INCUBATION -> Icons.Filled.Thermostat
  AlarmCategory.PCR_ASSAY -> Icons.Filled.Biotech
  AlarmCategory.CELL_CULTURE -> Icons.Filled.Science
  AlarmCategory.BREAK_CIRCADIAN -> Icons.Filled.WbSunny
}
