package id.xms.xtrakernelmanager.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.NavBarStyle

@Composable
fun NavBarStyleCard(
    settings: id.xms.xtrakernelmanager.ui.theme.XkmSettingsStore,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavBarStyle.entries.forEach { style ->
                SettingsOptionRow(
                    title = titleFor(style),
                    subtitle = subtitleFor(style),
                    selected = settings.navBarStyle == style,
                    onClick = { settings.updateNavBarStyle(style) },
                    leading = { NavBarPreview(style, selected = settings.navBarStyle == style) },
                )
            }
        }
    }
}

private fun titleFor(style: NavBarStyle): String = when (style) {
    NavBarStyle.PILL -> "Pill"
    NavBarStyle.M3 -> "Material 3"
    NavBarStyle.TABS -> "Tabs"
}

private fun subtitleFor(style: NavBarStyle): String = when (style) {
    NavBarStyle.PILL -> "Floating pill with expanding labels"
    NavBarStyle.M3 -> "Classic boxed bar, full width"
    NavBarStyle.TABS -> "Underlined tabs with overflow menu"
}

@Composable
private fun NavBarPreview(
    style: NavBarStyle,
    selected: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val chrome = if (selected) scheme.primaryContainer else scheme.surfaceContainerHighest
    val item = if (selected) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .size(width = 96.dp, height = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(chrome),
        contentAlignment = Alignment.Center,
    ) {
        when (style) {
            NavBarStyle.PILL -> {
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(scheme.surfaceContainerLowest.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(item),
                        )
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(scheme.onSurfaceVariant.copy(alpha = 0.35f)),
                            )
                        }
                    }
                }
            }

            NavBarStyle.M3 -> {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(item),
                    )
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(scheme.onSurfaceVariant.copy(alpha = 0.35f)),
                        )
                    }
                }
            }

            NavBarStyle.TABS -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .width(if (index == 0) 16.dp else 12.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (index == 0) item else scheme.onSurfaceVariant.copy(alpha = 0.35f)),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(item),
                    )
                }
            }
        }
    }
}
