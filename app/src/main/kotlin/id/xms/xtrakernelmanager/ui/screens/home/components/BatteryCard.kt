package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.domain.device.BatterySpec
import id.xms.xtrakernelmanager.domain.device.DeviceInfoResolver
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader
import id.xms.xtrakernelmanager.ui.components.ExpressivePill
import id.xms.xtrakernelmanager.ui.components.WavyCircularProgressIndicator

@Composable
fun BatteryCard(
    modifier: Modifier = Modifier,
    spec: BatterySpec = remember { DeviceInfoResolver.resolveBatterySpec() },
    onClick: () -> Unit = {},
) {
    ExpressiveCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        // Top Row: Category overline on left, Temperature pill on right
        ExpressiveCardHeader(
            category = "BATTERY",
            tag = spec.temperature,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Main Row: Left content (Headline status, condition subtitle, capacity pill) + Right (Wavy Circular Gauge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left Column: Status Headline, Condition, Capacity Pill
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = spec.status,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "${spec.health} condition",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Pill: Battery Capacity
                ExpressivePill(
                    text = spec.capacity,
                    isMonospace = true,
                    horizontalPadding = 11.dp,
                    verticalPadding = 5.dp,
                )
            }

            // Right: Material Design 3 Expressive Wavy Progress Indicator with Track Gap
            WavyCircularProgressIndicator(
                progress = spec.level / 100f,
                isAnimated = spec.isCharging,
                size = 88.dp,
                strokeWidth = 10.dp,
                amplitude = 4.dp,
                frequency = 7,
                gapSize = 6.dp,
                indicatorColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            )
        }
    }
}
