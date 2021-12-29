package com.alight.android.aoa_launcher.common.bean

/**
 * 每个数据可以包含两条课程内容
 */
data class AppRightDoubleDataBean(
    var appIcon1: Int,
    var appPackName1: String,
    var appClassName1: String,
    var params1: Map<String, Any>?,
    var appName1: String,
    var appIcon2: Int,
    var appPackName2: String,
    var appClassName2: String,
    var appName2: String,
    var params2: Map<String, Any>?
)