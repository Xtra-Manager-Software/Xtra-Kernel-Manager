package id.xms.xtrakernelmanager.ui.screens.misc.classic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicBatterySettingsScreen(viewModel: MiscViewModel, onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    // Collect states
    val notifIconType by viewModel.batteryNotifIconType.collectAsState()
    val notifRefreshRate by viewModel.batteryNotifRefreshRate.collectAsState()
    val notifSecureLockScreen by viewModel.batteryNotifSecureLockScreen.collectAsState()
    val notifHighPriority by viewModel.batteryNotifHighPriority.collectAsState()
    val notifForceOnTop by viewModel.batteryNotifForceOnTop.collectAsState()
    val notifDontUpdateScreenOff by viewModel.batteryNotifDontUpdateScreenOff.collectAsState()

    val statsActiveIdle by viewModel.batteryStatsActiveIdle.collectAsState()
    val statsScreen by viewModel.batteryStatsScreen.collectAsState()
    val statsAwakeSleep by viewModel.batteryStatsAwakeSleep.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.battery_settings_title),
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.Background,
                ),
            )
        },
        containerColor = ClassicColors.Background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
        ) {
            // Notification Settings
            ClassicSettingsCategoryHeader(title = stringResource(R.string.battery_notif_category_title))

            ClassicSettingsGroupCard {
                // Master Toggle
                val isNotifEnabled by viewModel.showBatteryNotif.collectAsState(initial = true)
                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_enable_title),
                    description = stringResource(R.string.battery_notif_enable_desc),
                    checked = isNotifEnabled,
                    onCheckedChange = { viewModel.setShowBatteryNotif(it) },
                )
            }

            ClassicSettingsGroupCard {
                ClassicSettingsListPreference(
                    title = stringResource(R.string.battery_notif_icon_type_title),
                    currentValue = notifIconType,
                    entries = mapOf(
                        "battery_icon" to stringResource(R.string.battery_notif_icon_type_app_icon),
                        "circle_percent" to stringResource(R.string.battery_notif_icon_type_circle_percent),
                        "percent_only" to stringResource(R.string.battery_notif_icon_type_percent_only),
                        "temp" to stringResource(R.string.battery_notif_icon_type_temp),
                        "percent_temp" to stringResource(R.string.battery_notif_icon_type_percent_temp),
                        "current" to stringResource(R.string.battery_notif_icon_type_current),
                        "voltage" to stringResource(R.string.battery_notif_icon_type_voltage),
                        "power" to stringResource(R.string.battery_notif_icon_type_power),
                    ),
                    onValueChange = { viewModel.setBatteryNotifIconType(it) },
                )

                ClassicSettingsListPreference(
                    title = stringResource(R.string.battery_notif_refresh_rate_title),
                    currentValue = notifRefreshRate.toString(),
                    entries = mapOf(
                        "500" to "0.5s",
                        "1000" to "1s",
                        "2000" to "2s",
                        "5000" to "5s",
                    ),
                    onValueChange = { viewModel.setBatteryNotifRefreshRate(it.toLong()) },
                )

                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_secure_lock_screen),
                    description = stringResource(R.string.battery_notif_secure_lock_screen_desc),
                    checked = notifSecureLockScreen,
                    onCheckedChange = { viewModel.setBatteryNotifSecureLockScreen(it) },
                )

                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_high_priority),
                    description = stringResource(R.string.battery_notif_high_priority_desc),
                    checked = notifHighPriority,
                    onCheckedChange = { viewModel.setBatteryNotifHighPriority(it) },
                )

                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_force_on_top),
                    description = stringResource(R.string.battery_notif_force_on_top_desc),
                    checked = notifForceOnTop,
                    onCheckedChange = { viewModel.setBatteryNotifForceOnTop(it) },
                )

                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_dont_update_screen_off),
                    description = null,
                    checked = notifDontUpdateScreenOff,
                    onCheckedChange = { viewModel.setBatteryNotifDontUpdateScreenOff(it) },
                )
            }

            // Statistics Settings
            ClassicSettingsCategoryHeader(title = "Statistics settings")

            ClassicSettingsGroupCard {
                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_active_idle),
                    description = "Configure what statistics are displayed",
                    checked = statsActiveIdle,
                    onCheckedChange = { viewModel.setBatteryStatsActiveIdle(it) },
                )
                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_screen),
                    description = null,
                    checked = statsScreen,
                    onCheckedChange = { viewModel.setBatteryStatsScreen(it) },
                )
                ClassicSettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_awake_sleep),
                    description = null,
                    checked = statsAwakeSleep,
                    onCheckedChange = { viewModel.setBatteryStatsAwakeSleep(it) },
                )
            }
        }
    }
}

@Composable
fun ClassicSettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = ClassicColors.Primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp, top = 24.dp),
    )
}

@Composable
fun ClassicSettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
    }
}

@Composable
fun ClassicSettingsSwitchPreference(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ClassicColors.OnSurface
            )
        },
        supportingContent = if (description != null) {
            {
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant
                )
            }
        } else null,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ClassicColors.Primary,
                    checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
                )
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }.fillMaxWidth(),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun ClassicSettingsListPreference(
    title: String,
    currentValue: String,
    entries: Map<String, String>,
    onValueChange: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ClassicColors.OnSurface
            )
        },
        supportingContent = {
            Text(
                text = entries[currentValue] ?: currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.Primary,
            )
        },
        modifier = Modifier.clickable { showDialog = true }.fillMaxWidth(),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )

    if (showDialog) {
        ClassicSettingsListDialog(
            title = title,
            currentValue = currentValue,
            entries = entries,
            onDismiss = { showDialog = false },
            onValueChange = {
                onValueChange(it)
                showDialog = false
            },
        )
    }
}

@Composable
fun ClassicSettingsListDialog(
    title: String,
    currentValue: String,
    entries: Map<String, String>,
    onDismiss: () -> Unit,
    onValueChange: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = ClassicColors.OnSurface
                )

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    entries.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onValueChange(key) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (key == currentValue),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ClassicColors.Primary,
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ClassicColors.OnSurface
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            color = ClassicColors.Primary
                        )
                    }
                }
            }
        }
    }
}
