package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupBottomBar

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = viewModel(),
    onSetupComplete: (String) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val hasRoot by viewModel.hasRoot.collectAsState()

    BackHandler(enabled = step > 0) {
        step -= 1
    }

    val pageCount = 4
    val isNextButtonEnabled =
        when (step) {
            1 -> hasRoot
            else -> true
        }

    Scaffold(
        bottomBar = {
            SetupBottomBar(
                currentPage = step,
                pageCount = pageCount,
                onNextClicked = {
                    if (step < pageCount - 1) {
                        step += 1
                    }
                },
                onFinishClicked = { onSetupComplete("material") },
                isNextButtonEnabled = isNextButtonEnabled,
                isFinishButtonEnabled = true,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(400)))
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(400)))
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "SetupStepTransition",
            ) { s ->
                when (s) {
                    0 -> WelcomeContent()
                    1 -> RootCheckContent(viewModel = viewModel, hasRoot = hasRoot)
                    2 -> OverlayContent()
                    3 -> DoneContent()
                }
            }
        }
    }
}
