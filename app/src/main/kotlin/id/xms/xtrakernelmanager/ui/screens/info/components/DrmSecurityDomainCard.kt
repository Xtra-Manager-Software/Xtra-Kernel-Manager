package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.DrmDomainData

@Composable
fun DrmSecurityDomainCard(
    data: DrmDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "DRM & TrustZone Security",
        subtitle = "Widevine CDM level, KeyMint & display crypto pipelines",
        icon = Icons.Rounded.Security,
        badgeText = "MEDIA DRM",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
