package com.alight.android.aoa_launcher.common.bean

data class CallVideoBean(
    val extras: String,
    val id: Int,
    val intent_url: String,
    val message: Message,
    val title: String,
    val trace: Trace,
    val type: String
)

data class Message(
    val fromUserId: Int,
    val fromUserInfo: FromUserInfo,
    val roomId: Int,
    val type: String,
    val userId: Int
)

data class Trace(
    val create_time: String,
    val dst_id: Int,
    val dst_platform: Int,
    val src_id: Int,
    val src_platform: Int,
    val update_time: String
)

data class FromUserInfo(
    val avatar: String,
    val birthday: Int,
    val city: String,
    val country: String,
    val create_time: String,
    val gender: Int,
    val id: Int,
    val is_active: Boolean,
    val language: Any,
    val name: String,
    val phone: String,
    val province: String,
    val update_time: String
)