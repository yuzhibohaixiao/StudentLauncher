package com.alight.android.aoa_launcher.common.bean

data class PlayTimeBean(
    val code: Int,
    val data: PlayTimeBeanData,
    val msg: String,
    val request: String
)

data class PlayTimeBeanData(
    val app_manage: List<AppManage>,
    val ar_manage: List<ArManage>,
    val create_time: String,
    val id: Int,
    val is_active: Boolean,
    val is_rest_day: Boolean,
    val playtime: Playtime,
    val rest_start_playtime: String,
    val rest_stop_playtime: String,
    val today_playtime: TodayPlaytime,
    val update_time: String,
    val user_id: Int,
    val work_start_playtime: String,
    val work_stop_playtime: String
)

data class AppManage(
    val app_id: Int,
    val app_info: AppManageInfo,
    val app_permission: Int,
    val args: Any,
    val category_id: Int,
    val class_name: Any,
    val create_time: String,
    val feature_name: String,
    val id: Int,
    val is_active: Boolean,
    val logo: String,
    val update_time: String
)

data class ArManage(
    val aoa_id: Int,
    val app_ar_permission: Boolean,
    val create_time: String,
    val id: Int,
    val is_active: Boolean,
    val logo: String,
    val name: String,
    val update_time: String
)

data class Playtime(
    val start_playtime: String,
    val stop_playtime: String
)

data class TodayPlaytime(
    val today_start_playtime: String,
    val today_stop_playtime: String,
    val unlimited: Boolean,
    val use: String
)

data class AppManageInfo(
    val app_name: String,
    val create_time: String,
    val id: Int,
    val is_active: Boolean,
    val package_name: String,
    val type: Int,
    val update_time: String
)