package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ClassicDisplaySizePreset(
    val width: Int,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicDisplaySizeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
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
    
    val presets = remember {
        listOf(
            ClassicDisplaySizePreset(
                width = 320,
                name = "Large",
                description = "Easier to tap, less content",
                icon = Icons.Rounded.ZoomIn,
                color = Color(0xFF34C759)
            ),
            ClassicDisplaySizePreset(
                width = 360,
                name = "Default",
                description = "Balanced size",
                icon = Icons.Rounded.PhoneAndroid,
                color = Color(0xFF007AFF)
            ),
            ClassicDisplaySizePreset(
                width = 411,
                name = "Compact",
                description = "More content visible",
                icon = Icons.Rounded.Compress,
                color = Color(0xFF5856D6)
            ),
            ClassicDisplaySizePreset(
                width = 480,
                name = "Dense",
                description = "High information density",
                icon = Icons.Rounded.GridView,
                color = Color(0xFFFF9F0A)
            ),
            ClassicDisplaySizePreset(
                width = 540,
                name = "Ultra Dense",
                description = "Maximum content",
                icon = Icons.Rounded.ViewCompact,
                color = Color(0xFFFF3B30)
            ),
            ClassicDisplaySizePreset(
                width = 600,
                name = "Tablet",
                description = "Tablet-like experience",
                icon = Icons.Rounded.TabletAndroid,
                color = Color(0xFF9C27B0)
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Display Size",
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
                    containerColor = ClassicColors.Background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ClassicColors.Primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = ClassicColors.Primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = ClassicColors.Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Current Display",
                                style = MaterialTheme.typography.labelMedium,
                                color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$currentSmallestWidth dp",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = ClassicColors.Primary
                            )
                            Text(
                                text = "DPI: $currentDPI",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Preset Cards
            items(presets.size) { index ->
                val preset = presets[index]
                val isSelected = !useCustom && selectedWidth == preset.width
                
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        preset.color.copy(alpha = 0.15f)
                    } else {
                        ClassicColors.SurfaceContainerHigh
                    },
                    label = "bg_color"
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
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
                        Surface(
                            color = preset.color.copy(alpha = if (isSelected) 0.3f else 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = null,
                                    tint = preset.color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) preset.color else ClassicColors.OnSurface
                            )
                            Text(
                                text = "${preset.width} dp",
                                style = MaterialTheme.typography.labelLarge,
                                color = ClassicColors.OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ClassicColors.OnSurfaceVariant
                            )
                        }
                        
                        if (isSelected) {
                            Surface(
                                color = preset.color,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
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
                    text = "Custom",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                
                val customColor = Color(0xFF00C7BE)
                val isCustomSelected = useCustom
                
                val customBgColor by animateColorAsState(
                    targetValue = if (isCustomSelected) {
                        customColor.copy(alpha = 0.15f)
                    } else {
                        ClassicColors.SurfaceContainerHigh
                    },
                    label = "custom_bg"
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = customBgColor
                    ),
                    onClick = { useCustom = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = customColor.copy(alpha = if (isCustomSelected) 0.3f else 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = customColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = customWidth,
                            onValueChange = { 
                                customWidth = it.filter { char -> char.isDigit() }
                                useCustom = true 
                            },
                            label = { 
                                Text(
                                    "Enter custom width (dp)",
                                    color = ClassicColors.OnSurfaceVariant
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = customColor,
                                unfocusedBorderColor = ClassicColors.OnSurfaceVariant.copy(alpha = 0.3f),
                                focusedLabelColor = customColor,
                                unfocusedLabelColor = ClassicColors.OnSurfaceVariant,
                                cursorColor = customColor,
                                focusedTextColor = ClassicColors.OnSurface,
                                unfocusedTextColor = ClassicColors.OnSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 16.dp)
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
                            containerColor = ClassicColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isApplying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), 
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Applying...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply Changes", fontWeight = FontWeight.Bold)
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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ClassicColors.OnSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            ClassicColors.OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset to Default", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
