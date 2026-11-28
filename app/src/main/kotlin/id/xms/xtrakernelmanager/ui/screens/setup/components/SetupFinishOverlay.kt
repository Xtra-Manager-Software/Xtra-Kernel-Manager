package id.xms.xtrakernelmanager.ui.screens.setup.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/** Timeline phases of the blackhole finish, in milliseconds. */
private const val SUCK_MS = 1200
private const val HOLD_MS = 800
private const val BURST_MS = 700

private const val PARTICLE_COUNT = 56

private class BlackholeParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
)

/**
 * One-time finish transition for the setup flow:
 *
 * 1. Suck  - the setup content (stretched spaghetti-thin by the parent through
 *            [suckProgress]) is pulled into a growing black hole.
 * 2. Hold  - the hole sits still for a beat, drifting slowly.
 * 3. Burst - an expanding flash plus a particle explosion, then [onExploded]
 *            hands control to the Home screen where elements stagger in.
 *
 * Tapping anywhere skips straight to the handover.
 */
@Composable
fun SetupFinishOverlay(
    suckProgress: MutableState<Float>,
    accentColor: Color,
    onExploded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val holdT = remember { mutableFloatStateOf(0f) }
    val burstT = remember { mutableFloatStateOf(0f) }

    var exploded by remember { mutableStateOf(false) }
    var timelineJob by remember { mutableStateOf<Job?>(null) }

    val particles = remember(accentColor) {
        val rnd = Random(20241128)
        List(PARTICLE_COUNT) {
            BlackholeParticle(
                angle = rnd.nextFloat() * 2f * PI.toFloat(),
                speed = 0.35f + rnd.nextFloat() * 0.65f,
                size = 1.5f + rnd.nextFloat() * 3.5f,
                color = when (rnd.nextInt(3)) {
                    0 -> accentColor
                    1 -> Color.White
                    else -> accentColor.copy(alpha = 0.65f)
                },
            )
        }
    }

    fun explode() {
        if (!exploded) {
            exploded = true
            onExploded()
        }
    }

    // Swallow back presses while the finish transition plays.
    BackHandler(enabled = true) { }

    LaunchedEffect(Unit) {
        timelineJob = launch {
            // Phase 1: suck the setup screen into the hole.
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(SUCK_MS, easing = FastOutSlowInEasing),
            ) { value, _ -> suckProgress.value = value }

            // Phase 2: the hole sits still for a moment, drifting slowly.
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(HOLD_MS, easing = LinearEasing),
            ) { value, _ -> holdT.floatValue = value }

            // Phase 3: flash + particle explosion, then hand over to Home.
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(BURST_MS, easing = LinearEasing),
            ) { value, _ -> burstT.floatValue = value }

            explode()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    // Skip: freeze visuals at full intensity and hand over.
                    timelineJob?.cancel()
                    suckProgress.value = 1f
                    holdT.floatValue = 1f
                    burstT.floatValue = 0f
                    explode()
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val suck = suckProgress.value
            val hold = holdT.floatValue
            val burst = burstT.floatValue

            // Fade the whole overlay out during the tail of the burst so the
            // handover to Home is seamless.
            val globalAlpha = if (burst > 0.75f) 1f - ((burst - 0.75f) / 0.25f) else 1f
            if (globalAlpha <= 0f) return@Canvas

            val maxR = hypot(size.width, size.height) * 0.5f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dark scrim so the hole reads against any theme.
            drawRect(color = Color.Black.copy(alpha = 0.94f * globalAlpha))

            if (burst <= 0f) {
                // Gentle breathing while the hole holds still.
                val pulse = 1f + 0.025f * sin(hold * 8f)
                val r = maxR * (0.10f + 0.30f * suck) * pulse

                // Glow halo behind the hole.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f * globalAlpha),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = r * 1.7f,
                    ),
                    radius = r * 1.7f,
                    center = center,
                )

                // The hole itself: pure black with a soft edge.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Black, Color.Transparent),
                        center = center,
                        radius = r,
                    ),
                    radius = r,
                    center = center,
                )

                // Accretion disk: spins in fast while sucking, then drifts slowly.
                val spin = suck * 540f + hold * 140f
                rotate(degrees = spin, pivot = center) {
                    for (i in 0 until 4) {
                        val ringR = r * (1.08f + i * 0.14f)
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    accentColor.copy(
                                        alpha = (0.55f - i * 0.09f).coerceAtLeast(0.1f) * globalAlpha,
                                    ),
                                    Color.Transparent,
                                ),
                                center = center,
                            ),
                            startAngle = i * 95f,
                            sweepAngle = 130f,
                            useCenter = false,
                            style = Stroke(width = (2f + i).dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(center.x - ringR, center.y - ringR),
                            size = Size(ringR * 2f, ringR * 2f),
                        )
                    }
                }
            } else {
                // Expanding flash.
                val flashR = maxR * burst * 1.1f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f * globalAlpha),
                            accentColor.copy(alpha = 0.35f * globalAlpha),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = flashR,
                    ),
                    radius = flashR,
                    center = center,
                )

                // Particle explosion flying outward from the hole.
                particles.forEach { p ->
                    val d = p.speed * maxR * (0.25f + 0.75f * burst)
                    val pos = center + Offset(cos(p.angle) * d, sin(p.angle) * d)
                    val alpha = (1f - burst).coerceIn(0f, 1f) * globalAlpha
                    if (alpha > 0f) {
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size.dp.toPx(),
                            center = pos,
                        )
                    }
                }
            }
        }
    }
}
