package id.xms.xtrakernelmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import id.xms.xtrakernelmanager.ui.components.XkmBottomBar
import id.xms.xtrakernelmanager.ui.screens.about.AboutScreen
import id.xms.xtrakernelmanager.ui.screens.display.DisplayScreen
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.processor.ProcessorScreen
import id.xms.xtrakernelmanager.ui.screens.settings.SettingsScreen
import id.xms.xtrakernelmanager.ui.screens.setup.SetupScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import id.xms.xtrakernelmanager.ui.theme.XkmSettingsStore

/**
 * Main application UI host for XKM.
 * Manages screen navigation, global themes, and container states.
 * Screen changes run through a SharedTransitionLayout so detail screens can
 * morph from their Home cards following the One UI "Contact detail view" motion.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun XkmApp() {
    val context = LocalContext.current
    val settings = remember { XkmSettingsStore.fromContext(context) }
    // On a fresh install (no persisted state yet) the setup/splash flow runs first;
    // after it is completed once, launch goes straight to Home.
    var currentScreen by rememberSaveable {
        mutableStateOf(if (settings.setupCompleted) "home" else "setup")
    }
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
        themeMode = settings.themeMode,
        colorStyle = settings.colorStyle,
        customAccent = Color(settings.customAccent),
        luminance = settings.luminance,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                // One UI crossfade rule: incoming content starts fading
                                // in before the outgoing content finishes. Linear for fades.
                                fadeIn(
                                    tween(durationMillis = 260, delayMillis = 60, easing = LinearEasing),
                                ) togetherWith fadeOut(
                                    tween(durationMillis = 150, easing = LinearEasing),
                                )
                            },
                            label = "ScreenTransition",
                        ) { screen ->
                            CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                                when (screen) {
                                    "setup" -> SetupScreen(
                                        themeMode = settings.themeMode,
                                        onThemeModeChanged = { settings.updateThemeMode(it) },
                                        onFinished = {
                                            settings.markSetupCompleted()
                                            currentScreen = "home"
                                        },
                                    )
                                    "settings" -> SettingsScreen(
                                        settings = settings,
                                        onNavigateBack = { currentScreen = previousScreen },
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
                                        themeMode = settings.themeMode,
                                        onThemeModeChanged = { settings.updateThemeMode(it) },
                                        onNavigateToSetup = {
                                            previousScreen = "home"
                                            currentScreen = "setup"
                                        },
                                        onNavigateToSettings = {
                                            previousScreen = "home"
                                            currentScreen = "settings"
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
                        }
                    }
                }

                if (showBottomBar) {
                    XkmBottomBar(
                        currentRoute = currentScreen,
                        navBarStyle = settings.navBarStyle,
                        onSelectRoute = { route ->
                            currentScreen = route
                        },
                        onOverflowClick = {
                            previousScreen = currentScreen
                            currentScreen = "settings"
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
