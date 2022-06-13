package com.alight.android.aoa_launcher.net.apiservice

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface AccountService {

    @Headers(
        "Connection:close"
    )
    @GET("auth/v1/token/renew")
    fun renewToken(@Header("ACToken") oldToken: String): Call<ResponseBody>

    @Headers("Connection:close")
    @GET("device/v1/student/user")
    fun getRelatedUsers(@Query("dsn")dsn:String): Call<ResponseBody>

    @Headers("Connection:close")
    @POST("device/v1/student/login")
    fun declareUser(@Header("ACToken") token: String,@Body body:RequestBody):Call<ResponseBody>


    @Headers("Connection:close")
    @POST("family/v1/parents/messages")
    fun postMsg(@Header("ACToken") token: String,@Body body: RequestBody):Call<ResponseBody>


    @Headers("Connection:close")
    @GET("/device/v1/qr-code")
    fun getQrCode(@Query("dsn")dsn:String):Call<ResponseBody>

    @Headers("Connection:close")
    @GET("/device/v1/cdk")
    fun getCDK(@Query("dsn")dsn:String):Call<ResponseBody>
}