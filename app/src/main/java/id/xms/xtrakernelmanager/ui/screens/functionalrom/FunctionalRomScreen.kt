package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit = {},
    onNavigateToHideAccessibility: () -> Unit = {},
    onNavigateToDisplaySize: () -> Unit,
    onNavigateToGlobalRefreshRate: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
    val layoutStyle by viewModel.layoutStyle.collectAsStateWithLifecycle()

    when (layoutStyle) {
        "material" -> MaterialFunctionalRomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToShimokuRom = onNavigateToShimokuRom,
            onNavigateToHideAccessibility = onNavigateToHideAccessibility,
            onNavigateToDisplaySize = onNavigateToDisplaySize,
            onNavigateToGlobalRefreshRate = onNavigateToGlobalRefreshRate,
            viewModel = viewModel
        )
        "classic" -> ClassicFunctionalRomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToShimokuRom = onNavigateToShimokuRom,
            onNavigateToHideAccessibility = onNavigateToHideAccessibility,
            onNavigateToDisplaySize = onNavigateToDisplaySize,
            onNavigateToGlobalRefreshRate = onNavigateToGlobalRefreshRate,
            viewModel = viewModel
        )
        else -> LiquidFunctionalRomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToShimokuRom = onNavigateToShimokuRom,
            onNavigateToHideAccessibility = onNavigateToHideAccessibility,
            onNavigateToDisplaySize = onNavigateToDisplaySize,
            onNavigateToGlobalRefreshRate = onNavigateToGlobalRefreshRate,
            viewModel = viewModel
        )
    }
}