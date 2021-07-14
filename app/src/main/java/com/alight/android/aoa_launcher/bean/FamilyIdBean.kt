package com.alight.android.aoa_launcher.bean

data class FamilyIdBean(
    val code: Int,
    val data: Data,
    val msg: String
)

data class Data(
    val family_id: Int
)