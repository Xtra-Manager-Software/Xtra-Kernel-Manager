package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class MaterialDisplaySizePreset(
    val width: Int,
    val nameRes: Int,
    val tagRes: Int,
    val icon: ImageVector,
    val densityLevel: Int,
    val isFeatured: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDisplaySizeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val focusManager = LocalFocusManager.current

    // Dynamically observe smallest screen width on config change
    val currentSmallestWidth = configuration.smallestScreenWidthDp
    val currentDPI = context.resources.displayMetrics.densityDpi

    val displayMetrics = context.resources.displayMetrics
    val smallestPixels = remember(displayMetrics) {
        minOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    var selectedWidth by rememberSaveable { mutableIntStateOf(currentSmallestWidth) }
    var customWidthText by rememberSaveable { mutableStateOf(currentSmallestWidth.toString()) }
    var useCustom by rememberSaveable { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }

    val presets = remember {
        listOf(
            MaterialDisplaySizePreset(
                width = 360,
                nameRes = R.string.display_size_preset_stock,
                tagRes = R.string.display_size_preset_stock_tag,
                icon = Icons.Default.PhoneAndroid,
                densityLevel = 2,
                isFeatured = true
            ),
            MaterialDisplaySizePreset(
                width = 411,
                nameRes = R.string.display_size_preset_compact,
                tagRes = R.string.display_size_preset_compact_tag,
                icon = Icons.Default.Compress,
                densityLevel = 3,
                isFeatured = true
            ),
            MaterialDisplaySizePreset(
                width = 320,
                nameRes = R.string.display_size_preset_large,
                tagRes = R.string.display_size_preset_large_tag,
                icon = Icons.Default.ZoomIn,
                densityLevel = 1
            ),
            MaterialDisplaySizePreset(
                width = 480,
                nameRes = R.string.display_size_preset_dense,
                tagRes = R.string.display_size_preset_dense_tag,
                icon = Icons.Default.GridView,
                densityLevel = 4
            ),
            MaterialDisplaySizePreset(
                width = 540,
                nameRes = R.string.display_size_preset_ultra,
                tagRes = R.string.display_size_preset_ultra_tag,
                icon = Icons.Default.ViewCompact,
                densityLevel = 5
            ),
            MaterialDisplaySizePreset(
                width = 600,
                nameRes = R.string.display_size_preset_tablet,
                tagRes = R.string.display_size_preset_tablet_tag,
                icon = Icons.Default.TabletAndroid,
                densityLevel = 6
            )
        )
    }

    val targetWidth = if (useCustom) {
        customWidthText.toIntOrNull() ?: selectedWidth
    } else {
        selectedWidth
    }

    // Real-time estimated DPI
    val projectedDPI = remember(smallestPixels, targetWidth) {
        if (targetWidth > 0) {
            (smallestPixels.toFloat() / targetWidth.toFloat() * 160).toInt()
        } else {
            currentDPI
        }
    }

    val hasPendingChanges = targetWidth != currentSmallestWidth

    // Helper functions for action execution
    val executeReset: () -> Unit = {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    id.xms.xtrakernelmanager.utils.RootShell.execute("wm density reset")
                }
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.display_size_reset_toast),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                useCustom = false
                selectedWidth = currentSmallestWidth
                customWidthText = currentSmallestWidth.toString()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val executeApply: () -> Unit = {
        scope.launch {
            isApplying = true
            try {
                if (targetWidth < 200 || targetWidth > 900) {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.display_size_invalid_toast),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    isApplying = false
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val newDensity = (smallestPixels.toFloat() / targetWidth.toFloat() * 160).toInt()
                    id.xms.xtrakernelmanager.utils.RootShell.execute("wm density $newDensity")
                }
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.display_size_applied_toast, targetWidth, projectedDPI),
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } finally {
                isApplying = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.display_size_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.display_size_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Top Action Controls: Quick Reset & Expressive Apply Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Reset Button
                        IconButton(
                            onClick = executeReset,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.display_size_reset),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Top Apply Action Pill Button
                        Button(
                            onClick = executeApply,
                            enabled = !isApplying,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasPendingChanges) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (hasPendingChanges) MaterialTheme.colorScheme.onPrimary
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (isApplying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasPendingChanges) stringResource(R.string.display_size_apply)
                                           else stringResource(R.string.display_size_status_active),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Bottom padding is 110.dp so all scrollable content has clearance above the floating bottom bar
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 110.dp)
        ) {
            // BENTO TILE 1: Virtual AMOLED Bezel Display & Real-time Metrics (Hero Card)
            item {
                BentoHeroDisplayCard(
                    currentWidth = currentSmallestWidth,
                    currentDPI = currentDPI,
                    targetWidth = targetWidth,
                    projectedDPI = projectedDPI
                )
            }

            // BENTO TILE 2: Presets Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.display_size_preset_tiles),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = stringResource(R.string.display_size_preset_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // BENTO TILE 2: Asymmetric Bento Presets
            item {
                BentoPresetsCluster(
                    presets = presets,
                    selectedWidth = targetWidth,
                    isCustomActive = useCustom,
                    onSelectPreset = { preset ->
                        useCustom = false
                        selectedWidth = preset.width
                        customWidthText = preset.width.toString()
                        focusManager.clearFocus()
                    }
                )
            }

            // BENTO TILE 3: Redesigned Precision Tuner Widget
            item {
                Text(
                    text = stringResource(R.string.display_size_precision_tuner),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                BentoPrecisionTunerCard(
                    targetWidth = targetWidth,
                    currentSmallestWidth = currentSmallestWidth,
                    projectedDPI = projectedDPI,
                    customWidthText = customWidthText,
                    isApplying = isApplying,
                    hasPendingChanges = hasPendingChanges,
                    onSliderChange = { newWidth ->
                        useCustom = false
                        selectedWidth = newWidth
                        customWidthText = newWidth.toString()
                    },
                    onCustomTextChange = { text ->
                        val digits = text.filter { it.isDigit() }.take(4)
                        customWidthText = digits
                        useCustom = true
                        val parsed = digits.toIntOrNull()
                        if (parsed != null && parsed in 200..900) {
                            selectedWidth = parsed
                        }
                    },
                    onStepAdjust = { delta ->
                        val currentVal = customWidthText.toIntOrNull() ?: selectedWidth
                        val adjusted = (currentVal + delta).coerceIn(200, 900)
                        selectedWidth = adjusted
                        customWidthText = adjusted.toString()
                        useCustom = true
                    },
                    onApply = executeApply,
                    onReset = executeReset
                )
            }
        }
    }
}

