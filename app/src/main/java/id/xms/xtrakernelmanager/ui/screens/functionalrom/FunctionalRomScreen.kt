package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.runtime.Composable

@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit = {},
    onNavigateToHideAccessibility: () -> Unit = {},
    onNavigateToDisplaySize: () -> Unit,
    onNavigateToGlobalRefreshRate: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
    MaterialFunctionalRomScreen(
        onNavigateBack = onNavigateBack,
        onNavigateToShimokuRom = onNavigateToShimokuRom,
        onNavigateToHideAccessibility = onNavigateToHideAccessibility,
        onNavigateToDisplaySize = onNavigateToDisplaySize,
        onNavigateToGlobalRefreshRate = onNavigateToGlobalRefreshRate,
        viewModel = viewModel
    )
}
