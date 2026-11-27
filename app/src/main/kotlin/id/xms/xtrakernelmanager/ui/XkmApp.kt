package id.xms.xtrakernelmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.ui.components.XkmBottomBar
import id.xms.xtrakernelmanager.ui.screens.about.AboutScreen
import id.xms.xtrakernelmanager.ui.screens.display.DisplayScreen
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.processor.ProcessorScreen
import id.xms.xtrakernelmanager.ui.screens.setup.SetupScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.theme.ThemeMode
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme

/**
 * Main application UI host for XKM.
 * Manages screen navigation, global themes, and container states.
 */
@Composable
fun XkmApp() {
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
    var currentScreen by rememberSaveable { mutableStateOf("home") }
    var previousScreen by rememberSaveable { mutableStateOf("home") }

    val mainTabs = remember { listOf("home", "tuning", "info", "about") }
    val showBottomBar = currentScreen in mainTabs

    BackHandler(enabled = currentScreen != "home") {
        if (currentScreen in mainTabs) {
            currentScreen = "home"
        } else {
            currentScreen = previousScreen
        }
    }

    XtraKernelManagerTheme(
        themeMode = themeMode,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                        "processor" -> ProcessorScreen(
                            onNavigateBack = { currentScreen = previousScreen },
                        )
                        "display" -> DisplayScreen(
                            onNavigateBack = { currentScreen = previousScreen },
                        )
                        "tuning" -> TuningScreen()
                        "info" -> InfoScreen(
                            onNavigateToProcessor = {
                                previousScreen = "info"
                                currentScreen = "processor"
                            },
                        )
                        "about" -> AboutScreen()
                        else -> HomeScreen(
                            themeMode = themeMode,
                            onThemeModeChanged = { themeMode = it },
                            onNavigateToSetup = {
                                previousScreen = "home"
                                currentScreen = "setup"
                            },
                            onNavigateToProcessor = {
                                previousScreen = "home"
                                currentScreen = "processor"
                            },
                            onNavigateToDisplay = {
                                previousScreen = "home"
                                currentScreen = "display"
                            },
                        )
                    }
                }

                if (showBottomBar) {
                    XkmBottomBar(
                        currentRoute = currentScreen,
                        onSelectRoute = { route ->
                            currentScreen = route
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
