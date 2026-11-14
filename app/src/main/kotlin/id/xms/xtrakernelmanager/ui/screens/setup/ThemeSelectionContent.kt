package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupStepHeader
import id.xms.xtrakernelmanager.ui.theme.ThemeMode

@Composable
fun ThemeSelectionContent(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemTitle = stringResource(R.string.setup_theme_system_title)
    val systemDesc = stringResource(R.string.setup_theme_system_desc)
    val darkTitle = stringResource(R.string.setup_theme_dark_title)
    val darkDesc = stringResource(R.string.setup_theme_dark_desc)
    val lightTitle = stringResource(R.string.setup_theme_light_title)
    val lightDesc = stringResource(R.string.setup_theme_light_desc)

    val options = remember(systemTitle, systemDesc, darkTitle, darkDesc, lightTitle, lightDesc) {
        listOf(
            Triple(ThemeMode.SYSTEM, systemTitle, systemDesc) to Icons.Rounded.BrightnessAuto,
            Triple(ThemeMode.DARK, darkTitle, darkDesc) to Icons.Rounded.DarkMode,
            Triple(ThemeMode.LIGHT, lightTitle, lightDesc) to Icons.Rounded.LightMode,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        SetupStepHeader(
            title = stringResource(R.string.setup_theme_title),
            subtitle = stringResource(R.string.setup_theme_subtitle),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            options.forEach { (meta, icon) ->
                val (mode, title, desc) = meta
                SelectableOptionCard(
                    option = SelectableOption(
                        id = mode.name,
                        title = title,
                        description = desc,
                        icon = icon,
                    ),
                    selected = themeMode == mode,
                    onClick = { onThemeModeChanged(mode) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
