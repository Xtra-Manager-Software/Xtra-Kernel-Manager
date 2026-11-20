package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded

data class XkmNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val DefaultXkmNavItems = listOf(
    XkmNavItem(route = "home", label = "Home", icon = Icons.Rounded.Home),
    XkmNavItem(route = "tuning", label = "Tuning", icon = Icons.Rounded.Tune),
    XkmNavItem(route = "info", label = "Info", icon = Icons.Rounded.Memory),
    XkmNavItem(route = "about", label = "About", icon = Icons.Rounded.Info),
)

@Composable
fun XkmBottomBar(
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<XkmNavItem> = DefaultXkmNavItems,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 14.dp,
        ) {
            Row(
                modifier = Modifier
                    .height(68.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items.forEach { item ->
                    XkmPillNavItem(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onSelectRoute(item.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun XkmPillNavItem(
    item: XkmNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 250),
        label = "nav-bg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = 250),
        label = "nav-fg",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .height(50.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = if (selected) 18.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = fg,
                modifier = Modifier.size(22.dp),
            )

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(180)) +
                        expandHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        ),
                exit = fadeOut(animationSpec = tween(150)) +
                        shrinkHorizontally(
                            animationSpec = tween(150),
                        ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        color = fg,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
