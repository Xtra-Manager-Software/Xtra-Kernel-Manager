package id.xms.xtrakernelmanager.ui.screens.setup

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded

@Composable
fun DoneContent(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader =
        remember(context) {
            ImageLoader
                .Builder(context)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }.build()
        }

    val platformColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_xkm_done_title),
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontFamily = GoogleSansRounded,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.setup_xkm_done_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Center Stage with Distinct Solid Pedestal Platform & Kuru Kuru Character
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(width = 280.dp, height = 230.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                // Solid Floor Platform Oval
                Canvas(
                    modifier =
                        Modifier
                            .width(280.dp)
                            .height(56.dp)
                            .align(Alignment.BottomCenter),
                ) {
                    drawOval(
                        color = platformColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                    )
                }

                // Kuru Kuru Character
                AsyncImage(
                    model = R.drawable.kurukuru,
                    imageLoader = imageLoader,
                    contentDescription = "Kuru Kuru Spinning",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .size(200.dp)
                            .padding(bottom = 16.dp)
                            .align(Alignment.BottomCenter),
                )
            }
        }

        // Bottom Clean Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
        ) {
            Text(
                text = "Let's explore XKM",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = GoogleSansRounded,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the checkmark button below to get started.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
