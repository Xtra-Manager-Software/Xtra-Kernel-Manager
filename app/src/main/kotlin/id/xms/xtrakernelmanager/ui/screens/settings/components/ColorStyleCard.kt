package id.xms.xtrakernelmanager.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.lerp as colorLerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.ColorStyle
import id.xms.xtrakernelmanager.ui.theme.XkmSettingsStore
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded

private val AccentPresets = listOf(
    Color(0xFF7C6FF0),
    Color(0xFF00B8D4),
    Color(0xFF26A69A),
    Color(0xFF66BB6A),
    Color(0xFFFFB300),
    Color(0xFFEC407A),
    Color(0xFFEF5350),
    Color(0xFF5C6BC0),
)

@Composable
fun ColorStyleCard(
    settings: XkmSettingsStore,
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
            ColorStyle.entries.forEach { style ->
                SettingsOptionRow(
                    title = style.name.lowercase().replaceFirstChar { c -> c.uppercase() },
                    subtitle = subtitleFor(style),
                    selected = settings.colorStyle == style,
                    onClick = { settings.updateColorStyle(style) },
                    leading = { StyleSwatch(style, settings) },
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = settings.colorStyle == ColorStyle.CUSTOM) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    ) {
                        AccentPresets.forEach { preset ->
                            AccentSwatch(
                                color = preset,
                                selected = settings.customAccent == preset.toArgb(),
                                onClick = { settings.updateCustomAccent(preset) },
                            )
                        }
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Luminance",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${(settings.luminance * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = settings.luminance,
                            onValueChange = { settings.updateLuminance(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun subtitleFor(style: ColorStyle): String = when (style) {
    ColorStyle.MONET -> "Wallpaper-derived Monet palette"
    ColorStyle.AMOLED -> "True black surfaces, battery friendly"
    ColorStyle.VIVID -> "Signature saturated palette"
    ColorStyle.CUSTOM -> "Your accent, your luminance"
}

@Composable
private fun StyleSwatch(
    style: ColorStyle,
    settings: XkmSettingsStore,
) {
    val scheme = MaterialTheme.colorScheme
    val swatchColor = when (style) {
        ColorStyle.MONET -> scheme.primary
        ColorStyle.AMOLED -> Color.Black
        ColorStyle.VIVID -> colorLerp(scheme.secondary, scheme.tertiary, 0.4f)
        ColorStyle.CUSTOM -> Color(settings.customAccent)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(swatchColor)
            .border(
                width = if (style == ColorStyle.AMOLED) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape,
            ),
    )
}

@Composable
private fun AccentSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                },
                shape = CircleShape,
            )
            .padding(4.dp),
    )
}
