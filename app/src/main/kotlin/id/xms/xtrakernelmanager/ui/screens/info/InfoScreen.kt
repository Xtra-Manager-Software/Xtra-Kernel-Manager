package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.domain.native.AllNativeDomainsSnapshot
import id.xms.xtrakernelmanager.domain.native.NativeInfoRepository
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.ui.screens.info.components.DrmSecurityDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.FpsDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.GpuDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.MediaDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.ModulesDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.NetworkDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.NpuDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.SensorsDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.SystemDomainCard
import id.xms.xtrakernelmanager.ui.screens.info.components.TelemetryDomainCard
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded
import kotlinx.coroutines.launch

private enum class InfoDomainTab(val label: String) {
    ALL("All Subsystems"),
    TELEMETRY("Telemetry"),
    SYSTEM("System"),
    GPU("GPU"),
    DRM("DRM"),
    SENSORS("Sensors"),
    NPU("NPU"),
    MEDIA("Media"),
    NETWORK("Network"),
    MODULES("Modules"),
    FPS("FPS"),
}

@Composable
fun InfoScreen(
    onNavigateToProcessor: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    var snapshot by remember { mutableStateOf(AllNativeDomainsSnapshot()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(InfoDomainTab.ALL) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            isLoading = true
            snapshot = NativeInfoRepository.loadAllDomains(context)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "System Subsystems",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Live JNI & Kernel Diagnostics",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = GoogleSansRounded,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = { reload() },
                enabled = !isLoading,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Domain Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoDomainTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = GoogleSansRounded,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            val navInsets = WindowInsets.navigationBars.asPaddingValues()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 100.dp + navInsets.calculateBottomPadding(),
                ),
            ) {
                // Engine Status Banner
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (snapshot.isNativeLoaded) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = if (snapshot.isNativeLoaded) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = if (snapshot.isNativeLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (snapshot.isNativeLoaded) {
                                        "Native Engine: Connected (libxkm_native.so)"
                                    } else {
                                        "Native Engine: Standby (Fallback active)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GoogleSansRounded,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (snapshot.isNativeLoaded) {
                                        "Direct Linux kernel and hardware subsystem polling active."
                                    } else {
                                        "Could not link native binary; operating in safe mode."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Cards rendered based on selectedTab
                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.TELEMETRY) {
                    item(key = "telemetry") {
                        TelemetryDomainCard(data = snapshot.telemetry)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.SYSTEM) {
                    item(key = "system") {
                        SystemDomainCard(data = snapshot.system)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.GPU) {
                    item(key = "gpu") {
                        GpuDomainCard(data = snapshot.gpu)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.DRM) {
                    item(key = "drm") {
                        DrmSecurityDomainCard(data = snapshot.drm)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.SENSORS) {
                    item(key = "sensors") {
                        SensorsDomainCard(data = snapshot.sensors)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.NPU) {
                    item(key = "npu") {
                        NpuDomainCard(data = snapshot.npu)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.MEDIA) {
                    item(key = "media") {
                        MediaDomainCard(data = snapshot.media)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.NETWORK) {
                    item(key = "network") {
                        NetworkDomainCard(data = snapshot.network)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.MODULES) {
                    item(key = "modules") {
                        ModulesDomainCard(data = snapshot.modules)
                    }
                }

                if (selectedTab == InfoDomainTab.ALL || selectedTab == InfoDomainTab.FPS) {
                    item(key = "fps") {
                        FpsDomainCard(data = snapshot.fps)
                    }
                }
            }
        }
    }
}
