package id.xms.xtrakernelmanager.ui.screens.donation

import androidx.compose.runtime.Composable

@Composable
fun DonationScreen(
    onNavigateBack: () -> Unit = {}
) {
    MaterialDonationScreen(onNavigateBack = onNavigateBack)
}
