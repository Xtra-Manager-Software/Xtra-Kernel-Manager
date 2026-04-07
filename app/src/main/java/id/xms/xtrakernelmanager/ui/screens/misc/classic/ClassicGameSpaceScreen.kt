package id.xms.xtrakernelmanager.ui.screens.misc.classic

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicGameSpaceScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
    onAddGames: () -> Unit,
    onGameMonitorClick: () -> Unit,
) {
    val gameApps by viewModel.gameApps.collectAsState()
    val appCount = try {
        val jsonArray = JSONArray(gameApps)
        var count = 0
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.opt(i)
            when (item) {
                is String -> count++
                is org.json.JSONObject -> {
                    if (item.optBoolean("enabled", true)) {
                        count++
                    }
                }
            }
        }
        count
    } catch (e: Exception) {
        0
    }

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.game_control),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = ClassicColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Back",
                            tint = ClassicColors.OnSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.Background,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Game Library Section
            item {
                ClassicGameSpaceSection(
                    title = "Game Library",
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "$appCount Games",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ClassicColors.OnSurface,
                                )
                                Text(
                                    text = "Registered in ${stringResource(R.string.game_control)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassicColors.OnSurfaceVariant,
                                )
                            }

                            Button(
                                onClick = onAddGames,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ClassicColors.Primary
                                ),
                            ) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Performance Monitor Entry
                        val context = LocalContext.current
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!android.provider.Settings.canDrawOverlays(context)) {
                                        android.widget.Toast
                                            .makeText(
                                                context,
                                                "Please grant Overlay permission",
                                                android.widget.Toast.LENGTH_LONG,
                                            )
                                            .show()
                                        val intent = android.content.Intent(
                                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            android.net.Uri.parse("package:${context.packageName}"),
                                        )
                                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                        return@clickable
                                    }

                                    val componentName = android.content.ComponentName(
                                        context,
                                        id.xms.xtrakernelmanager.service.GameMonitorService::class.java,
                                    )
                                    val enabledServices = android.provider.Settings.Secure.getString(
                                        context.contentResolver,
                                        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                                    )
                                    val isAccessibilityEnabled =
                                        enabledServices?.contains(componentName.flattenToString()) == true ||
                                                enabledServices?.contains(componentName.flattenToShortString()) == true

                                    if (!isAccessibilityEnabled) {
                                        android.widget.Toast
                                            .makeText(
                                                context,
                                                "Please enable XKM Game Monitor service",
                                                android.widget.Toast.LENGTH_LONG,
                                            )
                                            .show()
                                        val intent = android.content.Intent(
                                            android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                                        )
                                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else {
                                        android.widget.Toast
                                            .makeText(
                                                context,
                                                "Service Active for Instant Detection",
                                                android.widget.Toast.LENGTH_SHORT,
                                            )
                                            .show()

                                        val serviceIntent = android.content.Intent(
                                            context,
                                            id.xms.xtrakernelmanager.service.GameOverlayService::class.java,
                                        )
                                        context.startService(serviceIntent)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = ClassicColors.SurfaceContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Speed,
                                        null,
                                        tint = ClassicColors.Primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Performance Monitor",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ClassicColors.OnSurface,
                                        )
                                        Text(
                                            "Manage Permissions & Start",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ClassicColors.OnSurfaceVariant,
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    null,
                                    tint = ClassicColors.OnSurfaceVariant
                                )
                            }
                        }
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 2. Notifications Section
            item {
                ClassicGameSpaceSection(
                    title = "Notifications",
                    content = {
                        ClassicGameSpaceSwitchRow(
                            title = "Call overlay",
                            subtitle = "Show minimal call overlay to answer/reject calls",
                            icon = Icons.Rounded.Call,
                            checked = viewModel.callOverlay.collectAsState().value,
                            onCheckedChange = { viewModel.setCallOverlay(it) },
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val callAction = viewModel.inGameCallAction.collectAsState().value
                        ClassicGameSpaceExpandableRow(
                            title = "In-game call",
                            subtitle = when (callAction) {
                                "no_action" -> "No action"
                                "answer" -> "Auto Answer"
                                "reject" -> "Auto Reject"
                                else -> "No action"
                            },
                            icon = Icons.Rounded.PhoneInTalk,
                            options = listOf(
                                "no_action" to "No action",
                                "answer" to "Auto Answer",
                                "reject" to "Auto Reject",
                            ),
                            selectedOption = callAction,
                            onOptionSelected = { viewModel.setInGameCallAction(it) },
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val ringerMode = viewModel.inGameRingerMode.collectAsState().value
                        ClassicGameSpaceExpandableRow(
                            title = "In-game ringer mode",
                            subtitle = when (ringerMode) {
                                "no_change" -> "Do not change"
                                "silent" -> "Silent"
                                "vibrate" -> "Vibrate"
                                else -> "Do not change"
                            },
                            icon = Icons.AutoMirrored.Rounded.VolumeUp,
                            options = listOf(
                                "no_change" to "Do not change",
                                "silent" to "Silent",
                                "vibrate" to "Vibrate",
                            ),
                            selectedOption = ringerMode,
                            onOptionSelected = { viewModel.setInGameRingerMode(it) },
                        )
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 3. Display & Gestures Section
            item {
                ClassicGameSpaceSection(
                    title = "Display & Gestures",
                    content = {
                        ClassicGameSpaceSwitchRow(
                            title = "Disable auto-brightness",
                            subtitle = "Keep brightness settled while in-game",
                            icon = Icons.Rounded.BrightnessAuto,
                            checked = viewModel.disableAutoBrightness.collectAsState().value,
                            onCheckedChange = { viewModel.setDisableAutoBrightness(it) },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ClassicGameSpaceSwitchRow(
                            title = "Disable three fingers swipe gesture",
                            subtitle = "Temporary disable three fingers swipe gesture while in-game",
                            icon = Icons.Rounded.Gesture,
                            checked = viewModel.disableThreeFingerSwipe.collectAsState().value,
                            onCheckedChange = { viewModel.setDisableThreeFingerSwipe(it) },
                        )
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ClassicGameSpaceSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = ClassicColors.OnSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
        ) {
            Column(modifier = Modifier.padding(20.dp)) { content() }
        }
    }
}

@Composable
fun ClassicGameSpaceSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ClassicColors.SurfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ClassicColors.Primary,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ClassicColors.OnSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ClassicColors.OnSurfaceVariant,
                maxLines = 2,
                lineHeight = 16.sp,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ClassicColors.Primary,
                checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
            ),
        )
    }
}

@Composable
fun ClassicGameSpaceExpandableRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ClassicColors.SurfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ClassicColors.Primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ClassicColors.OnSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ClassicColors.SurfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    options.forEach { (key, label) ->
                        val isSelected = (key == selectedOption)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) ClassicColors.Primary.copy(alpha = 0.1f)
                                    else ClassicColors.SurfaceContainer
                                )
                                .clickable {
                                    onOptionSelected(key)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )

                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = ClassicColors.Primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
