package id.xms.xtrakernelmanager.ui.screens.misc.classic

import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

enum class ClassicProfileType(@StringRes val displayNameRes: Int, @StringRes val descriptionRes: Int, val governor: String, val thermalPreset: String) {
  PERFORMANCE(R.string.profile_performance, R.string.profile_desc_performance, "performance", "Extreme"),
  BALANCED(R.string.profile_balanced, R.string.profile_desc_balanced, "schedutil", "Dynamic"),
  POWER_SAVE(R.string.profile_power_save, R.string.profile_desc_power_save, "powersave", "Class 0"),
}

enum class ClassicRefreshRate(@StringRes val displayNameRes: Int, val value: Int) {
  DEFAULT(R.string.refresh_rate_default, 0),
  HZ_60(R.string.refresh_rate_60, 60),
  HZ_90(R.string.refresh_rate_90, 90),
  HZ_120(R.string.refresh_rate_120, 120),
  HZ_144(R.string.refresh_rate_144, 144),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicPerAppProfileScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val preferencesManager = remember { PreferencesManager(context) }
  
  // Master toggle for per-app profile feature
  val isPerAppProfileEnabled by preferencesManager.isPerAppProfileEnabled().collectAsState(initial = false)
  
  val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
  val availableRefreshRates = remember(maxRefreshRate) { 
    getAvailableRefreshRates(maxRefreshRate) 
  }
  
  val profilesJson by preferencesManager.getAppProfiles().collectAsState(initial = "[]")
  val savedProfiles = remember(profilesJson) { parseProfiles(profilesJson) }
  
