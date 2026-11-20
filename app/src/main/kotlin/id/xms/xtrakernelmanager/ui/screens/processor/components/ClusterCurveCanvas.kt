package id.xms.xtrakernelmanager.ui.screens.processor.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Animated real-time Canvas surface for rendering CPU cluster Energy Aware Scheduling (EAS) dynamics.
 * Plots living harmonic telemetry waves, glowing live tracking beacon, and filled gradient area.
 */
@Composable
fun ClusterCurveCanvas(
    easing: CubicBezierEasing,
    peakRatio: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    // Real-time animation loop for wave ripple and scanning beacon
    val infiniteTransition = rememberInfiniteTransition(label = "EASCurveTelemetry")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "WavePhase",
    )
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ScanProgress",
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp)) {
            val w = size.width
            val h = size.height

            // Reference dashed grid lines
            val gridColor = outlineVariant.copy(alpha = 0.4f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            drawLine(color = gridColor, start = Offset(0f, h * 0.18f), end = Offset(w, h * 0.18f), pathEffect = dashEffect)
            drawLine(color = gridColor, start = Offset(0f, h * 0.54f), end = Offset(w, h * 0.54f), pathEffect = dashEffect)
            drawLine(color = gridColor, start = Offset(0f, h * 0.88f), end = Offset(w, h * 0.88f), pathEffect = dashEffect)

            // Plot the living animated easing curve path
            val strokePath = Path()
            val fillPath = Path()
            val startX = 0f
            val startY = h * 0.88f
            strokePath.moveTo(startX, startY)
            fillPath.moveTo(startX, startY)

            val steps = 45
            for (i in 1..steps) {
                val t = i / steps.toFloat()
                val baseEased = easing.transform(t)
                // Subtle organic ripple that simulates governor dynamic sampling
                val ripple = sin(t * 7f + phase) * 0.022f * t * (1.1f - t)
                val dynamicEased = (baseEased + ripple).coerceIn(0f, 1.05f)

                val px = t * w
                val py = h * 0.88f - (dynamicEased * (h * 0.70f) * peakRatio)
                strokePath.lineTo(px, py)
                fillPath.lineTo(px, py)
            }

            fillPath.lineTo(w, h * 0.94f)
            fillPath.lineTo(0f, h * 0.94f)
            fillPath.close()

            // Vertical gradient fill under the curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.04f),
                    ),
                    startY = 0f,
                    endY = h,
                ),
            )

            // Primary curve line stroke
            drawPath(
                path = strokePath,
                color = accentColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            // Real-time live tracking beacon traversing the curve
            val beaconBase = easing.transform(scanProgress)
            val beaconRipple = sin(scanProgress * 7f + phase) * 0.022f * scanProgress * (1.1f - scanProgress)
            val beaconEased = (beaconBase + beaconRipple).coerceIn(0f, 1.05f)
            val beaconX = scanProgress * w
            val beaconY = h * 0.88f - (beaconEased * (h * 0.70f) * peakRatio)

            // Scanning laser line to baseline
            drawLine(
                color = accentColor.copy(alpha = 0.22f),
                start = Offset(beaconX, beaconY),
                end = Offset(beaconX, h * 0.88f),
                strokeWidth = 1.2.dp.toPx(),
            )

            // Beacon glowing circles
            drawCircle(
                color = accentColor.copy(alpha = 0.32f),
                radius = 7.dp.toPx(),
                center = Offset(beaconX, beaconY),
            )
            drawCircle(
                color = accentColor,
                radius = 3.5.dp.toPx(),
                center = Offset(beaconX, beaconY),
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(beaconX, beaconY),
            )
        }
    }
}
