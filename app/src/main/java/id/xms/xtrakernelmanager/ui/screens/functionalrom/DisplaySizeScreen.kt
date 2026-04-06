package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

data class DisplaySizePreset(
    val width: Int,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySizeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get current smallest width in dp
    val currentSmallestWidth = remember {
        val config = context.resources.configuration
        config.smallestScreenWidthDp
    }
    
    val currentDPI = remember {
        context.resources.displayMetrics.densityDpi
    }
    
    var selectedWidth by remember { mutableStateOf(currentSmallestWidth) }
    var customWidth by remember { mutableStateOf(currentSmallestWidth.toString()) }
    var isApplying by remember { mutableStateOf(false) }
    var useCustom by remember { mutableStateOf(false) }
    
    // Preset with modern identities
    val presets = remember {
        listOf(
            DisplaySizePreset(
                width = 320,
                name = "Large",
                description = "Easier to tap, less content",
                icon = Icons.Default.ZoomIn,
                color = Color(0xFF34C759)
            ),
            DisplaySizePreset(
                width = 360,
                name = "Default",
                description = "Balanced size",
                icon = Icons.Default.PhoneAndroid,
                color = Color(0xFF007AFF)
            ),
            DisplaySizePreset(
                width = 411,
                name = "Compact",
                description = "More content visible",
                icon = Icons.Default.Compress,
                color = Color(0xFF5856D6)
            ),
            DisplaySizePreset(
                width = 480,
                name = "Dense",
                description = "High information density",
                icon = Icons.Default.GridView,
                color = Color(0xFFFF9F0A)
            ),
            DisplaySizePreset(
                width = 540,
                name = "Ultra Dense",
                description = "Maximum content",
                icon = Icons.Default.ViewCompact,
                color = Color(0xFFFF3B30)
            ),
            DisplaySizePreset(
                width = 600,
                name = "Tablet",
                description = "Tablet-like experience",
                icon = Icons.Default.TabletAndroid,
                color = Color(0xFF9C27B0)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                onClick = onNavigateBack
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp).clickable(onClick = onNavigateBack)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Display Size",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Current value badge
                    Surface(
                        color = Color(0xFF007AFF).copy(0.25f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$currentSmallestWidth dp",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Info Card
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Current: $currentSmallestWidth dp",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "DPI: $currentDPI",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                // Preset Cards
                items(presets.size) { index ->
                    val preset = presets[index]
                    val isSelected = !useCustom && selectedWidth == preset.width
                    
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            useCustom = false
                            selectedWidth = preset.width
                        }
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) preset.color.copy(alpha = 0.3f)
                                        else preset.color.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = null,
                                    tint = preset.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) preset.color else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${preset.width} dp • ${preset.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            if (isSelected) {
                                Surface(
                                    color = preset.color.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = preset.color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Input
                item {
                    Text(
                        text = "CUSTOM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                    
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { useCustom = true }
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (useCustom) Color(0xFF00C7BE).copy(alpha = 0.3f)
                                        else Color(0xFF00C7BE).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF00C7BE),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            OutlinedTextField(
                                value = customWidth,
                                onValueChange = { 
                                    customWidth = it.filter { char -> char.isDigit() }
                                    useCustom = true 
                                },
                                label = { Text("Custom width (dp)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00C7BE),
                                    focusedLabelColor = Color(0xFF00C7BE)
                                )
                            )
                        }
                    }
                }

                // Action Buttons
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isApplying = true
                                    try {
                                        val targetWidth = if (useCustom) customWidth.toIntOrNull() ?: selectedWidth else selectedWidth
                                        
                                        if (targetWidth < 200 || targetWidth > 900) {
                                            android.widget.Toast.makeText(context, "Please enter a valid width (200-900)", android.widget.Toast.LENGTH_SHORT).show()
                                            isApplying = false
                                            return@launch
                                        }

                                        withContext(Dispatchers.IO) {
                                            val displayMetrics = context.resources.displayMetrics
                                            val smallestPixels = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
                                            val newDensity = (smallestPixels.toFloat() / targetWidth.toFloat() * 160).toInt()
                                            id.xms.xtrakernelmanager.utils.RootShell.execute("wm density $newDensity")
                                        }
                                        android.widget.Toast.makeText(context, "Applied! Screen may flicker.", android.widget.Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isApplying = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isApplying,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF)
                            )
                        ) {
                            if (isApplying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp), 
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Applying...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply Changes", color = Color.White)
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            id.xms.xtrakernelmanager.utils.RootShell.execute("wm density reset")
                                        }
                                        android.widget.Toast.makeText(context, "Reset to default!", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset to Default")
                        }
                    }
                }
            }
        }
    }
}
