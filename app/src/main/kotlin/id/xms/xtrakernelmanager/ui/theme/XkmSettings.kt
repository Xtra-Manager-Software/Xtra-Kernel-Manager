package id.xms.xtrakernelmanager.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class ColorStyle {
    MONET,
    AMOLED,
    VIVID,
    CUSTOM,
}

enum class NavBarStyle {
    PILL,
    M3,
    TABS,
}

class XkmSettingsStore(
    private val prefs: SharedPreferences,
) {
    var themeMode: ThemeMode by mutableStateOf(
        prefs.loadEnum(KEY_THEME, ThemeMode.SYSTEM),
    )
        private set

    var colorStyle: ColorStyle by mutableStateOf(
        prefs.loadEnum(KEY_COLOR, ColorStyle.MONET),
    )
        private set

    var navBarStyle: NavBarStyle by mutableStateOf(
        prefs.loadEnum(KEY_NAV, NavBarStyle.PILL),
    )
        private set

    var customAccent: Int by mutableIntStateOf(
        prefs.getInt(KEY_ACCENT, DEFAULT_CUSTOM_ACCENT),
    )
        private set

    var luminance: Float by mutableFloatStateOf(
        prefs.getFloat(KEY_LUMINANCE, 0.5f),
    )
        private set

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        persist()
    }

    fun updateColorStyle(value: ColorStyle) {
        colorStyle = value
        persist()
    }

    fun updateNavBarStyle(value: NavBarStyle) {
        navBarStyle = value
        persist()
    }

    fun updateCustomAccent(value: Color) {
        customAccent = value.toArgb()
        persist()
    }

    fun updateLuminance(value: Float) {
        luminance = value.coerceIn(0f, 1f)
        persist()
    }

    private fun persist() {
        prefs.edit()
            .putString(KEY_THEME, themeMode.name)
            .putString(KEY_COLOR, colorStyle.name)
            .putString(KEY_NAV, navBarStyle.name)
            .putInt(KEY_ACCENT, customAccent)
            .putFloat(KEY_LUMINANCE, luminance)
            .apply()
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_COLOR = "color_style"
        private const val KEY_NAV = "nav_bar_style"
        private const val KEY_ACCENT = "custom_accent"
        private const val KEY_LUMINANCE = "luminance"

        const val DEFAULT_CUSTOM_ACCENT: Int = 0xFF7C6FF0.toInt()

        fun fromContext(context: Context): XkmSettingsStore =
            XkmSettingsStore(context.getSharedPreferences("xkm_settings", Context.MODE_PRIVATE))
    }
}

private inline fun <reified T : Enum<T>> SharedPreferences.loadEnum(key: String, fallback: T): T =
    getString(key, null)?.let { stored ->
        runCatching { enumValueOf<T>(stored) }.getOrNull()
    } ?: fallback
