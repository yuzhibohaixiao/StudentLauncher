package com.alight.android.aoa_launcher.common.bean

data class CallArBean(
    val extras: String,
    val id: String,
    val intent_url: String,
    val message: CallArBeanMessage,
    val title: String,
    val trace: CallArBeanTrace,
    val type: Int
)

data class CallArBeanMessage(
    val fromUserId: Int,
    val fromUserInfo: CallArBeanFromUserInfo,
    val roomId: Int,
    val type: String,
    val userId: Int
)

data class CallArBeanTrace(
    val dst_id: Int,
    val dst_platform: Int,
    val src_id: Int,
    val src_platform: Int
)

data class CallArBeanFromUserInfo(
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