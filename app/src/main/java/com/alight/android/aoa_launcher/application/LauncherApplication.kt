package com.alight.android.aoa_launcher.application

import android.app.Application
import android.content.Context
import android.util.Log
import com.tencent.mmkv.MMKV


class LauncherApplication : Application() {
    var TAG = "LauncherApplication"
    override fun onCreate() {
        super.onCreate()
        context = this
        val rootDir = MMKV.initialize(this)
        Log.i(TAG, "mmkv root: $rootDir")
    }

    companion object {
        var context: Context? = null
            private set
    }
}