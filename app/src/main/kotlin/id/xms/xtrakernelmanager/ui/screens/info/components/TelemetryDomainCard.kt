package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.TelemetryDomainData

@Composable
fun TelemetryDomainCard(
    data: TelemetryDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "Telemetry & Hardware State",
        subtitle = "Live CPU load, clock frequencies, thermal zones & battery",
        icon = Icons.Rounded.Speed,
        badgeText = "LIVE SNAPSHOT",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
