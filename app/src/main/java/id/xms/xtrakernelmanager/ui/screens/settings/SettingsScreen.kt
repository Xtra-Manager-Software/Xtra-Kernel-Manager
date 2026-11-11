package id.xms.xtrakernelmanager.ui.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.settings.components.MaterialSettingsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onNavigateToDonation: () -> Unit = {}
) {
    MaterialSettingsContent(
        preferencesManager = preferencesManager,
        currentLayout = "material",
        onNavigateBack = onNavigateBack,
        onNavigateToDonation = onNavigateToDonation
    )
}
