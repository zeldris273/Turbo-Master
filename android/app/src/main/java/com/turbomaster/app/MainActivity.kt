package com.turbomaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turbomaster.app.ui.navigation.Screen
import com.turbomaster.app.ui.screens.*
import com.turbomaster.app.ui.theme.TurboTheme
import com.turbomaster.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TurboTheme {
                val metrics by viewModel.deviceMetrics.collectAsStateWithLifecycle()
                val shizukuState by viewModel.shizukuState.collectAsStateWithLifecycle()
                val apps by viewModel.installedApps.collectAsStateWithLifecycle()
                val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
                val history by viewModel.historyRecords.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == Screen.Home,
                                onClick = { currentScreen = Screen.Home },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Apps,
                                onClick = { currentScreen = Screen.Apps },
                                icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                                label = { Text("Apps") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Actions,
                                onClick = { currentScreen = Screen.Actions },
                                icon = { Icon(Icons.Default.Tune, contentDescription = "Tools") },
                                label = { Text("Tools") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Advanced,
                                onClick = { currentScreen = Screen.Advanced },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Tune") },
                                label = { Text("Tune") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Shizuku,
                                onClick = { currentScreen = Screen.Shizuku },
                                icon = { Icon(Icons.Default.Security, contentDescription = "Shizuku") },
                                label = { Text("Shizuku") }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                metrics = metrics,
                                shizukuState = shizukuState,
                                recentHistory = history,
                                onNavigateToShizuku = { currentScreen = Screen.Shizuku },
                                onNavigateToApps = { currentScreen = Screen.Apps },
                                onNavigateToActions = { currentScreen = Screen.Actions },
                                onRefresh = { viewModel.refreshMetrics() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Apps -> {
                            AppManagerScreen(
                                apps = apps,
                                isLoading = isLoadingApps,
                                onForceStop = { app ->
                                    viewModel.forceStopApp(app) { success, msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                onOpenDetails = { pkg -> viewModel.openAppDetails(pkg) },
                                onFilterSystemChange = { includeSystem ->
                                    viewModel.loadApps(includeSystem)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Actions -> {
                            ActionsScreen(
                                actions = viewModel.availableActions,
                                onExecuteAction = { action ->
                                    viewModel.executeAction(action) { success, msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Advanced -> {
                            AdvancedScreen(
                                onTuneAnimationScales = { scale ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Animation scales adjusted to ${scale}x")
                                    }
                                },
                                onResetDefaults = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Restored default 1.0x animation scales")
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Shizuku -> {
                            ShizukuScreen(
                                shizukuState = shizukuState,
                                onRequestPermission = { viewModel.requestShizukuPermission() },
                                onRefresh = { viewModel.refreshMetrics() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.History -> {
                            HistoryScreen(
                                records = history,
                                onClearHistory = { viewModel.clearHistory() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.About -> {
                            AboutScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
}
