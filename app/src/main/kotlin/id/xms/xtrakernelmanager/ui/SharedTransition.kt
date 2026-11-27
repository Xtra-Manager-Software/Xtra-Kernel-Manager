@file:OptIn(ExperimentalSharedTransitionApi::class)

package id.xms.xtrakernelmanager.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Motion utilities following the One UI motion guidelines
 * (developer.samsung.com/one-ui/motion/basic.html).
 */

/** One UI Basic Path Interpolator: [0.22, 0.25, 0.00, 1.00]. */
val OneUiBasicEasing = CubicBezierEasing(0.22f, 0.25f, 0.00f, 1.00f)

/** Shared-element key connecting the Home Display card with the Display detail hero card. */
const val SHARED_KEY_DISPLAY_HERO = "display_hero"

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * One UI "Contact detail view" morph: bounds animate with the One UI Basic Path
 * Interpolator (400 ms) while the differing contents crossfade — outgoing content
 * fades fully first, incoming content follows (One UI crossfade guidance).
 * No-op when the transition scopes are not provided by the current host.
 */
@Composable
fun Modifier.oneUiSharedBounds(key: String): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this

    return with(sharedTransitionScope) {
        this@oneUiSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
            enter = fadeIn(tween(durationMillis = 220, delayMillis = 80, easing = LinearEasing)),
            exit = fadeOut(tween(durationMillis = 120, easing = LinearEasing)),
            boundsTransform = { _, _ ->
                tween(durationMillis = 400, easing = OneUiBasicEasing)
            },
        )
    }
}
