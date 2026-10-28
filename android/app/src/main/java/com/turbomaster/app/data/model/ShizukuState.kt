package com.turbomaster.app.data.model

sealed class ShizukuConnectionState {
    object NotInstalled : ShizukuConnectionState()
    object ServiceStopped : ShizukuConnectionState()
    object PermissionRequired : ShizukuConnectionState()
    data class Connected(val version: Int, val uid: Int) : ShizukuConnectionState()
    data class BinderDead(val message: String) : ShizukuConnectionState()
}
