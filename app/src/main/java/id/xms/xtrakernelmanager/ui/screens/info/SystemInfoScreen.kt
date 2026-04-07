package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun SystemInfoScreen(
    layoutStyle: String,
    onNavigateBack: () -> Unit
) {
    when (layoutStyle) {
        "frosted" -> FrostedSystemInfoScreen(onNavigateBack)
        "material" -> MaterialSystemInfoScreen(onNavigateBack)
        "classic" -> ClassicSystemInfoScreen(onNavigateBack)
        else -> FrostedSystemInfoScreen(onNavigateBack)
    }
}

// Get system information (shared)
@Composable
private fun getSystemInfo(): Map<String, String> {
    return remember {
        // Check bootloader status
        val bootloaderStatus = getBootloaderStatus()
        
        mapOf(
            "Device Model" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "Android Version" to "Android ${android.os.Build.VERSION.RELEASE}",
            "SDK Level" to "API ${android.os.Build.VERSION.SDK_INT}",
            "Build ID" to android.os.Build.DISPLAY,
            "Security Patch" to (android.os.Build.VERSION.SECURITY_PATCH ?: "Unknown"),
            "Kernel Version" to (System.getProperty("os.version") ?: "Unknown"),
            "Build Type" to android.os.Build.TYPE,
            "Build Tags" to android.os.Build.TAGS,
            "Bootloader Status" to bootloaderStatus,
            "Build Fingerprint" to android.os.Build.FINGERPRINT,
            "Board" to android.os.Build.BOARD,
            "Hardware" to android.os.Build.HARDWARE,
            "Product" to android.os.Build.PRODUCT,
            "Device" to android.os.Build.DEVICE,
            "Radio Version" to (android.os.Build.getRadioVersion() ?: "Unknown")
        )
    }
}

// Check bootloader unlock status
private fun getBootloaderStatus(): String {
    return try {
        // Method 1: Check ro.boot.flash.locked property (Xiaomi/Redmi devices)
        val flashLocked = try {
            val process = Runtime.getRuntime().exec("getprop ro.boot.flash.locked")
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
        
        if (flashLocked == "0") {
            return "Unlocked"
        }
        
        // Method 2: Check ro.boot.verifiedbootstate property
        val verifiedBootState = try {
            val process = Runtime.getRuntime().exec("getprop ro.boot.verifiedbootstate")
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
        
        when (verifiedBootState) {
            "orange" -> return "Unlocked"
            "green" -> return "Locked"
            "red" -> return "Unlocked (Unverified)"
        }
        
        // Method 3: Check ro.boot.veritymode property
        val verityMode = try {
            val process = Runtime.getRuntime().exec("getprop ro.boot.veritymode")
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
        
        if (verityMode == "enforcing") {
            return "Locked"
        }
        
        // Method 4: Check ro.secureboot.lockstate property
        val lockState = try {
            val process = Runtime.getRuntime().exec("getprop ro.secureboot.lockstate")
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
        
        when (lockState) {
            "unlocked" -> return "Unlocked"
            "locked" -> return "Locked"
        }
        
        // Method 5: Check sys.oem_unlock_allowed property
        val oemUnlock = try {
            val process = Runtime.getRuntime().exec("getprop sys.oem_unlock_allowed")
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
        
        if (oemUnlock == "1") {
            return "Unlocked"
        }
        
        // Method 6: Check if device is rooted (strong indicator of unlocked bootloader)
        val isRooted = try {
            val suCheck = Runtime.getRuntime().exec("which su")
            suCheck.waitFor()
            suCheck.exitValue() == 0
        } catch (e: Exception) {
            false
        }
        
        if (isRooted) {
            return "Unlocked (Root Detected)"
        }
        
        // If all methods fail, return unknown
        "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

// Frosted System Info Screen
@Composable
private fun FrostedSystemInfoScreen(onNavigateBack: () -> Unit) {
    val systemInfo = getSystemInfo()
    
    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            GlassmorphicCard(
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
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "System Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
            
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                // Hero Header
                item {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFB74D),
                                            Color(0xFFEC407A),
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.logo_a),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.Unspecified
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = stringResource(R.string.app_name_short),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 42.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Android ${android.os.Build.VERSION.RELEASE}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White.copy(0.9f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        text = "Latest system version",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                }
                
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        FrostedSystemInfoRow(
                            label = "Build Number",
                            value = android.os.Build.DISPLAY
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                systemInfo.forEach { (label, value) ->
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            FrostedSystemInfoRow(label = label, value = value)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FrostedSystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.weight(0.4f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

// Material System Info Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialSystemInfoScreen(onNavigateBack: () -> Unit) {
    val systemInfo = getSystemInfo()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Information",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
        ) {
            // Hero Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFB74D),
                                        Color(0xFFEC407A),
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.logo_a),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Unspecified
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "XKM",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Android ${android.os.Build.VERSION.RELEASE}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White.copy(0.9f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Text(
                    text = "Latest system version",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    MaterialSystemInfoRow(
                        label = "Build Number",
                        value = android.os.Build.DISPLAY
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            systemInfo.forEach { (label, value) ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        MaterialSystemInfoRow(label = label, value = value)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MaterialSystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

// Classic System Info Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicSystemInfoScreen(onNavigateBack: () -> Unit) {
    val systemInfo = getSystemInfo()
    
    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Information",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = ClassicColors.OnSurface
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
                    containerColor = ClassicColors.Background
                )
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
        ) {
            // Hero Header
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = ClassicColors.SurfaceContainerHigh
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFB74D),
                                        Color(0xFFEC407A),
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.logo_a),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Unspecified
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "XKM",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Android ${android.os.Build.VERSION.RELEASE}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White.copy(0.9f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Text(
                    text = "Latest system version",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClassicColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
            }
            
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ClassicColors.SurfaceContainerHigh
                ) {
                    ClassicSystemInfoRow(
                        label = "Build Number",
                        value = android.os.Build.DISPLAY
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            systemInfo.forEach { (label, value) ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ClassicColors.SurfaceContainerHigh
                    ) {
                        ClassicSystemInfoRow(label = label, value = value)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ClassicSystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ClassicColors.OnSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = ClassicColors.OnSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}
