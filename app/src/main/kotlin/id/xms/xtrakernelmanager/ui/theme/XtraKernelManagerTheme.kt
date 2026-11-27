package id.xms.xtrakernelmanager.ui.theme

import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
}

@Composable
fun XtraKernelManagerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorStyle: ColorStyle = ColorStyle.MONET,
    customAccent: Color = Color(XkmSettingsStore.DEFAULT_CUSTOM_ACCENT),
    luminance: Float = 0.5f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val base = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) darkColorScheme() else lightColorScheme()
    }

    val colorScheme = when (colorStyle) {
        ColorStyle.MONET -> base
        ColorStyle.AMOLED -> if (isDark) base.toAmoled() else base
        ColorStyle.VIVID -> if (isDark) vividDark() else vividLight()
        ColorStyle.CUSTOM -> base.personalized(customAccent, luminance, isDark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = XkmTypography,
        content = content,
    )
}

private fun ColorScheme.toAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color(0xFF030303),
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainer = Color(0xFF0C0C0C),
    surfaceContainerHigh = Color(0xFF131313),
    surfaceContainerHighest = Color(0xFF1B1B1B),
    surfaceTint = primary,
)

private fun ColorScheme.personalized(accent: Color, luminance: Float, isDark: Boolean): ColorScheme {
    val shifted = accent
        .shiftHue(0f)
        .shiftSaturation(0f)
        .shiftBrightness((luminance - 0.5f) * 0.5f)
    val secondary = accent.shiftSaturation(-0.4f)
    val tertiary = accent.shiftHue(35f)
    val onAccent = if (shifted.luminance() > 0.45f) Color.Black else Color.White
    val onAccentSoft = if (isDark) lerp(shifted, Color.White, 0.82f) else lerp(shifted, Color.Black, 0.82f)

    return copy(
        primary = shifted,
        onPrimary = onAccent,
        primaryContainer = lerp(shifted, background, 0.72f),
        onPrimaryContainer = onAccentSoft,
        secondary = secondary,
        onSecondary = onAccent,
        secondaryContainer = lerp(secondary, background, 0.72f),
        onSecondaryContainer = onAccentSoft,
        tertiary = tertiary,
        onTertiary = onAccent,
        tertiaryContainer = lerp(tertiary, background, 0.72f),
        onTertiaryContainer = onAccentSoft,
        surfaceTint = shifted,
        outline = lerp(outline, shifted, 0.25f),
    )
}

private fun vividDark() = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF002028),
    primaryContainer = Color(0xFF00566B),
    onPrimaryContainer = Color(0xFFB8F6FF),
    secondary = Color(0xFFFF4081),
    onSecondary = Color(0xFF2B0011),
    secondaryContainer = Color(0xFF5C0F2F),
    onSecondaryContainer = Color(0xFFFFB2C8),
    tertiary = Color(0xFFFFC400),
    onTertiary = Color(0xFF2B1F00),
    tertiaryContainer = Color(0xFF5C4700),
    onTertiaryContainer = Color(0xFFFFE7A3),
    background = Color(0xFF07090E),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF07090E),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF151A24),
    onSurfaceVariant = Color(0xFF9AA8BC),
    surfaceContainerLowest = Color(0xFF020304),
    surfaceContainerLow = Color(0xFF0D1118),
    surfaceContainer = Color(0xFF11161E),
    surfaceContainerHigh = Color(0xFF191F29),
    surfaceContainerHighest = Color(0xFF242B37),
    outline = Color(0xFF3A4453),
    error = Color(0xFFFF5449),
)

private fun vividLight() = lightColorScheme(
    primary = Color(0xFF00B8D4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC3F4FF),
    onPrimaryContainer = Color(0xFF00323C),
    secondary = Color(0xFFC51162),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF3E0020),
    tertiary = Color(0xFFFFAB00),
    onTertiary = Color(0xFF2B1F00),
    tertiaryContainer = Color(0xFFFFE28A),
    onTertiaryContainer = Color(0xFF3D2E00),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1A212B),
    surface = Color(0xFFF7F9FC),
    onSurface = Color(0xFF1A212B),
    surfaceVariant = Color(0xFFE7EDF5),
    onSurfaceVariant = Color(0xFF5A6B80),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F5FA),
    surfaceContainer = Color(0xFFF5F7FB),
    surfaceContainerHigh = Color(0xFFECF0F7),
    surfaceContainerHighest = Color(0xFFE3E9F2),
    outline = Color(0xFFB7C2D0),
    error = Color(0xFFBA1A1A),
)

private fun Color.shiftHue(degrees: Float): Color {
    if (degrees == 0f) return this
    val hsv = toHsv()
    hsv[0] = (hsv[0] + degrees + 360f) % 360f
    return hsv.toColor()
}

private fun Color.shiftSaturation(amount: Float): Color {
    if (amount == 0f) return this
    val hsv = toHsv()
    hsv[1] = (hsv[1] + amount).coerceIn(0f, 1f)
    return hsv.toColor()
}

private fun Color.shiftBrightness(amount: Float): Color {
    if (amount == 0f) return this
    val hsv = toHsv()
    hsv[2] = (hsv[2] + amount).coerceIn(0.05f, 1f)
    return hsv.toColor()
}

private fun Color.toHsv(): FloatArray = FloatArray(3).also {
    AndroidColor.colorToHSV(toArgb(), it)
}

private fun FloatArray.toColor(): Color = Color(AndroidColor.HSVToColor(this))
