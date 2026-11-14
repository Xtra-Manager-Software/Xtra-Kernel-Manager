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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupBottomBar
import id.xms.xtrakernelmanager.ui.theme.ThemeMode

/**
 * Modular SetupScreen orchestrating setup steps, navigation, and state.
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

    val hasRoot by viewModel.hasRoot.collectAsStateWithLifecycle()
    val isCheckingRoot by viewModel.isCheckingRoot.collectAsStateWithLifecycle()

    BackHandler(enabled = step > 0) {
        step -= 1
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        bottomBar = {
            SetupBottomBar(
                currentPage = step,
                pageCount = totalSteps,
                onNextClicked = { if (step < totalSteps - 1) step += 1 },
                onFinishClicked = onFinished,
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
}
