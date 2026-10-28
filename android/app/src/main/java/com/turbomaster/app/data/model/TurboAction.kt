package com.turbomaster.app.data.model

enum class ActionRiskLevel {
    SAFE,
    MODERATE,
    ELEVATED
}

data class TurboAction(
    val id: String,
    val displayName: String,
    val category: String,
    val description: String,
    val requiredPermissions: List<String>,
    val supportedAndroidVersions: String,
    val minApiLevel: Int,
    val riskLevel: ActionRiskLevel,
    val modifiesSystemState: Boolean,
    val commandTemplate: String,
    val rollbackInstructions: String? = null,
    val isReversible: Boolean = false
)
