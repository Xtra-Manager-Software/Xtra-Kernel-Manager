package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.NetworkDomainData

@Composable
fun NetworkDomainCard(
    data: NetworkDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "Network & Connectivity",
        subtitle = "Wi-Fi link speeds, telephony bands & TCP congestion control",
        icon = Icons.Rounded.Wifi,
        badgeText = "SOCKETS & HAL",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
