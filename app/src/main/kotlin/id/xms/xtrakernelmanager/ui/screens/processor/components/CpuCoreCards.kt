package id.xms.xtrakernelmanager.ui.screens.processor.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.domain.device.CpuClusterSpec
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressivePill
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded

/**
 * Reusable compact card for CPU clusters (used side-by-side for Little and Big cores).
 */
@Composable
fun CompactCpuCoreCard(
    cluster: CpuClusterSpec,
    pillText: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    ExpressiveCard(
        modifier = modifier.fillMaxHeight(),
        contentPadding = 16.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExpressivePill(
                text = pillText,
                containerColor = accentColor.copy(alpha = 0.18f),
                contentColor = accentColor,
                horizontalPadding = 10.dp,
                verticalPadding = 4.dp,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = cluster.maxFrequency.substringBefore(" "),
                style = TextStyle(
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    textGeometricTransform = TextGeometricTransform(scaleX = 1.10f),
                    letterSpacing = (-0.02).em,
                    color = MaterialTheme.colorScheme.onSurface,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
            )
            Text(
                text = "GHz",
                style = TextStyle(
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textGeometricTransform = TextGeometricTransform(scaleX = 1.05f),
                    letterSpacing = (-0.01).em,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${cluster.coreCount}x ${cluster.architecture}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${cluster.minFrequency} - ${cluster.maxFrequency}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )
    }
}

/**
 * Reusable full-width hero card for the Prime CPU core.
 */
@Composable
fun PrimeCpuCoreCard(
    cluster: CpuClusterSpec,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    ExpressiveCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 20.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left side: Giant Hero Speed
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = cluster.maxFrequency.substringBefore(" "),
                    style = TextStyle(
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp,
                        textGeometricTransform = TextGeometricTransform(scaleX = 1.14f),
                        letterSpacing = (-0.02).em,
                        color = accentColor,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                )
                Text(
                    text = "GHz",
                    style = TextStyle(
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textGeometricTransform = TextGeometricTransform(scaleX = 1.05f),
                        letterSpacing = (-0.01).em,
                        color = accentColor.copy(alpha = 0.85f),
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            // Right side: Badge, architecture, and dynamic range
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExpressivePill(
                    text = "PRIME",
                    containerColor = accentColor.copy(alpha = 0.18f),
                    contentColor = accentColor,
                    horizontalPadding = 12.dp,
                    verticalPadding = 4.dp,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${cluster.coreCount}x ${cluster.architecture}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "${cluster.minFrequency} - ${cluster.maxFrequency}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                )
            }
        }
    }
}
