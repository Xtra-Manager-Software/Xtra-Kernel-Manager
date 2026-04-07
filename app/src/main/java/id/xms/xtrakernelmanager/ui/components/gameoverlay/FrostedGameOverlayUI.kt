package id.xms.xtrakernelmanager.ui.components.gameoverlay

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

/**
 * Frosted Game Overlay Theme - Dark Mode Glassmorphism
 * Using dark theme with white text
 */
@Composable
fun FrostedGameOverlayTheme(content: @Composable () -> Unit) {
  val colorScheme = darkColorScheme(
      primary = Color(0xFF38BDF8), // Cyan
      secondary = Color(0xFFA78BFA), // Purple
      tertiary = Color(0xFFF472B6), // Pink
      surface = Color.White.copy(alpha = 0.1f),
      surfaceContainer = Color.White.copy(alpha = 0.08f),
      surfaceContainerHigh = Color.White.copy(alpha = 0.12f),
      background = Color.Transparent,
      onSurface = Color.White,
      onSurfaceVariant = Color.White.copy(alpha = 0.7f)
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}

/**
 * Frosted Game Sidebar - Glassmorphism Dark Mode
 */
@Composable
fun FrostedGameSidebar(
    isExpanded: Boolean,
    overlayOnRight: Boolean,
    isDockedToEdge: Boolean = true,
    fps: String? = null,
    onToggleExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val contentColor = Color.White

  val shape = if (isDockedToEdge) {
      if (overlayOnRight) {
        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
      } else {
        RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
      }
  } else {
      CircleShape
  }

  val width = if (isDockedToEdge) 72.dp else 52.dp
  val height = if (isDockedToEdge) 48.dp else 52.dp

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = modifier
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
      shape = shape,
  ) {
    Box(
        contentAlignment = Alignment.Center, 
        modifier = Modifier.fillMaxSize()
    ) {
      if (fps != null) {
        Text(
            text = fps,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            maxLines = 1
        )
      } else {
        Icon(
            imageVector = Icons.Rounded.SportsEsports,
            contentDescription = "Expand",
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
      }
    }
  }
}

/**
 * Frosted Game Panel Card - Glassmorphism Dark Mode
 */
@Composable
fun FrostedGamePanelCard(
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

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = modifier
          .width(320.dp)
          .wrapContentHeight()
          .padding(8.dp),
      shape = RoundedCornerShape(32.dp),
  ) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header dengan Tools di kiri
      FrostedGameHeaderWithTools(
          time = time,
          temp = temp,
          batteryLevel = batteryLevel,
          viewModel = viewModel,
          isFpsEnabled = isFpsEnabled,
          onFpsToggle = onFpsToggle,
          onDrag = onDrag,
          onDragEnd = onDragEnd,
          onCollapse = onCollapse,
      )

      // Brightness Control
      FrostedBrightnessControl(viewModel)

      // Performance Bento
      FrostedPerformanceBento(fps, cpuLoad, gpuLoad)

      // Mode Selector
      FrostedModeSelector(viewModel)
    }
  }
}

@Composable
private fun FrostedGameHeaderWithTools(
    time: String,
    temp: String,
    batteryLevel: Int,
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onCollapse: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Row 1: Time + Status Pills (draggable & clickable to collapse)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
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
          color = Color.White,
          letterSpacing = (-1).sp
      )

      Spacer(modifier = Modifier.weight(1f))

      FrostedStatusPill(icon = Icons.Rounded.Thermostat, text = "$temp°C", isWarning = true)
      Spacer(modifier = Modifier.width(6.dp))
      FrostedStatusPill(icon = Icons.Rounded.BatteryStd, text = "$batteryLevel%")
    }
    
    // Row 2: Scrollable Tools Icons (icon only, no labels)
    FrostedCompactToolsRow(viewModel, isFpsEnabled, onFpsToggle)
  }
}

