package id.xms.xtrakernelmanager.ui.screens.display.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.domain.device.DisplayDetailSpec
import id.xms.xtrakernelmanager.ui.SHARED_KEY_DISPLAY_HERO
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader
import id.xms.xtrakernelmanager.ui.components.ExpressivePill
import id.xms.xtrakernelmanager.ui.oneUiSharedBounds

/**
 * Hero card for the display detail screen.
 * Mirrors the Home DisplayCard layout: phone silhouette on the left,
 * panel summary with expressive header, headline, and metric pills on the right.
 *
 * [collapseProgress] (0f..1f) is driven by the same scroll offset that collapses
 * the screen header, so the card shrinks in sync with the header instead of
 * scrolling away rigidly (One UI "Contact detail view / Scroll" nuance).
 * Read via graphicsLayer (deferred) so it tracks the scroll frame-per-frame
 * without recomposition.
 */
@Composable
fun HeroPanelCard(
    spec: DisplayDetailSpec,
    collapseProgress: Float = 0f,
    modifier: Modifier = Modifier,
) {
    ExpressiveCard(
        modifier = modifier
            .graphicsLayer {
                val scale = 1f - 0.10f * collapseProgress
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0f)
            }
            .oneUiSharedBounds(SHARED_KEY_DISPLAY_HERO),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Full Body Phone Mockup
            Box(
                contentAlignment = Alignment.Center,
            ) {
                DeviceSilhouette(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    showWallpaper = true,
                    fullBody = true,
                    phoneWidth = 66.dp,
                    phoneHeight = 128.dp,
                    cornerRadius = 14.dp,
                )
            }

            // Right: Panel summary
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Top Row: Category tag on left, GPU tag pill on right (ADRENO)
                ExpressiveCardHeader(
                    category = "DISPLAY",
                    tag = spec.gpuPillText,
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Headline: Active resolution (1080 x 2400)
                Text(
                    text = spec.resolutionText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Subtitle: Panel class (FHD+ AMOLED • 20:9)
                Text(
                    text = "${spec.resolutionClass} ${spec.panelTechnology} • ${spec.aspectRatioText}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Pills: Active refresh rate & diagonal size
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExpressivePill(
                        text = spec.activeRefreshRateText,
                        isMonospace = true,
                        horizontalPadding = 11.dp,
                        verticalPadding = 5.dp,
                    )
                    ExpressivePill(
                        text = spec.screenDiagonalText,
                        isMonospace = true,
                        horizontalPadding = 11.dp,
                        verticalPadding = 5.dp,
                    )
                }
            }
        }
    }
}
