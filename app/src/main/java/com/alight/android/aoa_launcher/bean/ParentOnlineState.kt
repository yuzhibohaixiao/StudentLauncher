package com.alight.android.aoa_launcher.bean

data class ParentOnlineState(
    val code: Int,
    val msg: String,
    var data: Data2
)

data class Data2(
    //在线状态 true表示在线
    var value: Boolean,
    var user_id: Int
)