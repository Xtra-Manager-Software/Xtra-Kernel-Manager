package id.xms.xtrakernelmanager.ui.screens.functionalrom

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicFunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit,
    onNavigateToHideAccessibility: () -> Unit,
    onNavigateToDisplaySize: () -> Unit,
    onNavigateToGlobalRefreshRate: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ClassicColors.Primary)
        }
        return
    }

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.functional_rom_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = ClassicColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.Background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ROM Info Card - Horizontal Layout
            item {
                uiState.romInfo?.let { romInfo ->
                    ClassicRomInfoCard(
                        romName = romInfo.displayName,
                        androidVersion = romInfo.androidVersion,
                        brand = romInfo.systemBrand,
                        isShimoku = romInfo.isShimokuRom
                    )
                }
            }

            // Universal Features - Grid Layout (2 columns)
            item {
                Text(
                    text = "Universal Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClassicColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClassicFeatureCard(
                        title = "Developer Options",
                        subtitle = if (checkDeveloperOptionsEnabled(context)) "Enabled" else "Disabled",
                        icon = Icons.Rounded.DeveloperMode,
                        iconColor = Color(0xFF5C6BC0),
                        onClick = { handleDeveloperOptionsClick(context) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    ClassicFeatureCard(
                        title = "Display Size",
                        subtitle = "${context.resources.configuration.smallestScreenWidthDp} dp",
                        icon = Icons.Rounded.AspectRatio,
                        iconColor = Color(0xFF26A69A),
                        onClick = onNavigateToDisplaySize,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                ClassicFeatureCard(
                    title = "Hide Accessibility Service",
                    subtitle = if (uiState.hideAccessibilityConfig.isEnabled) {
                        "${uiState.hideAccessibilityConfig.currentTab.displayName} • ${getTotalSelectedApps(uiState.hideAccessibilityConfig)} apps"
                    } else "Disabled",
                    icon = Icons.Rounded.VisibilityOff,
                    iconColor = Color(0xFFEF5350),
                    onClick = onNavigateToHideAccessibility,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ClassicFeatureCard(
                    title = "Global Refresh Rate",
                    subtitle = "System-wide refresh rate control",
                    icon = Icons.Rounded.Refresh,
                    iconColor = Color(0xFF42A5F5),
                    onClick = onNavigateToGlobalRefreshRate,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SELinux Mode Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ClassicColors.SurfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF5350).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SELinux Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.OnSurface
                            )
                            Text(
                                text = uiState.seLinuxMode,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.seLinuxMode == "Enforcing")
                                    Color(0xFF66BB6A) else Color(0xFFFF9800)
                            )
                        }
                        Switch(
                            checked = uiState.seLinuxMode == "Permissive",
                            onCheckedChange = { toPermissive ->
                                if (toPermissive) viewModel.showSeLinuxWarningDialog()
                                else viewModel.setSeLinuxMode("Enforcing")
                            }
                        )
                    }
                }
            }

            // Bypass Charging Feature (Kernel-specific)
            item {
                Text(
                    text = "Kernel Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClassicColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!uiState.bypassChargingAvailable) Modifier.blur(2.dp)
                            else Modifier
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ClassicColors.SurfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9500).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = null,
                                tint = Color(0xFFFF9500),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bypass Charging",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.OnSurface
                            )
                            Text(
                                text = if (uiState.bypassChargingAvailable) {
                                    if (uiState.bypassChargingEnabled) "Enabled" else "Disabled"
                                } else {
                                    "Not Available"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = ClassicColors.OnSurfaceVariant
                            )
                        }

                        Switch(
                            checked = uiState.bypassChargingEnabled,
                            onCheckedChange = { viewModel.setBypassCharging(it) },
                            enabled = uiState.bypassChargingAvailable
                        )
                    }
                }
                
                Text(
                    text = if (uiState.bypassChargingAvailable) {
                        "Bypass charging allows power to flow directly to the device without charging the battery."
                    } else {
                        "This feature is only available in XtraAether kernel version Mamad-Ibn-Solowie and later. If you are a kernel developer with bypass charging support, please contact XMS team to implement it in your kernel."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // ROM Specific Features
            item {
                Text(
                    text = "ROM-Specific Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClassicColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                ClassicFeatureCard(
                    title = "Shimoku ROM Features",
                    subtitle = if (uiState.isShimokuRom) "Available" else "Locked",
                    icon = Icons.Rounded.Verified,
                    iconColor = Color(0xFF66BB6A),
                    onClick = onNavigateToShimokuRom,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // SELinux Warning Dialog
    if (uiState.showSeLinuxWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSeLinuxWarningDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Switch to Permissive?", fontWeight = FontWeight.Bold, color = ClassicColors.OnSurface)
            },
            text = {
                Text(
                    text = "Switching SELinux to Permissive mode will allow all processes to bypass security policies without enforcement.\n\nAll risks resulting from this action — including security vulnerabilities, unauthorized access, and system instability — are entirely at your own responsibility and are outside the scope of Xtra Kernel Manager.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setSeLinuxMode("Permissive") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text("I Understand, Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSeLinuxWarningDialog() }) {
                    Text("Cancel", color = ClassicColors.Primary)
                }
            }
        )
    }
}

@Composable
private fun ClassicRomInfoCard(
    romName: String,
    androidVersion: String,
    brand: String,
    isShimoku: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isShimoku) 
                ClassicColors.Primary.copy(alpha = 0.15f) 
            else 
                ClassicColors.SurfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isShimoku) ClassicColors.Primary.copy(alpha = 0.2f)
                        else ClassicColors.SurfaceContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isShimoku) Icons.Rounded.Verified else Icons.Rounded.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (isShimoku) ClassicColors.Primary else ClassicColors.OnSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = romName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isShimoku) ClassicColors.Primary else ClassicColors.OnSurface
                )
                Text(
                    text = "Android $androidVersion • $brand",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ClassicFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClassicColors.SurfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = ClassicColors.OnSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun checkDeveloperOptionsEnabled(context: android.content.Context): Boolean {
    return android.provider.Settings.Global.getInt(
        context.contentResolver,
        android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1
}

private fun handleDeveloperOptionsClick(context: android.content.Context) {
    val isDeveloperEnabled = android.provider.Settings.Global.getInt(
        context.contentResolver,
        android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1

    if (isDeveloperEnabled) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Unable to open Developer Options", android.widget.Toast.LENGTH_SHORT).show()
        }
    } else {
        android.widget.Toast.makeText(context, "Please enable Developer Options in Settings > About Phone", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun getTotalSelectedApps(config: HideAccessibilityConfig): Int {
    return config.appsToHide.size + config.detectorApps.size
}
