package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.setup.components.CharacterPedestalStage
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupStepHeader

@Composable
fun DoneContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        SetupStepHeader(
            title = stringResource(R.string.setup_done_title),
            subtitle = stringResource(R.string.setup_done_subtitle),
        )

        // Center Stage with Distinct Solid Pedestal Platform & Kuru Kuru Character
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CharacterPedestalStage(
                drawableResId = R.drawable.kurukuru,
                contentDescription = stringResource(R.string.setup_done_kurukuru_desc),
                characterWidth = 200.dp,
                characterHeight = 200.dp,
                characterBottomPadding = 16.dp,
                ovalWidth = 260.dp,
                ovalHeight = 52.dp,
                ovalBottomPadding = 0.dp,
                stageWidth = 280.dp,
                stageHeight = 230.dp,
            )
        }

        // Bottom Encouragement Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_done_explore_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.setup_done_explore_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