  val installedApps = remember { getInstalledApps(context) }
  val appProfiles = remember(installedApps, savedProfiles) {
    installedApps.map { app ->
      savedProfiles.find { it.packageName == app.packageName } ?: app
    }
  }
  
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf<ClassicProfileType?>(null) }
  var expandedAppPackage by remember { mutableStateOf<String?>(null) }

  val filteredApps =
      remember(appProfiles, searchQuery, selectedFilter) {
        val baseList =
            if (searchQuery.isBlank()) {
              appProfiles.sortedBy { it.appName }
            } else {
              appProfiles
                  .filter {
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
                  }
                  .sortedBy { it.appName }
            }

        if (selectedFilter != null) {
          baseList.filter { getProfileTypeFromApp(it) == selectedFilter }
        } else {
          baseList
        }
      }

  Scaffold(
      containerColor = ClassicColors.Background,
      topBar = {
        TopAppBar(
            title = { 
                Text(
                    stringResource(R.string.per_app_profile), 
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
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = ClassicColors.Background,
                    scrolledContainerColor = ClassicColors.Background,
                ),
        )
      },
  ) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
      // Master Toggle Card
      Card(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(
              containerColor = if (isPerAppProfileEnabled) {
                  ClassicColors.Primary.copy(alpha = 0.15f)
              } else {
                  ClassicColors.SurfaceContainerHigh
              }
          )
      ) {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
          ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(16.dp),
                  modifier = Modifier.weight(1f)
              ) {
                  Icon(
                      Icons.Rounded.Tune,
                      contentDescription = null,
                      tint = if (isPerAppProfileEnabled) {
                          ClassicColors.Primary
                      } else {
                          ClassicColors.OnSurfaceVariant
                      },
                      modifier = Modifier.size(24.dp)
                  )
                  Column {
                      Text(
                          "Per-App Profile",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = if (isPerAppProfileEnabled) {
                              ClassicColors.Primary
                          } else {
                              ClassicColors.OnSurface
                          }
                      )
                      Text(
                          if (isPerAppProfileEnabled) "Active" else "Disabled",
                          style = MaterialTheme.typography.bodySmall,
                          color = if (isPerAppProfileEnabled) {
                              ClassicColors.Primary.copy(alpha = 0.7f)
                          } else {
                              ClassicColors.OnSurfaceVariant
                          }
                      )
                  }
              }
              
              Switch(
                  checked = isPerAppProfileEnabled,
                  onCheckedChange = { enabled ->
                      scope.launch {
                          preferencesManager.setPerAppProfileEnabled(enabled)
                      }
                  },
                  colors = SwitchDefaults.colors(
                      checkedThumbColor = ClassicColors.Primary,
                      checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
                  )
              )
          }
      }
      
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ClassicColors.SurfaceContainerHigh,
                    unfocusedContainerColor = ClassicColors.SurfaceContainer,
                    focusedBorderColor = ClassicColors.Primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = ClassicColors.OnSurface,
                    unfocusedTextColor = ClassicColors.OnSurface,
                    cursorColor = ClassicColors.Primary,
                ),
        )

        Box {
          var filterExpanded by remember { mutableStateOf(false) }

          FilledTonalIconButton(
              onClick = { filterExpanded = true },
              modifier = Modifier.size(56.dp),
              shape = MaterialTheme.shapes.large,
              colors =
                  IconButtonDefaults.filledTonalIconButtonColors(
                      containerColor =
                          if (selectedFilter != null)
                              getProfileColor(selectedFilter!!).copy(alpha = 0.2f)
                          else ClassicColors.SurfaceContainerHigh,
                      contentColor =
                          if (selectedFilter != null) getProfileColor(selectedFilter!!)
                          else ClassicColors.OnSurfaceVariant,
                  ),
          ) {
            Icon(
                imageVector =
                    if (selectedFilter != null) Icons.Rounded.FilterListOff
                    else Icons.Rounded.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(24.dp),
            )
          }

          DropdownMenu(
              expanded = filterExpanded,
              onDismissRequest = { filterExpanded = false },
              shape = MaterialTheme.shapes.large,
              containerColor = ClassicColors.SurfaceContainer,
          ) {
            DropdownMenuItem(
                text = { 
                    Text(
                        stringResource(R.string.filter_all),
                        color = ClassicColors.OnSurface
                    ) 
                },
                onClick = {
                  selectedFilter = null
                  filterExpanded = false
                },
                leadingIcon = { 
                    if (selectedFilter == null) Icon(
                        Icons.Rounded.Check, 
                        null,
                        tint = ClassicColors.Primary
                    ) 
                },
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.2f)
            )

            ClassicProfileType.entries.forEach { type ->
                  DropdownMenuItem(
                      text = { 
                          Text(
                              stringResource(type.displayNameRes), 
                              color = getProfileColor(type)
                          ) 
                      },
                      onClick = {
                        selectedFilter = type
                        filterExpanded = false
                      },
                      leadingIcon = {
                        if (selectedFilter == type) {
                          Icon(Icons.Rounded.Check, null, tint = getProfileColor(type))
                        } else {
                          Icon(getProfileIcon(type), null, tint = getProfileColor(type))
                        }
                      },
                  )
                }
          }
        }
      }

      LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        val activeConfigs = appProfiles.filter { 
             it.governor != "schedutil" || it.thermalPreset != "Not Set" || it.refreshRate != 0
        }
        
        if (searchQuery.isEmpty() && selectedFilter == null && activeConfigs.isNotEmpty()) {
          item {
            Text(
                stringResource(R.string.per_app_active_configs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            )
          }

          item {
            LazyRow(
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              items(activeConfigs) { app ->
                ClassicActiveConfigCard(
                    app = app, 
                    onClick = { expandedAppPackage = app.packageName }
                )
              }
            }
          }

          item {
            Text(
                stringResource(R.string.per_app_all_apps),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            )
          }
        }

        items(filteredApps, key = { it.packageName }) { app ->
          ClassicExpressiveAppItem(
              app = app,
              availableRefreshRates = availableRefreshRates,
              isExpanded = expandedAppPackage == app.packageName,
              onToggleExpand = {
                  expandedAppPackage = if (expandedAppPackage == app.packageName) null else app.packageName
              },
              onUpdate = { updatedApp ->
                   scope.launch {
                       saveProfile(preferencesManager, updatedApp, appProfiles)
                   }
              }
          )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
      }
    }
  }
}


