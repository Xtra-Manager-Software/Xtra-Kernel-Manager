package id.xms.xtrakernelmanager.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.setup.SetupScreen
import id.xms.xtrakernelmanager.ui.theme.ThemeMode
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme

/**
 * Main application UI host for XKM.
 * Manages screen navigation, global themes, and container states.
 */
@Composable
fun XkmApp() {
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }

    XtraKernelManagerTheme(
        themeMode = themeMode,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            var currentScreen by rememberSaveable { mutableStateOf("setup") }

            Crossfade(
                targetState = currentScreen,
                label = "ScreenCrossfade",
            ) { screen ->
                when (screen) {
                    "setup" -> SetupScreen(
                        themeMode = themeMode,
                        onThemeModeChanged = { themeMode = it },
                        onFinished = { currentScreen = "home" },
                    )
                    else -> HomeScreen(
                        themeMode = themeMode,
                        onThemeModeChanged = { themeMode = it },
                        onNavigateToSetup = { currentScreen = "setup" },
                    )
                }
            }
        }
    }
}
