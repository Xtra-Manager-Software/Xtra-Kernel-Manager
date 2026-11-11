package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.runtime.Composable

@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDisplaySize: () -> Unit,
    onNavigateToGlobalRefreshRate: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
    MaterialFunctionalRomScreen(
        onNavigateBack = onNavigateBack,
        onNavigateToDisplaySize = onNavigateToDisplaySize,
        onNavigateToGlobalRefreshRate = onNavigateToGlobalRefreshRate,
        viewModel = viewModel
    )
}
