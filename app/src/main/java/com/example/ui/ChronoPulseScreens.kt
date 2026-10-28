package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.alarm.WorkAlarmScheduler
import com.example.data.AlarmCategory
import com.example.data.WorkAlarm
import com.example.ui.theme.DomineFontFamily
import com.example.ui.theme.Gray100
import com.example.ui.theme.Gray200
import com.example.ui.theme.Gray400
import com.example.ui.theme.Gray50
import com.example.ui.theme.Gray500
import com.example.ui.theme.Gray600
import com.example.ui.theme.Gray700
import com.example.ui.theme.Gray800
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChronoTab(val label: String, val labelRes: Int, val icon: ImageVector, val tag: String) {
  ALARMS("Schedule", R.string.nav_alarms, Icons.Filled.Alarm, "tab_alarms"),
  BIO_TIMER("Bio-Timer", R.string.nav_bio_lab, Icons.Filled.Science, "tab_bio_timer"),
  MOLARITY_PREP("Molarity", R.string.nav_calculator, Icons.Filled.Calculate, "tab_molarity_prep"),
  INSIGHTS("Stats", R.string.nav_insights, Icons.Filled.Insights, "tab_insights")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeatureScreen(
  isDarkTheme: Boolean,
  onToggleDarkTheme: () -> Unit,
  viewModel: WorkAlarmViewModel = viewModel(
    factory = WorkAlarmViewModel.Factory
  )
) {
  val context = LocalContext.current
  val alarmScheduler = remember(context) { WorkAlarmScheduler(context.applicationContext) }

  val alarmsState by viewModel.alarmsState.collectAsStateWithLifecycle()
  val selectedFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
  val protocolTimer by viewModel.protocolTimer.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val livePopupNotification by viewModel.livePopupNotification.collectAsStateWithLifecycle()

  var currentTab by rememberSaveable { mutableStateOf(ChronoTab.ALARMS) }
  var showEditorSheet by rememberSaveable { mutableStateOf(false) }
  var showMobileMenuOverlay by rememberSaveable { mutableStateOf(false) }
  var showQuickActionsPopupMenu by remember { mutableStateOf(false) }
  var showNotificationsPopupMenu by remember { mutableStateOf(false) }

  var editingAlarm by remember { mutableStateOf<WorkAlarm?>(null) }
  var prefilledTitle by remember { mutableStateOf("") }
  var prefilledNotes by remember { mutableStateOf("") }
  var prefilledCategory by remember { mutableStateOf(AlarmCategory.DEEP_WORK) }

  val snackbarHostState = remember { SnackbarHostState() }

  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasNotificationPermission = granted
  }

  val allAlarms = (alarmsState as? UiState.Success)?.data.orEmpty()
  val unreadNotificationCount = remember(notifications) {
    notifications.count { !it.isRead }
  }

  fun triggerAlarmNotification(alarm: WorkAlarm, customSubtitle: String? = null) {
    alarmScheduler.triggerImmediateTestNotification(alarm)
    viewModel.pushNotificationEvent(
      alarm = alarm,
      customMessage = customSubtitle,
      showLivePopup = true
    )
  }

  fun triggerSampleOrNextNotification() {
    val targetAlarm = allAlarms.firstOrNull { it.enabled }
      ?: allAlarms.firstOrNull()
      ?: WorkAlarm(
        id = "instant_demo_alert",
        title = "Deep Work & Lab Protocol Alert",
        notes = "Immediate system & in-app notification triggered from ChronoPulse.",
        hour = 9,
        minute = 0,
        enabled = true,
        category = AlarmCategory.DEEP_WORK.name,
        bioTempCelsius = "37.0°C / Focus Ready",
        protocolDurationMin = 45
      )
    triggerAlarmNotification(
      alarm = targetAlarm,
      customSubtitle = "Live alert triggered • Tap Start Timer or Snooze +10m"
    )
  }

  LaunchedEffect(alarmsState) {
    val state = alarmsState
    if (state is UiState.Success) {
      alarmScheduler.syncAllAlarms(state.data)
    }
  }

  LaunchedEffect(bannerMessage) {
    val msg = bannerMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearBannerMessage()
    }
  }

  BackHandler(enabled = showMobileMenuOverlay || currentTab != ChronoTab.ALARMS) {
    if (showMobileMenuOverlay) {
      showMobileMenuOverlay = false
    } else {
      currentTab = ChronoTab.ALARMS
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideNavbar = maxWidth >= 600.dp

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      containerColor = PureWhite,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        Column(modifier = Modifier.fillMaxWidth().background(PureWhite)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left: Brand Logo & Title
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { currentTab = ChronoTab.ALARMS }
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_prompt2app_logo),
                contentDescription = "ChronoPulse Logo",
                modifier = Modifier
                  .size(28.dp)
                  .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Fit
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "ChronoPulse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
              )
            }

            // Center: Desktop/Tablet Navigation Links
            if (isWideNavbar) {
              Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                ChronoTab.entries.forEach { tab ->
                  Text(
                    text = tab.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (currentTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (currentTab == tab) Gray800 else Gray600,
                    modifier = Modifier
                      .clickable { currentTab = tab }
                      .testTag(tab.tag)
                  )
                }
              }
            }

            // Right: Notification Bell Pop-up + New Reminder CTA + Quick Actions Pop-up Menu
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // 1. Notification Bell Button + Notifications Pop-up Menu
              Box {
                IconButton(
                  onClick = { showNotificationsPopupMenu = true },
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray50)
                    .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                    .testTag("navbar_notification_bell")
                ) {
                  BadgedBox(
                    badge = {
                      if (unreadNotificationCount > 0) {
                        Badge(
                          containerColor = Gray800,
                          contentColor = PureWhite
                        ) {
                          Text(
                            text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                            fontSize = 10.sp
                          )
                        }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = if (unreadNotificationCount > 0) {
                        Icons.Filled.NotificationsActive
                      } else {
                        Icons.Filled.Notifications
                      },
                      contentDescription = "Notifications",
                      tint = Gray800,
                      modifier = Modifier.size(19.dp)
                    )
                  }
                }

                // Notifications Pop-up Menu
                DropdownMenu(
                  expanded = showNotificationsPopupMenu,
                  onDismissRequest = { showNotificationsPopupMenu = false },
                  modifier = Modifier
                    .width(340.dp)
                    .background(PureWhite)
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .testTag("notifications_popup_menu")
                ) {
                  Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = "NOTIFICATIONS",
                        style = TextStyle(
                          fontFamily = DomineFontFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 15.sp
                        ),
                        color = Gray800
                      )
                      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (unreadNotificationCount > 0) {
                          Text(
                            text = "Mark read",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray600,
                            modifier = Modifier
                              .clickable { viewModel.markAllNotificationsRead() }
                              .testTag("mark_all_notifications_read")
                          )
                        }
                        if (notifications.isNotEmpty()) {
                          Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500,
                            modifier = Modifier
                              .clickable { viewModel.clearAllNotifications() }
                              .testTag("clear_all_notifications")
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant Test Notification CTA Button inside Pop-up
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
                        .clickable {
                          showNotificationsPopupMenu = false
                          if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                          }
                          triggerSampleOrNextNotification()
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("popup_send_test_notification_button"),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Filled.NotificationsActive,
                          contentDescription = null,
                          tint = PureWhite,
                          modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = "Send Test Notification Now",
                          style = MaterialTheme.typography.labelLarge,
                          color = PureWhite
                        )
                      }
                    }
                  }

                  HorizontalDivider(color = Gray200)

                  if (notifications.isEmpty()) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "No notifications yet. Tap 'Send Test Notification Now' above to test live alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        textAlign = TextAlign.Center
                      )
                    }
                  } else {
                    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                    ) {
                      notifications.take(8).forEach { item ->
                        val bg = if (!item.isRead) Gray50 else PureWhite
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .clickable {
                              viewModel.markNotificationRead(item.id)
                              showNotificationsPopupMenu = false
                              val linked = allAlarms.firstOrNull { it.id == item.alarmId }
                              if (linked != null) {
                                editingAlarm = linked
                                showEditorSheet = true
                              }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Row(
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier.weight(1f)
                            ) {
                              if (!item.isRead) {
                                Box(
                                  modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Gray800)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                              }
                              Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (!item.isRead) FontWeight.SemiBold else FontWeight.Medium,
                                color = Gray800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                              )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                              text = timeFormatter.format(Date(item.timestampMillis)),
                              style = MaterialTheme.typography.labelSmall,
                              color = Gray500
                            )
                          }
                          Spacer(modifier = Modifier.height(3.dp))
                          Text(
                            text = item.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                        HorizontalDivider(color = Gray200)
                      }
                    }
                  }
                }
              }

              // 2. "New Reminder" Gradient CTA Button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
                  .clickable {
                    editingAlarm = null
                    prefilledTitle = ""
                    prefilledNotes = ""
                    prefilledCategory = selectedFilter ?: AlarmCategory.DEEP_WORK
                    showEditorSheet = true
                  }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
                  .testTag("navbar_new_reminder_button")
              ) {
                Text(
                  text = "New Reminder",
                  style = MaterialTheme.typography.labelLarge,
                  color = PureWhite
                )
              }

              // 3. Top Bar Pop-up Menu Button (`MoreVert` with DropdownMenu)
              Box {
                IconButton(
                  onClick = { showQuickActionsPopupMenu = true },
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gray50)
                    .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                    .testTag("navbar_popup_menu_button")
                ) {
                  Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Quick Actions Pop-up Menu",
                    tint = Gray800,
                    modifier = Modifier.size(20.dp)
                  )
                }

                DropdownMenu(
                  expanded = showQuickActionsPopupMenu,
                  onDismissRequest = { showQuickActionsPopupMenu = false },
                  modifier = Modifier
                    .width(250.dp)
                    .background(PureWhite)
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .testTag("navbar_quick_actions_dropdown")
                ) {
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "New Work / Lab Reminder",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                      )
                    },
                    leadingIcon = {
                      Icon(Icons.Filled.Add, contentDescription = null, tint = Gray800, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                      showQuickActionsPopupMenu = false
                      editingAlarm = null
                      prefilledTitle = ""
                      prefilledNotes = ""
                      prefilledCategory = selectedFilter ?: AlarmCategory.DEEP_WORK
                      showEditorSheet = true
                    },
                    modifier = Modifier.testTag("menu_new_reminder")
                  )

                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Send Test Notification",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
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
                      showQuickActionsPopupMenu = false
                      triggerSampleOrNextNotification()
                    },
                    modifier = Modifier.testTag("menu_send_test_notification")
                  )

                  HorizontalDivider(color = Gray200)

                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Arm All Reminders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                      )
                    },
                    leadingIcon = {
                      Icon(Icons.Filled.AlarmOn, contentDescription = null, tint = Gray800, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                      showQuickActionsPopupMenu = false
                      viewModel.setAllAlarmsEnabled(true)
                    },
                    modifier = Modifier.testTag("menu_arm_all")
                  )

                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Pause All Reminders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                      )
                    },
                    leadingIcon = {
                      Icon(
                        Icons.Filled.PauseCircleOutline,
                        contentDescription = null,
                        tint = Gray800,
                        modifier = Modifier.size(18.dp)
                      )
                    },
                    onClick = {
                      showQuickActionsPopupMenu = false
                      viewModel.setAllAlarmsEnabled(false)
                    },
                    modifier = Modifier.testTag("menu_pause_all")
                  )

                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Load Starter Schedule",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                      )
                    },
                    leadingIcon = {
                      Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Gray800, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                      showQuickActionsPopupMenu = false
                      viewModel.seedStarterSchedule()
                    },
                    modifier = Modifier.testTag("menu_seed_schedule")
                  )

                  HorizontalDivider(color = Gray200)

                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Bio-Protocol Timer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                      )
                    },
                    leadingIcon = {
                      Icon(Icons.Filled.Science, contentDescription = null, tint = Gray800, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                      showQuickActionsPopupMenu = false
                      currentTab = ChronoTab.BIO_TIMER
                    }
                  )

                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Molarity Calculator",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                      )
                    },
                    leadingIcon = {
                      Icon(Icons.Filled.Calculate, contentDescription = null, tint = Gray800, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                      showQuickActionsPopupMenu = false
                      currentTab = ChronoTab.MOLARITY_PREP
                    }
                  )

                  if (!isWideNavbar) {
                    DropdownMenuItem(
                      text = {
                        Text(
                          text = "Full-Screen Navigation",
                          style = MaterialTheme.typography.bodyMedium,
                          color = Gray800
                        )
                      },
                      leadingIcon = {
                        Icon(Icons.Filled.Menu, contentDescription = null, tint = Gray800, modifier = Modifier.size(18.dp))
                      },
                      onClick = {
                        showQuickActionsPopupMenu = false
                        showMobileMenuOverlay = true
                      }
                    )
                  }
                }
              }
            }
          }
          HorizontalDivider(color = Gray200)
        }
      },
      floatingActionButton = {
        if (currentTab == ChronoTab.ALARMS) {
          ExtendedFloatingActionButton(
            onClick = {
              editingAlarm = null
              prefilledTitle = ""
              prefilledNotes = ""
              prefilledCategory = selectedFilter ?: AlarmCategory.DEEP_WORK
              showEditorSheet = true
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Reminder") },
            text = { Text(stringResource(R.string.fab_new_alarm)) },
            containerColor = Gray800,
            contentColor = PureWhite,
            shape = CircleShape,
            modifier = Modifier.testTag("create_alarm_fab")
          )
        }
      },
      bottomBar = {
        if (!isWideNavbar) {
          Column {
            HorizontalDivider(color = Gray200)
            NavigationBar(
              windowInsets = WindowInsets.navigationBars,
              containerColor = PureWhite
            ) {
              ChronoTab.entries.forEach { tab ->
                NavigationBarItem(
                  selected = currentTab == tab,
                  onClick = { currentTab = tab },
                  icon = { Icon(tab.icon, contentDescription = tab.label) },
                  label = { Text(tab.label) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureWhite,
                    selectedTextColor = Gray800,
                    indicatorColor = Gray800,
                    unselectedIconColor = Gray500,
                    unselectedTextColor = Gray500
                  ),
                  modifier = Modifier.testTag(tab.tag)
                )
              }
            }
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(PureWhite),
        contentAlignment = Alignment.TopCenter
      ) {
        when (currentTab) {
          ChronoTab.ALARMS -> ScheduledTasksScreen(
            alarmsState = alarmsState,
            selectedCategory = selectedFilter,
            hasNotificationPermission = hasNotificationPermission,
            onRequestNotificationPermission = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            },
            onSelectCategory = viewModel::selectCategoryFilter,
            onToggleAlarm = viewModel::toggleAlarmEnabled,
            onEditAlarm = { alarm ->
              editingAlarm = alarm
              showEditorSheet = true
            },
            onDeleteAlarm = { alarm ->
              alarmScheduler.cancelAlarm(alarm.id)
              viewModel.deleteAlarm(alarm)
            },
            onMarkCompleted = viewModel::markSessionCompleted,
            onTestRing = { alarm ->
              triggerAlarmNotification(alarm)
            },
            onSnoozeAlarm = { alarm, mins ->
              viewModel.snoozeAlarmByMinutes(alarm, mins)
            },
            onDuplicateAlarm = { alarm ->
              viewModel.duplicateAlarm(alarm)
            },
            onStartProtocolTimer = { alarm ->
              viewModel.loadAlarmIntoProtocolTimer(alarm)
              currentTab = ChronoTab.BIO_TIMER
            },
            onCreateFromPrompt = { title, notes, hour, minute, category, durationMin ->
              viewModel.createWorkAlarm(
                title = title,
                notes = notes,
                hour = hour,
                minute = minute,
                category = category,
                daysOfWeek = listOf(1, 2, 3, 4, 5),
                bioTempCelsius = category.defaultBioTemp,
                protocolDurationMin = durationMin,
                vibrate = true,
                snoozeMinutes = 10
              )
            },
            onOpenNewReminderSheet = {
              editingAlarm = null
              prefilledTitle = ""
              prefilledNotes = ""
              prefilledCategory = selectedFilter ?: AlarmCategory.DEEP_WORK
              showEditorSheet = true
            },
            onSeedStarterSchedule = viewModel::seedStarterSchedule
          )

          ChronoTab.BIO_TIMER -> BioProtocolTimerTab(
            timerState = protocolTimer,
            onStartOrResume = viewModel::startOrResumeProtocolTimer,
            onPause = viewModel::pauseProtocolTimer,
            onReset = viewModel::resetProtocolTimer,
            onSelectPreset = { title, category, bioCond, mins ->
              viewModel.configureCustomProtocolTimer(title, category, bioCond, mins)
            }
          )

          ChronoTab.MOLARITY_PREP -> BioMolarityCalculatorTab(
            onSchedulePrepAlarm = { title, notes, category ->
              editingAlarm = null
              prefilledTitle = title
              prefilledNotes = notes
              prefilledCategory = category
              showEditorSheet = true
            }
          )

          ChronoTab.INSIGHTS -> WorkRhythmInsightsTab(
            alarmsState = alarmsState,
            onMarkCompleted = viewModel::markSessionCompleted
          )
        }

        // Live In-App Notification Pop-up Banner (slides down when an alarm/test notification fires)
        AnimatedVisibility(
          visible = livePopupNotification != null,
          enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          livePopupNotification?.let { popupItem ->
            LiveNotificationPopupBanner(
              item = popupItem,
              onStartTimer = {
                val linked = allAlarms.firstOrNull { it.id == popupItem.alarmId }
                if (linked != null) {
                  viewModel.loadAlarmIntoProtocolTimer(linked)
                }
                viewModel.markNotificationRead(popupItem.id)
                viewModel.dismissLivePopup()
                currentTab = ChronoTab.BIO_TIMER
              },
              onSnooze10m = {
                val linked = allAlarms.firstOrNull { it.id == popupItem.alarmId }
                if (linked != null) {
                  viewModel.snoozeAlarmByMinutes(linked, 10)
                } else {
                  viewModel.dismissLivePopup()
                  viewModel.showBannerMessage("Snoozed reminder for +10 minutes")
                }
              },
              onDismiss = {
                viewModel.markNotificationRead(popupItem.id)
                viewModel.dismissLivePopup()
              }
            )
          }
        }

        // Full-Screen Mobile Menu Overlay (matching navbar.tsx mobile menu)
        AnimatedVisibility(
          visible = showMobileMenuOverlay,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(PureWhite.copy(alpha = 0.96f))
              .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
              ChronoTab.entries.forEach { tab ->
                Text(
                  text = tab.label,
                  style = MaterialTheme.typography.headlineSmall,
                  color = Gray800,
                  modifier = Modifier.clickable {
                    currentTab = tab
                    showMobileMenuOverlay = false
                  }
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
                  .clickable {
                    showMobileMenuOverlay = false
                    editingAlarm = null
                    prefilledTitle = ""
                    prefilledNotes = ""
                    showEditorSheet = true
                  }
                  .padding(horizontal = 22.dp, vertical = 10.dp)
              ) {
                Text("New Work Reminder", color = PureWhite, style = MaterialTheme.typography.titleMedium)
              }

              OutlinedButton(
                onClick = {
                  showMobileMenuOverlay = false
                  triggerSampleOrNextNotification()
                },
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Gray800, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Test Notification", color = Gray800)
              }

              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
                  .clickable { showMobileMenuOverlay = false },
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Filled.Close, contentDescription = "Close Menu", tint = PureWhite)
              }
            }
          }
        }
      }
    }
  }

  if (showEditorSheet) {
    ScheduledTaskEditorSheet(
      initialAlarm = editingAlarm,
      prefilledTitle = prefilledTitle,
      prefilledNotes = prefilledNotes,
      prefilledCategory = prefilledCategory,
      onDismiss = { showEditorSheet = false },
      onSave = { title, notes, hour, minute, category, days, bioTemp, durationMin, vibrate, snooze ->
        val existing = editingAlarm
        if (existing != null) {
          viewModel.updateExistingAlarm(
            existing.copy(
              title = title,
              notes = notes,
              hour = hour.toLong(),
              minute = minute.toLong(),
              category = category.key,
              daysOfWeek = days.map { it.toLong() },
              bioTempCelsius = bioTemp,
              protocolDurationMin = durationMin.toLong(),
              vibrate = vibrate,
              snoozeMinutes = snooze.toLong()
            )
          )
        } else {
          viewModel.createWorkAlarm(
            title = title,
            notes = notes,
            hour = hour,
            minute = minute,
            category = category,
            daysOfWeek = days,
            bioTempCelsius = bioTemp,
            protocolDurationMin = durationMin,
            vibrate = vibrate,
            snoozeMinutes = snooze
          )
        }
        showEditorSheet = false
      }
    )
  }
}

