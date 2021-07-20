package com.alight.android.aoa_launcher.net.apiservice

import com.alight.android.aoa_launcher.net.urls.Urls
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface Apiservice {

    @GET
    fun getAllInfo(@Url url: String, @QueryMap map: HashMap<String, Any>): Observable<ResponseBody>

    @HTTP(method = "DELETE", path = Urls.DEVICE_RELATION, hasBody = true)
    fun deleteAllInfo(
        @Url url: String,
        @QueryMap map: HashMap<String, Any>
    ): Observable<ResponseBody>


    @Streaming
    @GET
    fun download(@Url url: String?): Call<ResponseBody>
}