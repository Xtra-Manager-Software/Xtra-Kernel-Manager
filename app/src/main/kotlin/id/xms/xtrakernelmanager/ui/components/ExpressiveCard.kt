package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard Material Design 3 Expressive Card container for XKM.
 * Applies bouncy click physics, uniform 24.dp rounded corners, and expressive surface elevation.
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

/**
 * Standard card header row featuring a spaced category overline on the left
 * and an optional expressive stadium pill on the right.
 */
@Composable
fun ExpressiveCardHeader(
    category: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    categoryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
    tagContentColor: Color = MaterialTheme.colorScheme.onSurface,
    tagContainerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
    isTagMonospace: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = categoryColor,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
        )

        if (tag != null) {
            ExpressivePill(
                text = tag,
                containerColor = tagContainerColor,
                contentColor = tagContentColor,
                isMonospace = isTagMonospace,
            )
        }
    }
}

/**
 * Material 3 Expressive stadium badge / pill.
 * Used for version badges, tags, chip states, and compact metrics.
 */
@Composable
fun ExpressivePill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isMonospace: Boolean = false,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 3.dp,
    shape: Shape = CircleShape,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = shape,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isMonospace) FontWeight.SemiBold else FontWeight.ExtraBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else null,
            color = contentColor,
            letterSpacing = if (isMonospace) 0.sp else 0.8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
        )
    }
}
