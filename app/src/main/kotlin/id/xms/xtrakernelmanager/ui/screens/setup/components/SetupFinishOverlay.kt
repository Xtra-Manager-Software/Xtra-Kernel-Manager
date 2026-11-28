package id.xms.xtrakernelmanager.ui.screens.setup.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val SUCK_MS = 1750
private const val HOLD_MS = 1000
private const val BURST_MS = 800
private const val PARTICLE_COUNT = 72

/**
 * A tidal bitmap mesh. Inner vertices move faster than outer vertices:
 * radial distances stretch while tangential distances compress into strands.
 * Rotation increases near the horizon, bending the actual UI pixels into it.
 */
internal class SetupAbsorptionMesh(val width: Float, val height: Float) {
    val columns = 48
    val rows = (columns * height / width).roundToInt().coerceIn(32, 128)
    val vertices = FloatArray((columns + 1) * (rows + 1) * 2)
    private val radii = FloatArray(vertices.size / 2)
    private val angles = FloatArray(radii.size)
    private val fallTimes = FloatArray(radii.size)
    private val maxRadius = hypot(width, height) * 0.5f

    init {
        for (row in 0..rows) {
            for (column in 0..columns) {
                val i = row * (columns + 1) + column
                val x = width * column / columns - width * 0.5f
                val y = height * row / rows - height * 0.5f
                radii[i] = hypot(x, y)
                angles[i] = kotlin.math.atan2(y, x)
                val normalizedRadius = radii[i] / maxRadius
                fallTimes[i] = normalizedRadius * normalizedRadius
            }
        }
        update(0f)
    }

    fun update(progress: Float) {
        val p = progress.coerceIn(0f, 1f)
        // Ease into the pull so the central character has time to stretch
        // visibly before the more distant header and bottom button follow.
        val elapsed = p * p * p
        for (i in radii.indices) {
            val radius = sqrt((fallTimes[i] - elapsed).coerceAtLeast(0f)) * maxRadius
            val contraction = if (radii[i] > 0f) (radius / radii[i]).coerceIn(0f, 1f) else 0f
            val twist = 4.8f * p * (1f - contraction).pow(1.15f)
            val angle = angles[i] + twist
            vertices[i * 2] = width * 0.5f + cos(angle) * radius
            vertices[i * 2 + 1] = height * 0.5f + sin(angle) * radius
        }
    }
}

private class BlackholeParticle(random: Random) {
    val angle = random.nextFloat() * 2f * PI.toFloat()
    val speed = 0.45f + random.nextFloat() * 0.65f
    val size = 1.2f + random.nextFloat() * 2.4f
}

/** The same lensed trajectory is used by the disk and the moving plasma. */
private fun accretionPoint(depth: Float, angle: Float): Offset {
    val front = sin(angle) >= 0f
    val majorRadius = 1.22f + 1.58f * depth
    val minorRadius = 0.07f + 0.21f * depth
    val lensRadius = 1.04f + 0.18f * depth
    val x = majorRadius * cos(angle)
    var y = (minorRadius + if (front) 0.1f else 0f) * sin(angle)
    if (!front && abs(x) < lensRadius) {
        val join = smoothRange(0f, 0.28f, lensRadius - abs(x))
        y -= sqrt((lensRadius * lensRadius - x * x).coerceAtLeast(0f)) * join
    }
    return Offset(x, y)
}

private class AccretionBand(val depth: Float) {
    val back = arc(front = false)
    val front = arc(front = true)

