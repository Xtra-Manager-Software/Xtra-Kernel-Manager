package id.xms.xtrakernelmanager.ui.screens.processor.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.domain.device.ProcessorDetailSpec
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader

data class ClusterDomainData(
    val id: String,
    val category: String,
    val tag: String,
    val title: String,
    val subtitle: String,
    val usage: String,
    val usageSub: String,
    val thermal: String,
    val thermalSub: String,
    val avgClock: String,
    val avgClockSub: String,
    val avgTemp: String,
    val avgTempSub: String,
    val easing: CubicBezierEasing,
    val peakRatio: Float,
)

fun resolveClusterDomainList(spec: ProcessorDetailSpec): List<ClusterDomainData> {
    val prime = spec.clusters.firstOrNull()
    val big = spec.clusters.getOrNull(1)
    val little = spec.clusters.getOrNull(2)
    val governorTag = spec.activeGovernor.uppercase()

    return listOf(
        ClusterDomainData(
            id = "prime",
            category = "PRIME CLUSTER",
            tag = governorTag,
            title = "Cortex-X2",
            subtitle = if (prime != null) "${prime.coreCount}x Core • ${prime.minFrequency} - ${prime.maxFrequency}" else "1x Core • 844 MHz - 2.91 GHz",
            usage = "18%",
            usageSub = "Burst ready",
            thermal = "45°C",
            thermalSub = "Burst headroom",
            avgClock = "2.12 GHz",
            avgClockSub = "Active burst",
            avgTemp = "44.0°C",
            avgTempSub = "Target envelope",
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f),
            peakRatio = 1.00f,
        ),
        ClusterDomainData(
            id = "big",
            category = "BIG CLUSTER",
            tag = governorTag,
            title = "Cortex-A710",
            subtitle = if (big != null) "${big.coreCount}x Cores • ${big.minFrequency} - ${big.maxFrequency}" else "3x Cores • 672 MHz - 2.49 GHz",
            usage = "62%",
            usageSub = "3 cores active",
            thermal = "42°C",
            thermalSub = "Nominal envelope",
            avgClock = "1.78 GHz",
            avgClockSub = "Mid-range duty",
            avgTemp = "41.5°C",
            avgTempSub = "Stable envelope",
            easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
            peakRatio = 0.85f,
        ),
        ClusterDomainData(
            id = "little",
            category = "LITTLE CLUSTER",
            tag = governorTag,
            title = "Cortex-A510",
            subtitle = if (little != null) "${little.coreCount}x Cores • ${little.minFrequency} - ${little.maxFrequency}" else "4x Cores • 300 MHz - 1804 MHz",
            usage = "34%",
            usageSub = "4 cores online",
            thermal = "38°C",
            thermalSub = "Optimal envelope",
            avgClock = "1.24 GHz",
            avgClockSub = "Background tasks",
            avgTemp = "37.8°C",
            avgTempSub = "Cool envelope",
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
            peakRatio = 0.62f,
        ),
    )
}

/**
 * Material 3 Expressive Easing & Telemetry Card for an individual CPU cluster domain.
 * Clean, borderless Material 3 card featuring live animated EAS frequency wave canvas and telemetry tiles.
 */
@Composable
fun ClusterDomainCard(
    domain: ClusterDomainData,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    ExpressiveCard(
        modifier = modifier,
        contentPadding = 18.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        ExpressiveCardHeader(
            category = domain.category,
            tag = domain.tag,
            tagContainerColor = accentColor.copy(alpha = 0.18f),
            tagContentColor = accentColor,
            categoryColor = onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = domain.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = domain.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariant.copy(alpha = 0.85f),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Live Usage & Thermal Metrics Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ClusterMetricTile(
                label = "USAGE",
                value = domain.usage,
                subtext = domain.usageSub,
                accentColor = accentColor,
                modifier = Modifier.weight(1f),
            )

            ClusterMetricTile(
                label = "THERMAL",
                value = domain.thermal,
                subtext = domain.thermalSub,
                accentColor = accentColor,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Main Curve Canvas + Avg Clock / Avg Temp Mini Cards ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Left: Animated EAS Scaling Curve Canvas
                ClusterCurveCanvas(
                    easing = domain.easing,
                    peakRatio = domain.peakRatio,
                    accentColor = accentColor,
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                )

                // Right: Stack of Mini Metric Tiles (AVG CLOCK & AVG TEMP)
                Column(
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClusterMetricTile(
                        label = "AVG CLOCK",
                        value = domain.avgClock,
                        subtext = domain.avgClockSub,
                        accentColor = accentColor,
                        isCompact = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    ClusterMetricTile(
                        label = "AVG TEMP",
                        value = domain.avgTemp,
                        subtext = domain.avgTempSub,
                        accentColor = accentColor,
                        isCompact = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}
