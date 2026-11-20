package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.NpuDomainData

@Composable
fun NpuDomainCard(
    data: NpuDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "NPU & AI Coprocessors",
        subtitle = "Hexagon/HTP tensor accelerators & remoteproc frameworks",
        icon = Icons.Rounded.Memory,
        badgeText = "AI ENGINE",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
