package com.alight.android.aoa_launcher.net.urls

import android.util.Log
import com.alight.android.aoa_launcher.BuildConfig
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.utils.SPUtils
import okhttp3.Interceptor
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
        var originalRequest = chain.request(); //Current Request

        var response = chain.proceed(originalRequest); //Get response of the request

        /** DEBUG STUFF */
        if (BuildConfig.DEBUG) {
            //I am logging the response body in debug mode. When I do this I consume the response (OKHttp only lets you do this once) so i have re-build a new one using the cached body
            var bodyString = response.body()?.string();
            Log.i(
                "OkHttp", "intercept: " + (
                        String.format(
                            "Sending request %s with headers %s ",
                            originalRequest.url(),
                            originalRequest.headers()
                        )
                        )
            )
            Log.i(
                "OkHttp", "intercept: " + (
                        String.format(
                            "Got response HTTP %s %s \n\n with body %s \n\n with headers %s ",
                            response.code(),
                            response.message(),
                            bodyString,
                            response.headers()
                        ))
            )
        }
        // 开始请求
        return chain.proceed(request)
    }
}