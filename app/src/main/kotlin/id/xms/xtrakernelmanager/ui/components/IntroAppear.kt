package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Staggered entrance used by the post-setup "explosion" intro.
 * Each element starts hidden and pops in (fade + rise + slight scale)
 * after [index] * [STAGGER_MS], only when [enabled] is true.
 * When disabled (normal launches) content renders immediately.
 */
@Composable
fun IntroAppear(
    index: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (!enabled) 1f else 0f) }

    LaunchedEffect(enabled) {
        if (enabled && progress.value < 1f) {
            delay(index * STAGGER_MS)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = APPEAR_MS, easing = FastOutSlowInEasing),
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            val p = progress.value
            alpha = p
            translationY = (1f - p) * RISE_PX
            val s = MIN_SCALE + (1f - MIN_SCALE) * p
            scaleX = s
            scaleY = s
        },
    ) {
        content()
    }
}

private const val STAGGER_MS = 110L
private const val APPEAR_MS = 520
private const val RISE_PX = 36f
private const val MIN_SCALE = 0.9f
