package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.SystemDomainData

@Composable
fun SystemDomainCard(
    data: SystemDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "Kernel & System Platform",
        subtitle = "Linux kernel release, Android OS build & Treble stack",
        icon = Icons.Rounded.Terminal,
        badgeText = "SYSFS & BUILD",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
