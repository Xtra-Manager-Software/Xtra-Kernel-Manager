package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.FpsDomainData

@Composable
fun FpsDomainCard(
    data: FpsDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "FPS & Frame Timing",
        subtitle = "eBPF kernel tracepoints, target package & frame drops",
        icon = Icons.Rounded.Timer,
        badgeText = "EBPF TRACE",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
