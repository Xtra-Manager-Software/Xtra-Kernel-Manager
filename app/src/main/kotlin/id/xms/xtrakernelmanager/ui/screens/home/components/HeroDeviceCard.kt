package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.domain.device.DeviceInfoResolver
import id.xms.xtrakernelmanager.domain.device.DeviceSpec
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader
import id.xms.xtrakernelmanager.ui.components.ExpressivePill

@Composable
fun HeroDeviceCard(
    modifier: Modifier = Modifier,
    spec: DeviceSpec = remember { DeviceInfoResolver.resolveDeviceSpec() },
    onClick: () -> Unit = {},
) {
    ExpressiveCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Device Mock on the bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .offset(y = 6.dp),
            ) {
                DeviceSilhouette(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f),
                    showWallpaper = true,
                    phoneWidth = 72.dp,
                    phoneHeight = 92.dp,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Top Row: Manufacturer on left, Board pill on right
                ExpressiveCardHeader(
                    category = spec.manufacturer,
                    tag = spec.board,
                    categoryColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    tagContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Left Column: Model Headline, Codename Subtitle, and Kernel Pill
                Column(
                    modifier = Modifier.padding(end = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = spec.commercialModel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = if (spec.commercialModel != spec.model) "${spec.model} (${spec.codename})" else spec.codename,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressivePill(
                        text = spec.kernelVersion,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        isMonospace = true,
                        horizontalPadding = 12.dp,
                        verticalPadding = 6.dp,
                    )
                }
            }
        }
    }
}
