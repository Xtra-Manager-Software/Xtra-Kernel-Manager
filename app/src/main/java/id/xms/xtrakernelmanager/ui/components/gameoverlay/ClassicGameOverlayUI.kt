package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun ClassicGameOverlayTheme(content: @Composable () -> Unit) {
  val colorScheme = darkColorScheme(
      primary = ClassicColors.Primary,
      secondary = ClassicColors.Secondary,
      surface = ClassicColors.SurfaceContainerHigh,
      surfaceContainer = ClassicColors.SurfaceContainer,
      background = ClassicColors.Background,
      onSurface = ClassicColors.OnSurface,
      onSurfaceVariant = ClassicColors.OnSurfaceVariant,
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}

@Composable
fun ClassicGameSidebar(
    isExpanded: Boolean,
    overlayOnRight: Boolean,
    isDockedToEdge: Boolean = true,
    fps: String? = null,
    onToggleExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val pillColor = ClassicColors.SurfaceContainerHigh.copy(alpha = 0.95f)
  val contentColor = ClassicColors.Primary

  val shape = if (isDockedToEdge) {
      if (overlayOnRight) {
        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
      } else {
        RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
      }
  } else {
      CircleShape
  }

  val width = if (isDockedToEdge) 52.dp else 40.dp
  val height = if (isDockedToEdge) 36.dp else 40.dp

  Surface(
      color = pillColor,
      shape = shape,
      tonalElevation = 2.dp,
      shadowElevation = 4.dp,
      modifier =
          modifier
              .width(width)
              .height(height)
              .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onDragEnd,
                ) { change, dragAmount ->
                  change.consume()
                  onDrag(dragAmount.x, dragAmount.y)
                }
              }
              .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
              ) {
                onToggleExpand()
              },
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      if (fps != null) {
        Text(
            text = fps,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
        )
      } else {
        Icon(
            imageVector = Icons.Rounded.SportsEsports,
            contentDescription = "Expand",
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
fun ClassicGamePanelCard(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
    onCollapse: () -> Unit,
    onMoveSide: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val time by
      produceState(initialValue = "") {
        while (true) {
          value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
          delay(1000)
        }
      }

  val batteryLevel by viewModel.batteryPercentage.collectAsStateWithLifecycle()
  val temp by viewModel.tempValue.collectAsStateWithLifecycle()

  val fps by viewModel.fpsValue.collectAsStateWithLifecycle()
  val cpuLoad by viewModel.cpuLoad.collectAsStateWithLifecycle()
  val gpuLoad by viewModel.gpuLoad.collectAsStateWithLifecycle()

  Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier =
          modifier
              .width(312.dp)
              .wrapContentHeight()
              .padding(8.dp),
  ) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
              Modifier.fillMaxWidth()
                  .pointerInput(Unit) {
                      detectDragGestures(onDragEnd = onDragEnd) { change, dragAmount ->
                          change.consume()
                          onDrag(dragAmount.x, dragAmount.y)
                      }
                  }
                  .clickable(
                      interactionSource = remember { MutableInteractionSource() },
                      indication = null,
                  ) {
                    onCollapse()
                  },
      ) {
        Text(
            text = time,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = ClassicColors.Primary,
            letterSpacing = (-1).sp,
        )

        Spacer(modifier = Modifier.weight(1f))

        ClassicStatusPill(icon = Icons.Rounded.Thermostat, text = "$temp°C", isWarning = true)
        Spacer(modifier = Modifier.width(6.dp))
        ClassicStatusPill(icon = Icons.Rounded.BatteryStd, text = "$batteryLevel%")
      }

      // Brightness
      ClassicBrightnessControlExpressive(viewModel)

      // Performance
      ClassicPerformanceBento(fps, cpuLoad, gpuLoad)

      // Mode Selector
      ClassicExpressiveModeSelector(viewModel)

      // Tools
      ClassicToolsGridExpressive(viewModel, isFpsEnabled, onFpsToggle)
    }
  }
}

