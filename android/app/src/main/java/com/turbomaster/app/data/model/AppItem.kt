package com.turbomaster.app.data.model

data class AppItem(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val targetSdk: Int,
    val uid: Int,
    val isSystemApp: Boolean,
    val isCriticalPackage: Boolean,
    val isTurboSelf: Boolean,
    val standbyBucket: StandbyBucket = StandbyBucket.ACTIVE,
    val ramUsageMb: Long = 0
)

enum class StandbyBucket {
    ACTIVE,
    WORKING_SET,
    FREQUENT,
    RARE,
    RESTRICTED,
    UNKNOWN
}
