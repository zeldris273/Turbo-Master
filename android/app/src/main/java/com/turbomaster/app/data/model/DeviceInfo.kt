package com.turbomaster.app.data.model

data class DeviceMetrics(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalInternalStorageBytes: Long,
    val availableInternalStorageBytes: Long,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val isLowRamDevice: Boolean,
    val cpuAbi: String
) {
    val totalRamMb: Long get() = totalRamBytes / (1024 * 1024)
    val availableRamMb: Long get() = availableRamBytes / (1024 * 1024)
    val ramUsedPercent: Int get() = if (totalRamBytes > 0) {
        (((totalRamBytes - availableRamBytes).toDouble() / totalRamBytes) * 100).toInt()
    } else 0
    val totalStorageGb: Double get() = totalInternalStorageBytes.toDouble() / (1024 * 1024 * 1024)
    val availableStorageGb: Double get() = availableInternalStorageBytes.toDouble() / (1024 * 1024 * 1024)
}
