package id.xms.xtrakernelmanager.ui.screens.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.ThemeMode
import id.xms.xtrakernelmanager.ui.theme.XkmSettingsStore

@Composable
fun ThemeModeCard(
    settings: XkmSettingsStore,
    modifier: Modifier = Modifier,
) {
    val modes = ThemeMode.entries
    val selectedIndex = modes.indexOf(settings.themeMode)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        XkmSegmented(
            options = modes.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = selectedIndex,
            onSelect = { index -> settings.updateThemeMode(modes[index]) },
            modifier = Modifier.padding(16.dp),
        )
    }
}
