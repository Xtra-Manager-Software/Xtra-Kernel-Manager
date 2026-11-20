package id.xms.xtrakernelmanager.ui.screens.processor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.domain.device.DeviceInfoResolver
import id.xms.xtrakernelmanager.domain.device.ProcessorDetailSpec
import id.xms.xtrakernelmanager.ui.screens.processor.components.ClusterDomainCard
import id.xms.xtrakernelmanager.ui.screens.processor.components.CompactCpuCoreCard
import id.xms.xtrakernelmanager.ui.screens.processor.components.HeroSocCard
import id.xms.xtrakernelmanager.ui.screens.processor.components.PrimeCpuCoreCard
import id.xms.xtrakernelmanager.ui.screens.processor.components.resolveClusterDomainList

@Composable
fun ProcessorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    spec: ProcessorDetailSpec = remember { DeviceInfoResolver.resolveProcessorDetailSpec() },
) {
    val clusterDomains = remember(spec) { resolveClusterDomainList(spec) }

    BackHandler(enabled = true) {
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        // --- 1. TOP PINNED HEADER AREA ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = onNavigateBack,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Processor",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "System on Chip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // --- 2. FOREGROUND SHEET ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = WindowInsets.navigationBars.asPaddingValues().let { navInsets ->
                    PaddingValues(
                        top = 16.dp,
                        bottom = 32.dp + navInsets.calculateBottomPadding(),
                    )
                },
            ) {
                // 1. Reusable Hero SoC Card
                item(key = "hero_soc_card") {
                    HeroSocCard(
                        spec = spec,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // 2. Reusable Side-by-Side Little & Big Cores
                item(key = "little_and_big_cores_row") {
                    val littleCluster = spec.clusters.getOrNull(2)
                    val bigCluster = spec.clusters.getOrNull(1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (littleCluster != null) {
                            CompactCpuCoreCard(
                                cluster = littleCluster,
                                pillText = "LITTLE",
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (bigCluster != null) {
                            CompactCpuCoreCard(
                                cluster = bigCluster,
                                pillText = "BIG",
                                accentColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // 3. Reusable Prime Core Card
                item(key = "prime_core_card") {
                    val primeCluster = spec.clusters.firstOrNull()
                    if (primeCluster != null) {
                        PrimeCpuCoreCard(
                            cluster = primeCluster,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // 4. Section Header: Cluster Dynamics
                item(key = "cluster_dynamics_section_header") {
                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                        Text(
                            text = "Cluster Dynamics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Governor curve, workload utilization, and thermal envelope per domain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                }

                // 5. Reusable Telemetry & Scaling Curve Cards for Prime, Big, and Little
                item(key = "prime_domain_card") {
                    val primeDomain = clusterDomains.getOrNull(0)
                    if (primeDomain != null) {
                        ClusterDomainCard(
                            domain = primeDomain,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item(key = "big_domain_card") {
                    val bigDomain = clusterDomains.getOrNull(1)
                    if (bigDomain != null) {
                        ClusterDomainCard(
                            domain = bigDomain,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item(key = "little_domain_card") {
                    val littleDomain = clusterDomains.getOrNull(2)
                    if (littleDomain != null) {
                        ClusterDomainCard(
                            domain = littleDomain,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
