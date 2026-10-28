package com.turbomaster.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.turbomaster.app.data.model.ActionRiskLevel
import com.turbomaster.app.data.model.TurboAction
import com.turbomaster.app.data.shizuku.CommandExecutionResult
import com.turbomaster.app.data.shizuku.ShizukuCommandRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TurboActionRepository(private val context: Context) {

    fun getActions(): List<TurboAction> {
        return listOf(
            TurboAction(
                id = "open_storage_settings",
                displayName = "Open Storage Settings",
                category = "storage",
                description = "Launches native Android Storage overview intent where user can review partition usage and clear verified cache.",
                requiredPermissions = listOf("Intent (No special permission needed)"),
                supportedAndroidVersions = "Android 6.0+ (API 23 - 35)",
                minApiLevel = 23,
                riskLevel = ActionRiskLevel.SAFE,
                modifiesSystemState = false,
                commandTemplate = "am start -a android.settings.INTERNAL_STORAGE_SETTINGS"
            ),
            TurboAction(
                id = "open_battery_settings",
                displayName = "Open Battery & Power Manager",
                category = "system",
                description = "Launches native Android battery optimization settings to review background usage.",
                requiredPermissions = listOf("Intent (No special permission needed)"),
                supportedAndroidVersions = "Android 6.0+ (API 23 - 35)",
                minApiLevel = 23,
                riskLevel = ActionRiskLevel.SAFE,
                modifiesSystemState = false,
                commandTemplate = "am start -a android.intent.action.POWER_USAGE_SUMMARY"
            ),
            TurboAction(
                id = "inspect_memory_info",
                displayName = "Inspect Detailed Process & RAM (dumpsys)",
                category = "inspect",
                description = "Queries ActivityManager memory stats without continuous polling.",
                requiredPermissions = listOf("Shizuku / Shell (DUMP privilege)"),
                supportedAndroidVersions = "Android 6.0+ (API 23 - 35)",
                minApiLevel = 23,
                riskLevel = ActionRiskLevel.SAFE,
                modifiesSystemState = false,
                commandTemplate = "dumpsys meminfo --compact"
            ),
            TurboAction(
                id = "tune_animator_scale",
                displayName = "Tune UI Animation Scales (0.5x)",
                category = "settings",
                description = "Adjusts Window, Transition, and Animator scales to 0.5x to reduce GPU compositing overhead on low-end chips.",
                requiredPermissions = listOf("Shizuku / Shell (WRITE_SECURE_SETTINGS)"),
                supportedAndroidVersions = "Android 6.0+ (API 23 - 35)",
                minApiLevel = 23,
                riskLevel = ActionRiskLevel.SAFE,
                modifiesSystemState = true,
                commandTemplate = "settings put global window_animation_scale 0.5",
                rollbackInstructions = "settings put global window_animation_scale 1.0",
                isReversible = true
            ),
            TurboAction(
                id = "query_fbe_storage_trim",
                displayName = "Run Flash Storage Trim (sm fstrim)",
                category = "storage",
                description = "Sends TRIM command to unallocated flash blocks on aging eMMC memory.",
                requiredPermissions = listOf("Shizuku / Shell privilege"),
                supportedAndroidVersions = "Android 6.0+ (API 23 - 35)",
                minApiLevel = 23,
                riskLevel = ActionRiskLevel.SAFE,
                modifiesSystemState = true,
                commandTemplate = "sm fstrim"
            )
        )
    }

    suspend fun executeAction(action: TurboAction): Result<CommandExecutionResult> = withContext(Dispatchers.IO) {
        if (action.minApiLevel > Build.VERSION.SDK_INT) {
            return@withContext Result.failure(
                UnsupportedOperationException("Action requires Android API ${action.minApiLevel}+")
            )
        }

        when (action.id) {
            "open_storage_settings" -> {
                val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Result.success(CommandExecutionResult(0, "Storage settings opened", "", 50))
            }
            "open_battery_settings" -> {
                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Result.success(CommandExecutionResult(0, "Battery settings opened", "", 50))
            }
            "inspect_memory_info" -> {
                ShizukuCommandRunner.executeSafe(arrayOf("dumpsys", "meminfo", "--compact"))
            }
            "tune_animator_scale" -> {
                ShizukuCommandRunner.executeSafe(arrayOf("settings", "put", "global", "window_animation_scale", "0.5"))
            }
            "query_fbe_storage_trim" -> {
                ShizukuCommandRunner.executeSafe(arrayOf("sm", "fstrim"))
            }
            else -> Result.failure(IllegalArgumentException("Unknown action id: ${action.id}"))
        }
    }
}