@Composable
private fun FrostedCompactToolsRow(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
) {
  val dnd by viewModel.doNotDisturb.collectAsStateWithLifecycle()
  val ringerMode by viewModel.ringerMode.collectAsStateWithLifecycle()
  val callMode by viewModel.callMode.collectAsStateWithLifecycle()
  val threeFingerSwipe by viewModel.threeFingerSwipe.collectAsStateWithLifecycle()
  val touchGuard by viewModel.touchGuard.collectAsStateWithLifecycle()

  androidx.compose.foundation.lazy.LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 0.dp),
  ) {
    item {
      FrostedCompactToolButton(
          icon = Icons.Rounded.Speed,
          isActive = isFpsEnabled,
          onClick = { onFpsToggle() }
      )
    }
    item {
        val icon = when(ringerMode) {
             1 -> Icons.Rounded.Vibration
             2 -> Icons.Rounded.NotificationsOff
             else -> Icons.Rounded.Notifications
        }
        FrostedCompactToolButton(
            icon = icon,
            isActive = ringerMode != 0,
            onClick = { viewModel.cycleRingerMode() }
        )
    }
    item {
        val icon = when(callMode) {
             1 -> Icons.Rounded.NotificationsPaused
             2 -> Icons.Rounded.CallEnd
             else -> Icons.Rounded.Call
        }
        FrostedCompactToolButton(
            icon = icon,
            isActive = callMode != 0,
            onClick = { 
                if (callMode == 2) {
                    // If in reject mode, test the call functionality
                    viewModel.testCallFunctionality()
                } else {
                    viewModel.cycleCallMode()
                }
            }
        )
    }
    item {
        FrostedCompactToolButton(
            icon = if (threeFingerSwipe) Icons.Rounded.TouchApp else Icons.Rounded.DoNotDisturb,
            isActive = threeFingerSwipe,
            onClick = { viewModel.toggleThreeFingerSwipe() }
        )
    }
    item {
      FrostedCompactToolButton(
          icon = Icons.Rounded.DoNotDisturb,
          isActive = dnd,
          onClick = { viewModel.setDND(!dnd) }
      )
    }
    item {
        FrostedCompactToolButton(
            icon = if (touchGuard) Icons.Rounded.BackHand else Icons.Rounded.PanTool,
            isActive = touchGuard,
            onClick = { viewModel.setTouchGuard(!touchGuard) }
        )
    }
    item {
      FrostedCompactToolButton(
          icon = Icons.Rounded.RocketLaunch,
          isActive = false,
          onClick = { viewModel.performGameBoost() }
      )
    }
    item {
      FrostedCompactToolButton(
          icon = Icons.Rounded.Screenshot,
          isActive = false,
          onClick = { viewModel.takeScreenshot() }
      )
    }
  }
}

@Composable
private fun FrostedCompactToolButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
  val iconColor = if (isActive) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f)

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.size(40.dp),
      shape = RoundedCornerShape(12.dp),
  ) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
      Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
private fun FrostedStatusPill(icon: ImageVector, text: String, isWarning: Boolean = false) {
  Box(
      modifier = Modifier
          .height(32.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(
              if (isWarning) 
                  Color(0xFFDC2626).copy(alpha = 0.9f) 
              else 
                  Color(0xFF16A34A).copy(alpha = 0.9f)
          )
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp),
    ) {
      Icon(
          icon, 
          null, 
          modifier = Modifier.size(14.dp),
          tint = Color.White
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text, 
          style = MaterialTheme.typography.labelMedium, 
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 13.sp
      )
    }
  }
}

