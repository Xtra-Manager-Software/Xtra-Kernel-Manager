package id.xms.xtrakernelmanager.ui.screens.home.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.domain.device.DisplaySpec
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader
import id.xms.xtrakernelmanager.ui.components.ExpressivePill

@Composable
fun DisplayCard(
    modifier: Modifier = Modifier,
    spec: DisplaySpec = remember { DisplaySpec() },
    onClick: () -> Unit = {},
) {
    ExpressiveCard(
        modifier = modifier,
        onClick = onClick,
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

            // Right: Content with uniform expressive typography
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

                // Headline: Refresh Rate (120 Hz)
                Text(
                    text = spec.refreshRateText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Subtitle: PPI Density
                Text(
                    text = "${spec.ppiText} Density",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Pill: Screen Physical Dimensions
                ExpressivePill(
                    text = spec.physicalSizeText,
                    isMonospace = true,
                    horizontalPadding = 11.dp,
                    verticalPadding = 5.dp,
                )
            }
        }
    }
}
