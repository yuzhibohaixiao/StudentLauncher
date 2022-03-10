package com.alight.android.aoa_launcher.common.bean

data class ParentControlBean(
    val extras: Extras,
    val id: Double,
    val intent_url: String,
    val message: String,
    val title: String,
    val trace: Trace,
    val type: Int
)

data class Extras(
    val dst_id: Int,
    val dst_platform: Int,
    val src_id: Int,
    val src_platform: Int
)