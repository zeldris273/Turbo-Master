package com.turbomaster.app.data.model

data class OperationRecord(
    val id: String,
    val timestamp: String,
    val actionId: String,
    val actionName: String,
    val targetPackage: String? = null,
    val isSuccess: Boolean,
    val exitCode: Int,
    val outputMessage: String,
    val commandExecuted: String? = null,
    val reversalAvailable: Boolean = false,
    val reversalCommand: String? = null
)
