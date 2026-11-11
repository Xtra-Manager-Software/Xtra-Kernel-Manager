package id.xms.xtrakernelmanager.ui.screens.functionalrom

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R

enum class ConnectedItemPosition {
    TOP, MIDDLE, BOTTOM, ALONE
}

private fun getConnectedItemShape(
    position: ConnectedItemPosition,
    cornerRadius: Dp = 24.dp,
    innerRadius: Dp = 4.dp,
): RoundedCornerShape {
    return when (position) {
        ConnectedItemPosition.TOP -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        ConnectedItemPosition.MIDDLE -> RoundedCornerShape(innerRadius)
        ConnectedItemPosition.BOTTOM -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )
        ConnectedItemPosition.ALONE -> RoundedCornerShape(cornerRadius)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialFunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDisplaySize: () -> Unit,
    onNavigateToGlobalRefreshRate: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isLoading) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val currentSmallestWidth = remember {
        context.resources.configuration.smallestScreenWidthDp
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Compact Top App Header (matching modern Material 3 Expressive / Pixel settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = stringResource(R.string.functional_rom_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = stringResource(R.string.functional_rom_card_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Section: System & Security
                item {
                    SectionHeader(title = stringResource(R.string.category_system_security))
                }

                item {
                    val isDevEnabled = checkDeveloperOptionsEnabled(context)
                    ConnectedNavCard(
                        title = stringResource(R.string.developer_options),
                        subtitle = if (isDevEnabled) stringResource(R.string.developer_options_desc_enabled)
                                  else stringResource(R.string.developer_options_desc_disabled),
                        icon = Icons.Default.DeveloperMode,
                        position = ConnectedItemPosition.TOP,
                        onClick = { handleDeveloperOptionsClick(context) }
                    )
                }

                item {
                    val isEnforcing = uiState.seLinuxMode == "Enforcing"
                    ConnectedSwitchCard(
                        title = stringResource(R.string.selinux_mode),
                        subtitle = stringResource(R.string.selinux_mode_status, uiState.seLinuxMode),
                        subtitleColor = if (isEnforcing) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                        icon = Icons.Default.Security,
                        position = ConnectedItemPosition.BOTTOM,
                        checked = uiState.seLinuxMode == "Permissive",
                        onCheckedChange = { toPermissive ->
                            if (toPermissive) {
                                viewModel.showSeLinuxWarningDialog()
                            } else {
                                viewModel.setSeLinuxMode("Enforcing")
                            }
                        }
                    )
                }

                // Section: Display & Refresh Rate
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = stringResource(R.string.category_display_refresh_rate))
                }

                item {
                    ConnectedNavCard(
                        title = stringResource(R.string.display_size_changer),
                        subtitle = stringResource(R.string.display_size_desc, currentSmallestWidth),
                        icon = Icons.Default.AspectRatio,
                        position = ConnectedItemPosition.TOP,
                        onClick = onNavigateToDisplaySize
                    )
                }

                item {
                    ConnectedNavCard(
                        title = stringResource(R.string.global_refresh_rate),
                        subtitle = stringResource(R.string.global_refresh_rate_desc),
                        icon = Icons.Default.Refresh,
                        position = ConnectedItemPosition.BOTTOM,
                        onClick = onNavigateToGlobalRefreshRate
                    )
                }
            }
        }
    }

    // SELinux Warning Dialog
    if (uiState.showSeLinuxWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSeLinuxWarningDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.selinux_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.selinux_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setSeLinuxMode("Permissive") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.selinux_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSeLinuxWarningDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 6.dp)
    )
}

/**
 * Modern Connected Card Item (Navigation) matching Material 3 Expressive grouped layout.
 */
@Composable
private fun ConnectedNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    position: ConnectedItemPosition = ConnectedItemPosition.ALONE,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val shape = getConnectedItemShape(position)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle icon badge with vibrant Monet palette
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Modern Connected Card Item (Toggle Switch) matching Material 3 Expressive grouped layout.
 */
@Composable
private fun ConnectedSwitchCard(
    title: String,
    subtitle: String,
    subtitleColor: Color? = null,
    icon: ImageVector,
    position: ConnectedItemPosition = ConnectedItemPosition.ALONE,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val shape = getConnectedItemShape(position)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle icon badge with vibrant Monet palette
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    }
}

private fun checkDeveloperOptionsEnabled(context: Context): Boolean {
    return Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1
}

private fun handleDeveloperOptionsClick(context: Context) {
    val isDeveloperEnabled = Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1

    if (isDeveloperEnabled) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.developer_options_open_error), Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, context.getString(R.string.developer_options_enable_prompt), Toast.LENGTH_LONG).show()
    }
}
