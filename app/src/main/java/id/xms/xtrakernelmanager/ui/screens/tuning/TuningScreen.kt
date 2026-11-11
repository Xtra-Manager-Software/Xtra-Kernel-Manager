package id.xms.xtrakernelmanager.ui.screens.tuning

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.TuningConfig
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.material.MaterialTuningScreen
import kotlinx.coroutines.launch

@Composable
fun TuningScreen(preferencesManager: PreferencesManager, onNavigate: (String) -> Unit = {}) {
  val factory = TuningViewModel.Factory(preferencesManager)
  val viewModel: TuningViewModel = viewModel(factory = factory)

  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val lifecycleOwner = LocalLifecycleOwner.current
  var resumeKey by remember { mutableStateOf(0) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showSOCWarning by remember { mutableStateOf(false) }
  var socWarningMessage by remember { mutableStateOf("") }
  var pendingImportConfig by remember { mutableStateOf<TuningConfig?>(null) }
  var isImporting by remember { mutableStateOf(false) }

  val exportLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.CreateDocument("application/toml")
      ) { uri ->
        uri?.let {
          scope.launch {
            try {
              viewModel.getExportFileName() 
              val success = viewModel.exportConfigToUri(context, it)
              Toast.makeText(
                      context,
                      if (success) context.getString(R.string.tuning_export_success)
                      else context.getString(R.string.tuning_export_failed),
                      Toast.LENGTH_SHORT,
                  )
                  .show()
            } catch (e: Exception) {
              Toast.makeText(
                      context,
                      context.getString(R.string.tuning_error_format, e.message),
                      Toast.LENGTH_SHORT,
                  )
                  .show()
            }
          }
        }
      }

  val importLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
          isImporting = true
          scope.launch {
            val result = viewModel.importConfigFromUri(context, it)
            isImporting = false
            when (result) {
              is ImportResult.Success -> {
                Toast.makeText(
                        context,
                        context.getString(R.string.tuning_import_success),
                        Toast.LENGTH_SHORT,
                    )
                    .show()
              }
              is ImportResult.Warning -> {
                pendingImportConfig = result.config
                socWarningMessage = result.warning
                showSOCWarning = true
              }
              is ImportResult.Error -> {
                Toast.makeText(
                        context,
                        context.getString(R.string.tuning_error_format, result.message),
                        Toast.LENGTH_LONG,
                    )
                    .show()
              }
            }
          }
        }
      }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        resumeKey++
        viewModel.startRealTimeMonitoring()
      } else if (event == Lifecycle.Event.ON_PAUSE) {
        viewModel.stopRealTimeMonitoring()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(resumeKey) { viewModel.applyAllConfigurations() }

  Box(modifier = Modifier.fillMaxSize()) {
    MaterialTuningScreen(
        viewModel = viewModel,
        preferencesManager = preferencesManager,
        onNavigate = onNavigate,
        onExportConfig = { showExportDialog = true },
        onImportConfig = { showImportDialog = true },
    )

    // Export Confirmation Dialog
    if (showExportDialog) {
      AlertDialog(
          onDismissRequest = { showExportDialog = false },
          title = { Text(stringResource(R.string.tuning_export_title)) },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.tuning_export_message),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
              Text(
                  text = stringResource(R.string.tuning_export_description),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          confirmButton = {
            TextButton(
                onClick = {
                  showExportDialog = false
                  scope.launch {
                    val fileName = viewModel.getExportFileName()
                    exportLauncher.launch(fileName)
                  }
                }
            ) {
              Text(stringResource(R.string.tuning_export_button))
            }
          },
          dismissButton = {
            TextButton(onClick = { showExportDialog = false }) {
              Text(stringResource(R.string.cancel))
            }
          }
      )
    }

    // Import Confirmation Dialog
    if (showImportDialog) {
      AlertDialog(
          onDismissRequest = { showImportDialog = false },
          title = { Text(stringResource(R.string.tuning_import_title)) },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.tuning_import_message),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
              Text(
                  text = stringResource(R.string.tuning_import_warning),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
              )
            }
          },
          confirmButton = {
            TextButton(
                onClick = {
                  showImportDialog = false
                  importLauncher.launch(arrayOf("application/toml", "text/plain", "*/*"))
                }
            ) {
              Text(stringResource(R.string.tuning_import_button))
            }
          },
          dismissButton = {
            TextButton(onClick = { showImportDialog = false }) {
              Text(stringResource(R.string.cancel))
            }
          }
      )
    }

    // LOADING POPUP saat import
    if (isImporting) {
      AlertDialog(
          onDismissRequest = {},
          title = { Text(stringResource(R.string.tuning_importing)) },
          text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
              CircularProgressIndicator()
              Text(
                  text = stringResource(R.string.tuning_applying_config),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
            }
          },
          confirmButton = {},
          properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
      )
    }

    // SOC Compatibility Warning Dialog
    if (showSOCWarning) {
      AlertDialog(
          onDismissRequest = {
            showSOCWarning = false
            pendingImportConfig = null
          },
          title = { Text(stringResource(R.string.tuning_soc_warning_title)) },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = socWarningMessage, 
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
              HorizontalDivider()
              Text(
                  text = stringResource(R.string.tuning_soc_warning_question),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                  text = stringResource(R.string.tuning_soc_warning_desc),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
              )
            }
          },
          confirmButton = {
            TextButton(
                onClick = {
                  scope.launch {
                    pendingImportConfig?.let { config ->
                      viewModel.applyPreset(config)
                      Toast.makeText(
                              context,
                              context.getString(R.string.tuning_apply_with_warning),
                              Toast.LENGTH_SHORT,
                          )
                          .show()
                    }
                  }
                  showSOCWarning = false
                  pendingImportConfig = null
                }
            ) {
              Text(stringResource(R.string.tuning_apply_anyway))
            }
          },
          dismissButton = {
            TextButton(
                onClick = {
                  showSOCWarning = false
                  pendingImportConfig = null
                }
            ) {
              Text(stringResource(R.string.cancel))
            }
          }
      )
    }
  }
}
