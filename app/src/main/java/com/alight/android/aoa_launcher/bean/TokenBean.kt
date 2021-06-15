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

class TokenManagerException(code: Int, message: String) :
    Exception("TokenException($code,$message)") {
    val code = code;
    val msg = message

    companion object {
        const val CODE_OK = 200
        const val CODE_ERR = 500
    }
}

data class TokenMessage(
    val title: String,
    val message: String,
    @SerializedName("intent_url")
    val intentUrl: String? = null,
    val type: String,
    val extra: Map<String, Any>? = null
)
