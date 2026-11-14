package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.components.ExpressiveList
import id.xms.xtrakernelmanager.ui.components.SettingsGroupCard
import id.xms.xtrakernelmanager.ui.components.SettingsGroupItem
import id.xms.xtrakernelmanager.ui.components.StatusBadge
import id.xms.xtrakernelmanager.ui.components.StatusTone
import id.xms.xtrakernelmanager.ui.components.bouncyClickable
import id.xms.xtrakernelmanager.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onNavigateToSetup: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Xtra Kernel Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.SYSTEM
                            }
                            onThemeModeChanged(nextMode)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .bouncyClickable {
                                val nextMode = when (themeMode) {
                                    ThemeMode.SYSTEM -> ThemeMode.DARK
                                    ThemeMode.DARK -> ThemeMode.LIGHT
                                    ThemeMode.LIGHT -> ThemeMode.SYSTEM
                                }
                                onThemeModeChanged(nextMode)
                            },
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                                ThemeMode.DARK -> Icons.Rounded.DarkMode
                                ThemeMode.LIGHT -> Icons.Rounded.LightMode
                            },
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToSetup,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .bouncyClickable(onClick = onNavigateToSetup),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Setup",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Hero Device Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable {},
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "POCO F5",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        StatusBadge(label = "TARO KERNEL", tone = StatusTone.PRIMARY)
                    }

                    Text(
                        text = "Linux 5.10.269-Chandelier • Snapdragon 7+ Gen 2 (SM7475)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Stacking Telemetry Cards
            Text(
                text = "Live Telemetry",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )

            val telemetryItems = listOf(
                TelemetryData(Icons.Rounded.Memory, "CPU Clocks", "2918 MHz (Peak)", "walt", StatusTone.PRIMARY),
                TelemetryData(Icons.Rounded.DeveloperBoard, "GPU Frequency", "220 MHz", "Load 16%", StatusTone.SECONDARY),
                TelemetryData(Icons.Rounded.Thermostat, "Thermal Core", "38.5°C", "NORMAL", StatusTone.SUCCESS),
                TelemetryData(Icons.Rounded.Storage, "Memory Usage", "5.8 GB / 11.2 GB", "52% RAM", StatusTone.PRIMARY),
            )

            ExpressiveList(
                count = telemetryItems.size,
                modifier = Modifier.fillMaxWidth(),
                outerCorner = 24.dp,
                innerCorner = 6.dp,
                gap = 3.dp,
            ) { index ->
                val item = telemetryItems[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    StatusBadge(label = item.badge, tone = item.badgeTone)
                }
            }

            Text(
                text = "Tuning Sections",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )

            SettingsGroupCard {
                SettingsGroupItem(
                    icon = Icons.Rounded.Speed,
                    title = "CPU Governor & Clusters",
                    subtitle = "Frequency locks, energy models, and cluster governor",
                    badgeText = "walt",
                    onClick = {},
                )
                SettingsGroupItem(
                    icon = Icons.Rounded.DeveloperBoard,
                    title = "GPU & Render Pipeline",
                    subtitle = "Power level tables and Vulkan/OpenCL caps",
                    onClick = {},
                )
                SettingsGroupItem(
                    icon = Icons.Rounded.Thermostat,
                    title = "Thermal Control Policies",
                    subtitle = "Temperature mitigation and throttle indexes",
                    onClick = {},
                )
                SettingsGroupItem(
                    icon = Icons.Rounded.Storage,
                    title = "Memory & ZRAM Compression",
                    subtitle = "Swap size, swappiness, and dirty ratios",
                    onClick = {},
                )
                SettingsGroupItem(
                    icon = Icons.Rounded.Palette,
                    title = "Appearance & Theme",
                    subtitle = when (themeMode) {
                        ThemeMode.SYSTEM -> "Follow System (Auto Sync active)"
                        ThemeMode.DARK -> "Dark Theme enabled"
                        ThemeMode.LIGHT -> "Light Theme enabled"
                    },
                    badgeText = when (themeMode) {
                        ThemeMode.SYSTEM -> "AUTO"
                        ThemeMode.DARK -> "DARK"
                        ThemeMode.LIGHT -> "LIGHT"
                    },
                    showDivider = false,
                    onClick = {
                        val nextMode = when (themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.SYSTEM
                        }
                        onThemeModeChanged(nextMode)
                    },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private data class TelemetryData(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val badge: String,
    val badgeTone: StatusTone,
)
