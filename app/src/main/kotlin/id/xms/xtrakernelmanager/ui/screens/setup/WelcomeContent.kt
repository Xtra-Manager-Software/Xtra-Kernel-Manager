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
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.setup.components.CharacterPedestalStage
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupStepHeader

@Composable
fun WelcomeContent(
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
            title = stringResource(R.string.setup_welcome_title),
            subtitle = stringResource(R.string.setup_welcome_subtitle),
        )

        // Center Stage with Pedestal Platform & Bongo Cat GIF
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CharacterPedestalStage(
                drawableResId = R.drawable.bongocat,
                contentDescription = stringResource(R.string.setup_welcome_bongo_desc),
                characterWidth = 260.dp,
                characterHeight = 164.dp,
                characterBottomPadding = 32.dp,
                ovalWidth = 300.dp,
                ovalHeight = 135.dp,
                ovalBottomPadding = 8.dp,
                stageWidth = 320.dp,
                stageHeight = 260.dp,
            )
        }

        // Bottom Quote Note (Anonymous easter egg)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_welcome_quote),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            )
        }
    }
}
