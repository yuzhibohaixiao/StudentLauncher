package com.alight.android.aoa_launcher.bean

import android.graphics.drawable.Drawable

data class AppBean(
    var appName: CharSequence,
    var appPackName: String,
    var appIcon: Drawable
)