/**
 * Bento Tile 1: Hero Virtual AMOLED Screen Bezel Preview.
 * Simulates real screen scaling smoothly with animated spring motion.
 */
@Composable
private fun BentoHeroDisplayCard(
    currentWidth: Int,
    currentDPI: Int,
    targetWidth: Int,
    projectedDPI: Int
) {
    val previewScale by animateFloatAsState(
        targetValue = (currentWidth.toFloat() / targetWidth.coerceIn(280, 700).toFloat()).coerceIn(0.68f, 1.32f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "hero_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Display Engine Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = stringResource(R.string.display_size_virtual_bezel),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.display_size_current_format, currentWidth, currentDPI),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Big Metric Typography
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$targetWidth",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "dp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(R.string.display_size_target_density, projectedDPI),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Dynamic Pill Status
                val percentDiff = ((targetWidth - currentWidth) * 100) / currentWidth
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        targetWidth == currentWidth -> MaterialTheme.colorScheme.surfaceContainerHighest
                        targetWidth > currentWidth -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    val statusText = when {
                        targetWidth == currentWidth -> stringResource(R.string.display_size_status_active)
                        targetWidth > currentWidth -> stringResource(R.string.display_size_status_denser, percentDiff)
                        else -> stringResource(R.string.display_size_status_larger, percentDiff)
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            targetWidth == currentWidth -> MaterialTheme.colorScheme.onSurfaceVariant
                            targetWidth > currentWidth -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Interactive Phone Screen Mockup Bezel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F1115))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Top Camera Hole / Speaker notch representation
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                )

                // Scaled Mini App UI Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .graphicsLayer {
                            scaleX = previewScale
                            scaleY = previewScale
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.display_size_sim_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.display_size_sim_desc, targetWidth),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${targetWidth}dp",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Simulated Widget Bar inside Mockup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bento Tile 2: Asymmetric Presets Cluster.
 */
