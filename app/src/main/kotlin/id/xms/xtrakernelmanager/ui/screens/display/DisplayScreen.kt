package id.xms.xtrakernelmanager.ui.screens.display

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.HdrOn
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.domain.device.DeviceInfoResolver
import id.xms.xtrakernelmanager.domain.device.DisplayDetailSpec
import id.xms.xtrakernelmanager.ui.components.ExpressiveInfoCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveSectionHeader
import id.xms.xtrakernelmanager.ui.screens.display.components.DisplayMetricTile
import id.xms.xtrakernelmanager.ui.screens.display.components.DisplayModesCard
import id.xms.xtrakernelmanager.ui.screens.display.components.HeroPanelCard

@Composable
fun DisplayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    spec: DisplayDetailSpec = remember { DeviceInfoResolver.resolveDisplayDetailSpec() },
) {
    BackHandler(enabled = true) {
        onNavigateBack()
    }

    // One UI "Scroll" pattern: the pinned header collapses smoothly with the
    // scroll position — padding tightens, the title scales down, and the
    // subtitle folds away — then expands back when the list returns to top.
    val listState = rememberLazyListState()
    val headerCollapseProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 140f).coerceIn(0f, 1f)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        // --- 1. TOP PINNED HEADER AREA ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 20.dp,
                    top = lerp(14.dp, 6.dp, headerCollapseProgress),
                    bottom = lerp(14.dp, 6.dp, headerCollapseProgress),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = onNavigateBack,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Display",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1f - 0.12f * headerCollapseProgress
                        scaleY = 1f - 0.12f * headerCollapseProgress
                        transformOrigin = TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
                    },
                )
                Box(
                    modifier = Modifier
                        .height(lerp(20.dp, 0.dp, headerCollapseProgress))
                        .clipToBounds(),
                ) {
                    Text(
                        text = "Panel & Graphics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            alpha = 1f - headerCollapseProgress
                        },
                    )
                }
            }
        }

        // --- 2. FOREGROUND SHEET ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = WindowInsets.navigationBars.asPaddingValues().let { navInsets ->
                    PaddingValues(
                        top = 16.dp,
                        bottom = 32.dp + navInsets.calculateBottomPadding(),
                    )
                },
            ) {
                // 1. Reusable Hero Panel Card — collapses in sync with the header
                item(key = "hero_panel_card") {
                    HeroPanelCard(
                        spec = spec,
                        collapseProgress = headerCollapseProgress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // 2. Reusable Metric Tiles: Refresh Rate & Pixel Density
                item(key = "metric_tiles_row_one") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DisplayMetricTile(
                            label = "Refresh Rate",
                            value = spec.activeRefreshRateText,
                            subtext = "Active mode",
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        DisplayMetricTile(
                            label = "Pixel Density",
                            value = spec.ppiText,
                            subtext = spec.densityDpiText,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // 3. Reusable Metric Tiles: Brightness
                item(key = "metric_tiles_row_two") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DisplayMetricTile(
                            label = "Brightness",
                            value = spec.brightnessLevelText,
                            subtext = "Current level",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                        DisplayMetricTile(
                            label = "Peak Brightness",
                            value = spec.peakBrightnessText,
                            subtext = "High Brightness Mode",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // 4. Section Header: Display Modes
                item(key = "modes_section_header") {
                    ExpressiveSectionHeader(
                        title = "Display Modes",
                        subtitle = "All resolution & refresh rate combinations exposed by the panel driver.",
                    )
                }

                // 5. Reusable Supported Modes Card
                item(key = "modes_card") {
                    DisplayModesCard(
                        modes = spec.modes,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // 6. Section Header: Color & HDR
                item(key = "color_section_header") {
                    ExpressiveSectionHeader(
                        title = "Color & HDR",
                        subtitle = "Colorimetry and dynamic range capabilities.",
                    )
                }

                // 7. Color & HDR Info Rows
                item(key = "color_rows") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ExpressiveInfoCard(
                            title = "HDR Capabilities",
                            subtitle = spec.hdrTypes.joinToString(", "),
                            icon = Icons.Rounded.HdrOn,
                        )
                        ExpressiveInfoCard(
                            title = "Wide Color Gamut",
                            value = spec.wideColorGamutText,
                            icon = Icons.Rounded.Palette,
                        )
                        ExpressiveInfoCard(
                            title = "Color Depth",
                            value = spec.colorDepthText,
                            icon = Icons.Rounded.Contrast,
                        )
                    }
                }

                // 8. Section Header: Panel & Link
                item(key = "panel_section_header") {
                    ExpressiveSectionHeader(
                        title = "Panel & Link",
                        subtitle = "Physical panel identity and display pipeline state.",
                    )
                }

                // 9. Panel & Link Info Rows
                item(key = "panel_rows") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ExpressiveInfoCard(
                            title = "Panel Technology",
                            value = spec.panelTechnology,
                            icon = Icons.Rounded.Smartphone,
                        )
                        ExpressiveInfoCard(
                            title = "Display Connector",
                            subtitle = spec.connectorText,
                            value = spec.connectionStatus,
                            icon = Icons.Rounded.Cable,
                        )
                        ExpressiveInfoCard(
                            title = "Screen Size",
                            subtitle = spec.physicalSizeText,
                            icon = Icons.Rounded.Straighten,
                        )
                    }
                }
            }
        }
    }
}
