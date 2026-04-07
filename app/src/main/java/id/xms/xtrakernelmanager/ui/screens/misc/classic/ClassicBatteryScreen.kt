package id.xms.xtrakernelmanager.ui.screens.misc.classic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import id.xms.xtrakernelmanager.data.model.AppBatteryStats
import id.xms.xtrakernelmanager.data.model.BatteryUsageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicBatteryScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onGraphClick: () -> Unit = {},
    onCurrentSessionClick: () -> Unit = {},
) {
    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.battery_monitor_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = ClassicColors.OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings),
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. History Chart Card
            item { ClassicHistoryChartCard(viewModel, onCurrentSessionClick) }

            // 2. Current & Session Cards (Row)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ClassicBatteryCapacityCard(viewModel)
                        ClassicElectricCurrentCard(viewModel, onGraphClick)
                    }

                    val screenOnTime by viewModel.screenOnTime.collectAsState()
                    val screenOffTime by viewModel.screenOffTime.collectAsState()
                    val deepSleepTime by viewModel.deepSleepTime.collectAsState()
                    val batteryInfo by viewModel.batteryInfo.collectAsState()

                    Box(modifier = Modifier.weight(1f)) {
                        ClassicCurrentSessionCard(
                            onClick = onCurrentSessionClick,
                            screenOnTime = screenOnTime,
                            screenOffTime = screenOffTime,
                            deepSleepTime = deepSleepTime,
                            chargedInfo = "${batteryInfo.level}% • ${if (batteryInfo.currentNow > 0) "+ " else if (batteryInfo.currentNow < 0) "- " else ""}${kotlin.math.abs(batteryInfo.currentNow)} mA",
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 3. App Battery Usage List
            item { ClassicAppBatteryUsageList(viewModel) }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ClassicHistoryChartCard(viewModel: MiscViewModel, onCurrentSessionClick: () -> Unit = {}) {
    val state by id.xms.xtrakernelmanager.data.repository.HistoryRepository.hourlyStats.collectAsState()
    var showScreenOn by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header with Filter Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface,
                    )
                    Text(
                        text = state.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurfaceVariant,
                    )
                }

                // Toggle Pill
                Surface(
                    color = ClassicColors.SurfaceContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ClassicColors.OnSurface.copy(alpha = 0.1f)),
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (showScreenOn) ClassicColors.Good else ClassicColors.SurfaceContainer)
                                .clickable { showScreenOn = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.screen_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (showScreenOn) ClassicColors.OnSurface else ClassicColors.OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (!showScreenOn) ClassicColors.Accent else ClassicColors.SurfaceContainer)
                                .clickable { showScreenOn = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.drain_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!showScreenOn) ClassicColors.OnSurface else ClassicColors.OnSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bar Chart
            val buckets = state.buckets
            val maxVal = if (showScreenOn) 60f else buckets.maxOfOrNull { it.drainPercent }?.toFloat()?.coerceAtLeast(10f) ?: 10f

            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                val primaryColor = if (showScreenOn) ClassicColors.Good else ClassicColors.Accent

                buckets.forEachIndexed { index, bucket ->
                    val value = if (showScreenOn) (bucket.screenOnMs / 60000f) else bucket.drainPercent.toFloat()
                    val heightPercent = (value / maxVal).coerceIn(0.05f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight(heightPercent)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (value > 0) primaryColor else ClassicColors.SurfaceContainer)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (index % 4 == 0) {
                            Text(
                                text = "%02d".format(index),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ClassicColors.OnSurfaceVariant,
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalStr = if (showScreenOn) {
                val totalMs = buckets.sumOf { it.screenOnMs }
                formatDuration(totalMs)
            } else {
                "${buckets.sumOf { it.drainPercent }}%"
            }

            Text(
                text = "${stringResource(R.string.total_today)}: $totalStr",
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(ClassicColors.OnSurface.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Session Stats Card
            val sessionState by id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()
            val activeDrain = "%.2f".format(sessionState.activeDrainRate)
            val idleDrain = "%.2f".format(sessionState.idleDrainRate)
            val screenOnStr = formatDuration(sessionState.screenOnTime)

            Card(
                onClick = onCurrentSessionClick,
                colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainer),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ClassicSummaryStat(stringResource(R.string.session_active), "$activeDrain%/h")
                    VerticalDivider(
                        modifier = Modifier.height(32.dp),
                        thickness = 1.dp,
                        color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    )
                    ClassicSummaryStat(stringResource(R.string.session_idle), "$idleDrain%/h")
                    VerticalDivider(
                        modifier = Modifier.height(32.dp),
                        thickness = 1.dp,
                        color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    )
                    ClassicSummaryStat(stringResource(R.string.session_duration), screenOnStr)
                }
            }
        }
    }
}

