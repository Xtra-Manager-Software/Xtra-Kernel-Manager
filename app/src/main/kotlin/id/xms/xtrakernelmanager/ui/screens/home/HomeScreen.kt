package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.screens.home.components.AndroidVersionCard
import id.xms.xtrakernelmanager.ui.screens.home.components.BatteryCard
import id.xms.xtrakernelmanager.ui.screens.home.components.DisplayCard
import id.xms.xtrakernelmanager.ui.components.IntroAppear
import id.xms.xtrakernelmanager.ui.screens.home.components.HeroDeviceCard
import id.xms.xtrakernelmanager.ui.screens.home.components.ProcessorCard
import id.xms.xtrakernelmanager.ui.theme.ThemeMode

/** Index of the last staggered element; drives when the intro reports done. */
private const val LAST_INTRO_INDEX = 6

@Composable
fun HomeScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToSettings: () -> Unit = onNavigateToSetup,
    onNavigateToProcessor: () -> Unit = {},
    onNavigateToDisplay: () -> Unit = {},
    // Post-setup "explosion" intro: elements appear one by one, then this
    // screen reports back so the bottom bar can slide in last.
    playIntro: Boolean = false,
    onIntroFinished: () -> Unit = {},
) {
    LaunchedEffect(playIntro) {
        if (playIntro) {
            // Enough time for the last element to fully appear.
            delay(LAST_INTRO_INDEX * 110L + 700L)
            onIntroFinished()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntroAppear(index = 0, enabled = playIntro) {
                Text(
                    text = "Xtra Kernel Manager",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IntroAppear(index = 1, enabled = playIntro) {
            FilledIconButton(
                onClick = onNavigateToSettings,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(22.dp),
                )
            }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = WindowInsets.navigationBars.asPaddingValues().let { navInsets ->
                    PaddingValues(
                        top = 16.dp,
                        bottom = 100.dp + navInsets.calculateBottomPadding(),
                    )
                },
            ) {
                // Hero Device Card with Device Silhouette & Material Expressive Pills
                item {
                    IntroAppear(index = 2, enabled = playIntro) { HeroDeviceCard() }
                }

                // Processor Specifications Card
                item {
                    IntroAppear(index = 3, enabled = playIntro) {
                        ProcessorCard(
                            onClick = onNavigateToProcessor,
                        )
                    }
                }

                // Display Specifications Card
                item {
                    IntroAppear(index = 4, enabled = playIntro) {
                        DisplayCard(
                            onClick = onNavigateToDisplay,
                        )
                    }
                }

                // Operating System Card
                item {
                    IntroAppear(index = 5, enabled = playIntro) { AndroidVersionCard() }
                }

                // Battery Card
                item {
                    IntroAppear(index = 6, enabled = playIntro) { BatteryCard() }
                }
            }
        }
    }
}
