package id.xms.xtrakernelmanager.ui.screens.misc.classic

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.ui.screens.misc.shared.ProcessInfo
import id.xms.xtrakernelmanager.ui.screens.misc.shared.loadRunningProcesses
import id.xms.xtrakernelmanager.ui.screens.misc.shared.getAppIconSafe
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ClassicProcessManager"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicProcessManagerScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var processes by remember { mutableStateOf<List<ProcessInfo>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var isKilling by remember { mutableStateOf<String?>(null) }
  var sortByMemory by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    Log.d(TAG, "Starting realtime process monitoring...")
    while (true) {
      try {
        val newProcesses = loadRunningProcesses(context)
        processes = newProcesses
        Log.d(TAG, "Updated ${newProcesses.size} processes")
        if (isLoading) isLoading = false
      } catch (e: Exception) {
        Log.e(TAG, "Error loading processes", e)
        if (isLoading) isLoading = false
      }
      kotlinx.coroutines.delay(3000)
    }
  }

  fun refreshProcesses() {
    scope.launch {
      Log.d(TAG, "Refreshing processes...")
      isLoading = true
      try {
        processes = loadRunningProcesses(context)
        Log.d(TAG, "Refreshed ${processes.size} processes")
      } catch (e: Exception) {
        Log.e(TAG, "Error refreshing processes", e)
      } finally {
        isLoading = false
      }
    }
  }

  fun killProcess(packageName: String) {
    scope.launch {
      Log.d(TAG, "Killing process: $packageName")
      isKilling = packageName
      try {
        val result =
            withContext(Dispatchers.IO) { RootManager.executeCommand("am force-stop $packageName") }
        if (result.isSuccess) {
          processes = processes.filter { it.packageName != packageName }
          Log.d(TAG, "Successfully killed: $packageName")
        } else {
          Log.w(TAG, "Failed to kill: $packageName")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error killing process: $packageName", e)
      } finally {
        isKilling = null
      }
    }
  }

  val sortedProcesses =
      remember(processes, sortByMemory) {
        if (sortByMemory) {
          processes.sortedByDescending { it.memoryMB }
        } else {
          processes.sortedBy { it.packageName }
        }
      }

  Scaffold(
      containerColor = ClassicColors.Background,
      topBar = {
        TopAppBar(
            title = { 
                Text(
                    "Process Manager", 
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
            actions = {
              IconButton(onClick = { sortByMemory = !sortByMemory }) {
                Icon(
                    if (sortByMemory) Icons.Rounded.Memory else Icons.Rounded.SortByAlpha,
                    "Sort",
                    tint = ClassicColors.OnSurface
                )
              }
              IconButton(onClick = { refreshProcesses() }) {
                Icon(
                    Icons.Rounded.Refresh, 
                    "Refresh",
                    tint = ClassicColors.OnSurface
                )
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.Background,
                ),
        )
      },
  ) { paddingValues ->
    if (isLoading) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = ClassicColors.Primary)
      }
    } else if (processes.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
              Icons.Rounded.CheckCircle,
              null,
              Modifier.size(64.dp),
              tint = ClassicColors.Primary,
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              stringResource(id.xms.xtrakernelmanager.R.string.all_clear_message), 
              style = MaterialTheme.typography.titleLarge,
              color = ClassicColors.OnSurface
          )
        }
      }
    } else {
      LazyColumn(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          val totalMemory = processes.sumOf { it.memoryMB.toDouble() }.toFloat()
          ClassicMemorySummaryHero(totalMemory, processes.size)
        }

        item {
          Text(
              "Running Processes",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = ClassicColors.OnSurface,
              modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
          )
        }

        items(sortedProcesses, key = { it.pid }) { process ->
          ClassicProcessItem(
              process = process,
              isKilling = isKilling == process.packageName,
              onKill = { killProcess(process.packageName) },
          )
        }
      }
    }
  }
}

@Composable
fun ClassicMemorySummaryHero(totalMemoryMB: Float, processCount: Int) {
  val totalDeviceRAM = 8192f
  val usagePercent = (totalMemoryMB / totalDeviceRAM).coerceIn(0f, 1f)
  
  val animatedMemoryGB by androidx.compose.animation.core.animateFloatAsState(
      targetValue = totalMemoryMB / 1024,
      animationSpec = androidx.compose.animation.core.tween(
          durationMillis = 800,
          easing = androidx.compose.animation.core.FastOutSlowInEasing
      ),
      label = "memoryAnimation"
  )
  
  val animatedPercent by androidx.compose.animation.core.animateFloatAsState(
      targetValue = usagePercent,
      animationSpec = androidx.compose.animation.core.tween(
          durationMillis = 800,
          easing = androidx.compose.animation.core.FastOutSlowInEasing
      ),
      label = "percentAnimation"
  )

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(32.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = ClassicColors.Primary.copy(alpha = 0.15f),
              contentColor = ClassicColors.Primary,
          ),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
      ) {
        Column {
          Text(
              "RAM Usage",
              style = MaterialTheme.typography.labelLarge,
              color = ClassicColors.OnSurface.copy(alpha = 0.8f),
          )
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${String.format("%.1f", animatedMemoryGB).replace('.', ',')}",
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                    ),
                lineHeight = 64.sp,
                color = ClassicColors.Primary,
            )
            Text(
                text = "GB",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                color = ClassicColors.OnSurface.copy(alpha = 0.8f),
            )
          }
        }

        Surface(
            color = ClassicColors.SurfaceContainer,
            contentColor = ClassicColors.Primary,
            shape = CircleShape,
        ) {
          Icon(
              Icons.Rounded.Memory,
              contentDescription = null,
              modifier = Modifier.padding(12.dp).size(32.dp),
          )
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(
              "$processCount processes active",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = ClassicColors.OnSurface.copy(alpha = 0.8f),
          )
          Text(
              "${(animatedPercent * 100).toInt()}%",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = ClassicColors.Primary,
          )
        }

        LinearProgressIndicator(
            progress = { animatedPercent },
            modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
            color = ClassicColors.Primary,
            trackColor = ClassicColors.SurfaceContainer,
        )
      }
    }
  }
}


