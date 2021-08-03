package com.alight.android.aoa_launcher.common.bean

data class DeviceBindBean(
    val code: Int,
    val data: DeviceBindBeanData,
    val msg: String,
    val request: String
)

data class DeviceBindBeanData(
    val exists: Boolean
)