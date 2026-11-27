package id.xms.xtrakernelmanager.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.components.ExpressiveSectionHeader
import id.xms.xtrakernelmanager.ui.screens.settings.components.ColorStyleCard
import id.xms.xtrakernelmanager.ui.screens.settings.components.NavBarStyleCard
import id.xms.xtrakernelmanager.ui.screens.settings.components.ThemeModeCard
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded
import id.xms.xtrakernelmanager.ui.theme.XkmSettingsStore

@Composable
fun SettingsScreen(
    settings: XkmSettingsStore,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = true) {
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = onNavigateBack,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = GoogleSansRounded,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Personalization & system",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = WindowInsets.navigationBars.asPaddingValues().let { navInsets ->
                PaddingValues(
                    top = 8.dp,
                    bottom = 32.dp + navInsets.calculateBottomPadding(),
                )
            },
        ) {
            item(key = "theme_header") {
                ExpressiveSectionHeader(
                    title = "Theme",
                    subtitle = "Light, dark, or follow the system.",
                )
            }
            item(key = "theme_card") {
                ThemeModeCard(settings = settings)
            }

            item(key = "color_header") {
                ExpressiveSectionHeader(
                    title = "Color",
                    subtitle = "Palette engine for every surface.",
                )
            }
            item(key = "color_card") {
                ColorStyleCard(settings = settings)
            }

            item(key = "nav_header") {
                ExpressiveSectionHeader(
                    title = "Navigation Bar",
                    subtitle = "How you move between screens.",
                )
            }
            item(key = "nav_card") {
                NavBarStyleCard(settings = settings)
            }
        }
    }
}
