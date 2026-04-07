package id.xms.xtrakernelmanager.ui.screens.misc.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.classic.components.ClassicMiscFeatureCard
import id.xms.xtrakernelmanager.ui.screens.misc.classic.components.ClassicMiscHubCard
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.material.*
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicMiscScreen(
    viewModel: MiscViewModel,
    onNavigateToFunctionalRom: () -> Unit,
    onNavigateToAppPicker: () -> Unit
) {
    val context = LocalContext.current
    var showBatteryDetail by rememberSaveable { mutableStateOf(false) }
    var showBatterySettings by rememberSaveable { mutableStateOf(false) }
    var showBatteryGraph by rememberSaveable { mutableStateOf(false) }
    var showProcessManager by remember { mutableStateOf(false) }
    var showGameSpace by remember { mutableStateOf(false) }
    var showPerAppProfile by remember { mutableStateOf(false) }
    var showCurrentSession by rememberSaveable { mutableStateOf(false) }
    var showGameMonitor by rememberSaveable { mutableStateOf(false) }
    var showDisplaySettings by remember { mutableStateOf(false) }

    when {
        showBatterySettings ->
            ClassicBatterySettingsScreen(viewModel = viewModel, onBack = { showBatterySettings = false })
        showCurrentSession ->
            MaterialCurrentSessionScreen(viewModel = viewModel, onBack = { showCurrentSession = false })
        showBatteryGraph ->
            MaterialBatteryAnalyticsScreen(viewModel = viewModel, onBack = { showBatteryGraph = false })
        showBatteryDetail ->
            ClassicBatteryScreen(
                viewModel = viewModel,
                onBack = { showBatteryDetail = false },
                onSettingsClick = { showBatterySettings = true },
                onGraphClick = { showBatteryGraph = true },
                onCurrentSessionClick = { showCurrentSession = true },
            )
        showGameMonitor ->
            MaterialGameMonitorScreen(
                viewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel {
                        GameMonitorViewModel(context, viewModel.preferencesManager)
                    },
                onBack = { showGameMonitor = false },
            )
        showProcessManager ->
            MaterialProcessManagerScreen(viewModel = viewModel, onBack = { showProcessManager = false })
        showGameSpace ->
            ClassicGameSpaceScreen(
                viewModel = viewModel,
                onBack = { showGameSpace = false },
                onAddGames = onNavigateToAppPicker,
                onGameMonitorClick = { showGameMonitor = true },
            )
        showPerAppProfile ->
            MaterialPerAppProfileScreen(viewModel = viewModel, onBack = { showPerAppProfile = false })
        showDisplaySettings ->
            ClassicDisplaySettingsScreen(viewModel = viewModel, onBack = { showDisplaySettings = false })
        else ->
            ClassicMiscScreenContent(
                viewModel = viewModel,
                onBatteryDetailClick = { showBatteryDetail = true },
                onGameSpaceClick = { showGameSpace = true },
                onDisplaySettingsClick = { showDisplaySettings = true },
                onPerAppProfileClick = { showPerAppProfile = true },
                onProcessManagerClick = { showProcessManager = true },
                onFunctionalRomClick = onNavigateToFunctionalRom
            )
    }
}

@Composable
private fun ClassicMiscScreenContent(
    viewModel: MiscViewModel,
    onBatteryDetailClick: () -> Unit,
    onGameSpaceClick: () -> Unit,
    onDisplaySettingsClick: () -> Unit,
    onPerAppProfileClick: () -> Unit,
    onProcessManagerClick: () -> Unit,
    onFunctionalRomClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Miscellaneous",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
        }

        // Miscellaneous Hub Header Card
        ClassicMiscHubCard()

        // Power Insight Feature Card
        ClassicMiscFeatureCard(
            icon = Icons.Rounded.BatteryChargingFull,
            title = "Power Insight",
            description = "Monitor battery usage, screen time, and power statistics",
            statusLabel = "ANALYTICS",
            statusColor = ClassicColors.Primary,
            onClick = onBatteryDetailClick
        )
        
        // Game Space Feature Card
        ClassicMiscFeatureCard(
            icon = Icons.Rounded.SportsEsports,
            title = stringResource(R.string.game_control),
            description = "Manage game apps and boost gaming performance",
            statusLabel = "GAMING",
            statusColor = ClassicColors.Good,
            onClick = onGameSpaceClick
        )
        
        // Display Settings Feature Card
        ClassicMiscFeatureCard(
            icon = Icons.Rounded.Palette,
            title = stringResource(R.string.display_settings),
            description = "Adjust screen saturation and color settings",
            statusLabel = "VISUALS",
            statusColor = ClassicColors.Secondary,
            onClick = onDisplaySettingsClick
        )
        
        // Per App Profile Feature Card
        ClassicMiscFeatureCard(
            icon = Icons.Rounded.AppSettingsAlt,
            title = "Per App Profile",
            description = "Configure custom settings for individual apps",
            statusLabel = "CUSTOM",
            statusColor = ClassicColors.Accent,
            onClick = onPerAppProfileClick
        )
        
        // Process Manager Feature Card
        ClassicMiscFeatureCard(
            icon = Icons.Rounded.Memory,
            title = "Process Manager",
            description = "View and manage running processes and apps",
            statusLabel = "MONITOR",
            statusColor = ClassicColors.Primary,
            onClick = onProcessManagerClick
        )
        
        // Functional ROM Feature Card
        ClassicMiscFeatureCard(
            icon = Icons.Rounded.Extension,
            title = stringResource(R.string.functional_rom_title),
            description = "Access ROM-specific features and system settings",
            statusLabel = "UNIVERSAL",
            statusColor = ClassicColors.Good,
            onClick = onFunctionalRomClick
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ClassicDisplaySettingsScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit
) {
    ClassicDisplayDetailScreen(
        viewModel = viewModel,
        onBack = onBack
    )
}
