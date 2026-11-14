package id.xms.xtrakernelmanager.ui.screens.setup.components

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

/**
 * Cached GIF image loader with support for animated GIFs across API levels.
 */
@Composable
fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}

/**
 * Reusable stage component displaying an animated GIF character perched on a solid oval pedestal.
 */
@Composable
fun CharacterPedestalStage(
    @DrawableRes drawableResId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    characterWidth: Dp = 260.dp,
    characterHeight: Dp = 164.dp,
    characterBottomPadding: Dp = 32.dp,
    ovalWidth: Dp = 300.dp,
    ovalHeight: Dp = 135.dp,
    ovalBottomPadding: Dp = 8.dp,
    stageWidth: Dp = 320.dp,
    stageHeight: Dp = 260.dp,
) {
    val context = LocalContext.current
    val imageLoader = rememberGifImageLoader()
    val platformColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Box(
        modifier = modifier.size(width = stageWidth, height = stageHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(
            modifier = Modifier
                .width(ovalWidth)
                .height(ovalHeight)
                .align(Alignment.BottomCenter)
                .padding(bottom = ovalBottomPadding),
        ) {
            drawOval(
                color = platformColor,
                topLeft = Offset.Zero,
                size = size,
            )
        }

        AsyncImage(
            model = remember(drawableResId, context) {
                ImageRequest.Builder(context)
                    .data(drawableResId)
                    .crossfade(true)
                    .build()
            },
            imageLoader = imageLoader,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(characterWidth)
                .height(characterHeight)
                .padding(bottom = characterBottomPadding)
                .align(Alignment.BottomCenter),
        )
    }
}
