package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.runtime.Composable
import id.xms.xtrakernelmanager.ui.screens.misc.material.MaterialMiscScreen

@Composable
fun MiscScreen(
    viewModel: MiscViewModel,
    onNavigateToFunctionalRom: () -> Unit = {},
    onNavigateToAppPicker: () -> Unit = {},
) {
    MaterialMiscScreen(
        viewModel = viewModel,
        onNavigate = { route ->
            when (route) {
                "functionalrom" -> onNavigateToFunctionalRom()
                "app_picker" -> onNavigateToAppPicker()
            }
        },
    )
}