@Composable
fun ClassicActiveConfigCard(app: AppProfile, onClick: () -> Unit) {
  val context = LocalContext.current
  val profileType = getProfileTypeFromApp(app)
  val color = getProfileColor(profileType)

  Card(
      onClick = onClick,
      modifier = Modifier.size(width = 160.dp, height = 180.dp),
      shape = MaterialTheme.shapes.extraLarge,
      colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(getAppIcon(context, app.packageName))
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.medium),
        )

        Surface(color = color, shape = CircleShape, modifier = Modifier.size(24.dp)) {
          Icon(
              getProfileIcon(profileType),
              null,
              tint = Color.White,
              modifier = Modifier.padding(4.dp),
          )
        }
      }

      Column {
        Text(
            app.appName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = ClassicColors.OnSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if(app.refreshRate != 0) "${app.refreshRate}Hz" else stringResource(profileType.displayNameRes),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
fun ClassicExpressiveAppItem(
    app: AppProfile,
    availableRefreshRates: List<Int>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdate: (AppProfile) -> Unit
) {
  val context = LocalContext.current
  val isCustomized = app.governor != "schedutil" || app.thermalPreset != "Not Set" || app.refreshRate != 0
  val profileType = getProfileTypeFromApp(app)
  val profileColor = getProfileColor(profileType)

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { onToggleExpand() },
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isCustomized) {
                    profileColor.copy(alpha = 0.05f)
                  } else {
                    ClassicColors.SurfaceContainerHigh
                  }
          ),
      border =
          if (isCustomized) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                profileColor.copy(alpha = 0.3f),
            )
          } else null,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(getAppIcon(context, app.packageName))
                    .crossfade(true)
                    .build(),
            contentDescription = app.appName,
            loading = {
              Box(
                  modifier =
                      Modifier.size(56.dp)
                          .clip(MaterialTheme.shapes.large)
                          .background(ClassicColors.SurfaceContainer),
                  contentAlignment = Alignment.Center,
              ) {
                Text(
                    app.appName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                )
              }
            },
            error = {
              Box(
                  modifier =
                      Modifier.size(56.dp)
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
            },
            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.large),
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = app.appName,
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
          
          if(isCustomized && !isExpanded) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Surface(
                      color = profileColor.copy(alpha = 0.1f), 
                      shape = RoundedCornerShape(8.dp)
                  ) {
                      Text(
                          stringResource(profileType.displayNameRes),
                          style = MaterialTheme.typography.labelSmall,
                          color = profileColor,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                  }
                  if (app.refreshRate != 0) {
                      Surface(
                          color = ClassicColors.Secondary.copy(alpha = 0.1f), 
                          shape = RoundedCornerShape(8.dp)
                      ) {
                          Text(
                              "${app.refreshRate}Hz",
                              style = MaterialTheme.typography.labelSmall,
                              color = ClassicColors.Secondary,
                              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                          )
                      }
                  }
              }
          }
        }
        
         Icon(
              if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
              contentDescription = null,
              tint = ClassicColors.OnSurfaceVariant,
          )
      }
      
      AnimatedVisibility(visible = isExpanded) {
          Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
              HorizontalDivider(
                  color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.2f),
                  modifier = Modifier.padding(bottom = 12.dp)
              )
              
              // Enable/Disable Toggle
              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
              ) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(16.dp),
                      modifier = Modifier.weight(1f)
                  ) {
                      Icon(
                          Icons.Rounded.PowerSettingsNew,
                          null,
                          tint = ClassicColors.OnSurfaceVariant,
                          modifier = Modifier.size(24.dp)
                      )
                      Text(
                          "Enable Profile",
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Medium,
                          color = ClassicColors.OnSurface
                      )
                  }
                  
                  Switch(
                      checked = app.enabled,
                      onCheckedChange = { enabled ->
                          onUpdate(app.copy(enabled = enabled))
                      },
                      colors = SwitchDefaults.colors(
                          checkedThumbColor = ClassicColors.Primary,
                          checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
                      )
                  )
              }
              
              ClassicConfigDropdownRow(
                  label = stringResource(R.string.per_app_performance_profile),
                  currentValue = stringResource(getProfileTypeFromApp(app).displayNameRes),
                  icon = Icons.Rounded.Settings,
                  isModified = true
              ) { dismiss ->
                  ClassicProfileType.entries.forEach { type ->
                      DropdownMenuItem(
                          text = { 
                              Text(
                                  stringResource(type.displayNameRes),
                                  color = ClassicColors.OnSurface
                              ) 
                          },
                          onClick = { 
                              onUpdate(app.copy(
                                  governor = type.governor,
                                  thermalPreset = type.thermalPreset
                              ))
                              dismiss()
                          },
                          leadingIcon = { 
                              val icon = getProfileIcon(type)
                              val color = getProfileColor(type)
                              Icon(icon, null, tint = color)
                          },
                          trailingIcon = {
                              if(getProfileTypeFromApp(app) == type) Icon(
                                  Icons.Rounded.Check, 
                                  null,
                                  tint = ClassicColors.Primary
                              )
                          }
                      )
                  }
              }

              if (availableRefreshRates.isNotEmpty()) {
                  ClassicConfigDropdownRow(
                      label = stringResource(R.string.per_app_refresh_rate),
                      currentValue = if(app.refreshRate == 0) stringResource(ClassicRefreshRate.DEFAULT.displayNameRes) else "${app.refreshRate}Hz",
                      icon = Icons.Rounded.Refresh,
                      isModified = app.refreshRate != 0
                  ) { dismiss ->
                      DropdownMenuItem(
                          text = { 
                              Text(
                                  stringResource(ClassicRefreshRate.DEFAULT.displayNameRes),
                                  color = ClassicColors.OnSurface
                              ) 
                          },
                          onClick = { 
                              onUpdate(app.copy(refreshRate = 0))
                              dismiss()
                          },
                          leadingIcon = {
                              if(app.refreshRate == 0) Icon(
                                  Icons.Rounded.Check, 
                                  null,
                                  tint = ClassicColors.Primary
                              )
                          }
                      )
                      
                      availableRefreshRates.forEach { rate ->
                          DropdownMenuItem(
                              text = { 
                                  Text(
                                      "${rate}Hz",
                                      color = ClassicColors.OnSurface
                                  ) 
                              },
                              onClick = { 
                                  onUpdate(app.copy(refreshRate = rate))
                                  dismiss()
                              },
                              leadingIcon = {
                                  if(app.refreshRate == rate) Icon(
                                      Icons.Rounded.Check, 
                                      null,
                                      tint = ClassicColors.Primary
                                  )
                              }
                          )
                      }
                  }
              }
          }
      }
    }
  }
}


