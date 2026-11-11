package id.xms.xtrakernelmanager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.BottomNavItem
import id.xms.xtrakernelmanager.ui.components.HolidayCelebrationDialog
import id.xms.xtrakernelmanager.ui.components.material.MaterialFloatingBottomBar
import id.xms.xtrakernelmanager.ui.screens.donation.DonationScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.*
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.home.components.material.PowerMenuContent
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.info.SystemInfoScreen
import id.xms.xtrakernelmanager.ui.screens.info.UpdateViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.MiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.material.MaterialGameAppSelectorScreen
import id.xms.xtrakernelmanager.ui.screens.root.RootAccessScreen
import id.xms.xtrakernelmanager.ui.screens.settings.SettingsScreen
import id.xms.xtrakernelmanager.ui.screens.setup.SetupScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.material.CPUTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.MemoryTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialSmartFrequencyLockScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalIndexSelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.webview.MaterialWebViewScreen
import id.xms.xtrakernelmanager.ui.splash.checkRootAccess
import id.xms.xtrakernelmanager.utils.Holiday
import id.xms.xtrakernelmanager.utils.HolidayChecker
import id.xms.xtrakernelmanager.utils.RootShell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    preferencesManager: PreferencesManager,
    shouldShowDonationDialog: Boolean = false,
    shouldOpenSystemInfo: Boolean = false,
    updateViewModel: UpdateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  
  // Update state from ViewModel
  val updateState by updateViewModel.updateState.collectAsState()

  // State for Power Menu Bottom Sheet
  var showPowerBottomSheet by remember { mutableStateOf(false) }

  // Collect holiday shown years from preferences
  val christmasShownYear by preferencesManager.getChristmasShownYear().collectAsState(initial = 0)
  val newYearShownYear by preferencesManager.getNewYearShownYear().collectAsState(initial = 0)
  val ramadanShownYear by preferencesManager.getRamadanShownYear().collectAsState(initial = 0)
  val eidFitrShownYear by preferencesManager.getEidFitrShownYear().collectAsState(initial = 0)

  var showHolidayDialog by remember { mutableStateOf(false) }
  var currentHoliday by remember { mutableStateOf<Holiday?>(null) }
  var hasCheckedHoliday by remember { mutableStateOf(false) }
  val currentYear = remember { HolidayChecker.getCurrentYear() }
  val currentHijriYear = remember { HolidayChecker.getCurrentHijriYear() }

  // Handle donation dialog from notification
  LaunchedEffect(shouldShowDonationDialog) {
    if (shouldShowDonationDialog) {
      delay(200)
      try {
        if (navController.currentDestination?.route != "home") {
          navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId) { inclusive = false }
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("Navigation", "Error navigating to home from notification", e)
      }
    }
  }

  // Handle update notification - navigate to system info screen
  LaunchedEffect(shouldOpenSystemInfo) {
    if (shouldOpenSystemInfo) {
      delay(200)
      try {
        navController.navigate("system_info") {
          popUpTo(navController.graph.startDestinationId) { inclusive = false }
        }
      } catch (e: Exception) {
        android.util.Log.e("Navigation", "Error navigating to system info from notification", e)
      }
    }
  }

  // Check for holidays on launch - only once
  LaunchedEffect(Unit) {
    if (!hasCheckedHoliday) {
      hasCheckedHoliday = true
      val holiday = HolidayChecker.getCurrentHoliday()
      if (holiday != null) {
        val lastShownYear =
            when (holiday) {
              Holiday.CHRISTMAS -> christmasShownYear
              Holiday.NEW_YEAR -> newYearShownYear
              Holiday.RAMADAN -> ramadanShownYear
              Holiday.EID_FITR -> eidFitrShownYear
            }
        if (HolidayChecker.shouldShowHolidayDialog(holiday, lastShownYear)) {
          currentHoliday = holiday
          showHolidayDialog = true
        }
      }
    }
  }

  // Show holiday celebration dialog
  if (showHolidayDialog && currentHoliday != null) {
    HolidayCelebrationDialog(
        holiday = currentHoliday!!,
        year =
            if (currentHoliday == Holiday.RAMADAN || currentHoliday == Holiday.EID_FITR)
                currentHijriYear
            else currentYear,
        onDismiss = {
          showHolidayDialog = false
          scope.launch {
            val yearToSave =
                if (currentHoliday == Holiday.RAMADAN || currentHoliday == Holiday.EID_FITR)
                    currentHijriYear
                else currentYear
            when (currentHoliday) {
              Holiday.CHRISTMAS -> preferencesManager.setChristmasShownYear(yearToSave)
              Holiday.NEW_YEAR -> preferencesManager.setNewYearShownYear(yearToSave)
              Holiday.RAMADAN -> preferencesManager.setRamadanShownYear(yearToSave)
              Holiday.EID_FITR -> preferencesManager.setEidFitrShownYear(yearToSave)
              null -> {}
            }
            currentHoliday = null
          }
        },
    )
  }

  val bottomNavItems =
      listOf(
          BottomNavItem(route = "home", icon = Icons.Default.Home, label = R.string.nav_home),
          BottomNavItem(
              route = "tuning",
              icon = Icons.Default.Tune,
              label = R.string.nav_tuning,
          ),
          BottomNavItem(route = "profiles", icon = Icons.Default.Category, label = R.string.nav_misc),
          BottomNavItem(route = "info", icon = Icons.Default.Info, label = R.string.nav_info),
      )

  // Collect Setup State
  val isSetupCompleteState by preferencesManager.isSetupComplete.collectAsState(initial = null)
  
  // Check root access
  var hasRootAccess by remember { mutableStateOf<Boolean?>(null) }
  
  LaunchedEffect(isSetupCompleteState) {
    if (isSetupCompleteState == true) {
      hasRootAccess = checkRootAccess()
    }
  }

  // Show nothing while checking setup state to confirm start destination
  if (isSetupCompleteState == null) return
  
  // Determine start destination based on setup and root status
  val startDest = when {
    isSetupCompleteState == false -> "setup"
    hasRootAccess == null -> "home"
    hasRootAccess == false -> "root_access"
    else -> "home"
  }

  // Redirect to Home if setup is completed while on setup screen
  LaunchedEffect(isSetupCompleteState, currentRoute) {
    if (isSetupCompleteState == true && currentRoute == "setup") {
      val rootCheck = checkRootAccess()
      if (rootCheck) {
        navController.navigate("home") { popUpTo("setup") { inclusive = true } }
      } else {
        navController.navigate("root_access") { popUpTo("setup") { inclusive = true } }
      }
    }
  }

  // Shared FunctionalRomViewModel
  val functionalRomFactory = remember { FunctionalRomViewModel.Companion.Factory(preferencesManager, context.applicationContext) }
  val sharedFunctionalRomViewModel: FunctionalRomViewModel = viewModel(factory = functionalRomFactory)

  val backgroundColor = MaterialTheme.colorScheme.background

  Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
      NavHost(
          navController = navController,
          startDestination = startDest,
          modifier = Modifier.padding(paddingValues),
      ) {
        composable("setup") {
          SetupScreen(
              onSetupComplete = { layoutStyle ->
                scope.launch {
                  preferencesManager.setLayoutStyle(layoutStyle)
                  preferencesManager.setSetupComplete(true)
                  delay(100)
                  val rootCheck = checkRootAccess()
                  if (rootCheck) {
                    navController.navigate("home") { popUpTo("setup") { inclusive = true } }
                  } else {
                    navController.navigate("root_access") { popUpTo("setup") { inclusive = true } }
                  }
                }
              }
          )
        }
        
        composable("root_access") {
          RootAccessScreen(
              onGrantPermissions = {
                scope.launch {
                  val rootGranted = checkRootAccess()
                  if (rootGranted) {
                    navController.navigate("home") { 
                      popUpTo("root_access") { inclusive = true } 
                    }
                  }
                }
              },
              onTryAgain = {
                scope.launch {
                  val rootGranted = checkRootAccess()
                  if (rootGranted) {
                    navController.navigate("home") { 
                      popUpTo("root_access") { inclusive = true } 
                    }
                  }
                }
              }
          )
        }
        
        composable("home") { 
          HomeScreen(
              preferencesManager = preferencesManager,
              onNavigateToSettings = { navController.navigate("settings") },
              onNavigateToDonation = { navController.navigate("donation") },
              forceShowDonationDialog = shouldShowDonationDialog
          ) 
        }
        composable("tuning") {
          TuningScreen(
              preferencesManager = preferencesManager,
              onNavigate = { route -> navController.navigate(route) },
          )
        }
        composable("cpu_tuning") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          CPUTuningScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToSmartLock = { navController.navigate("material_smart_frequency_lock") }
          )
        }
        composable("material_smart_frequency_lock") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MaterialSmartFrequencyLockScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() }
          )
        }
        composable("memory_tuning") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MemoryTuningScreen(viewModel = tuningViewModel, navController = navController)
        }
        composable("material_thermal_settings") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MaterialThermalSettingsScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToIndexSelection = { navController.navigate("material_thermal_index_selection") }
          )
        }
        composable("material_thermal_index_selection") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          val currentIndex by tuningViewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
          val currentOnBoot by tuningViewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
          MaterialThermalIndexSelectionScreen(
              viewModel = tuningViewModel,
              currentIndex = currentIndex,
              onNavigateBack = { navController.popBackStack() },
              onIndexSelected = { index ->
                  tuningViewModel.setThermalPreset(index, currentOnBoot)
                  scope.launch {
                      delay(100)
                      navController.popBackStack()
                  }
              }
          )
        }
        composable("app_picker") {
          val miscViewModel = remember {
            MiscViewModel(
                preferencesManager = preferencesManager,
                context = context.applicationContext,
            )
          }
          MaterialGameAppSelectorScreen(
              viewModel = miscViewModel,
              onBack = { navController.popBackStack() },
          )
        }
        composable("profiles") {
          val miscViewModel = remember {
            MiscViewModel(
                preferencesManager = preferencesManager,
                context = context.applicationContext,
            )
          }
          MiscScreen(
              viewModel = miscViewModel,
              onNavigateToFunctionalRom = { navController.navigate("functionalrom") },
              onNavigateToAppPicker = { navController.navigate("app_picker") },
          )
        }

        composable("functionalrom") {
          FunctionalRomScreen(
              onNavigateBack = { navController.popBackStack() },
              onNavigateToShimokuRom = { navController.navigate("shimokurom") },
              onNavigateToHideAccessibility = { navController.navigate("hideaccessibilitysettings") },
              onNavigateToDisplaySize = { navController.navigate("display_size") },
              onNavigateToGlobalRefreshRate = { navController.navigate("global_refresh_rate") },
              viewModel = sharedFunctionalRomViewModel,
          )
        }

        composable("global_refresh_rate") {
          MaterialGlobalRefreshRateScreen(
              onNavigateBack = { navController.popBackStack() },
              viewModel = sharedFunctionalRomViewModel
          )
        }

        composable("shimokurom") {
          ShimokuRomScreen(
              onNavigateBack = { navController.popBackStack() },
              onNavigateToPlayIntegrity = { navController.navigate("playintegritysettings") },
              onNavigateToXiaomiTouch = { navController.navigate("xiaomitouchsettings") },
              viewModel = sharedFunctionalRomViewModel,
          )
        }

        composable("playintegritysettings") {
          PlayIntegritySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("xiaomitouchsettings") {
          XiaomiTouchSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("display_size") {
          MaterialDisplaySizeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("hideaccessibilitysettings") {
          val uiState by sharedFunctionalRomViewModel.uiState.collectAsState()
          
          HideAccessibilitySettingsScreen(
              config = uiState.hideAccessibilityConfig,
              onNavigateBack = { navController.popBackStack() },
              onConfigChange = { newConfig: HideAccessibilityConfig ->
                if (newConfig.isEnabled != uiState.hideAccessibilityConfig.isEnabled) {
                  sharedFunctionalRomViewModel.setHideAccessibilityEnabled(newConfig.isEnabled)
                }
                if (newConfig.currentTab != uiState.hideAccessibilityConfig.currentTab) {
                  sharedFunctionalRomViewModel.setHideAccessibilityTab(newConfig.currentTab)
                }
                if (newConfig.appsToHide != uiState.hideAccessibilityConfig.appsToHide) {
                  sharedFunctionalRomViewModel.setHideAccessibilityAppsToHide(newConfig.appsToHide)
                }
                if (newConfig.detectorApps != uiState.hideAccessibilityConfig.detectorApps) {
                  sharedFunctionalRomViewModel.setHideAccessibilityDetectorApps(newConfig.detectorApps)
                }
              },
              onRefreshLSPosedStatus = {
                sharedFunctionalRomViewModel.refreshLSPosedStatus()
              }
          )
        }

        composable("info") { 
          InfoScreen(
              preferencesManager = preferencesManager,
              onNavigateToWebView = { navController.navigate("webview") },
              onNavigateToLicense = { navController.navigate("license_webview") },
              onNavigateToSystemInfo = { navController.navigate("system_info") }
          )
        }
        
        composable("system_info") {
          SystemInfoScreen(
              onNavigateBack = { navController.popBackStack() }
          )
        }
        
        composable("webview") {
          MaterialWebViewScreen(
              url = "https://xtramanagersoftwares.tech/",
              title = "XMS Website",
              onNavigateBack = { navController.popBackStack() }
          )
        }
        
        composable("license_webview") {
          MaterialWebViewScreen(
              url = "https://raw.githubusercontent.com/Xtra-Manager-Software/Xtra-Kernel-Manager/staging-version-3.1/LICENSE",
              title = "MIT License",
              onNavigateBack = { navController.popBackStack() }
          )
        }
        
        composable("settings") {
          SettingsScreen(
              preferencesManager = preferencesManager,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToDonation = { navController.navigate("donation") }
          )
        }
        
        composable("donation") {
          DonationScreen(
              onNavigateBack = { navController.popBackStack() }
          )
        }
      }
    }

    // Floating Bottom Dock
    if (currentRoute != "setup" && currentRoute != "root_access") {
      val navigateToRoute: (String) -> Unit = { route ->
        if (currentRoute != route) {
          navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      }

      MaterialFloatingBottomBar(
          currentRoute = currentRoute,
          onNavigate = navigateToRoute,
          items = bottomNavItems,
          hasUpdate = updateState.hasUpdate,
          onPowerMenuClick = { showPowerBottomSheet = true },
          modifier = Modifier.align(Alignment.BottomCenter),
      )
    }

    // Power Menu Bottom Sheet for Material theme
    if (showPowerBottomSheet) {
      ModalBottomSheet(
          onDismissRequest = { showPowerBottomSheet = false },
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
      ) {
        PowerMenuContent(
            onAction = { action ->
              showPowerBottomSheet = false
              scope.launch {
                RootShell.execute(action.command)
              }
            }
        )
      }
    }
  }
}
