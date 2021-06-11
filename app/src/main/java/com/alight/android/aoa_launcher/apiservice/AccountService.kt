package com.alight.android.aoa_launcher.apiservice

import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Call
import retrofit2.http.*

interface AccountService {

    @Headers(
        "Connection : close"
    )
    @GET("auth/v1/token/renew")
    fun renewToken(@Header("ACToken") oldToken: String): Call<Response>

    @Headers("Connection : close")
    @GET("device/v1/student/user")
    fun getRelatedUsers(@Query("dsn")dsn:String): Call<Response>

    @Headers("Connection : close")
    @POST("device/v1/student/login")
    fun declareUser(@Header("ACToken") token: String,@Body body:RequestBody):Call<Response>


}