@Composable
fun ClassicElectricCurrentCard(viewModel: MiscViewModel, onClick: () -> Unit = {}) {
    val state by id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()
    val currentMa = state.currentNow
    val voltageMv = state.voltage
    val watts = (kotlin.math.abs(currentMa) / 1000f) * (voltageMv / 1000f)

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.electric_current_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${if (currentMa > 0) "+ " else if (currentMa < 0) "- " else ""}${kotlin.math.abs(currentMa)} mA",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                fontWeight = FontWeight.Medium,
                color = if (currentMa > 0) ClassicColors.Good else if (currentMa < 0) ClassicColors.Accent else ClassicColors.OnSurface,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "%.1f W • %d mV".format(watts, voltageMv),
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurfaceVariant,
            )
        }
    }
}

@Composable
fun ClassicBatteryCapacityCard(viewModel: MiscViewModel) {
    val state by id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()
    val design = if (state.totalCapacity > 0) state.totalCapacity else 5000
    val current = if (state.currentCapacity > 0) state.currentCapacity else 4500
    val healthPercent = ((current.toFloat() / design.toFloat()) * 100).toInt().coerceIn(0, 100)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
        modifier = Modifier.fillMaxWidth().height(84.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.battery_health_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = ClassicColors.OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$healthPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                )
            }
        }
    }
}

@Composable
fun ClassicCurrentSessionCard(
    onClick: () -> Unit = {},
    screenOnTime: String = "--",
    screenOffTime: String = "--",
    deepSleepTime: String = "--",
    chargedInfo: String = "0% • 0 mAh",
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
        modifier = Modifier.fillMaxWidth().height(260.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.current_session_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ClassicStatRowCompact(Icons.Rounded.WbSunny, stringResource(R.string.screen_on), screenOnTime)
            Spacer(modifier = Modifier.height(16.dp))
            ClassicStatRowCompact(Icons.Rounded.NightsStay, stringResource(R.string.screen_off), screenOffTime)
            Spacer(modifier = Modifier.height(16.dp))
            ClassicStatRowCompact(Icons.Rounded.Bedtime, stringResource(R.string.deep_sleep), deepSleepTime)
            Spacer(modifier = Modifier.height(16.dp))
            ClassicStatRowCompact(Icons.Rounded.BatteryStd, stringResource(R.string.charged), chargedInfo)
        }
    }
}

@Composable
fun ClassicStatRowCompact(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ClassicColors.Primary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = ClassicColors.OnSurfaceVariant,
            )
        }
    }
}

@Composable
fun ClassicSummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = ClassicColors.OnSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = ClassicColors.OnSurfaceVariant,
        )
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) "%dh %02dm".format(hours, minutes % 60)
    else "%dm %02ds".format(minutes, seconds % 60)
}

@Composable
fun ClassicAppBatteryUsageList(viewModel: MiscViewModel) {
    val appUsageList by viewModel.appBatteryUsage.collectAsState()
    val isLoading by viewModel.isLoadingAppUsage.collectAsState()
    var showSystem by rememberSaveable { mutableStateOf(false) }

    val filteredList = remember(appUsageList, showSystem) {
        if (showSystem) {
            appUsageList.filter { it.usageType == BatteryUsageType.SYSTEM }
        } else {
            appUsageList.filter { it.usageType == BatteryUsageType.APP }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.battery_usage_since_last_charge),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ClassicColors.OnSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ClassicFilterDropdown(isSystem = showSystem, onFilterChange = { showSystem = it })

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && appUsageList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ClassicColors.Primary)
            }
        } else if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_usage_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant,
                )
            }
        } else {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    filteredList.forEachIndexed { index, app ->
                        ClassicAppUsageItem(app)
                        if (index < filteredList.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                                thickness = 1.dp,
                                color = ClassicColors.OnSurface.copy(alpha = 0.05f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassicFilterDropdown(isSystem: Boolean, onFilterChange: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(50),
            color = ClassicColors.SurfaceContainer,
            modifier = Modifier.clickable { expanded = true },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isSystem) stringResource(R.string.view_by_systems) else stringResource(R.string.view_by_apps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = ClassicColors.OnSurface,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ClassicColors.SurfaceContainer,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.view_by_apps), color = ClassicColors.OnSurface) },
                onClick = {
                    onFilterChange(false)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.view_by_systems), color = ClassicColors.OnSurface) },
                onClick = {
                    onFilterChange(true)
                    expanded = false
                },
            )
        }
    }
}

@Composable
fun ClassicAppUsageItem(app: AppBatteryStats) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (app.icon != null) {
            Image(
                painter = rememberAsyncImagePainter(model = app.icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(ClassicColors.SurfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (app.usageType == BatteryUsageType.SYSTEM) Icons.Rounded.Android else Icons.Rounded.Apps,
                    contentDescription = null,
                    tint = ClassicColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
            )

            Text(
                text = stringResource(R.string.battery_usage_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = ClassicColors.OnSurfaceVariant,
            )
        }

        Text(
            text = "${app.percent.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ClassicColors.OnSurface,
        )
    }
}
