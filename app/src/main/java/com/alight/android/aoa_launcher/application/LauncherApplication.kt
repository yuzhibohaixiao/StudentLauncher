package com.alight.android.aoa_launcher.application

import android.app.Application
import android.content.Context

class LauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        var context: Context? = null
            private set
    }
}