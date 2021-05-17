package com.alight.android.aoa_launcher

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.qweather.sdk.view.HeConfig

class LauncherApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        var context: Application? = null

        fun getContext(): Context {
            return context!!
        }
    }

}