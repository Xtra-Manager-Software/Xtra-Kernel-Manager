package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.pow
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupBottomBar
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupFinishOverlay
import id.xms.xtrakernelmanager.ui.theme.ThemeMode

/**
 * Modular SetupScreen orchestrating setup steps, navigation, and state.
 *
 * Finishing does not jump straight to Home: the whole screen is first sucked
 * into a black hole (scaled/rotated here via [suckProgress]) while the
 * [SetupFinishOverlay] draws the hole, its shake, and the final explosion.
 */
@Composable
fun SetupScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onFinished: () -> Unit,
    viewModel: SetupViewModel = viewModel(),
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Blackhole finish state: 0 = normal screen, 1 = fully sucked into the hole.
    var finishing by remember { mutableStateOf(false) }
    val suckProgress = remember { mutableFloatStateOf(0f) }

    val hasRoot by viewModel.hasRoot.collectAsStateWithLifecycle()
    val isCheckingRoot by viewModel.isCheckingRoot.collectAsStateWithLifecycle()

    BackHandler(enabled = step > 0 && !finishing) {
        step -= 1
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.graphicsLayer {
                val p = suckProgress.floatValue
                if (p > 0f) {
                    // Spaghettification: tidal pull stretches the screen into a
                    // long thin strand while it collapses into the hole.
                    scaleY = (1f + 3f * p) * (1f - p).pow(0.8f)
                    scaleX = (1f - p).pow(1.2f)
                    rotationZ = 200f * p
                    alpha = 1f - 0.4f * p
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            bottomBar = {
                SetupBottomBar(
                    currentPage = step,
                    pageCount = totalSteps,
                    onNextClicked = { if (step < totalSteps - 1 && !finishing) step += 1 },
                    onFinishClicked = { if (!finishing) finishing = true },
                    isNextEnabled = when (step) {
                        1 -> hasRoot
                        else -> true
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(360, easing = FastOutSlowInEasing)) { width -> width } + fadeIn(animationSpec = tween(360)))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(360, easing = FastOutSlowInEasing)) { width -> -width } + fadeOut(animationSpec = tween(360)),
                                )
                        } else {
                            (slideInHorizontally(animationSpec = tween(360, easing = FastOutSlowInEasing)) { width -> -width } + fadeIn(animationSpec = tween(360)))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(360, easing = FastOutSlowInEasing)) { width -> width } + fadeOut(animationSpec = tween(360)),
                                )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "SetupStepTransition",
                ) { currentStep ->
                    when (currentStep) {
                        0 -> WelcomeContent()
                        1 -> RootCheckContent(
                            hasRoot = hasRoot,
                            isCheckingRoot = isCheckingRoot,
                            onRequestRoot = { viewModel.checkRoot() },
                        )
                        2 -> ThemeSelectionContent(
                            themeMode = themeMode,
                            onThemeModeChanged = onThemeModeChanged,
                        )
                        3 -> DoneContent()
                    }
                }
            }
        }

        // Hole + shake + explosion overlay covering the whole screen, above the
        // scaffold so the hole center matches the suck pivot (screen center).
        if (finishing) {
            SetupFinishOverlay(
                suckProgress = suckProgress,
                accentColor = MaterialTheme.colorScheme.primary,
                onExploded = onFinished,
            )
        }
    }
}
