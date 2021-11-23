package com.alight.android.aoa_launcher.common.bean

import java.io.Serializable

data class UpdateBean(
    val code: Int,
    val data: List<UpdateBeanData>,
    val msg: String,
    val request: String
) : Serializable

data class UpdateBeanData(
    val apk_md5: String,
    val apk_size: Long,
    val app_force_upgrade: Int,
    var app_name: String,
    val app_url: String,
    val content: String,
    val create_time: String,
    val id: Int,
    val is_active: Boolean,
    val update_time: String,
    val version_code: Int,
    val version_name: String,
    var packName: String,
    var format: Int,
    var app_info: AppInfo
) : Serializable

/**
 *
System = 1  # 系统应用
PresetsApp = 2  # 预置应用
Ota = 3  # ota包


class APP_FORMAT(enum.Enum):
Zip = 1  # zip包
Apk = 2  # apk包
 */
data class AppInfo(
    val package_name: String,
    val type: Int
) : Serializable