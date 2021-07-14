package com.alight.android.aoa_launcher.bean

data class FamilyInfoBean(
    val code: Int,
    val data: Data,
    val msg: String,
    val request: String
)

data class Data(
    val children: List<Children>,
    val create_time: String,
    val id: Int,
    val is_active: Boolean,
    val name: String,
    val parents: List<Parent>,
    val update_time: String
)

data class Children(
    val avatar: String,
    val birthday: Int,
    val city: String,
    val country: String,
    val create_time: String,
    val gender: Int,
    val grade_type: Int,
    val id: Int,
    val is_active: Boolean,
    val language: Any,
    val name: String,
    val phone: String,
    val province: String,
    val update_time: String,
    val user_id: Int
)

data class Parent(
    val avatar: String,
    val birthday: Int,
    val city: String,
    val country: String,
    val create_time: String,
    val family_id: Int,
    val gender: Int,
    val id: Int,
    val is_active: Boolean,
    val language: Any,
    val name: String,
    val parent_id: Int,
    val phone: String,
    val province: String,
    val role_type: Int,
    val update_time: String,
    val user_id: Int
)