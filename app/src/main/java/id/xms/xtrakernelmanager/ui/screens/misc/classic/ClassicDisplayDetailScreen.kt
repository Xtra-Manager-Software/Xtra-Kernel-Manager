package id.xms.xtrakernelmanager.ui.screens.misc.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

private data class ClassicSaturationPreset(
    val name: String,
    val value: Float,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun ClassicDisplayDetailScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit
) {
    val currentSaturation by viewModel.displaySaturation.collectAsState()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()
    val applyStatus by viewModel.saturationApplyStatus.collectAsState()
    
    var sliderValue by remember(currentSaturation) { mutableFloatStateOf(currentSaturation) }

    val presets = remember {
        listOf(
            ClassicSaturationPreset(
                "Mono", 
                0.5f, 
                Icons.Rounded.Circle, 
                Color(0xFF9E9E9E),
                "Grayscale mode"
            ),
            ClassicSaturationPreset(
                "sRGB", 
                1.0f, 
                Icons.Rounded.Palette, 
                Color(0xFF2196F3),
                "Standard colors"
            ),
            ClassicSaturationPreset(
                "P3", 
                1.1f, 
                Icons.Rounded.ColorLens, 
                Color(0xFF9C27B0),
                "Wide gamut"
            ),
            ClassicSaturationPreset(
                "Vivid", 
                1.3f, 
                Icons.Rounded.AutoAwesome, 
                Color(0xFFFF9F0A),
                "Enhanced colors"
            ),
            ClassicSaturationPreset(
                "Ultra", 
                1.5f, 
                Icons.Rounded.Whatshot, 
                Color(0xFFF44336),
                "Maximum saturation"
            ),
        )
    }

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.display_settings),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = ClassicColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Back",
                            tint = ClassicColors.OnSurface,
                        )
                    }
                },
                actions = {
                    // Current value badge
                    Surface(
                        color = ClassicColors.Primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = String.format("%.2f", sliderValue),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = ClassicColors.Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.Background,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Status message
            if (applyStatus.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            applyStatus.contains("Failed") || applyStatus.contains("Root") ->
                                Color(0xFF2E1A1A)
                            else -> ClassicColors.Primary.copy(alpha = 0.15f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                applyStatus.contains("Failed") || applyStatus.contains("Root") ->
                                    Icons.Rounded.Error
                                else -> Icons.Rounded.CheckCircle
                            },
                            contentDescription = null,
                            tint = when {
                                applyStatus.contains("Failed") || applyStatus.contains("Root") ->
                                    Color(0xFFEF9A9A)
                                else -> ClassicColors.Primary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = applyStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                applyStatus.contains("Failed") || applyStatus.contains("Root") ->
                                    Color(0xFFEF9A9A)
                                else -> ClassicColors.Primary
                            }
                        )
                    }
                }
            }

            // Preset Grid - 2 columns
            Text(
                text = "Color Profiles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                presets.chunked(2).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowPresets.forEach { preset ->
                            ClassicPresetCard(
                                preset = preset,
                                isSelected = kotlin.math.abs(sliderValue - preset.value) < 0.05f,
                                isEnabled = isRootAvailable,
                                onClick = {
                                    sliderValue = preset.value
                                    viewModel.setDisplaySaturation(preset.value)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number
                        if (rowPresets.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Slider Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ClassicColors.SurfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Value",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        
                        Surface(
                            color = ClassicColors.SurfaceContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = String.format("%.2fx", sliderValue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.Primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Slider
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = sliderValue,
                            onValueChange = { newValue ->
                                sliderValue = newValue
                            },
                            valueRange = 0.5f..2.0f,
                            enabled = isRootAvailable,
                            colors = SliderDefaults.colors(
                                thumbColor = ClassicColors.Primary,
                                activeTrackColor = ClassicColors.Primary,
                                inactiveTrackColor = ClassicColors.SurfaceContainer,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Auto-apply with debounce
                        LaunchedEffect(Unit) {
                            snapshotFlow { sliderValue }
                                .debounce(800)
                                .collect { value ->
                                    viewModel.setDisplaySaturation(value)
                                }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0.5x",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "1.0x",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "2.0x",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Apply button
                    Button(
                        onClick = { viewModel.setDisplaySaturation(sliderValue) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = isRootAvailable,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClassicColors.Primary,
                            contentColor = Color.White,
                            disabledContainerColor = ClassicColors.SurfaceContainer,
                            disabledContentColor = ClassicColors.OnSurfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Check, 
                            contentDescription = null, 
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Apply Changes",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ClassicColors.SurfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = ClassicColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "About Display Saturation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "Adjust color intensity to match your preference. Lower values reduce saturation, higher values make colors more vivid. Root access required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Root warning
            if (!isRootAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2E1A1A)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF9A9A),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.display_requires_root),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEF9A9A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ClassicPresetCard(
    preset: ClassicSaturationPreset,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                preset.color.copy(alpha = 0.2f)
            } else {
                ClassicColors.SurfaceContainerHigh
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, preset.color)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) preset.color.copy(alpha = 0.3f)
                            else ClassicColors.SurfaceContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = preset.icon,
                        contentDescription = preset.name,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) preset.color else ClassicColors.OnSurfaceVariant
                    )
                }

                if (isSelected) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Selected",
                        tint = preset.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) preset.color else ClassicColors.OnSurface
                )
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurfaceVariant
                )
            }
        }
    }
}
