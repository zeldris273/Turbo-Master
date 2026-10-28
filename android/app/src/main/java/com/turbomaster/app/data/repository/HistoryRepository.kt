package com.turbomaster.app.data.repository

import com.turbomaster.app.data.model.OperationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryRepository {
    private val _records = MutableStateFlow<List<OperationRecord>>(emptyList())
    val records: StateFlow<List<OperationRecord>> = _records.asStateFlow()

    fun addRecord(
        actionId: String,
        actionName: String,
        targetPackage: String? = null,
        isSuccess: Boolean,
        exitCode: Int,
        outputMessage: String,
        commandExecuted: String? = null,
        reversalCommand: String? = null
    ) {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val record = OperationRecord(
            id = "log_${System.currentTimeMillis()}",
            timestamp = timestamp,
            actionId = actionId,
            actionName = actionName,
            targetPackage = targetPackage,
            isSuccess = isSuccess,
            exitCode = exitCode,
            outputMessage = outputMessage,
            commandExecuted = commandExecuted,
            reversalAvailable = reversalCommand != null,
            reversalCommand = reversalCommand
        )

        _records.value = listOf(record) + _records.value
    }

    fun clearHistory() {
        _records.value = emptyList()
    }
}
