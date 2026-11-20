package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.SensorsDomainData

@Composable
fun SensorsDomainCard(
    data: SensorsDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "Sensors & Input Hardware",
        subtitle = "Physical sensors HAL, input devices & camera subsystems",
        icon = Icons.Rounded.Sensors,
        badgeText = "SENSORS HAL",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
