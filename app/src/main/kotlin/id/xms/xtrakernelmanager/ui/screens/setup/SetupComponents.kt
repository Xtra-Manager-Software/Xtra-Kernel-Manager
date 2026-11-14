package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.StatusBadge
import id.xms.xtrakernelmanager.ui.components.StatusTone
import id.xms.xtrakernelmanager.ui.components.bouncyClickable

@Immutable
data class SelectableOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String? = null,
)

@Composable
fun SelectableOptionCard(
    option: SelectableOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = when (selected) {
        true -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        false -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val iconBoxColor = when (selected) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val iconTint = when (selected) {
        true -> MaterialTheme.colorScheme.onPrimary
        false -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = iconBoxColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (option.badge != null) {
                        StatusBadge(
                            label = option.badge,
                            tone = when (selected) {
                                true -> StatusTone.PRIMARY
                                false -> StatusTone.SECONDARY
                            },
                        )
                    }
                }
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                )
            }
        }
    }
}
