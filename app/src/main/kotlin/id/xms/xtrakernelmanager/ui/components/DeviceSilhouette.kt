package id.xms.xtrakernelmanager.ui.components

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeviceSilhouette(
    modifier: Modifier = Modifier,
    color: Color? = null,
    screenColor: Color? = null,
    showWallpaper: Boolean = true,
    fullBody: Boolean = false,
    phoneWidth: Dp = 76.dp,
    phoneHeight: Dp = 98.dp,
    cornerRadius: Dp = 16.dp,
) {
    val innerCornerRadius = (cornerRadius - 4.dp).coerceAtLeast(6.dp)
    val cameraSize = 6.dp
    val cameraPadding = 5.dp

    val context = LocalContext.current
    val wallpaperBitmap by produceState<ImageBitmap?>(initialValue = null, showWallpaper) {
        if (showWallpaper) {
            value = withContext(Dispatchers.IO) {
                loadWallpaper(context)
            }
        }
    }

    val frameColor = color ?: MaterialTheme.colorScheme.onSurfaceVariant
    val bezelColor = color?.copy(alpha = 0.5f) ?: MaterialTheme.colorScheme.surfaceVariant
    val screenTopColor = screenColor ?: color?.copy(alpha = 0.4f) ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    val screenBottomColor = screenColor ?: color?.copy(alpha = 0.6f) ?: MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .width(phoneWidth)
            .height(phoneHeight)
            .clip(
                if (fullBody) RoundedCornerShape(cornerRadius)
                else RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
            )
            .drawWithCache {
                val strokeWidth = 2.dp.toPx()
                val cornerRadiusPx = cornerRadius.toPx()
                val inset = strokeWidth / 2

                val borderPath = Path().apply {
                    moveTo(inset, size.height)
                    lineTo(inset, cornerRadiusPx)
                    arcTo(
                        rect = Rect(
                            inset,
                            inset,
                            (cornerRadiusPx * 2) - inset,
                            (cornerRadiusPx * 2) - inset,
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(size.width - cornerRadiusPx, inset)
                    arcTo(
                        rect = Rect(
                            size.width - (cornerRadiusPx * 2) + inset,
                            inset,
                            size.width - inset,
                            (cornerRadiusPx * 2) - inset,
                        ),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(size.width - inset, size.height)
                }

                val stroke = Stroke(width = strokeWidth)

                onDrawBehind {
                    if (fullBody) {
                        drawRoundRect(
                            color = bezelColor,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                            style = Fill,
                        )
                        drawRoundRect(
                            color = frameColor,
                            topLeft = Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - strokeWidth,
                                size.height - strokeWidth,
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx - inset),
                            style = stroke,
                        )
                    } else {
                        drawRect(color = bezelColor, style = Fill)
                        drawPath(
                            path = borderPath,
                            color = frameColor,
                            style = stroke,
                        )
                    }
                }
            }
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    if (fullBody) RoundedCornerShape(innerCornerRadius)
                    else RoundedCornerShape(topStart = innerCornerRadius, topEnd = innerCornerRadius),
                )
                .then(
                    if (wallpaperBitmap == null) {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                colors = listOf(screenTopColor, screenBottomColor),
                                start = Offset.Zero,
                                end = Offset.Infinite,
                            ),
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            wallpaperBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            // Punch hole camera
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = cameraPadding)
                    .size(cameraSize)
                    .clip(CircleShape)
                    .background(Color.Black),
            )

            // Screen reflection overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            ),
                        ),
                    ),
            )
        }
    }
}

private fun loadWallpaper(context: android.content.Context): ImageBitmap? {
    return try {
        val wallpaperManager = WallpaperManager.getInstance(context)

        // 1. Try getWallpaperFile if available (API 24+)
        val pfd = try {
            wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
        } catch (_: Throwable) {
            null
        }

        if (pfd != null) {
            pfd.use {
                val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                val bmp = BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, options)
                if (bmp != null) {
                    val scaled = Bitmap.createScaledBitmap(bmp, 180, 240, true)
                    return scaled.asImageBitmap()
                }
            }
        }

        // 2. Try drawable / peekDrawable
        val drawable = try {
            wallpaperManager.drawable ?: wallpaperManager.peekDrawable()
        } catch (_: Throwable) {
            null
        }

        if (drawable != null) {
            val targetWidth = 180
            val targetHeight = 240
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                Bitmap.createScaledBitmap(drawable.bitmap, targetWidth, targetHeight, true)
            } else {
                drawable.toBitmap(width = targetWidth, height = targetHeight)
            }
            return bitmap.asImageBitmap()
        }

        null
    } catch (_: Throwable) {
        null
    }
}