/**
 * Live Notification Pop-up Banner styled in the Prompt2App dark charcoal theme (`Gray800` card,
 * `PureWhite` text, `Domine` badge, and quick action buttons for Start Timer, Snooze +10m, Dismiss).
 */
@Composable
private fun LiveNotificationPopupBanner(
  item: NotificationLogItem,
  onStartTimer: () -> Unit,
  onSnooze10m: () -> Unit,
  onDismiss: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Gray800,
    border = BorderStroke(1.dp, Gray700),
    shadowElevation = 10.dp,
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 560.dp)
      .testTag("live_notification_popup_banner")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PureWhite.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.NotificationsActive,
              contentDescription = null,
              tint = PureWhite,
              modifier = Modifier.size(15.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "LIVE REMINDER ALERT • ${item.category.displayName.uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = Gray200
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(28.dp)
            .testTag("dismiss_live_notification_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Dismiss Notification",
            tint = Gray400,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = item.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PureWhite
      )

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = item.message,
        style = MaterialTheme.typography.bodySmall,
        color = Gray200
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          onClick = onStartTimer,
          shape = RoundedCornerShape(8.dp),
          color = PureWhite,
          modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .testTag("popup_banner_start_timer")
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = null,
              tint = Gray800,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Start Timer",
              style = MaterialTheme.typography.labelLarge,
              color = Gray800
            )
          }
        }

        Surface(
          onClick = onSnooze10m,
          shape = RoundedCornerShape(8.dp),
          color = PureWhite.copy(alpha = 0.12f),
          modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .testTag("popup_banner_snooze_10m")
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Snooze,
              contentDescription = null,
              tint = PureWhite,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Snooze +10m",
              style = MaterialTheme.typography.labelLarge,
              color = PureWhite
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BioProtocolTimerTab(
  timerState: ActiveProtocolTimerState,
  onStartOrResume: () -> Unit,
  onPause: () -> Unit,
  onReset: () -> Unit,
  onSelectPreset: (String, AlarmCategory, String, Int) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 640.dp)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "BIO-LAB & FOCUS TIMER",
      style = TextStyle(fontFamily = DomineFontFamily, fontSize = 26.sp),
      color = Gray800
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Precision countdown for cell culture incubations, PCR cycles, and deep work sprints.",
      style = MaterialTheme.typography.bodyMedium,
      color = Gray500,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))

    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, Slate200)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          color = Gray100,
          shape = CircleShape
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Thermostat,
              contentDescription = null,
              tint = Gray800,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = timerState.bioCondition,
              style = MaterialTheme.typography.labelLarge,
              color = Gray800
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = timerState.title,
          style = MaterialTheme.typography.titleLarge,
          color = Gray800,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
          modifier = Modifier.size(220.dp),
          contentAlignment = Alignment.Center
        ) {
          val progress = timerState.progressFraction

          Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            drawArc(
              color = Gray100,
              startAngle = 0f,
              sweepAngle = 360f,
              useCenter = false,
              style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
              color = Gray800,
              startAngle = -90f,
              sweepAngle = 360f * (1f - progress),
              useCenter = false,
              style = Stroke(width = stroke, cap = StrokeCap.Round),
              topLeft = Offset.Zero,
              size = Size(size.width, size.height)
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = timerState.formattedRemaining(),
              style = MaterialTheme.typography.displayMedium,
              color = Gray800
            )
            Text(
              text = if (timerState.isRunning) "RUNNING" else "PAUSED / READY",
              style = MaterialTheme.typography.labelMedium,
              color = Gray500
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .height(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
              .clickable {
                if (timerState.isRunning) onPause() else onStartOrResume()
              }
              .padding(horizontal = 20.dp)
              .testTag("bio_timer_play_pause_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (timerState.isRunning) "Pause Protocol" else "Start Timer",
                style = MaterialTheme.typography.titleSmall,
                color = PureWhite
              )
            }
          }

          OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .height(44.dp)
              .testTag("bio_timer_reset_button")
          ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reset Timer", tint = Gray800, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset", color = Gray800)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(22.dp))

    val presets = listOf(
      Triple("Trypsinization 37°C Digest", AlarmCategory.CELL_CULTURE, "37.0°C • 5 Min"),
      Triple("Western Blot Primary Antibody Block", AlarmCategory.BIO_INCUBATION, "4.0°C Shaker • 60 Min"),
      Triple("PCR Extension & Gel Electrophoresis", AlarmCategory.PCR_ASSAY, "72°C / 100V • 35 Min"),
      Triple("Pomodoro Cognitive Deep Work", AlarmCategory.DEEP_WORK, "Silent Focus • 50 Min")
    )
    val presetMinutes = listOf(5, 60, 35, 50)

    presets.forEachIndexed { idx, (title, cat, cond) ->
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 5.dp)
          .clickable { onSelectPreset(title, cat, cond, presetMinutes[idx]) }
          .testTag("bio_preset_$idx"),
        shape = RoundedCornerShape(10.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Gray200)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = Gray800)
            Text(text = cond, style = MaterialTheme.typography.bodySmall, color = Gray500)
          }
          Text(
            text = "${presetMinutes[idx]}m",
            style = MaterialTheme.typography.titleMedium,
            color = Gray800
          )
        }
      }
    }
  }
}

