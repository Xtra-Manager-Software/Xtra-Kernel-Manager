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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.bouncyClickable
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupDrawableCollage
import id.xms.xtrakernelmanager.ui.screens.setup.components.SetupStepHeader
import java.util.concurrent.atomic.AtomicBoolean

enum class RootCheckState {
    CHECKING,
    GRANTED,
    UNGRANTED;

    companion object {
        fun from(hasRoot: Boolean, isCheckingRoot: Boolean): RootCheckState = when {
            isCheckingRoot -> CHECKING
            hasRoot -> GRANTED
            else -> UNGRANTED
        }
    }
}

@Composable
fun RootCheckContent(
    hasRoot: Boolean,
    isCheckingRoot: Boolean,
    onRequestRoot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Automatically trigger active check when screen is first composed
    LaunchedEffect(Unit) {
        if (!hasRoot) {
            onRequestRoot()
        }
    }

    // Automatically re-check when returning from Superuser manager (ON_RESUME)
    val hasEverPaused = remember { AtomicBoolean(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> hasEverPaused.set(true)
                Lifecycle.Event.ON_RESUME -> {
                    if (hasEverPaused.get() && !hasRoot) {
                        onRequestRoot()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val rootDrawables = remember {
        listOf(
            R.drawable.ic_magisk,
            R.drawable.ic_kernelsu,
            R.drawable.ic_apatch,
            R.drawable.ic_kernelsu_next,
            R.drawable.ic_kowsu,
        )
    }

    val currentState = RootCheckState.from(hasRoot, isCheckingRoot)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        SetupStepHeader(
            title = stringResource(R.string.setup_root_title),
            subtitle = stringResource(R.string.setup_root_subtitle),
        )

        // Center Root Manager Logos Collage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            SetupDrawableCollage(
                drawables = rootDrawables,
                height = 220.dp,
                useDynamicTint = true,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Status Card
            val statusCardColor = when (hasRoot) {
                true -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                false -> MaterialTheme.colorScheme.surfaceContainerHigh
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = statusCardColor,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val iconBgColor = when (hasRoot) {
                        true -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        false -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    }
                    val iconTint = when (hasRoot) {
                        true -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.error
                    }
                    val iconVector = when (hasRoot) {
                        true -> Icons.Rounded.CheckCircle
                        false -> Icons.Rounded.Security
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (hasRoot) {
                                true -> stringResource(R.string.setup_root_status_granted)
                                false -> stringResource(R.string.setup_root_status_awaiting)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = when (hasRoot) {
                                true -> stringResource(R.string.setup_root_status_granted_desc)
                                false -> stringResource(R.string.setup_root_status_awaiting_desc)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Action Button
            val isButtonEnabled = currentState == RootCheckState.UNGRANTED

            Button(
                onClick = { if (!hasRoot) onRequestRoot() },
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (hasRoot) {
                        true -> MaterialTheme.colorScheme.primaryContainer
                        false -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = when (hasRoot) {
                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                        false -> MaterialTheme.colorScheme.onPrimary
                    },
                    disabledContainerColor = when (hasRoot) {
                        true -> MaterialTheme.colorScheme.primaryContainer
                        false -> MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    disabledContentColor = when (hasRoot) {
                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                        false -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable(enabled = isButtonEnabled, onClick = onRequestRoot),
            ) {
                AnimatedContent(
                    targetState = currentState,
                    label = "RootButtonContentAnim",
                ) { state ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        when (state) {
                            RootCheckState.CHECKING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.setup_root_btn_verifying),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            RootCheckState.GRANTED -> {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.setup_root_btn_verified),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            RootCheckState.UNGRANTED -> {
                                Text(
                                    text = stringResource(R.string.setup_root_btn_grant),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
