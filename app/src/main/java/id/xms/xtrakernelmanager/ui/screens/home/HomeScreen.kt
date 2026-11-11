package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.DonationDialog
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.utils.RootShell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDonation: () -> Unit = {},
    forceShowDonationDialog: Boolean = false,
    tuningViewModel: id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel = viewModel(
        factory = id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel.Factory(preferencesManager)
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // UI State for Power Menu
    var showPowerMenu by remember { mutableStateOf(false) }
    var activePowerAction by remember { mutableStateOf<PowerAction?>(null) }

    // Donation Dialog State
    var showDonationDialog by remember { mutableStateOf(false) }
    var hasCheckedDonation by remember { mutableStateOf(false) }

    // Check if donation dialog should be shown
    LaunchedEffect(Unit) {
        if (!hasCheckedDonation) {
            hasCheckedDonation = true
            val shouldShow = preferencesManager.shouldShowDonationDialog()
            if (shouldShow) {
                id.xms.xtrakernelmanager.utils.DonationNotificationHelper.showDonationNotification(context)
                showDonationDialog = true
            }
        }
    }

    // Force show donation dialog from notification
    LaunchedEffect(forceShowDonationDialog) {
        if (forceShowDonationDialog) {
            showDonationDialog = true
        }
    }

    LaunchedEffect(Unit) { viewModel.loadBatteryInfo(context) }

    // --- DIALOGS ---

    // Donation Dialog
    if (showDonationDialog) {
        DonationDialog(
            onDismiss = {
                showDonationDialog = false
                scope.launch {
                    preferencesManager.setLastDonationDialogShown(System.currentTimeMillis())
                    preferencesManager.incrementDonationDialogDismissedCount()
                }
            },
            onSupportClick = {
                showDonationDialog = false
                scope.launch {
                    preferencesManager.setLastDonationDialogShown(System.currentTimeMillis())
                    id.xms.xtrakernelmanager.utils.DonationNotificationHelper.cancelDonationNotification(context)
                }
                onNavigateToDonation()
            }
        )
    }

    // 1. Power Menu Selection
    if (showPowerMenu) {
        PowerMenuDialog(
            onDismiss = { showPowerMenu = false },
            onActionSelected = { action ->
                showPowerMenu = false
                if (action == PowerAction.LockScreen) {
                    scope.launch { RootShell.execute(action.command) }
                } else {
                    activePowerAction = action
                }
            },
        )
    }

    // 2. Countdown Execution
    activePowerAction?.let { action ->
        CountdownRebootDialog(
            action = action,
            onCancel = { activePowerAction = null },
            onFinished = {
                scope.launch {
                    RootShell.execute(action.command)
                    activePowerAction = null
                }
            },
        )
    }

    MaterialHomeScreen(
        preferencesManager = preferencesManager,
        viewModel = viewModel,
        currentProfile = tuningViewModel.selectedProfile.collectAsState().value,
        onProfileChange = { tuningViewModel.applyGlobalProfile(it) },
        onPowerAction = { action ->
            if (action == PowerAction.LockScreen) {
                scope.launch {
                    RootShell.execute(action.command)
                }
            } else {
                activePowerAction = action
            }
        },
        onSettingsClick = onNavigateToSettings,
    )
}

@Composable
fun PowerMenuDialog(onDismiss: () -> Unit, onActionSelected: (PowerAction) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.PowerSettingsNew, null) },
        title = { Text(text = "Power Menu", textAlign = TextAlign.Center) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(PowerAction.values()) { action ->
                    FilledTonalButton(
                        onClick = { onActionSelected(action) },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (action == PowerAction.PowerOff)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (action == PowerAction.PowerOff)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                action.icon,
                                null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                action.getLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun CountdownRebootDialog(action: PowerAction, onCancel: () -> Unit, onFinished: () -> Unit) {
    var countdown by remember { mutableIntStateOf(5) }
    val progress by animateFloatAsState(targetValue = countdown / 5f, label = "Countdown")

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        onFinished()
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    "${action.getLabel()}...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 10.dp,
                        trackColor = Color.Transparent,
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (countdown <= 2)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary,
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        text = if (countdown > 0) "$countdown" else "!",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    stringResource(R.string.processing_action),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(id = R.string.cancel)) }
            }
        }
    }
}