@Composable
private fun BioMolarityCalculatorTab(
  onSchedulePrepAlarm: (String, String, AlarmCategory) -> Unit
) {
  var c1StockMolar by rememberSaveable { mutableStateOf("10.0") }
  var c2TargetMolar by rememberSaveable { mutableStateOf("0.5") }
  var v2FinalVolumeMl by rememberSaveable { mutableStateOf("50.0") }

  var mwGramsPerMol by rememberSaveable { mutableStateOf("58.44") }
  var targetMolarity by rememberSaveable { mutableStateOf("0.15") }
  var targetVolumeLiters by rememberSaveable { mutableStateOf("0.5") }

  val c1 = c1StockMolar.toDoubleOrNull() ?: 0.0
  val c2 = c2TargetMolar.toDoubleOrNull() ?: 0.0
  val v2 = v2FinalVolumeMl.toDoubleOrNull() ?: 0.0
  val requiredStockMl = if (c1 > 0.0) (c2 * v2) / c1 else 0.0
  val bufferVolumeMl = (v2 - requiredStockMl).coerceAtLeast(0.0)

  val mw = mwGramsPerMol.toDoubleOrNull() ?: 0.0
  val mol = targetMolarity.toDoubleOrNull() ?: 0.0
  val volL = targetVolumeLiters.toDoubleOrNull() ?: 0.0
  val requiredMassGrams = mw * mol * volL

  Column(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 640.dp)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    Text(
      text = "MOLARITY & REAGENT PREP",
      style = TextStyle(fontFamily = DomineFontFamily, fontSize = 26.sp),
      color = Gray800
    )

    Surface(
      shape = RoundedCornerShape(14.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, Slate200)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Solution Dilution Calculator (C₁V₁ = C₂V₂)",
          style = MaterialTheme.typography.titleLarge,
          color = Gray800
        )
        Text(
          text = "Calculate stock aliquot volume and schedule a reagent preparation reminder.",
          style = MaterialTheme.typography.bodySmall,
          color = Gray500
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = c1StockMolar,
            onValueChange = { c1StockMolar = it },
            label = { Text("Stock C₁") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("input_c1_stock")
          )
          OutlinedTextField(
            value = c2TargetMolar,
            onValueChange = { c2TargetMolar = it },
            label = { Text("Target C₂") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("input_c2_target")
          )
          OutlinedTextField(
            value = v2FinalVolumeMl,
            onValueChange = { v2FinalVolumeMl = it },
            label = { Text("Final V₂ (mL)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("input_v2_volume")
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          color = Gray50,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Gray200),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = String.format(
                Locale.US,
                "Pipette %.2f mL Stock + %.2f mL Diluent",
                requiredStockMl,
                bufferVolumeMl
              ),
              style = MaterialTheme.typography.titleMedium,
              color = Gray800
            )
            Text(
              text = String.format(Locale.US, "Total Final Volume: %.2f mL", v2),
              style = MaterialTheme.typography.bodySmall,
              color = Gray500
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(Gray600, Gray800)))
            .clickable {
              val summary = String.format(
                Locale.US,
                "Mix %.2f mL of %s stock with %.2f mL buffer (Final: %.2f mL @ %s)",
                requiredStockMl,
                c1StockMolar,
                bufferVolumeMl,
                v2,
                c2TargetMolar
              )
              onSchedulePrepAlarm(
                "Reagent Dilution Prep (${c2TargetMolar}M)",
                summary,
                AlarmCategory.CELL_CULTURE
              )
            }
            .testTag("schedule_dilution_alarm_button"),
          contentAlignment = Alignment.Center
        ) {
          Text("Schedule Dilution Prep Alarm", color = PureWhite, style = MaterialTheme.typography.titleSmall)
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(14.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, Slate200)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Molar Mass Weigh-Out (Mass = M × V × MW)",
          style = MaterialTheme.typography.titleLarge,
          color = Gray800
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = mwGramsPerMol,
            onValueChange = { mwGramsPerMol = it },
            label = { Text("MW (g/mol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = targetMolarity,
            onValueChange = { targetMolarity = it },
            label = { Text("Molarity (M)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = targetVolumeLiters,
            onValueChange = { targetVolumeLiters = it },
            label = { Text("Volume (L)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          color = Gray50,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Gray200),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = String.format(Locale.US, "Weigh Out: %.4f g (%.1f mg)", requiredMassGrams, requiredMassGrams * 1000.0),
              style = MaterialTheme.typography.titleMedium,
              color = Gray800
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WorkRhythmInsightsTab(
  alarmsState: UiState<List<WorkAlarm>>,
  onMarkCompleted: (WorkAlarm) -> Unit
) {
  val alarms = (alarmsState as? UiState.Success)?.data.orEmpty()
  val totalCompleted = alarms.sumOf { it.completedCount }
  val totalDailyFocusMinutes = alarms.filter { it.enabled }.sumOf { it.protocolDurationMin }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 640.dp)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "WORK & LAB RHYTHM STATS",
      style = TextStyle(fontFamily = DomineFontFamily, fontSize = 26.sp),
      color = Gray800
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        color = Gray800
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "COMPLETED BLOCKS",
            style = MaterialTheme.typography.labelMedium,
            color = Gray200
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "$totalCompleted",
            style = MaterialTheme.typography.displayMedium,
            color = PureWhite
          )
        }
      }

      Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "DAILY ACTIVE FOCUS",
            style = MaterialTheme.typography.labelMedium,
            color = Gray500
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "${totalDailyFocusMinutes}m",
            style = MaterialTheme.typography.displayMedium,
            color = Gray800
          )
        }
      }
    }

    alarms.forEach { alarm ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Gray200),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = alarm.title,
              style = MaterialTheme.typography.titleSmall,
              color = Gray800
            )
            Text(
              text = "Completed ${alarm.completedCount}× • ${alarm.formattedTime24()}",
              style = MaterialTheme.typography.bodySmall,
              color = Gray500
            )
          }
          Button(
            onClick = { onMarkCompleted(alarm) },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gray800, contentColor = PureWhite),
            modifier = Modifier.testTag("complete_alarm_${alarm.id}")
          ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("+1 Done")
          }
        }
      }
    }
  }
}
