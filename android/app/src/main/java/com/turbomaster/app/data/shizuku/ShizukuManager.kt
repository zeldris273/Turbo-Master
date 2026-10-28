package com.turbomaster.app.data.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.turbomaster.app.data.model.ShizukuConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuManager {
    private const val TAG = "ShizukuManager"
    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val REQUEST_CODE_SHIZUKU = 9001

    private val _connectionState = MutableStateFlow<ShizukuConnectionState>(ShizukuConnectionState.NotInstalled)
    val connectionState: StateFlow<ShizukuConnectionState> = _connectionState.asStateFlow()

    private var appContext: Context? = null
    fun applicationContextOrNull(): Context? = appContext
    
    // Binder Received Listener
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder token received.")
        checkCurrentStatus()
    }

    // Binder Dead Listener
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "Shizuku binder died unexpectedly.")
        _connectionState.value = ShizukuConnectionState.BinderDead("Shizuku service crashed or stopped.")
    }

    // Permission Result Listener
    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_SHIZUKU) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Shizuku permission granted by user.")
                    checkCurrentStatus()
                } else {
                    Log.w(TAG, "Shizuku permission denied by user.")
                    _connectionState.value = ShizukuConnectionState.PermissionRequired
                }
            }
        }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            checkCurrentStatus()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize Shizuku listeners", t)
        }
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to clean up Shizuku listeners", t)
        }
    }

    fun isShizukuInstalled(): Boolean {
        val context = appContext ?: return false
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun checkCurrentStatus() {
        if (!isShizukuInstalled()) {
            _connectionState.value = ShizukuConnectionState.NotInstalled
            return
        }

        if (!Shizuku.pingBinder()) {
            _connectionState.value = ShizukuConnectionState.ServiceStopped
            return
        }

        if (Shizuku.isPreV11()) {
            _connectionState.value = ShizukuConnectionState.ServiceStopped
            return
        }

        try {
            val hasPermission = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                val version = Shizuku.getVersion()
                val uid = Shizuku.getUid()
                _connectionState.value = ShizukuConnectionState.Connected(version, uid)
            } else {
                _connectionState.value = ShizukuConnectionState.PermissionRequired
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error checking Shizuku permission", t)
            _connectionState.value = ShizukuConnectionState.BinderDead(t.localizedMessage ?: "Unknown binder error")
        }
    }

    fun requestPermission() {
        if (Shizuku.isPreV11()) return
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to invoke requestPermission", t)
        }
    }
}
