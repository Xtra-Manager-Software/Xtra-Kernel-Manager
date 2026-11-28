package id.xms.xtrakernelmanager.ui.screens.setup

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupBottomBar
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupFinishOverlay
import id.xms.xtrakernelmanager.ui.theme.ThemeMode
import kotlinx.coroutines.CancellationException

/**
 * Modular SetupScreen orchestrating setup steps, navigation, and state.
 *
 * Finishing does not jump straight to Home: the final page elements are
 * individually stretched and pulled into the black hole before it bursts.
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

    var finishing by remember { mutableStateOf(false) }
    var finishFrame by remember { mutableStateOf<Bitmap?>(null) }
    val setupLayer = rememberGraphicsLayer()
    val finishCallback by rememberUpdatedState(onFinished)

    LaunchedEffect(finishing) {
        if (finishing) {
            try {
                // Capture once, before hiding anything. The transparent layer
                // contains the real text, GIF frame, pedestal, and finish button.
                finishFrame = setupLayer.toImageBitmap().asAndroidBitmap()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // A failed capture must not trap the user in onboarding.
                Log.w("SetupFinish", "Unable to capture setup finish frame", error)
                finishCallback()
            }
        }
    }

    val hasRoot by viewModel.hasRoot.collectAsStateWithLifecycle()
    val isCheckingRoot by viewModel.isCheckingRoot.collectAsStateWithLifecycle()

    BackHandler(enabled = step > 0 || finishing) {
        if (!finishing) step -= 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Scaffold(
            modifier = Modifier.drawWithContent {
                if (finishFrame == null) {
                    if (step == totalSteps - 1) {
                        setupLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(setupLayer)
                    } else {
                        drawContent()
                    }
                }
            },
            containerColor = Color.Transparent,
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

        // The overlay redraws the captured pixels through a curved mesh, so
        // every visible element is distorted toward the same event horizon.
        finishFrame?.let { frame ->
            SetupFinishOverlay(
                setupFrame = frame,
                accentColor = MaterialTheme.colorScheme.primary,
                onExploded = onFinished,
            )
        }
    }
}
