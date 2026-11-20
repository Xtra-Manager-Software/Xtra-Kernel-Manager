package id.xms.xtrakernelmanager.ui.screens.processor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable metric display tile for CPU cluster telemetry.
 * Clean, borderless Material 3 tonal elevation for natural contrast in both light and dark themes.
 */
@Composable
fun ClusterMetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    isCompact: Boolean = false,
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val containerShape = if (isCompact) RoundedCornerShape(12.dp) else RoundedCornerShape(16.dp)

    val containerColor = if (isCompact) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        modifier = modifier,
        shape = containerShape,
        color = containerColor,
    ) {
        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    maxLines = 1,
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        color = onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}
