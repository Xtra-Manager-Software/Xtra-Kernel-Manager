package id.xms.xtrakernelmanager.ui.screens.root

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.BuildConfig

@Composable
fun RootAccessScreen(
    onGrantPermissions: () -> Unit,
    onTryAgain: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = LocalUriHandler.current
    
    // Animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1929),
                        Color(0xFF1A2332),
                        Color(0xFF0A1929)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title at top
            Text(
                text = "System Access",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.offset(y = (-120).dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animated Icon Container
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(y = (-60).dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating gradient ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(rotation / 360f * 0.1f + 0.95f)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.3f),
                                    Color(0xFF818CF8).copy(alpha = 0.3f),
                                    Color(0xFF38BDF8).copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .blur(40.dp)
                )
                
                // Inner circle background
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2D3748),
                                    Color(0xFF1A202C)
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Icon - Android Robot with Root symbol
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Android,
                        contentDescription = "Android Root",
                        modifier = Modifier.size(140.dp),
                        tint = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Title
            Text(
                text = "Root Access",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Required",
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            Text(
                text = "This application requires administrative privileges to modify low-level system parameters and optimize kernel performance.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Grant Permissions Button
            Button(
                onClick = {
                    // Detect and open root manager app
                    val targetNames = listOf("KernelSU", "Magisk", "APatch", "SukiSU")
                    val packageManager = context.packageManager
                    val installedApps = packageManager.getInstalledApplications(0)
                    var detectedRootManager: Pair<String, String>? = null
                    
                    for (app in installedApps) {
                        try {
                            val appName = packageManager.getApplicationLabel(app).toString()
                            for (targetName in targetNames) {
                                if (appName.contains(targetName, ignoreCase = true)) {
                                    detectedRootManager = Pair(app.packageName, appName)
                                    break
                                }
                            }
                            if (detectedRootManager != null) break
                        } catch (e: Exception) {
                            // Continue to next app
                        }
                    }
                    
                    // Open detected root manager or call the callback
                    if (detectedRootManager != null) {
                        try {
                            val intent = packageManager.getLaunchIntentForPackage(detectedRootManager.first)
                            if (intent != null) {
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    // Also trigger the callback to check root after user returns
                    onGrantPermissions()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "GRANT PERMISSIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A1929),
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Try Again Button
            TextButton(
                onClick = onTryAgain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TRY AGAIN",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Learn More Button
            TextButton(
                onClick = {
                    uriHandler.openUri("https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/blob/staging-version-3.1/How%20To%20Root.md")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Learn More",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Version info at bottom
            Text(
                text = "XTRA KERNEL MANAGER ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                letterSpacing = 2.sp
            )
        }
    }
}
