package id.xms.xtrakernelmanager.ui.screens.setup.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.bouncyClickable
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded

/**
 * SetupBottomBar featuring smooth top corners, animated step counter,
 * and morphing 360-degree rotating FAB button.
 */
@Composable
fun SetupBottomBar(
    currentPage: Int,
    pageCount: Int,
    onNextClicked: () -> Unit,
    onFinishClicked: () -> Unit,
    isNextEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val morphAnimationSpec = tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing)
    val rotationAnimationSpec = tween<Float>(durationMillis = 900, easing = FastOutSlowInEasing)

    val (targetTopStart, targetTopEnd, targetBottomStart, targetBottomEnd) = when (currentPage % 3) {
        0 -> floatArrayOf(50f, 50f, 50f, 50f)
        1 -> floatArrayOf(26f, 26f, 26f, 26f)
        else -> floatArrayOf(18f, 50f, 18f, 50f)
    }

    val animatedTopStart by animateFloatAsState(targetTopStart, morphAnimationSpec, label = "TopStart")
    val animatedTopEnd by animateFloatAsState(targetTopEnd, morphAnimationSpec, label = "TopEnd")
    val animatedBottomStart by animateFloatAsState(targetBottomStart, morphAnimationSpec, label = "BottomStart")
    val animatedBottomEnd by animateFloatAsState(targetBottomEnd, morphAnimationSpec, label = "BottomEnd")

    val animatedRotation by animateFloatAsState(
        targetValue = currentPage * 360f,
        animationSpec = rotationAnimationSpec,
        label = "Rotation",
    )

    var errorTrigger by remember { mutableStateOf(0L) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(errorTrigger) {
        if (errorTrigger > 0L) {
            for (i in 0 until 2) {
                shakeOffset.animateTo(-10f, tween(40, easing = LinearEasing))
                shakeOffset.animateTo(10f, tween(40, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            kotlinx.coroutines.delay(1000)
            errorTrigger = 0L
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left area: Animated Step Text
            AnimatedContent(
                targetState = currentPage,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut(),
                        )
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut(),
                        )
                    }
                },
                label = "StepTextAnimation",
            ) { targetPage ->
                when (targetPage) {
                    0 -> {
                        Text(
                            text = stringResource(R.string.setup_btn_lets_go),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.setup_step_counter, targetPage, pageCount - 1),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }

            // Right area: Morphing rotating FAB
            val isLastPage = currentPage == pageCount - 1
            val isPrimaryButtonEnabled = isNextEnabled
            val isErrorActive = errorTrigger > 0L

            val containerColor by animateColorAsState(
                targetValue = when {
                    isErrorActive -> MaterialTheme.colorScheme.errorContainer
                    !isPrimaryButtonEnabled -> MaterialTheme.colorScheme.surfaceContainerHighest
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                animationSpec = tween(200),
                label = "fab-container-color",
            )
            val contentColor by animateColorAsState(
                targetValue = when {
                    isErrorActive -> MaterialTheme.colorScheme.onErrorContainer
                    !isPrimaryButtonEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                },
                animationSpec = tween(200),
                label = "fab-content-color",
            )

            val dynamicShape = RoundedCornerShape(
                topStartPercent = animatedTopStart.toInt(),
                topEndPercent = animatedTopEnd.toInt(),
                bottomStartPercent = animatedBottomStart.toInt(),
                bottomEndPercent = animatedBottomEnd.toInt(),
            )

            Surface(
                shape = dynamicShape,
                color = containerColor,
                contentColor = contentColor,
                modifier = Modifier
                    .offset { IntOffset(x = shakeOffset.value.dp.roundToPx(), y = 0) }
                    .size(width = 84.dp, height = 56.dp)
                    .bouncyClickable(onClick = {
                        when {
                            !isPrimaryButtonEnabled -> errorTrigger = System.currentTimeMillis()
                            isLastPage -> onFinishClicked()
                            else -> onNextClicked()
                        }
                    })
                    .graphicsLayer { rotationZ = animatedRotation },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        modifier = Modifier.graphicsLayer { rotationZ = -animatedRotation },
                        targetState = when {
                            isErrorActive -> 2
                            currentPage < pageCount - 1 -> 0
                            else -> 1
                        },
                        transitionSpec = {
                            (scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn())
                                .togetherWith(scaleOut() + fadeOut())
                        },
                        label = "AnimatedFabIcon",
                    ) { iconState ->
                        when (iconState) {
                            2 -> {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.setup_error_permission_required),
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            0 -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = stringResource(R.string.setup_btn_next),
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = stringResource(R.string.setup_btn_finish),
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