@Composable
fun ClassicConfigDropdownRow(
    label: String,
    currentValue: String,
    icon: ImageVector,
    isModified: Boolean,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
             Icon(
                 icon, 
                 null, 
                 tint = ClassicColors.OnSurfaceVariant, 
                 modifier = Modifier.size(24.dp)
             )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ClassicColors.OnSurface
            )
        }
        
        Box {
             Surface(
                 shape = RoundedCornerShape(12.dp),
                 color = ClassicColors.SurfaceContainer,
                 modifier = Modifier.height(36.dp).clickable { expanded = true }
             ) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.padding(horizontal = 12.dp)
                 ) {
                     Text(
                         currentValue,
                         style = MaterialTheme.typography.labelMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = if(isModified) ClassicColors.Primary else ClassicColors.OnSurface
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     Icon(
                         Icons.Rounded.ArrowDropDown,
                         null,
                         modifier = Modifier.size(16.dp),
                         tint = ClassicColors.OnSurfaceVariant
                     )
                 }
             }
             
             DropdownMenu(
                 expanded = expanded,
                 onDismissRequest = { expanded = false },
                 shape = RoundedCornerShape(16.dp),
                 containerColor = ClassicColors.SurfaceContainer,
                 tonalElevation = 4.dp
             ) {
                 content { expanded = false }
             }
        }
    }
}

@Composable
private fun getProfileColor(profile: ClassicProfileType): Color {
  return when (profile) {
    ClassicProfileType.PERFORMANCE -> ClassicColors.Primary
    ClassicProfileType.BALANCED -> ClassicColors.Secondary
    ClassicProfileType.POWER_SAVE -> ClassicColors.Accent
  }
}

