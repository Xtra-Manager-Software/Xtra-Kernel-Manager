package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupDrawableCollage
import id.xms.xtrakernelmanager.ui.theme.GoogleSansRounded
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun RootCheckContent(
    viewModel: SetupViewModel,
    hasRoot: Boolean,
    modifier: Modifier = Modifier,
) {
    val isRequesting by viewModel.isCheckingRoot.collectAsState()

    // Auto-trigger root check on screen entry
    LaunchedEffect(Unit) {
        viewModel.checkRoot()
    }

    // Dynamic auto-detect: Re-check when app returns from background (ON_RESUME after ON_PAUSE)
    val hasEverPaused = remember { AtomicBoolean(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    hasEverPaused.set(true)
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (hasEverPaused.get() && !viewModel.hasRoot.value) {
                        viewModel.checkRoot()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val rootDrawables =
        remember {
            listOf(
                R.drawable.ic_magisk,
                R.drawable.ic_kernelsu,
                R.drawable.ic_apatch,
                R.drawable.ic_kernelsu_next,
                R.drawable.ic_kowsu,
            )
        }

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
                text = stringResource(R.string.setup_xkm_root_title),
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontFamily = GoogleSansRounded,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.setup_xkm_root_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Center Icon Collage with Root Manager Logos
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            SetupDrawableCollage(
                drawables = rootDrawables,
                height = 220.dp,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Status Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color =
                    if (hasRoot) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasRoot) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (hasRoot) Icons.Rounded.CheckCircle else Icons.Rounded.Security,
                            contentDescription = null,
                            tint = if (hasRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasRoot) "Root Privilege Granted" else "Awaiting Authorization",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = GoogleSansRounded,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Text(
                            text =
                                if (hasRoot) {
                                    "Superuser authorized via libsu"
                                } else {
                                    "Grant Magisk / KernelSU / APatch prompt"
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Action Button
            Button(
                onClick = {
                    if (!hasRoot) {
                        viewModel.checkRoot()
                    }
                },
                enabled = !hasRoot && !isRequesting,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (hasRoot) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        contentColor =
                            if (hasRoot) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedContent(targetState = hasRoot, label = "RootButtonAnim") { isGranted ->
                    when {
                        isGranted -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Check, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.setup_xkm_root_granted),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        isRequesting -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Requesting Root...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.setup_xkm_root_btn_grant),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
