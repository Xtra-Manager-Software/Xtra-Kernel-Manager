package id.xms.xtrakernelmanager.ui.screens.display.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.domain.device.DisplayModeSpec
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.StatusBadge
import id.xms.xtrakernelmanager.ui.components.StatusTone

/**
 * Stacked list of supported display modes (resolution @ refresh rate).
 * The currently active mode is highlighted with an ACTIVE status badge.
 */
@Composable
fun DisplayModesCard(
    modes: List<DisplayModeSpec>,
    modifier: Modifier = Modifier,
) {
    ExpressiveCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            modes.forEach { mode ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "${mode.resolutionText} @ ${mode.refreshRateText}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (mode.isActive) {
                            Text(
                                text = "Currently active mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (mode.isActive) {
                        StatusBadge(
                            label = "ACTIVE",
                            tone = StatusTone.PRIMARY,
                        )
                    }
                }
            }
        }
    }
}
