package com.alight.android.aoa_launcher.common.bean

data class AppSortBean(
    val code: Int,
    val data: List<AppSortBeanData>,
    val msg: String,
    val request: String
)

data class AppSortBeanData(
    val app_icon: String,
    val app_label: Int,
    val app_name: String,
    val app_version: Any,
    val create_time: String,
    val del_flag: Int,
    val id: Int,
    val is_active: Boolean,
    val order_num: Int,
    val package_name: String,
    val remark: String,
    val top_flag: Int,
    val type: Int,
    val un_install: Int,
    val update_time: String,
    val args: String,
    val class_name: String
)