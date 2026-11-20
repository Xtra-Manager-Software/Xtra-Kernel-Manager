package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.device.DeviceInfoResolver
import id.xms.xtrakernelmanager.domain.device.ProcessorSpec
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader

@Composable
fun ProcessorCard(
    modifier: Modifier = Modifier,
    spec: ProcessorSpec = remember { DeviceInfoResolver.resolveProcessorSpec() },
    onClick: () -> Unit = {},
) {
    ExpressiveCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 76.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                ExpressiveCardHeader(category = "PROCESSOR")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = spec.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = spec.platform,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Right: Clean Logo without circle background, vertically centered to card height
            Box(
                modifier = Modifier.align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center,
            ) {
                if (spec.logoRes == R.drawable.ic_exynos || spec.logoRes == R.drawable.ic_kirin) {
                    Image(
                        painter = painterResource(spec.logoRes),
                        contentDescription = "SoC Logo",
                        modifier = Modifier.size(56.dp),
                    )
                } else {
                    Icon(
                        painter = painterResource(spec.logoRes),
                        contentDescription = "SoC Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
        }
    }
}
