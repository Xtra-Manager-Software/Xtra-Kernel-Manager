package id.xms.xtrakernelmanager.ui.screens.misc.classic.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicMiscHubCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClassicColors.SurfaceContainerHigh)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Icon Silhouette
        Icon(
            imageVector = Icons.Rounded.Widgets,
            contentDescription = null,
            tint = ClassicColors.OnSurface.copy(alpha = 0.05f),
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 40.dp)
        )
        
        // Content
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Miscellaneous",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                fontSize = 32.sp
            )
            Text(
                text = "Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Access system utilities, battery analytics, gaming features, and advanced settings. Manage your device with powerful tools and customization options.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurfaceVariant,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
