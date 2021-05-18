package com.alight.android.aoa_launcher.bean

data class BannerBean(
        val message: String,
        val result: List<BannerResult>,
        val status: String
)

data class BannerResult(
        val imageUrl: String,
        val jumpUrl: String,
        val rank: Int
)