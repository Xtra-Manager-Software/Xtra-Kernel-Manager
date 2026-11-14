package id.xms.xtrakernelmanager.ui.screens.setup.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class SetupIconPlacement(
    val size: Dp,
    val color: Color,
    val align: Alignment,
    val rot: Float,
    val shape: Shape,
    val offsetX: Dp,
    val offsetY: Dp,
)

@Composable
fun SetupIconCollage(
    icons: List<ImageVector>,
    modifier: Modifier = Modifier,
    height: Dp = 210.dp,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val minDim = minOf(180.dp, maxHeight)
        val primaryCol = MaterialTheme.colorScheme.primary
        val secCol = MaterialTheme.colorScheme.secondary
        val tertCol = MaterialTheme.colorScheme.tertiary
        val onSurf = MaterialTheme.colorScheme.onSurfaceVariant

        val configs = remember(minDim, primaryCol, secCol, tertCol, onSurf) {
            listOf(
                SetupIconPlacement(
                    size = minDim * 0.72f,
                    color = secCol,
                    align = Alignment.Center,
                    rot = -12f,
                    shape = RoundedCornerShape(24.dp),
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.42f,
                    color = onSurf,
                    align = Alignment.TopStart,
                    rot = 16f,
                    shape = CircleShape,
                    offsetX = 18.dp,
                    offsetY = 6.dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.44f,
                    color = primaryCol,
                    align = Alignment.BottomEnd,
                    rot = 8f,
                    shape = CircleShape,
                    offsetX = (-18).dp,
                    offsetY = (-6).dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.48f,
                    color = tertCol,
                    align = Alignment.TopEnd,
                    rot = -18f,
                    shape = RoundedCornerShape(20.dp),
                    offsetX = (-22).dp,
                    offsetY = 8.dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.38f,
                    color = secCol,
                    align = Alignment.BottomStart,
                    rot = 12f,
                    shape = RoundedCornerShape(16.dp),
                    offsetX = 24.dp,
                    offsetY = (-8).dp,
                ),
            )
        }

        configs.forEachIndexed { i, cfg ->
            val icon = icons.getOrNull(i % icons.size) ?: return@forEachIndexed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(cfg.align)
                    .offset(x = cfg.offsetX, y = cfg.offsetY),
            ) {
                Surface(
                    modifier = Modifier
                        .size(cfg.size)
                        .graphicsLayer { rotationZ = cfg.rot },
                    shape = cfg.shape,
                    color = cfg.color.copy(alpha = 0.14f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = cfg.color,
                            modifier = Modifier.size(cfg.size * 0.52f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetupDrawableCollage(
    drawables: List<Int>,
    modifier: Modifier = Modifier,
    height: Dp = 210.dp,
    useDynamicTint: Boolean = true,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val minDim = minOf(180.dp, maxHeight)
        val primaryCol = MaterialTheme.colorScheme.primary
        val secCol = MaterialTheme.colorScheme.secondary
        val tertCol = MaterialTheme.colorScheme.tertiary

        val configs = remember(minDim, primaryCol, secCol, tertCol) {
            listOf(
                SetupIconPlacement(
                    size = minDim * 0.72f,
                    color = primaryCol,
                    align = Alignment.Center,
                    rot = -8f,
                    shape = RoundedCornerShape(26.dp),
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.44f,
                    color = secCol,
                    align = Alignment.TopStart,
                    rot = 14f,
                    shape = CircleShape,
                    offsetX = 16.dp,
                    offsetY = 6.dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.44f,
                    color = tertCol,
                    align = Alignment.BottomEnd,
                    rot = -12f,
                    shape = CircleShape,
                    offsetX = (-16).dp,
                    offsetY = (-6).dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.48f,
                    color = primaryCol,
                    align = Alignment.TopEnd,
                    rot = 16f,
                    shape = RoundedCornerShape(20.dp),
                    offsetX = (-20).dp,
                    offsetY = 8.dp,
                ),
                SetupIconPlacement(
                    size = minDim * 0.40f,
                    color = secCol,
                    align = Alignment.BottomStart,
                    rot = -10f,
                    shape = RoundedCornerShape(16.dp),
                    offsetX = 22.dp,
                    offsetY = (-8).dp,
                ),
            )
        }

        configs.forEachIndexed { i, cfg ->
            val drawableId = drawables.getOrNull(i % drawables.size) ?: return@forEachIndexed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(cfg.align)
                    .offset(x = cfg.offsetX, y = cfg.offsetY),
            ) {
                Surface(
                    modifier = Modifier
                        .size(cfg.size)
                        .graphicsLayer { rotationZ = cfg.rot },
                    shape = cfg.shape,
                    color = if (useDynamicTint) cfg.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = drawableId),
                            contentDescription = null,
                            tint = if (useDynamicTint) cfg.color else Color.Unspecified,
                            modifier = Modifier.size(cfg.size * 0.52f),
                        )
                    }
                }
            }
        }
    }
}
