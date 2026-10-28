package com.turbomaster.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.turbomaster.app.data.model.AppItem
import com.turbomaster.app.data.model.DeviceMetrics
import com.turbomaster.app.data.model.OperationRecord
import com.turbomaster.app.data.model.ShizukuConnectionState
import com.turbomaster.app.data.model.TurboAction
import com.turbomaster.app.data.repository.AppManagerRepository
import com.turbomaster.app.data.repository.DeviceRepository
import com.turbomaster.app.data.repository.HistoryRepository
import com.turbomaster.app.data.repository.TurboActionRepository
import com.turbomaster.app.data.shizuku.ShizukuManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepo = DeviceRepository(application)
    private val appRepo = AppManagerRepository(application)
    private val actionRepo = TurboActionRepository(application)
    private val historyRepo = HistoryRepository()

    val shizukuState: StateFlow<ShizukuConnectionState> = ShizukuManager.connectionState

    private val _deviceMetrics = MutableStateFlow<DeviceMetrics?>(null)
    val deviceMetrics: StateFlow<DeviceMetrics?> = _deviceMetrics.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    val historyRecords: StateFlow<List<OperationRecord>> = historyRepo.records

    val availableActions: List<TurboAction> = actionRepo.getActions()

    init {
        refreshMetrics()
        loadApps(includeSystem = false)
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            try {
                _deviceMetrics.value = deviceRepo.getDeviceMetrics()
            } catch (e: Exception) {
                // Keep previous state
            }
        }
    }

    fun loadApps(includeSystem: Boolean = false) {
        viewModelScope.launch {
            _isLoadingApps.value = true
            try {
                _installedApps.value = appRepo.getInstalledApps(includeSystem)
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun forceStopApp(app: AppItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = appRepo.forceStopApp(app.packageName)
            if (result.isSuccess) {
                historyRepo.addRecord(
                    actionId = "force_stop",
                    actionName = "Force Stop: ${app.appName}",
                    targetPackage = app.packageName,
                    isSuccess = true,
                    exitCode = 0,
                    outputMessage = "Process terminated cleanly",
                    commandExecuted = "am force-stop ${app.packageName}"
                )
                onResult(true, "Application ${app.appName} force-stopped successfully.")
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Operation failed"
                historyRepo.addRecord(
                    actionId = "force_stop",
                    actionName = "Force Stop: ${app.appName}",
                    targetPackage = app.packageName,
                    isSuccess = false,
                    exitCode = 1,
                    outputMessage = errorMsg,
                    commandExecuted = "am force-stop ${app.packageName}"
                )
                onResult(false, errorMsg)
            }
        }
    }

    fun openAppDetails(packageName: String) {
        appRepo.openAppDetailsSettings(packageName)
    }

    fun executeAction(action: TurboAction, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = actionRepo.executeAction(action)
            if (result.isSuccess) {
                val cmdRes = result.getOrNull()
                val message = cmdRes?.stdout?.ifEmpty { "Executed successfully." } ?: "Executed successfully."
                historyRepo.addRecord(
                    actionId = action.id,
                    actionName = action.displayName,
                    isSuccess = true,
                    exitCode = cmdRes?.exitCode ?: 0,
                    outputMessage = message,
                    commandExecuted = action.commandTemplate,
                    reversalCommand = action.rollbackInstructions
                )
                onResult(true, message)
            } else {
                val error = result.exceptionOrNull()?.localizedMessage ?: "Execution failed"
                historyRepo.addRecord(
                    actionId = action.id,
                    actionName = action.displayName,
                    isSuccess = false,
                    exitCode = 1,
                    outputMessage = error,
                    commandExecuted = action.commandTemplate
                )
                onResult(false, error)
            }
        }
    }

    fun clearHistory() {
        historyRepo.clearHistory()
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
    }
}