private fun getProfileIcon(profile: ClassicProfileType): ImageVector {
  return when (profile) {
    ClassicProfileType.PERFORMANCE -> Icons.Rounded.RocketLaunch
    ClassicProfileType.BALANCED -> Icons.Rounded.Balance
    ClassicProfileType.POWER_SAVE -> Icons.Rounded.BatteryChargingFull
  }
}

private fun getInstalledApps(context: android.content.Context): List<AppProfile> {
  return try {
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
    apps
        .filter {
          (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
        }
        .map { info ->
          AppProfile(
              packageName = info.packageName,
              appName = pm.getApplicationLabel(info).toString(),
              governor = "schedutil",
              thermalPreset = "Not Set",
              refreshRate = 0,
              enabled = true
          )
        }
  } catch (e: Exception) {
    emptyList()
  }
}

private fun getAppIcon(
    context: android.content.Context,
    packageName: String,
): android.graphics.drawable.Drawable? {
  return try {
    context.packageManager.getApplicationIcon(packageName)
  } catch (e: Exception) {
    null
  }
}

private fun getProfileTypeFromApp(app: AppProfile): ClassicProfileType {
    return when {
        app.governor == "performance" && app.thermalPreset == "Extreme" -> ClassicProfileType.PERFORMANCE
        app.governor == "powersave" && app.thermalPreset == "Class 0" -> ClassicProfileType.POWER_SAVE
        app.governor == "schedutil" && app.thermalPreset == "Dynamic" -> ClassicProfileType.BALANCED
        else -> ClassicProfileType.BALANCED
    }
}

private fun getDeviceMaxRefreshRate(context: android.content.Context): Int {
    return try {
        val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = context.display ?: windowManager.defaultDisplay
            display.supportedModes.maxOfOrNull { it.refreshRate.toInt() } ?: 60
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            display.refreshRate.toInt()
        }
    } catch (e: Exception) {
        60
    }
}

private fun getAvailableRefreshRates(maxRate: Int): List<Int> {
    return when {
        maxRate >= 144 -> listOf(60, 90, 120, 144)
        maxRate >= 120 -> listOf(60, 90, 120)
        maxRate >= 90 -> listOf(60, 90)
        else -> emptyList()
    }
}

private fun parseProfiles(json: String): List<AppProfile> {
    return try {
        val jsonArray = JSONArray(json)
        val profiles = mutableListOf<AppProfile>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            profiles.add(
                AppProfile(
                    packageName = obj.getString("packageName"),
                    appName = obj.getString("appName"),
                    governor = obj.optString("governor", "schedutil"),
                    thermalPreset = obj.optString("thermalPreset", "Not Set"),
                    refreshRate = obj.optInt("refreshRate", 0),
                    enabled = obj.optBoolean("enabled", true),
                )
            )
        }
        profiles
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun saveProfile(
    preferencesManager: PreferencesManager,
    updatedApp: AppProfile,
    allApps: List<AppProfile>
) {
    val currentJson = preferencesManager.getAppProfiles().first()
    val currentProfiles = parseProfiles(currentJson).toMutableList()
    
    val isCustomized = updatedApp.thermalPreset != "Not Set" || updatedApp.refreshRate != 0
    
    if (isCustomized) {
        val existingIndex = currentProfiles.indexOfFirst { it.packageName == updatedApp.packageName }
        if (existingIndex >= 0) {
            currentProfiles[existingIndex] = updatedApp
        } else {
            currentProfiles.add(updatedApp)
        }
    } else {
        currentProfiles.removeAll { it.packageName == updatedApp.packageName }
    }
    
    val jsonArray = JSONArray()
    currentProfiles.forEach { profile ->
        val obj = JSONObject().apply {
            put("packageName", profile.packageName)
            put("appName", profile.appName)
            put("governor", profile.governor)
            put("thermalPreset", profile.thermalPreset)
            put("refreshRate", profile.refreshRate)
            put("enabled", profile.enabled)
        }
        jsonArray.put(obj)
    }
    preferencesManager.saveAppProfiles(jsonArray.toString())
}