    private fun arc(front: Boolean): Path = Path().apply {
        for (segment in 0..96) {
            val angle = (if (front) 0f else PI.toFloat()) + PI.toFloat() * segment / 96f
            val point = accretionPoint(depth, angle)
            if (segment == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
    }
}

/** Reused paths, with faster inner orbits and real front/back occlusion. */
private class AccretionStream(index: Int) {
    val depth = 0.06f + 0.88f * ((index * 0.618034f) % 1f)
    private val phase = index * 2.399963f
    private val speed = 4.4f + 4.8f * (1f - depth)
    private val trailLength = 0.4f + (index % 4) * 0.18f
    val back = Path()
    val front = Path()
    var head = Offset.Zero
    var headInFront = false

    fun update(seconds: Float, charge: Float) {
        back.reset()
        front.reset()
        var previousFront: Boolean? = null
        val headAngle = phase + seconds * speed + charge * 2.4f
        for (segment in 0..20) {
            val angle = headAngle - trailLength * (1f - segment / 20f)
            val point = accretionPoint(depth, angle)
            val inFront = sin(angle) >= 0f
            val path = if (inFront) front else back
            if (previousFront != inFront) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            previousFront = inFront
        }
        head = accretionPoint(depth, headAngle)
        headInFront = sin(headAngle) >= 0f
    }
}

private class BlackholeScene(accent: Color) {
    val bands = List(28) { AccretionBand(it / 27f) }
    val streams = List(16) { AccretionStream(it) }
    val colors = listOf(
        Color(0xFFFF813B), Color(0xFFFFCD6A), Color(0xFFFFF8DC), Color.White,
        Color(0xFF72EFFF), Color(0xFF6681FF), Color(0xFFEB6FFF), accent,
    )
}

private fun smoothRange(start: Float, end: Float, value: Float): Float {
    val t = ((value - start) / (end - start)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

/** Actual UI pixels fall in, the live disk gathers energy, then releases an outward burst. */
@Composable
fun SetupFinishOverlay(
    setupFrame: Bitmap,
    accentColor: Color,
    onExploded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val latestOnExploded by rememberUpdatedState(onExploded)
    val suckT = remember { mutableFloatStateOf(0f) }
    val holdT = remember { mutableFloatStateOf(0f) }
    val burstT = remember { mutableFloatStateOf(0f) }
    val mesh = remember(setupFrame) {
        SetupAbsorptionMesh(setupFrame.width.toFloat(), setupFrame.height.toFloat())
    }
    val bitmapPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val blackhole = remember(accentColor) { BlackholeScene(accentColor) }
    val particles = remember {
        val random = Random(20241128)
        List(PARTICLE_COUNT) { BlackholeParticle(random) }
    }
    val diskColors = blackhole.colors
    var completed by remember { mutableStateOf(false) }
    var timelineJob by remember { mutableStateOf<Job?>(null) }

    fun complete() {
        if (!completed) {
            completed = true
            latestOnExploded()
        }
    }

    BackHandler { }
    LaunchedEffect(Unit) {
        timelineJob = currentCoroutineContext().job
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        // The mesh supplies the acceleration; a linear clock leaves enough
        // time to see the text, character, and button stretch before swallowing.
        animate(0f, 1f, animationSpec = tween(SUCK_MS, easing = LinearEasing)) { value, _ ->
            suckT.floatValue = value
        }
        animate(0f, 1f, animationSpec = tween(HOLD_MS, easing = LinearEasing)) { value, _ ->
            holdT.floatValue = value
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        animate(0f, 1f, animationSpec = tween(BURST_MS, easing = LinearEasing)) { value, _ ->
            burstT.floatValue = value
        }
        complete()
    }

    Canvas(
        modifier = modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures {
                timelineJob?.cancel()
                complete()
            }
        },
    ) {
        val suck = suckT.floatValue
        val hold = holdT.floatValue
        val burst = burstT.floatValue
        val center = Offset(size.width * 0.5f, size.height * 0.5f)
        val maxR = hypot(size.width, size.height) * 0.5f
        val coreR = minOf(size.width * 0.155f, size.height * 0.1f)
        val holeGrowth = smoothRange(0f, 0.52f, suck)
        val seconds = (SUCK_MS * suck + HOLD_MS * hold + BURST_MS * burst) / 1000f
        val charge = smoothRange(0.52f, 1f, hold)
        val feeding = smoothRange(0.05f, 0.45f, suck) * (1f - 0.25f * hold)

        // Background first, actual pixels second, event horizon last. A scrim
        // over the UI would hide the deformation (the previous fade-only bug).
        drawRect(Color.Black.copy(alpha = smoothRange(0.6f, 0.96f, suck)))
        if (suck < 1f) {
            mesh.update(suck)
            scale(size.width / mesh.width, size.height / mesh.height, pivot = Offset.Zero) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawBitmapMesh(
                        setupFrame, mesh.columns, mesh.rows, mesh.vertices, 0, null, 0, bitmapPaint,
                    )
                }
            }
        }

        if (burst <= 0f && holeGrowth > 0f) {
            drawBlackhole(blackhole, center, coreR * holeGrowth, seconds, feeding, charge)
        } else if (burst > 0f) {
            val remnant = (1f - burst / 0.2f).coerceAtLeast(0f)
            if (remnant > 0f) drawBlackhole(blackhole, center, coreR * remnant, seconds, feeding, 1f)
            val travel = burst * (2f - burst)
            val flashR = coreR + maxR * travel
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = 0.85f * (1f - burst).pow(2)),
                        accentColor.copy(alpha = 0.38f * (1f - burst)),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = flashR,
                ),
                radius = flashR,
                center = center,
            )
            drawCircle(
                color = Color.White.copy(alpha = (1f - burst).pow(2) * 0.8f),
                radius = coreR + maxR * travel * 0.9f,
                center = center,
                style = Stroke(width = (1f + 2f * (1f - burst)).dp.toPx()),
            )
            // A second wave travels along the disk's plane, giving the
            // release depth instead of reducing it to a flat circular flash.
            val planeWave = smoothRange(0.04f, 0.8f, burst)
            val planeR = coreR * 2f + maxR * planeWave
            rotate(13f, pivot = center) {
                drawOval(
                    color = diskColors[1].copy(alpha = 0.72f * (1f - burst).pow(2)),
                    topLeft = center - Offset(planeR, planeR * 0.26f),
                    size = Size(planeR * 2f, planeR * 0.52f),
                    style = Stroke(width = (2.4f * (1f - burst) + 0.5f).dp.toPx()),
                )
            }
            particles.forEachIndexed { index, particle ->
                val direction = Offset(cos(particle.angle), sin(particle.angle))
                val distance = coreR * 0.8f + particle.speed * maxR * travel
                val tail = (distance - (8f + 34f * burst).dp.toPx()).coerceAtLeast(0f)
                drawLine(
                    color = (if (index % 3 == 0) Color.White else diskColors[index % diskColors.size])
                        .copy(alpha = (1f - burst).pow(1.5f)),
                    start = center + direction * tail,
                    end = center + direction * distance,
                    strokeWidth = particle.size.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.drawDiskHalf(
    scene: BlackholeScene,
    front: Boolean,
    seconds: Float,
    feeding: Float,
    charge: Float,
) {
    scene.bands.asReversed().forEachIndexed { index, band ->
        val path = if (front) band.front else band.back
        val baseColor = if (index % 3 == 0) scene.colors[index % scene.colors.size] else Color(0xFFFFE1B8)
        val color = lerp(baseColor, Color.White, charge * 0.8f)
        val ripple = 0.34f + 0.12f * sin(index * 0.85f - seconds * 9f) + 0.15f * charge
        drawPath(path, color.copy(alpha = 0.055f + 0.04f * charge), style = Stroke(width = 0.075f))
        drawPath(path, color.copy(alpha = ripple), style = Stroke(width = 0.014f))
    }
    scene.streams.forEachIndexed { index, stream ->
        val path = if (front) stream.front else stream.back
        val color = lerp(scene.colors[index % scene.colors.size], Color.White, 0.35f + 0.6f * charge)
        drawPath(path, color.copy(alpha = 0.18f + 0.16f * feeding), style = Stroke(width = 0.065f, cap = StrokeCap.Round))
        drawPath(path, color.copy(alpha = 0.86f), style = Stroke(width = 0.019f, cap = StrokeCap.Round))
        if (stream.headInFront == front) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color.copy(alpha = 0.65f), color.copy(alpha = 0.12f), Color.Transparent),
                    center = stream.head,
                    radius = 0.21f + 0.1f * charge,
                ),
                radius = 0.21f + 0.1f * charge,
                center = stream.head,
            )
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 0.016f, center = stream.head)
        }
    }
    // Fresh matter follows curved, inward orbits above and below the disk.
    // It keeps the field alive even during the stationary-center hold.
    repeat(28) { index ->
        val phase = (index * 0.618034f) % 1f
        val age = (seconds * (0.3f + phase * 0.14f) + phase) % 1f
        val angle = phase * 2f * PI.toFloat() + age * 6f + seconds * 0.8f
        if ((sin(angle) >= 0f) == front) {
            val radius = 3.7f - 2.3f * age
            val tailAngle = angle - 0.08f
            val tailR = radius + 0.14f
            val alpha = sin(age * PI.toFloat()) * feeding * 0.62f
            drawLine(
                color = scene.colors[index % scene.colors.size].copy(alpha = alpha),
                start = Offset(cos(tailAngle) * tailR, sin(tailAngle) * tailR * 0.22f),
                end = Offset(cos(angle) * radius, sin(angle) * radius * 0.22f),
                strokeWidth = 0.007f,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.drawBlackhole(
    scene: BlackholeScene,
    center: Offset,
    radius: Float,
    seconds: Float,
    feeding: Float,
    charge: Float,
) {
    if (radius < 0.5f) return
    scene.streams.forEach { it.update(seconds, charge) }
    // The center stays anchored. Motion comes from differential orbits,
    // precession, lensing, and a final inward squeeze before the release.
    val breath = 1f + 0.025f * sin(seconds * 5.4f) + 0.009f * sin(seconds * 11.7f)
    val tilt = 13f + 6f * sin(seconds * 2.1f) - 7f * charge
    val projection = 1f + 0.1f * sin(seconds * 2.7f + 0.5f)
    val diskSqueeze = 1f - 0.24f * charge
    val shadowR = 0.995f + 0.065f * charge
    translate(center.x, center.y) {
        rotate(tilt, pivot = Offset.Zero) {
            scale(radius * breath, radius * breath, pivot = Offset.Zero) {
                scale(diskSqueeze, 0.55f * projection, pivot = Offset.Zero) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD79B).copy(alpha = 0.2f + 0.25f * charge),
                                scene.colors[4].copy(alpha = 0.07f + 0.08f * feeding),
                                Color.Transparent,
                            ),
                            center = Offset.Zero,
                            radius = 3.6f,
                        ),
                        radius = 3.6f,
                        center = Offset.Zero,
                    )
                }
                scale(diskSqueeze, projection * diskSqueeze, pivot = Offset.Zero) {
                    drawDiskHalf(scene, front = false, seconds, feeding, charge)
                }
                drawCircle(Color.Black, radius = shadowR, center = Offset.Zero)
                drawCircle(
                    lerp(Color(0xFF658CFF), Color.White, charge).copy(alpha = 0.72f),
                    radius = shadowR + 0.021f, center = Offset.Zero,
                    style = Stroke(width = 0.032f + 0.022f * charge),
                )
                drawCircle(
                    Color.White.copy(alpha = 0.62f + 0.3f * charge),
                    radius = shadowR + 0.006f, center = Offset.Zero,
                    style = Stroke(width = 0.01f),
                )
                // Light sweeps around the photon rim while the shadow stays dark.
                rotate(seconds * 165f, pivot = Offset.Zero) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, Color.Transparent, Color(0xFF87EFFF), Color.White, Color.Transparent),
                            center = Offset.Zero,
                        ),
                        radius = shadowR + 0.012f,
                        center = Offset.Zero,
                        style = Stroke(width = 0.025f),
                    )
                }
                scale(diskSqueeze, projection * diskSqueeze, pivot = Offset.Zero) {
                    drawDiskHalf(scene, front = true, seconds, feeding, charge)
                }
            }
        }
    }
}
