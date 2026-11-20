package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.GpuDomainData

@Composable
fun GpuDomainCard(
    data: GpuDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "GPU & Vulkan Graphics",
        subtitle = "Vulkan driver, extensions & graphics pipeline",
        icon = Icons.Rounded.VideogameAsset,
        badgeText = "VULKAN API",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
