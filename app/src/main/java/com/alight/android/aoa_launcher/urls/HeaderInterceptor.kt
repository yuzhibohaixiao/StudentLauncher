package com.alight.android.aoa_launcher.urls

import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.utils.SPUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @author wangzhe
 * header拦截器
 */
class HeaderInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SPUtils.getData(
            AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN,
            ""
        ) as String
        // 以拦截到的请求为基础创建一个新的请求对象，然后插入Header
        var request = chain.request().newBuilder()
            .addHeader("ACToken", token)
            .build();
        // 开始请求
        return chain.proceed(request)
    }
}