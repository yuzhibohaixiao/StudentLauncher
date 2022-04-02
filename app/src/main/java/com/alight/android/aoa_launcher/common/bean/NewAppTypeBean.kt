package com.alight.android.aoa_launcher.common.bean

data class NewAppTypeBean(
    var appName: String,
    var appIcon: Int,
    var appPackName: String,
    var className: String?,
    var params: Map<String, Any>?,
)