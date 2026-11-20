package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Expressive Circular Wavy Progress Indicator.
 *
 * Implements the official m3.material.io wavy progress indicator specifications:
 * - Sinusoidal wave modulation along the circular perimeter
 * - Configurable frequency and amplitude for fine-tuned rhythm
 * - Track gap separating the active wavy progress from the muted background track
 * - Optional continuous wave ripple animation (e.g., active charging state)
 */
@Composable
fun WavyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = false,
    size: Dp = 88.dp,
    strokeWidth: Dp = 10.dp,
    amplitude: Dp = 4.dp,
    frequency: Int = 7,
    gapSize: Dp = 6.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "wavyCircularProgress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wavyCircularTransition")
    val wavePhase by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "wavePhase",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = with(density) { strokeWidth.toPx() }
            val amplitudePx = with(density) { amplitude.toPx() }
            val gapPx = with(density) { gapSize.toPx() }

            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension - strokePx) / 2f - amplitudePx

            if (baseRadius <= 0f) return@Canvas

            val circumference = (2 * Math.PI * baseRadius).toFloat()
            val totalSeparationPx = strokePx + gapPx
            val gapAngle = ((totalSeparationPx / circumference) * 360f).coerceIn(12f, 32f)

            val sweep = animatedProgress * 360f

            fun buildWavyArc(startAngleDeg: Float, sweepAngleDeg: Float): Path {
                val path = Path()
                if (sweepAngleDeg <= 0.5f) return path

                val steps = (sweepAngleDeg * 1.8f).toInt().coerceAtLeast(4)
                val stepAngle = sweepAngleDeg / steps

                for (i in 0..steps) {
                    val angleDeg = startAngleDeg + (i * stepAngle)
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val waveAngle = Math.toRadians((angleDeg * frequency).toDouble()) + wavePhase
                    val r = baseRadius + amplitudePx * kotlin.math.sin(waveAngle).toFloat()

                    val x = center.x + r * kotlin.math.cos(rad).toFloat()
                    val y = center.y + r * kotlin.math.sin(rad).toFloat()

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                return path
            }

            when {
                animatedProgress >= 0.98f -> {
                    // Full circular wavy ring without gaps
                    val fullPath = buildWavyArc(startAngleDeg = -90f, sweepAngleDeg = 360f)
                    drawPath(
                        path = fullPath,
                        color = indicatorColor,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
                animatedProgress <= 0.03f -> {
                    // Empty ring: track only
                    val fullTrack = buildWavyArc(startAngleDeg = -90f, sweepAngleDeg = 360f)
                    drawPath(
                        path = fullTrack,
                        color = trackColor,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
                else -> {
                    val halfGap = gapAngle / 2f

                    // 1. Active Wavy Indicator Arc
                    val activeSweep = (sweep - gapAngle).coerceAtLeast(1f)
                    val activeStart = -90f + halfGap
                    val activePath = buildWavyArc(startAngleDeg = activeStart, sweepAngleDeg = activeSweep)
                    drawPath(
                        path = activePath,
                        color = indicatorColor,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )

                    // 2. Inactive Background Track Arc
                    val trackSweep = (360f - sweep - gapAngle).coerceAtLeast(1f)
                    val trackStart = -90f + sweep + halfGap
                    val trackPath = buildWavyArc(startAngleDeg = trackStart, sweepAngleDeg = trackSweep)
                    drawPath(
                        path = trackPath,
                        color = trackColor,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Material Design 3 Expressive Linear Wavy Progress Indicator.
 *
 * Implements the horizontal wavy loading track for bars and linear progress.
 */
@Composable
fun WavyLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = false,
    strokeWidth: Dp = 6.dp,
    amplitude: Dp = 3.dp,
    wavelength: Dp = 28.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "wavyLinearProgress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wavyLinearTransition")
    val wavePhase by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "linearWavePhase",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(amplitude * 2 + strokeWidth + 4.dp),
    ) {
        val strokePx = with(density) { strokeWidth.toPx() }
        val amplitudePx = with(density) { amplitude.toPx() }
        val wavelengthPx = with(density) { wavelength.toPx() }

        val midY = size.height / 2f
        val totalWidth = size.width
        val activeWidth = totalWidth * animatedProgress

        // Background track (subtle line or wave)
        if (activeWidth < totalWidth) {
            drawLine(
                color = trackColor,
                start = Offset(activeWidth, midY),
                end = Offset(totalWidth, midY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        }

        // Active wavy path
        if (activeWidth > 0f) {
            val wavyPath = Path().apply {
                moveTo(0f, midY)
                var x = 0f
                val step = 2f
                while (x <= activeWidth) {
                    val angle = ((x / wavelengthPx) * 2 * Math.PI).toFloat() + wavePhase
                    val y = midY + amplitudePx * kotlin.math.sin(angle)
                    lineTo(x, y)
                    x += step
                }
            }
            drawPath(
                path = wavyPath,
                color = indicatorColor,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}
