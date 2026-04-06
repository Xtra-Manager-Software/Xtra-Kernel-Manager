package id.xms.xtrakernelmanager.ui.screens.misc.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel

data class SaturationPreset(
    val name: String, 
    val value: Float, 
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun DisplaySection(viewModel: MiscViewModel) {
  val currentSaturation by viewModel.displaySaturation.collectAsState()
  val isRootAvailable by viewModel.isRootAvailable.collectAsState()
  val applyStatus by viewModel.saturationApplyStatus.collectAsState()

  // Use currentSaturation directly from ViewModel to maintain state
  var sliderValue by remember(currentSaturation) { mutableFloatStateOf(currentSaturation) }

  // Sync slider with ViewModel state
  LaunchedEffect(currentSaturation) {
    sliderValue = currentSaturation
  }

  val presets = remember {
    listOf(
        SaturationPreset("Mono", 0.5f, Icons.Default.Circle, Color(0xFF9E9E9E)),
        SaturationPreset("sRGB", 1.0f, Icons.Default.Palette, Color(0xFF2196F3)),
        SaturationPreset("P3", 1.1f, Icons.Default.ColorLens, Color(0xFF9C27B0)),
        SaturationPreset("Vivid", 1.3f, Icons.Default.AutoAwesome, Color(0xFFFF9F0A)),
        SaturationPreset("Ultra", 1.5f, Icons.Default.Whatshot, Color(0xFFF44336)),
    )
  }

  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.display_settings),
              style = MaterialTheme.typography.titleMedium,
          )
          Text(
              text = stringResource(R.string.display_saturation_desc),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          )
        }
      }

      // Status indicator
      if (applyStatus.isNotEmpty()) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (applyStatus == "Failed" || applyStatus == "Root required")
                            MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                ),
            shape = RoundedCornerShape(8.dp),
        ) {
          Text(
              text = applyStatus,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              style = MaterialTheme.typography.bodySmall,
              color =
                  if (applyStatus == "Failed" || applyStatus == "Root required")
                      MaterialTheme.colorScheme.onErrorContainer
                  else MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }

      // Saturation value display
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = stringResource(R.string.display_saturation_level),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = String.format("%.2f", sliderValue),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
      }

      // Saturation slider
      Slider(
          value = sliderValue,
          onValueChange = { 
            sliderValue = it 
          },
          onValueChangeFinished = { 
            viewModel.setDisplaySaturation(sliderValue)
          },
          valueRange = 0.5f..2.0f,
          steps = 29, // 0.05 increments
          enabled = isRootAvailable,
          colors =
              SliderDefaults.colors(
                  thumbColor = MaterialTheme.colorScheme.primary,
                  activeTrackColor = MaterialTheme.colorScheme.primary,
                  inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
              ),
          modifier = Modifier.fillMaxWidth(),
      )

      // Range labels
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "0.5",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Text(
            text = "1.0 (Default)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Text(
            text = "2.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

      // Quick presets
      Text(
          text = stringResource(R.string.display_quick_presets),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        presets.forEach { preset ->
          val isSelected = kotlin.math.abs(sliderValue - preset.value) < 0.05f

          Box(
              modifier =
                  Modifier.weight(1f)
                      .clip(RoundedCornerShape(14.dp))
                      .background(
                          if (isSelected) preset.color.copy(alpha = 0.2f)
                          else MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f)
                      )
                      .clickable(enabled = isRootAvailable) {
                        sliderValue = preset.value
                        viewModel.setDisplaySaturation(preset.value)
                      }
                      .padding(vertical = 14.dp),
              contentAlignment = Alignment.Center,
          ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                  imageVector = preset.icon,
                  contentDescription = preset.name,
                  modifier = Modifier.size(22.dp),
                  tint = if (isSelected) preset.color else MaterialTheme.colorScheme.onSurface.copy(0.6f)
              )
              Text(
                  text = preset.name,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) preset.color else MaterialTheme.colorScheme.onSurface.copy(0.6f)
              )
            }
          }
        }
      }

      // Root warning if not available
      if (!isRootAvailable) {
        Text(
            text = stringResource(R.string.display_requires_root),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}
