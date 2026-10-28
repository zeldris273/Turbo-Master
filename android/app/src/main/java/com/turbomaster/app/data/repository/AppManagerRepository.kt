package com.turbomaster.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.turbomaster.app.data.model.AppItem
import com.turbomaster.app.data.model.StandbyBucket
import com.turbomaster.app.data.shizuku.CommandExecutionResult
import com.turbomaster.app.data.shizuku.ShizukuCommandRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManagerRepository(private val context: Context) {

    // Critical packages that MUST NEVER be force-stopped
    private val criticalPackages = setOf(
        context.packageName,
        "com.android.systemui",
        "com.android.phone",
        "com.android.settings",
        "com.google.android.gms",
        "com.google.android.gsf",
        "android",
        "com.android.server.telecom",
        "com.android.launcher3"
    )

    fun isCriticalPackage(packageName: String): Boolean {
        return criticalPackages.contains(packageName) || packageName == context.packageName
    }

    suspend fun getInstalledApps(includeSystem: Boolean = false): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        installed.mapNotNull { appInfo ->
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!includeSystem && isSystem) return@mapNotNull null

            val appName = pm.getApplicationLabel(appInfo).toString()
            val packageName = appInfo.packageName

            AppItem(
                packageName = packageName,
                appName = appName,
                versionName = try {
                    pm.getPackageInfo(packageName, 0).versionName ?: "1.0"
                } catch (e: Exception) { "1.0" },
                targetSdk = appInfo.targetSdkVersion,
                uid = appInfo.uid,
                isSystemApp = isSystem,
                isCriticalPackage = isCriticalPackage(packageName),
                isTurboSelf = packageName == context.packageName,
                standbyBucket = StandbyBucket.ACTIVE
            )
        }.sortedBy { it.appName.lowercase() }
    }

    suspend fun forceStopApp(packageName: String): Result<CommandExecutionResult> {
        if (isCriticalPackage(packageName)) {
            return Result.failure(
                SecurityException("Package '$packageName' is protected by Turbo safety exclusions.")
            )
        }

        // am force-stop <package>
        val tokens = arrayOf("am", "force-stop", packageName)
        return ShizukuCommandRunner.executeSafe(tokens)
    }

    fun openAppDetailsSettings(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
