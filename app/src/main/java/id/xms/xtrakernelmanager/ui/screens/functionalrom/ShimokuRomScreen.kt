package id.xms.xtrakernelmanager.ui.screens.functionalrom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.PillCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Helper: copy a file URI to a root-owned path via su -c
private suspend fun copyUriToRootPath(
    context: Context,
    uri: Uri,
    destPath: String,
): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        // 1. Write URI bytes to app's private cache first
        val tempFile = File(context.cacheDir, "xkm_import_temp")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return@withContext Result.failure(Exception("Cannot open selected file"))

        // 2. Copy to destination via root shell
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c",
            "mkdir -p ${destPath.substringBeforeLast('/')} && cp '${tempFile.absolutePath}' '$destPath' && chmod 644 '$destPath'"
        ))
        process.waitFor()
        tempFile.delete()

        if (process.exitValue() == 0) {
            Result.success(Unit)
        } else {
            val err = process.errorStream.bufferedReader().readText()
            Result.failure(Exception("Root copy failed: $err"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Whether Shimoku features should be accessible (debug bypasses ROM check)
private fun isShimokuAccessible(isShimokuRom: Boolean, isVipCommunity: Boolean): Boolean =
    BuildConfig.IS_DEBUG_BUILD || (isShimokuRom && isVipCommunity)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShimokuRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayIntegrity: () -> Unit = {},
    onNavigateToXiaomiTouch: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Feature dialog state
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    var showChargingLimitDialog by remember { mutableStateOf(false) }

    // PIF / Keybox import state
    var pifImportStatus by remember { mutableStateOf<String?>(null) }
    var keyboxImportStatus by remember { mutableStateOf<String?>(null) }
    var isImportingPif by remember { mutableStateOf(false) }
    var isImportingKeybox by remember { mutableStateOf(false) }

    // File pickers
    val pifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        isImportingPif = true
        scope.launch {
            val result = copyUriToRootPath(context, uri, "/data/adb/pif.json")
            isImportingPif = false
            pifImportStatus = if (result.isSuccess) "✓ Imported" else "✗ Failed"
            snackbarHostState.showSnackbar(
                if (result.isSuccess) "pif.json imported to /data/adb/pif.json"
                else "Import failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    val keyboxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        isImportingKeybox = true
        scope.launch {
            val result = copyUriToRootPath(context, uri, "/data/adb/keybox.xml")
            isImportingKeybox = false
            keyboxImportStatus = if (result.isSuccess) "✓ Imported" else "✗ Failed"
            snackbarHostState.showSnackbar(
                if (result.isSuccess) "keybox.xml imported to /data/adb/keybox.xml"
                else "Import failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    // Derived access flag
    val shimokuAccessible = isShimokuAccessible(
        uiState.isShimokuRom, uiState.isVipCommunity
    )

    // Loading state
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.loading_features),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledIconButton(
                            onClick = onNavigateBack,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        PillCard(text = "Shimoku ROM Features")
                    }
                }
            }

            // Debug Mode Banner (only shown in debug builds on non-Shimoku ROM)
            if (BuildConfig.IS_DEBUG_BUILD) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = Color(0xFFFF9800)
                            )
                            Column {
                                Text(
                                    text = "Debug Build — Shimoku Lock Bypassed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                                Text(
                                    text = "All Shimoku features are accessible in debug builds. Release builds enforce ROM detection.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // ROM Information Card
            item {
                uiState.romInfo?.let { romInfo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                romInfo.isShimokuRom -> MaterialTheme.colorScheme.primaryContainer
                                BuildConfig.IS_DEBUG_BUILD -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        romInfo.isShimokuRom -> Icons.Default.Verified
                                        BuildConfig.IS_DEBUG_BUILD -> Icons.Default.BugReport
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        romInfo.isShimokuRom -> MaterialTheme.colorScheme.onPrimaryContainer
                                        BuildConfig.IS_DEBUG_BUILD -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                                Text(
                                    text = when {
                                        romInfo.isShimokuRom -> "Shimoku ROM Detected"
                                        BuildConfig.IS_DEBUG_BUILD -> "Non-Shimoku ROM (Debug Access)"
                                        else -> "Non-Shimoku ROM"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        romInfo.isShimokuRom -> MaterialTheme.colorScheme.onPrimaryContainer
                                        BuildConfig.IS_DEBUG_BUILD -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${romInfo.displayName} • Android ${romInfo.androidVersion}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    romInfo.isShimokuRom -> MaterialTheme.colorScheme.onPrimaryContainer
                                    BuildConfig.IS_DEBUG_BUILD -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                            )

                            if (!romInfo.isShimokuRom && !BuildConfig.IS_DEBUG_BUILD) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Features on this screen are only available on Shimoku ROM",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // ── Play Integrity Category ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryHeader(title = stringResource(R.string.category_play_integrity))
            }

            item {
                ClickableFeatureCard(
                    title = stringResource(R.string.play_integrity_fix),
                    description = "Bypass root and bootloader unlock detection via Play Integrity Fix.",
                    icon = Icons.Default.Security,
                    onClick = onNavigateToPlayIntegrity,
                    enabled = shimokuAccessible,
                    statusText = if (uiState.playIntegrityFixEnabled) "Enabled" else "Disabled"
                )
            }

            // PIF JSON Import
            item {
                ImportFileCard(
                    title = "Import pif.json",
                    description = "Import a custom pif.json to /data/adb/pif.json. Used by PIF Magisk module. (Contributor Feature)",
                    icon = Icons.Default.FileOpen,
                    enabled = shimokuAccessible,
                    isLoading = isImportingPif,
                    statusText = pifImportStatus,
                    onImportClick = { pifLauncher.launch("application/json") }
                )
            }

            // Keybox XML Import
            item {
                ImportFileCard(
                    title = "Import keybox.xml",
                    description = "Import a keybox attestation file to /data/adb/keybox.xml. Used for Strong Integrity spoofing.",
                    icon = Icons.Default.Key,
                    enabled = shimokuAccessible,
                    isLoading = isImportingKeybox,
                    statusText = keyboxImportStatus,
                    onImportClick = { keyboxLauncher.launch("text/xml") }
                )
            }

            // ── Touch & Kernel Category ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryHeader(title = stringResource(R.string.category_touch_kernel))
            }
            item {
                ClickableFeatureCard(
                    title = stringResource(R.string.xiaomi_touch_settings),
                    description = "Touch sensitivity and game mode settings for Xiaomi devices.",
                    icon = Icons.Default.TouchApp,
                    onClick = onNavigateToXiaomiTouch,
                    enabled = shimokuAccessible,
                    statusText = if (uiState.touchGameModeEnabled || uiState.touchActiveModeEnabled) "Configured" else "Default"
                )
            }

            // ── Native Features (when accessible) ───────────────────────────
            if (shimokuAccessible) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryHeader(title = "Native Features")
                }

                // Bypass Charging
                if (uiState.bypassChargingAvailable) {
                    item {
                        FeatureCard(
                            title = stringResource(R.string.bypass_charging),
                            description = "Prevent battery overcharge during gaming by bypassing the charging circuit.",
                            icon = Icons.Default.BatteryChargingFull,
                            isEnabled = uiState.bypassChargingEnabled,
                            onToggle = { viewModel.setBypassCharging(it) },
                            enabled = shimokuAccessible,
                        )
                    }
                }

                // Charging Limit
                if (uiState.chargingLimitAvailable) {
                    item {
                        FeatureCard(
                            title = stringResource(R.string.charging_limit),
                            description = "Limit battery charging to ${uiState.chargingLimitValue}% to improve long-term battery health.",
                            icon = Icons.Default.BatteryAlert,
                            isEnabled = uiState.chargingLimitEnabled,
                            onToggle = { viewModel.setChargingLimit(it) },
                            enabled = shimokuAccessible,
                            onClick = { showChargingLimitDialog = true }
                        )
                    }
                }

                // Double Tap to Wake
                if (uiState.dt2wAvailable) {
                    item {
                        FeatureCard(
                            title = stringResource(R.string.double_tap_wake),
                            description = "Wake the screen with a double tap on the display.",
                            icon = Icons.Default.TouchApp,
                            isEnabled = uiState.doubleTapWakeEnabled,
                            onToggle = { viewModel.setDoubleTapToWake(it) },
                            enabled = shimokuAccessible,
                        )
                    }
                }

                // Force Refresh Rate
                item {
                    FeatureCard(
                        title = stringResource(R.string.force_refresh_rate),
                        description = "Force the display refresh rate to ${uiState.forceRefreshRateValue}Hz system-wide.",
                        icon = Icons.Default.Speed,
                        isEnabled = uiState.forceRefreshRateEnabled,
                        onToggle = { viewModel.setForceRefreshRate(it) },
                        enabled = shimokuAccessible,
                        onClick = { showRefreshRateDialog = true }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryHeader(title = "Property Features")
                }

                // Touch Boost
                item {
                    FeatureCard(
                        title = stringResource(R.string.touch_boost),
                        description = "Boost touch responsiveness for a smoother gaming experience.",
                        icon = Icons.Default.Speed,
                        isEnabled = uiState.touchBoostEnabled,
                        onToggle = { viewModel.setTouchBoost(it) },
                        enabled = shimokuAccessible,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryHeader(title = "Display Features")
                }

                item {
                    FeatureCard(
                        title = "Unlock Additional Nits",
                        description = "Unlock extra brightness levels beyond the stock display limit.",
                        icon = Icons.Default.Brightness7,
                        isEnabled = uiState.unlockNitsEnabled,
                        onToggle = { viewModel.setUnlockNits(it) },
                        enabled = shimokuAccessible,
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.dynamic_refresh_rate),
                        description = "Dynamically adjust refresh rate to save battery based on content.",
                        icon = Icons.Default.DisplaySettings,
                        isEnabled = uiState.dynamicRefreshRateEnabled,
                        onToggle = { viewModel.setDynamicRefreshRate(it) },
                        enabled = shimokuAccessible,
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.dc_dimming),
                        description = "Reduce display flicker at low brightness levels using DC dimming.",
                        icon = Icons.Default.Brightness4,
                        isEnabled = uiState.dcDimmingEnabled,
                        onToggle = { viewModel.setDcDimming(it) },
                        enabled = shimokuAccessible,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryHeader(title = "System Features")
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.smart_charging),
                        description = "Intelligently optimize charging patterns to preserve battery health.",
                        icon = Icons.Default.Psychology,
                        isEnabled = uiState.smartChargingEnabled,
                        onToggle = { viewModel.setSmartCharging(it) },
                        enabled = shimokuAccessible,
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.fix_dt2w),
                        description = "Fix Double Tap to Wake on devices where it is not natively supported.",
                        icon = Icons.Default.Build,
                        isEnabled = uiState.fixDt2wEnabled,
                        onToggle = { viewModel.setFixDt2w(it) },
                        enabled = shimokuAccessible,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Dialogs
    if (showRefreshRateDialog) {
        RefreshRateDialog(
            currentValue = uiState.forceRefreshRateValue,
            onValueChange = { viewModel.setForceRefreshRateValue(it) },
            onDismiss = { showRefreshRateDialog = false }
        )
    }

    if (showChargingLimitDialog) {
        ChargingLimitDialog(
            currentValue = uiState.chargingLimitValue,
            onValueChange = { viewModel.setChargingLimitValue(it) },
            onDismiss = { showChargingLimitDialog = false }
        )
    }
}

// ── New Composable: Import File Card ──────────────────────────────────────────

@Composable
private fun ImportFileCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    isLoading: Boolean,
    statusText: String?,
    onImportClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        onClick = if (enabled && !isLoading) onImportClick else ({})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (statusText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (statusText.startsWith("✓"))
                            Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = "Import",
                    tint = if (enabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Existing private composables (unchanged) ──────────────────────────────────

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun ClickableFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    statusText: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f),
        onClick = if (enabled) onClick else { {} }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LottieSwitchControlled(
                checked = isEnabled,
                onCheckedChange = onToggle,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun RefreshRateDialog(
    currentValue: Int,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val refreshRates = listOf(60, 90, 120, 144, 165, 240)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Refresh Rate") },
        text = {
            LazyColumn {
                items(refreshRates.size) { index ->
                    val rate = refreshRates[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentValue == rate,
                            onClick = {
                                onValueChange(rate)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${rate}Hz")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ChargingLimitDialog(
    currentValue: Int,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val limits = listOf(80, 85, 90, 95)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Charging Limit") },
        text = {
            LazyColumn {
                items(limits.size) { index ->
                    val limit = limits[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentValue == limit,
                            onClick = {
                                onValueChange(limit)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${limit}%")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}