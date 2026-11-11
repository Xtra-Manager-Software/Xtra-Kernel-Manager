package id.xms.xtrakernelmanager.ui.components

import androidx.compose.runtime.Composable
import id.xms.xtrakernelmanager.ui.components.donation.MaterialDonationDialog

@Composable
fun DonationDialog(
    onDismiss: () -> Unit,
    onSupportClick: () -> Unit
) {
    MaterialDonationDialog(
        onDismiss = onDismiss,
        onSupportClick = onSupportClick
    )
}
