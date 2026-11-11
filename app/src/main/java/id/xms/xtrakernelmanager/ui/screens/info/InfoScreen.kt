package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager

@Composable
fun InfoScreen(
    preferencesManager: PreferencesManager,
    onNavigateToWebView: () -> Unit = {},
    onNavigateToLicense: () -> Unit = {},
    onNavigateToSystemInfo: () -> Unit = {},
    updateViewModel: UpdateViewModel = viewModel()
) {
  val context = LocalContext.current
  val updateState by updateViewModel.updateState.collectAsState()
  
  // Check for updates when screen is first composed
  LaunchedEffect(Unit) {
    updateViewModel.checkForUpdates(context)
  }
  
  MaterialAboutScreen(
      onNavigateToWebView = onNavigateToWebView,
      onNavigateToLicense = onNavigateToLicense,
      onNavigateToSystemInfo = onNavigateToSystemInfo,
      hasUpdate = updateState.hasUpdate
  )
}
