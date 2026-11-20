package id.xms.xtrakernelmanager.ui.screens.processor.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.device.ProcessorDetailSpec
import id.xms.xtrakernelmanager.ui.components.ExpressiveCard
import id.xms.xtrakernelmanager.ui.components.ExpressiveCardHeader

/**
 * Reusable hero card displaying System on Chip details and vendor emblem.
 */
@Composable
fun HeroSocCard(
    spec: ProcessorDetailSpec,
    modifier: Modifier = Modifier,
) {
    ExpressiveCard(
        modifier = modifier,
        contentPadding = 20.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ExpressiveCardHeader(category = "SOC CHIPSET")

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SM7475",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = spec.vendor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (spec.logoRes == R.drawable.ic_exynos || spec.logoRes == R.drawable.ic_kirin) {
                    Image(
                        painter = painterResource(spec.logoRes),
                        contentDescription = "SoC Logo",
                        modifier = Modifier.size(50.dp),
                    )
                } else {
                    Icon(
                        painter = painterResource(spec.logoRes),
                        contentDescription = "SoC Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp),
                    )
                }
            }
        }
    }
}