@Composable
fun ClassicProcessItem(
    process: ProcessInfo,
    isKilling: Boolean,
    onKill: () -> Unit,
) {
  val context = LocalContext.current
  var expanded by remember { mutableStateOf(false) }
  
  val animatedMemoryMB by androidx.compose.animation.core.animateFloatAsState(
      targetValue = process.memoryMB,
      animationSpec = androidx.compose.animation.core.tween(
          durationMillis = 600,
          easing = androidx.compose.animation.core.FastOutSlowInEasing
      ),
      label = "processMemoryAnimation"
  )

  Log.d(TAG, "=== Rendering ClassicProcessItem START ===")
  Log.d(TAG, "Package: ${process.packageName}, PID: ${process.pid}, Memory: ${process.memoryMB}MB")

  Card(
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { 
            Log.d(TAG, "Card clicked: ${process.packageName}")
            expanded = !expanded 
        },
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(containerColor = ClassicColors.SurfaceContainerHigh),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          var appIcon by remember(process.packageName) { 
              Log.d(TAG, "Initializing icon state for: ${process.packageName}")
              mutableStateOf<android.graphics.drawable.Drawable?>(null) 
          }
          var iconLoadError by remember(process.packageName) { mutableStateOf(false) }
          
          LaunchedEffect(process.packageName) {
              Log.d(TAG, "LaunchedEffect: Starting icon load for ${process.packageName}")
              withContext(Dispatchers.IO) {
                  try {
                      Log.d(TAG, "IO Thread: Loading icon for ${process.packageName}")
                      val icon = context.packageManager.getApplicationIcon(process.packageName)
                      Log.d(TAG, "IO Thread: Icon loaded successfully for ${process.packageName}")
                      appIcon = icon
                      iconLoadError = false
                  } catch (e: Exception) {
                      Log.e(TAG, "IO Thread: Error loading icon for ${process.packageName}", e)
                      Log.e(TAG, "Exception type: ${e.javaClass.simpleName}, Message: ${e.message}")
                      appIcon = null
                      iconLoadError = true
                  }
              }
              Log.d(TAG, "LaunchedEffect: Finished icon load for ${process.packageName}")
          }
          
          Log.d(TAG, "Rendering SubcomposeAsyncImage for ${process.packageName}")
          SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(appIcon)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            loading = {
              Box(
                  modifier =
                      Modifier.size(48.dp)
                          .clip(MaterialTheme.shapes.large)
                          .background(ClassicColors.SurfaceContainer),
                  contentAlignment = Alignment.Center,
              ) {
                Text(
                    process.packageName.substringAfterLast('.').take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                )
              }
            },
            error = {
              Box(
                  modifier =
                      Modifier.size(48.dp)
                          .clip(MaterialTheme.shapes.large)
                          .background(ClassicColors.SurfaceContainer),
                  contentAlignment = Alignment.Center,
              ) {
                Icon(
                    Icons.Rounded.Android,
                    null,
                    tint = ClassicColors.OnSurfaceVariant,
                )
              }
            },
            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.large),
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = process.packageName.substringAfterLast('.'),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = ClassicColors.OnSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
          Text(
              text = process.packageName,
              style = MaterialTheme.typography.bodySmall,
              color = ClassicColors.OnSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
        }

        Surface(
            color = ClassicColors.Secondary.copy(alpha = 0.2f),
            shape = CircleShape,
        ) {
          Text(
              text = "${String.format("%.0f", animatedMemoryMB)} MB",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = ClassicColors.Secondary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }

        FilledTonalIconButton(
            onClick = onKill,
            enabled = !isKilling,
            modifier = Modifier.size(36.dp),
            colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2E1A1A),
                    contentColor = androidx.compose.ui.graphics.Color(0xFFEF9A9A),
                ),
        ) {
          if (isKilling) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = androidx.compose.ui.graphics.Color(0xFFEF9A9A),
            )
          } else {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Kill process",
                modifier = Modifier.size(18.dp),
            )
          }
        }
      }

      AnimatedVisibility(visible = expanded) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          HorizontalDivider(color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.2f))

          Row(
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                  "Process ID",
                  style = MaterialTheme.typography.labelSmall,
                  color = ClassicColors.OnSurfaceVariant,
              )
              Text(
                  "${process.pid}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = ClassicColors.OnSurface,
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                  "CPU Usage (Est.)",
                  style = MaterialTheme.typography.labelSmall,
                  color = ClassicColors.OnSurfaceVariant,
              )
              Text(
                  "${String.format("%.1f", process.cpuPercent)}%",
                  style = MaterialTheme.typography.bodyMedium,
                  color = ClassicColors.OnSurface,
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Column {
            Text(
                "Memory Allocation",
                style = MaterialTheme.typography.labelSmall,
                color = ClassicColors.OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            val animatedMemoryProgress by androidx.compose.animation.core.animateFloatAsState(
                targetValue = (process.memoryMB / 1024f).coerceIn(0f, 1f),
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 600,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                label = "memoryProgressAnimation"
            )
            LinearProgressIndicator(
                progress = { animatedMemoryProgress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                color = ClassicColors.Secondary,
                trackColor = ClassicColors.SurfaceContainer,
            )
          }
        }
      }
    }
  }
  
  Log.d(TAG, "=== Rendering ClassicProcessItem END: ${process.packageName} ===")
}
