package com.alight.android.aoa_launcher.common.bean

data class AppUninstallBean(
    val code: Int,
    val data: List<AppUninstallBeanData>,
    val msg: String,
    val request: String
)

data class AppUninstallBeanData(
    val create_time: String,
    val del_flag: Int,
    val device_id: Int,
    val id: Int,
    val is_active: Boolean,
    val package_name: String,
    val update_time: String
)