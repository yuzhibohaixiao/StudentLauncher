package com.alight.android.aoa_launcher.utils

import com.alight.android.aoa_launcher.apiservice.AccountService
import com.alight.android.aoa_launcher.bean.TokenPair
import com.alight.android.aoa_launcher.urls.Urls
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object AccountUtil {
    var currentUserId: Int? = null
    var tokenMap: MutableMap<Int, TokenPair> = HashMap()
    var retrofit = Retrofit.Builder()
        .baseUrl(Urls.ALIGHT_URL)
//        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var service:AccountService = retrofit.create(AccountService::class.java)

    private fun renewToken(tokenPair: TokenPair,blocking:Boolean)
    }
}