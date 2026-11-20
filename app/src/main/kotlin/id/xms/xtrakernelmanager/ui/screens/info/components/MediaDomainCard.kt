package id.xms.xtrakernelmanager.ui.screens.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.domain.native.MediaDomainData

@Composable
fun MediaDomainCard(
    data: MediaDomainData,
    modifier: Modifier = Modifier,
) {
    DomainCardBase(
        title = "Media Codecs & Audio",
        subtitle = "Hardware video decoding/encoding pipelines & Hi-Res audio",
        icon = Icons.Rounded.Movie,
        badgeText = "CODEC2 / OMX",
        metrics = data.metrics,
        rawJson = data.rawJson,
        modifier = modifier,
    )
}
