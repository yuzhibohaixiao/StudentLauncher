package com.alight.android.aoa_launcher.common.bean

data class UploadImage(
    val code: Int,
    val data: UploadImageData,
    val msg: String,
    val request: String
)

data class UploadImageData(
    val url: String
)