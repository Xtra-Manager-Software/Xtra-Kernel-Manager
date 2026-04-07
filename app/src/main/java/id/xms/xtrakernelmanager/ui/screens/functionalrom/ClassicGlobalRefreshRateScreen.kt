package id.xms.xtrakernelmanager.ui.screens.functionalrom

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicGlobalRefreshRateScreen(
    onNavigateBack: () -> Unit,
    viewModel: FunctionalRomViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get device max refresh rate
    val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
    val availableRefreshRates = remember(maxRefreshRate) {
        when {
            maxRefreshRate >= 144 -> listOf(60, 90, 120, 144)
            maxRefreshRate >= 120 -> listOf(60, 90, 120)
            maxRefreshRate >= 90 -> listOf(60, 90)
            else -> emptyList() // 60Hz or less = no options
        }
    }
    
    // Get current refresh rate
    var currentRefreshRate by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        currentRefreshRate = viewModel.getCurrentRefreshRate()
        isLoading = false
    }
    
    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Global Refresh Rate",
                        color = ClassicColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.SurfaceContainerHigh
                )
            )
        }
    ) { paddingValues ->
        if (availableRefreshRates.isEmpty()) {
            // Device only supports 60Hz
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ClassicColors.SurfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = ClassicColors.OnSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Not Available",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            "Your device only supports 60Hz refresh rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClassicColors.OnSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ClassicColors.Primary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = ClassicColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Global Refresh Rate",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ClassicColors.OnSurface
                                )
                                Text(
                                    "Set a fixed refresh rate for your entire system. This will override per-app settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassicColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Refresh Rate Comparison Cards
                item {
                    if (maxRefreshRate > 60) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ClassicRefreshRateCard(
                                rate = maxRefreshRate,
                                modifier = Modifier.weight(1f)
                            )
                            ClassicRefreshRateCard(
                                rate = 60,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        ClassicRefreshRateCard(
                            rate = 60,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Current Status
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ClassicColors.SurfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Current Setting",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassicColors.OnSurfaceVariant
                                )
                                Text(
                                    if (currentRefreshRate == 0) "Auto (Default)" else "${currentRefreshRate}Hz",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ClassicColors.OnSurface
                                )
                            }
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = ClassicColors.Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                // Auto/Default Option
                item {
                    ClassicRefreshRateOption(
                        label = "Auto (Default)",
                        description = "Let system manage refresh rate automatically",
                        isSelected = currentRefreshRate == 0,
                        onClick = {
                            scope.launch {
                                val result = viewModel.resetRefreshRate()
                                if (result.isSuccess) {
                                    currentRefreshRate = 0
                                    android.widget.Toast.makeText(
                                        context,
                                        "Refresh rate reset to auto",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to reset refresh rate",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
                
                // Available Refresh Rates
                availableRefreshRates.forEach { rate ->
                    item {
                        ClassicRefreshRateOption(
                            label = "${rate}Hz",
                            description = when (rate) {
                                60 -> "Standard refresh rate, best battery life"
                                90 -> "Balanced smoothness and battery"
                                120 -> "Maximum smoothness"
                                144 -> "Ultra smooth experience"
                                else -> "Fixed ${rate}Hz refresh rate"
                            },
                            isSelected = currentRefreshRate == rate,
                            onClick = {
                                scope.launch {
                                    val result = viewModel.setForceRefreshRate(rate)
                                    if (result.isSuccess) {
                                        currentRefreshRate = rate
                                        android.widget.Toast.makeText(
                                            context,
                                            "Refresh rate set to ${rate}Hz",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Failed to set refresh rate",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicRefreshRateCard(
    rate: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Phone mockup with animated scrolling
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ClassicColors.SurfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(
                    width = 3.dp,
                    color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Animated scrolling content lines
                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "scroll")
                
                repeat(4) { index ->
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 20f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(
                                durationMillis = when {
                                    rate >= 120 -> 600
                                    rate >= 90 -> 900
                                    else -> 1400
                                },
                                easing = androidx.compose.animation.core.LinearEasing,
                                delayMillis = index * 150
                            ),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "offset_$index"
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (index == 0) ClassicColors.Primary.copy(alpha = 0.3f) 
                               else ClassicColors.SurfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .graphicsLayer {
                                translationY = offsetY
                            }
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            // Placeholder lines
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(8.dp)
                                        .background(
                                            ClassicColors.OnSurfaceVariant.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(8.dp)
                                        .background(
                                            ClassicColors.OnSurfaceVariant.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${rate}Hz",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
            Text(
                when (rate) {
                    60 -> "Standard"
                    90 -> "Smooth Scrolling"
                    120 -> "Smooth Scrolling"
                    144 -> "Ultra Smooth"
                    else -> "Refresh Rate"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClassicRefreshRateOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.SurfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = ClassicColors.Primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = ClassicColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Not selected",
                    tint = ClassicColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun getDeviceMaxRefreshRate(context: Context): Int {
    return try {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        val display = windowManager.defaultDisplay
        val supportedModes = display.supportedModes
        val maxRate = supportedModes.maxOfOrNull { it.refreshRate.toInt() } ?: 60
        maxRate
    } catch (e: Exception) {
        60 // Fallback to 60Hz
    }
}