@Composable
private fun BentoPresetsCluster(
    presets: List<MaterialDisplaySizePreset>,
    selectedWidth: Int,
    isCustomActive: Boolean,
    onSelectPreset: (MaterialDisplaySizePreset) -> Unit
) {
    val featured = presets.filter { it.isFeatured }
    val others = presets.filter { !it.isFeatured }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Featured Hero Preset Cards (Stock Default 360 & Compact Pixel 411)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            featured.forEach { preset ->
                val isSelected = !isCustomActive && selectedWidth == preset.width
                BentoHeroPresetItem(
                    preset = preset,
                    isSelected = isSelected,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectPreset(preset) }
                )
            }
        }

        // Row 2 & 3: 2x2 Grid of secondary presets
        val chunkedOthers = others.chunked(2)
        chunkedOthers.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { preset ->
                    val isSelected = !isCustomActive && selectedWidth == preset.width
                    BentoCompactPresetItem(
                        preset = preset,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectPreset(preset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoHeroPresetItem(
    preset: MaterialDisplaySizePreset,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "hero_preset_bg"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = preset.icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = stringResource(preset.tagRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column {
                Text(
                    text = stringResource(preset.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${preset.width} dp",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DensityDotsMeter(level = preset.densityLevel, isSelected = isSelected)
                }
            }
        }
    }
}

@Composable
private fun BentoCompactPresetItem(
    preset: MaterialDisplaySizePreset,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "compact_preset_bg"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(preset.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${preset.width} dp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                DensityDotsMeter(level = preset.densityLevel, isSelected = false)
            }
        }
    }
}

@Composable
private fun DensityDotsMeter(level: Int, isSelected: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val isFilled = index < level
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = if (index + 1 == level) 9.dp else 5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isFilled && isSelected -> MaterialTheme.colorScheme.primary
                            isFilled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        }
                    )
            )
        }
    }
}

/**
 * Bento Tile 3: Redesigned Precision Tuner Card with Segmented Hub,
 * Quick Density Chips, and Integrated Action Triggers.
 */
@Composable
private fun BentoPrecisionTunerCard(
    targetWidth: Int,
    currentSmallestWidth: Int,
    projectedDPI: Int,
    customWidthText: String,
    isApplying: Boolean,
    hasPendingChanges: Boolean,
    onSliderChange: (Int) -> Unit,
    onCustomTextChange: (String) -> Unit,
    onStepAdjust: (Int) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Icon + Title + Target Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.display_size_density_controller),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.display_size_density_controller_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${targetWidth} dp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Slider Scale with Range Indicators
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Slider(
                    value = targetWidth.toFloat().coerceIn(300f, 600f),
                    onValueChange = { onSliderChange(it.roundToInt()) },
                    valueRange = 300f..600f,
                    steps = 29, // 10dp intervals
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.display_size_slider_min),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = stringResource(R.string.display_size_slider_max),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Segmented Micro & Macro Stepper Hub
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decrement Cluster: [-10] and [-1]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = { onStepAdjust(-10) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Text("-10", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    FilledTonalIconButton(
                        onClick = { onStepAdjust(-1) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Text("-1", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Center Digital Gauge & Direct Input
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    OutlinedTextField(
                        value = customWidthText,
                        onValueChange = onCustomTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.display_size_custom_dp_placeholder),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Increment Cluster: [+1] and [+10]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = { onStepAdjust(1) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Text("+1", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    FilledTonalIconButton(
                        onClick = { onStepAdjust(10) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Text("+10", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Quick Density Chips Scroll Row
            val quickChips = listOf(360, 392, 411, 440, 480, 540)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickChips.forEach { chipDp ->
                    val isChipSelected = targetWidth == chipDp
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSliderChange(chipDp) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChipSelected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(
                            1.dp,
                            if (isChipSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )
                    ) {
                        Text(
                            text = "${chipDp}dp",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isChipSelected) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Extreme value warning alert
            if (targetWidth < 300 || targetWidth > 650) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.display_size_warning_range),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Integrated Action Buttons inside the Tuner Card!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.display_size_reset),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onApply,
                    enabled = !isApplying,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasPendingChanges) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (hasPendingChanges) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.display_size_applying),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasPendingChanges) stringResource(R.string.display_size_apply_with_dp, targetWidth)
                                   else stringResource(R.string.display_size_status_active),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
