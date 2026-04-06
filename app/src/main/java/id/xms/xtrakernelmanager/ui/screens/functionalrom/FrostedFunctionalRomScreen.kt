package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidFunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit,
    onNavigateToHideAccessibility: () -> Unit,
    onNavigateToDisplaySize: () -> Unit,
    viewModel: FunctionalRomViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Header like other frosted screens
                id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    contentPadding = PaddingValues(16.dp, 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                        
                        Text(
                            text = stringResource(R.string.functional_rom_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ROM Info Section
                item {
                    uiState.romInfo?.let { romInfo ->
                        id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (romInfo.isShimokuRom) Color(0xFF34C759) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (romInfo.isShimokuRom) Icons.Default.Verified else Icons.Default.PhoneAndroid,
                                            contentDescription = null,
                                            tint = if (romInfo.isShimokuRom) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                
                                Column {
                                    Text(
                                        text = romInfo.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Android ${romInfo.androidVersion} • ${romInfo.systemBrand}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Universal Features Group
                item {
                    Text(
                        text = "UNIVERSAL FEATURES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    
                    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column {
                            LiquidNavigationCell(
                                title = "Developer Options",
                                status = if (checkDeveloperOptionsEnabled()) "On" else "Off",
                                icon = Icons.Default.DeveloperMode,
                                iconColor = Color(0xFF5856D6),
                                onClick = { handleDeveloperOptionsClick(context) },
                                showDivider = true
                            )
                            
                            LiquidNavigationCell(
                                title = "Display Zoom",
                                status = "${androidx.compose.ui.platform.LocalContext.current.resources.configuration.smallestScreenWidthDp} dp",
                                icon = Icons.Default.PhoneAndroid,
                                iconColor = Color(0xFF007AFF),
                                onClick = onNavigateToDisplaySize,
                                showDivider = true
                            )
                            
                            LiquidNavigationCell(
                                title = "Hide Accessibility",
                                status = if (uiState.hideAccessibilityConfig.isEnabled) "On" else "Off",
                                icon = Icons.Default.VisibilityOff,
                                iconColor = Color(0xFFFF3B30),
                                onClick = onNavigateToHideAccessibility,
                                showDivider = false
                            )
                        }
                    }
                    
                    Text(
                        text = "Manage system accessibility features visibility.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // ROM Specific Features Group
                item {
                    Text(
                        text = "ROM FEATURES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        LiquidNavigationCell(
                            title = "Shimoku Features",
                            status = if (uiState.isShimokuRom) "Available" else "Locked",
                            icon = Icons.Default.Verified,
                            iconColor = Color(0xFF34C759),
                            onClick = onNavigateToShimokuRom,
                            showDivider = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidNavigationCell(
    title: String,
    status: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp)
            )
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
    }
}

@Composable
private fun checkDeveloperOptionsEnabled(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    return android.provider.Settings.Global.getInt(
        context.contentResolver,
        android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1
}

@Composable
private fun getTotalSelectedApps(config: HideAccessibilityConfig): Int {
  return config.appsToHide.size + config.detectorApps.size
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
        // Need root to enable via shell or guide user
        // Using simplified approach - just attempt to open settings or show toast
        android.widget.Toast.makeText(context, "Please enable Developer Options in Settings > About Phone", android.widget.Toast.LENGTH_LONG).show()
    }
}

// Reusing the DPI Dialog logic removed since we moved to a dedicated screen

