package id.xms.xtrakernelmanager.ui.screens.misc.classic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppInfo
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicGameAppSelectorScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
    val gameAppsJson by viewModel.gameApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoading by viewModel.isLoadingApps.collectAsState()

    val filterAll = stringResource(R.string.filter_all)
    val filterAdded = stringResource(R.string.filter_added)
    val filterNotAdded = stringResource(R.string.filter_not_added)

    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(filterAll) }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (installedApps.isEmpty()) {
            viewModel.loadInstalledApps()
        }
    }

    val filteredApps = remember(installedApps, searchQuery, gameAppsJson, filterMode) {
        val sortedApps = if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }

        val filteredByMode = when (filterMode) {
            filterAdded -> sortedApps.filter { viewModel.isGameApp(it.packageName) }
            filterNotAdded -> sortedApps.filter { !viewModel.isGameApp(it.packageName) }
            else -> sortedApps
        }

        filteredByMode.sortedWith(
            compareByDescending<AppInfo> { viewModel.isGameApp(it.packageName) }.thenBy { it.label }
        )
    }

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.add_games_title),
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
                            tint = ClassicColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.SurfaceContainer,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Search Bar & Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            stringResource(R.string.search_apps_placeholder),
                            color = ClassicColors.OnSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = ClassicColors.OnSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = ClassicColors.OnSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ClassicColors.SurfaceContainerHigh,
                        unfocusedContainerColor = ClassicColors.SurfaceContainer,
                        focusedBorderColor = ClassicColors.Primary,
                        unfocusedBorderColor = ClassicColors.SurfaceContainer,
                        focusedTextColor = ClassicColors.OnSurface,
                        unfocusedTextColor = ClassicColors.OnSurface,
                        cursorColor = ClassicColors.Primary,
                    ),
                )

                Box {
                    FilledTonalIconButton(
                        onClick = { showFilterMenu = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (filterMode != filterAll) ClassicColors.Primary.copy(alpha = 0.2f)
                            else ClassicColors.SurfaceContainerHigh
                        ),
                    ) {
                        Icon(
                            Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterMode != filterAll) ClassicColors.Primary
                            else ClassicColors.OnSurface,
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        containerColor = ClassicColors.SurfaceContainer
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.filter_all),
                                    color = ClassicColors.OnSurface
                                )
                            },
                            onClick = {
                                filterMode = filterAll
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (filterMode == filterAll) Icon(
                                    Icons.Rounded.Check,
                                    null,
                                    tint = ClassicColors.Primary
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(filterAdded, color = ClassicColors.OnSurface) },
                            onClick = {
                                filterMode = filterAdded
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (filterMode == filterAdded) Icon(
                                    Icons.Rounded.Check,
                                    null,
                                    tint = ClassicColors.Primary
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(filterNotAdded, color = ClassicColors.OnSurface) },
                            onClick = {
                                filterMode = filterNotAdded
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (filterMode == filterNotAdded) Icon(
                                    Icons.Rounded.Check,
                                    null,
                                    tint = ClassicColors.Primary
                                )
                            },
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClassicColors.Primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredApps, key = { it.packageName }) { app: AppInfo ->
                        val isGame = viewModel.isGameApp(app.packageName)

                        ClassicGameAppItem(
                            app = app,
                            isAdded = isGame,
                            onToggle = { viewModel.toggleGameApp(app.packageName) },
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun ClassicGameAppItem(app: AppInfo, isAdded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isAdded) {
                ClassicColors.Primary.copy(alpha = 0.15f)
            } else {
                ClassicColors.SurfaceContainerHigh
            }
        ),
        border = if (isAdded) {
            BorderStroke(1.dp, ClassicColors.Primary.copy(alpha = 0.5f))
        } else null,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // App icon
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.large),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(ClassicColors.SurfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Android,
                        null,
                        tint = ClassicColors.OnSurfaceVariant
                    )
                }
            }

            // App info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Switch
            Switch(
                checked = isAdded,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ClassicColors.Primary,
                    checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
                )
            )
        }
    }
}