@Composable
private fun FrostedPerformanceBento(fps: String, cpu: Float, gpu: Float) {
  Row(
      modifier = Modifier.fillMaxWidth().height(90.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // FPS Card - Cyan dengan background hitam untuk angka
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.weight(1.3f).fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
    ) {
      Box(
          contentAlignment = Alignment.Center, 
          modifier = Modifier.fillMaxSize()
      ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
          val fpsVal = fps.toFloatOrNull()?.toInt() ?: 60
          // FPS number dengan background hitam solid
          Box(
              modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.Black.copy(alpha = 0.7f))
                  .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            Text(
                text = "$fpsVal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 32.sp,
                fontSize = 36.sp
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          // Label FPS dengan background
          Box(
              modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.6f))
                  .padding(horizontal = 10.dp, vertical = 3.dp)
          ) {
            Text(
                text = "FPS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                fontSize = 13.sp
            )
          }
        }
      }
    }

    // Load Stats - CPU & GPU
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // CPU Card
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(
                  Brush.horizontalGradient(
                      colors = listOf(
                          Color.White.copy(alpha = 0.15f),
                          Color.White.copy(alpha = 0.1f)
                      )
                  )
              ),
          contentAlignment = Alignment.Center
      ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
          Text(
              "CPU", 
              style = MaterialTheme.typography.labelSmall, 
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
              "${cpu.toInt()}%",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Black,
              color = Color(0xFFF472B6),
              fontSize = 16.sp
          )
        }
      }
      
      // GPU Card
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(
                  Brush.horizontalGradient(
                      colors = listOf(
                          Color.White.copy(alpha = 0.15f),
                          Color.White.copy(alpha = 0.1f)
                      )
                  )
              ),
          contentAlignment = Alignment.Center
      ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
          Text(
              "GPU", 
              style = MaterialTheme.typography.labelSmall, 
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
              "${gpu.toInt()}%",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Black,
              color = Color(0xFFA78BFA),
              fontSize = 16.sp
          )
        }
      }
    }
  }
}

@Composable
private fun FrostedModeSelector(viewModel: GameMonitorViewModel) {
  val currentMode by viewModel.currentPerformanceMode.collectAsStateWithLifecycle()
  val modes = listOf(
      "powersave" to "Power",
      "balanced" to "Balance",
      "performance" to "Perform"
  )

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth().height(50.dp),
      shape = RoundedCornerShape(25.dp),
  ) {
    Row(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      modes.forEach { (modeKey, modeLabel) ->
        val isSelected = currentMode == modeKey
        val animatedColor by animateColorAsState(
            if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(21.dp))
                .background(animatedColor)
                .clickable { viewModel.setPerformanceMode(modeKey) },
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = modeLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
              maxLines = 1,
              fontSize = 11.sp
          )
        }
      }
    }
  }
}

@Composable
private fun FrostedBrightnessControl(viewModel: GameMonitorViewModel) {
  val vmBrightness by viewModel.brightness.collectAsStateWithLifecycle()
  var localSliderValue by remember { mutableFloatStateOf(vmBrightness) }
  var isDragging by remember { mutableStateOf(false) }
  val displayValue = if (isDragging) localSliderValue else vmBrightness

  LaunchedEffect(vmBrightness) {
    if (!isDragging) {
      localSliderValue = vmBrightness
    }
  }

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth().height(46.dp),
      shape = RoundedCornerShape(23.dp),
  ) {
    Box(contentAlignment = Alignment.CenterStart) {
      // Track Fill
      Box(
          modifier = Modifier
              .fillMaxWidth(displayValue.coerceIn(0.01f, 1f))
              .fillMaxHeight()
              .background(
                  Brush.horizontalGradient(
                      colors = listOf(
                          Color(0xFFFBBF24).copy(alpha = 0.3f),
                          Color(0xFFF59E0B).copy(alpha = 0.4f)
                      )
                  ),
                  RoundedCornerShape(23.dp)
              )
      )

      // Icons (non-interactive, behind slider)
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            Icons.Rounded.BrightnessLow,
            null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
        Icon(
            Icons.Rounded.BrightnessHigh,
            null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
      }

      // Slider (on top, interactive)
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
          colors = SliderDefaults.colors(
              thumbColor = Color.Transparent,
              activeTrackColor = Color.Transparent,
              inactiveTrackColor = Color.Transparent,
          ),
          modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
