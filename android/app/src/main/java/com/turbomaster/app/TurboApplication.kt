package com.turbomaster.app

import android.app.Application
import android.util.Log
import com.turbomaster.app.data.shizuku.ShizukuManager

class TurboApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Turbo Application starting on Android API ${android.os.Build.VERSION.SDK_INT}")

        // Initialize official Shizuku Binder listener lifecycle
        ShizukuManager.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ShizukuManager.destroy()
    }

    companion object {
        const val TAG = "TurboApp"
    }
}