@Composable
fun ClassicStatusPill(icon: ImageVector, text: String, isWarning: Boolean = false) {
  Surface(
      color = if (isWarning) Color(0xFF2E1A1A) else Color(0xFF1A261A),
      contentColor = if (isWarning) Color(0xFFEF9A9A) else Color(0xFFA5D6A7),
      shape = CircleShape,
      modifier = Modifier.height(28.dp),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp),
    ) {
      Icon(icon, null, modifier = Modifier.size(12.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun ClassicPerformanceBento(fps: String, cpu: Float, gpu: Float) {
  Row(
      modifier = Modifier.fillMaxWidth().height(86.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Card(
        modifier = Modifier.weight(1.3f).fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.Primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(32.dp),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          val fpsVal = fps.toFloatOrNull()?.toInt() ?: 60
          Text(
              text = "$fpsVal",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Black,
              color = ClassicColors.Primary,
              lineHeight = 32.sp,
          )
          Text(
              text = "FPS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = ClassicColors.OnSurfaceVariant,
          )
        }
      }
    }

    Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ClassicLoadChip(
          label = "CPU",
          value = cpu,
          color = ClassicColors.Accent,
          modifier = Modifier.weight(1f),
      )
      ClassicLoadChip(
          label = "GPU",
          value = gpu,
          color = ClassicColors.Secondary,
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
fun ClassicLoadChip(label: String, value: Float, color: Color, modifier: Modifier = Modifier) {
  Surface(
      color = ClassicColors.SurfaceContainer,
      shape = RoundedCornerShape(14.dp),
      modifier = modifier.fillMaxWidth(),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = ClassicColors.OnSurfaceVariant)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${value.toInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
      }
    }
  }
}

@Composable
fun ClassicExpressiveModeSelector(viewModel: GameMonitorViewModel) {
  val currentMode by viewModel.currentPerformanceMode.collectAsStateWithLifecycle()
  val modes =
      listOf("powersave" to "Power Save", "balanced" to "Balance", "performance" to "Performance")

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .height(48.dp)
              .background(ClassicColors.SurfaceContainer, CircleShape)
              .padding(4.dp)
  ) {
    modes.forEach { (modeKey, modeLabel) ->
      val isSelected = currentMode == modeKey
      val animatedColor by
          animateColorAsState(
              if (isSelected) ClassicColors.Primary.copy(alpha = 0.2f) else Color.Transparent
          )

      Box(
          modifier =
              Modifier.weight(1f)
                  .fillMaxHeight()
                  .clip(CircleShape)
                  .background(animatedColor)
                  .clickable { viewModel.setPerformanceMode(modeKey) },
          contentAlignment = Alignment.Center,
      ) {
        Text(
            text = modeLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurfaceVariant,
            maxLines = 1,
        )
      }
    }
  }
}

@Composable
fun ClassicBrightnessControlExpressive(viewModel: GameMonitorViewModel) {
  val vmBrightness by viewModel.brightness.collectAsStateWithLifecycle()
  var localSliderValue by remember { mutableFloatStateOf(vmBrightness) }
  var isDragging by remember { mutableStateOf(false) }
  val displayValue = if (isDragging) localSliderValue else vmBrightness

  LaunchedEffect(vmBrightness) {
    if (!isDragging) {
      localSliderValue = vmBrightness
    }
  }

  Surface(
      color = ClassicColors.SurfaceContainer,
      shape = CircleShape,
      modifier = Modifier.fillMaxWidth().height(44.dp),
  ) {
    Box(contentAlignment = Alignment.CenterStart) {
      Box(
          modifier =
              Modifier.fillMaxWidth(displayValue.coerceIn(0.01f, 1f))
                  .fillMaxHeight()
                  .background(ClassicColors.Accent, CircleShape)
      )

      Slider(
          value = displayValue,
          onValueChange = { 
              isDragging = true
              localSliderValue = it
          },
          onValueChangeFinished = {
              viewModel.setBrightness(localSliderValue) 
              isDragging = false 
          },
          colors =
              SliderDefaults.colors(
                  thumbColor = Color.Transparent,
                  activeTrackColor = Color.Transparent,
                  inactiveTrackColor = Color.Transparent,
              ),
          modifier = Modifier.fillMaxWidth(),
      )

      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            Icons.Rounded.BrightnessLow,
            null,
            tint = ClassicColors.OnSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Icon(
            Icons.Rounded.BrightnessHigh,
            null,
            tint = ClassicColors.OnSurface,
            modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
fun ClassicToolsGridExpressive(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
) {
  val dnd by viewModel.doNotDisturb.collectAsStateWithLifecycle()
  val ringerMode by viewModel.ringerMode.collectAsStateWithLifecycle()
  val callMode by viewModel.callMode.collectAsStateWithLifecycle()
  val threeFingerSwipe by viewModel.threeFingerSwipe.collectAsStateWithLifecycle()

  androidx.compose.foundation.lazy.LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(horizontal = 2.dp),
  ) {
    item {
      ClassicToolButtonExpressive(
          icon = Icons.Rounded.Speed,
          label = "FPS",
          isActive = isFpsEnabled,
      ) {
        onFpsToggle()
      }
    }
    item {
        val (icon, label) = when(ringerMode) {
             1 -> Icons.Rounded.Vibration to "Vibrate"
             2 -> Icons.Rounded.NotificationsOff to "Silent"
             else -> Icons.Rounded.Notifications to "Ring"
        }
        ClassicToolButtonExpressive(
            icon = icon,
            label = label,
            isActive = ringerMode != 0,
        ) {
            viewModel.cycleRingerMode()
        }
    }
    item {
        val (icon, label) = when(callMode) {
             1 -> Icons.Rounded.NotificationsPaused to "No HeadsUp"
             2 -> Icons.Rounded.CallEnd to "Reject"
             else -> Icons.Rounded.Call to "Call"
        }
        ClassicToolButtonExpressive(
            icon = icon,
            label = label,
            isActive = callMode != 0,
        ) {
            if (callMode == 2) {
                viewModel.testCallFunctionality()
            } else {
                viewModel.cycleCallMode()
            }
        }
    }

    item {
        val isActive = threeFingerSwipe
        val icon = if (isActive) Icons.Rounded.TouchApp else Icons.Rounded.DoNotDisturb
        ClassicToolButtonExpressive(
            icon = icon,
            label = if (isActive) "Swipe On" else "Swipe Off",
            isActive = isActive,
        ) {
            viewModel.toggleThreeFingerSwipe()
        }
    }

    item {
      ClassicToolButtonExpressive(
          icon = Icons.Rounded.DoNotDisturb,
          label = "DND",
          isActive = dnd,
      ) {
        viewModel.setDND(!dnd)
      }
    }

    item {
        val isTouchGuard by viewModel.touchGuard.collectAsStateWithLifecycle()
        ClassicToolButtonExpressive(
            icon = if (isTouchGuard) Icons.Rounded.BackHand else Icons.Rounded.PanTool,
            label = "Disable Gesture", 
            isActive = isTouchGuard,
        ) {
            viewModel.setTouchGuard(!isTouchGuard)
        }
    }

    item {
      ClassicToolButtonExpressive(
          icon = Icons.Rounded.RocketLaunch,
          label = "Boost",
          isActive = false,
      ) {
        viewModel.performGameBoost()
      }
    }

    item {
      ClassicToolButtonExpressive(
          icon = Icons.Rounded.Screenshot,
          label = "Screenshot",
          isActive = false,
      ) { 
        viewModel.takeScreenshot()
      }
    }
  }
}

@Composable
fun ClassicToolButtonExpressive(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
  val bgColor =
      if (isActive) ClassicColors.Primary
      else ClassicColors.SurfaceContainer
  val iconColor =
      if (isActive) Color.White else ClassicColors.OnSurface

  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp), 
        color = bgColor,
        modifier = Modifier.size(56.dp).aspectRatio(1f), 
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp)) 
      }
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = ClassicColors.OnSurfaceVariant,
        fontSize = 11.sp,
    )
  }
}
