package com.alight.android.aoa_launcher.common.bean

/**
 * 每个数据可以包含三条课程内容
 */
data class AppTrebleDataBean(
    var appIcon1: Int,
    var appPackName1: String,
    var appName1: String,
    var className1: String?,
    var params1: Map<String, Any>?,
    var appIcon2: Int,
    var appPackName2: String,
    var appName2: String,
    var className2: String?,
    var params2: Map<String, Any>?,
    var appIcon3: Int,
    var appPackName3: String,
    var appName3: String,
    var className3: String?,
    var params3: Map<String, Any>?,
)