package com.alight.android.aoa_launcher.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TokenPair(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("ACToken")
    var token: String? = null,
    @SerializedName("expire_time")
    var expireTime: Double? = null,
    var gender: Int? = null,
    var avatar: String? = null,
    var name: String? = null
) : Serializable
