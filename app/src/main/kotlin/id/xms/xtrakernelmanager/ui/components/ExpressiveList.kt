package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun itemShapeFor(
    index: Int,
    total: Int,
    outerCorner: Dp = 24.dp,
    innerCorner: Dp = 6.dp,
): RoundedCornerShape = when {
    total <= 1 -> RoundedCornerShape(outerCorner)
    index == 0 -> RoundedCornerShape(
        topStart = outerCorner,
        topEnd = outerCorner,
        bottomStart = innerCorner,
        bottomEnd = innerCorner,
    )
    index == total - 1 -> RoundedCornerShape(
        topStart = innerCorner,
        topEnd = innerCorner,
        bottomStart = outerCorner,
        bottomEnd = outerCorner,
    )
    else -> RoundedCornerShape(innerCorner)
}

/**
 * Stacking card container that wraps children in adaptive corner shapes with micro-gaps.
 */
@Composable
fun ExpressiveList(
    count: Int,
    modifier: Modifier = Modifier,
    outerCorner: Dp = 24.dp,
    innerCorner: Dp = 6.dp,
    gap: Dp = 3.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    item: @Composable (index: Int) -> Unit,
) {
    if (count == 0) return

    val singleShape = remember(outerCorner) { RoundedCornerShape(outerCorner) }
    val topShape = remember(outerCorner, innerCorner) {
        RoundedCornerShape(
            topStart = outerCorner,
            topEnd = outerCorner,
            bottomStart = innerCorner,
            bottomEnd = innerCorner,
        )
    }
    val bottomShape = remember(outerCorner, innerCorner) {
        RoundedCornerShape(
            topStart = innerCorner,
            topEnd = innerCorner,
            bottomStart = outerCorner,
            bottomEnd = outerCorner,
        )
    }
    val middleShape = remember(innerCorner) { RoundedCornerShape(innerCorner) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        for (i in 0 until count) {
            val shape = when {
                count == 1 -> singleShape
                i == 0 -> topShape
                i == count - 1 -> bottomShape
                else -> middleShape
            }
            Surface(
                shape = shape,
                color = containerColor,
            ) {
                item(i)
            }
        }
    }
}
