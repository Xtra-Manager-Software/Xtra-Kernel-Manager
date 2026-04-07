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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedGlobalRefreshRateScreen(
    onNavigateBack: () -> Unit,
    viewModel: FunctionalRomViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val frostedBlobColors = listOf(
        Color(0xFF4A9B8E),
        Color(0xFF8BA8D8),
        Color(0xFF6BC4E8)
    )
    
    // Get device max refresh rate
    val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
    val availableRefreshRates = remember(maxRefreshRate) {
        when {
            maxRefreshRate >= 144 -> listOf(60, 90, 120, 144)
            maxRefreshRate >= 120 -> listOf(60, 90, 120)
            maxRefreshRate >= 90 -> listOf(60, 90)
            else -> emptyList()
        }
    }
    
    var currentRefreshRate by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        currentRefreshRate = viewModel.getCurrentRefreshRate()
        isLoading = false
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize(),
            colors = frostedBlobColors
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            "Global Refresh Rate",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            if (availableRefreshRates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassmorphicCard {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Not Available",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Your device only supports 60Hz refresh rate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info Card
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Global Refresh Rate",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Set a fixed refresh rate for your entire system. This will override per-app settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
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
                                FrostedRefreshRateCard(
                                    rate = maxRefreshRate,
                                    modifier = Modifier.weight(1f)
                                )
                                FrostedRefreshRateCard(
                                    rate = 60,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            FrostedRefreshRateCard(
                                rate = 60,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Current Status
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Current Setting",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        if (currentRefreshRate == 0) "Auto (Default)" else "${currentRefreshRate}Hz",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    // Auto/Default Option
                    item {
                        FrostedRefreshRateOption(
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
                            FrostedRefreshRateOption(
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
}

@Composable
private fun FrostedRefreshRateCard(
    rate: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Phone mockup with animated scrolling
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                        color = if (index == 0) Color.White.copy(alpha = 0.3f) 
                               else Color.White.copy(alpha = 0.15f),
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
                                            Color.White.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(8.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.3f),
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
                color = Color.White
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
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FrostedRefreshRateOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    color = Color.White
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Not selected",
                    tint = Color.White.copy(alpha = 0.5f),
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
        60
    }
}
