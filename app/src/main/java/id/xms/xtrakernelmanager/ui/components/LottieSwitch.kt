package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Replacement for LottieSwitch using Material3 Switch
 * This reduces app size (~2MB) and memory usage (~20-30MB) by removing Lottie dependency
 * 
 * All three variants (LottieSwitch, LottieSwitchSimple, LottieSwitchControlled) now use
 * the same Material3 Switch implementation for consistency and performance.
 */
@Composable
fun LottieSwitchControlled(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 40.dp,
    scale: Float = 2.2f,
    enabled: Boolean = true,
) {
  Box(
      modifier = modifier.width(width).height(height),
      contentAlignment = Alignment.Center,
  ) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF4CAF50),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f),
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
            disabledCheckedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
            disabledUncheckedThumbColor = Color.White.copy(alpha = 0.5f),
            disabledUncheckedTrackColor = Color.Gray.copy(alpha = 0.3f),
        )
    )
  }
}

/**
 * Alias for backward compatibility
 */
@Composable
fun LottieSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 40.dp,
    scale: Float = 2.2f,
    enabled: Boolean = true,
) {
  LottieSwitchControlled(checked, onCheckedChange, modifier, width, height, scale, enabled)
}

/**
 * Alias for backward compatibility
 */
@Composable
fun LottieSwitchSimple(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 40.dp,
    scale: Float = 2.2f,
    enabled: Boolean = true,
) {
  LottieSwitchControlled(checked, onCheckedChange, modifier, width, height, scale, enabled)
}
