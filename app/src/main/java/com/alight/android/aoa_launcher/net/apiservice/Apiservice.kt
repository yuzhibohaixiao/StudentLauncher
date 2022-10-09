package com.alight.android.aoa_launcher.net.apiservice

import com.alight.android.aoa_launcher.net.urls.Urls
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface Apiservice {

    @GET
    fun getAllInfo(@Url url: String, @QueryMap map: HashMap<String, Any>): Observable<ResponseBody>


    /*  @DELETE(Urls.DEVICE_RELATION)
      fun deleteAllInfo(@Body("comment_id") comment_id: String)*/

    @HTTP(method = "DELETE", path = Urls.DEVICE_RELATION, hasBody = true)
    fun deleteAllInfo(
        @Body requestBody: RequestBody
    ): Observable<ResponseBody>


    @Streaming
    @GET
    fun download(@Url url: String?): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST
    fun postAllInfo(@Url url: String, @Body requestBody: RequestBody): Observable<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT
    fun putAllInfo(@Url url: String, @Body requestBody: RequestBody): Observable<ResponseBody>

    @Multipart
    @POST
    fun  //这里是自己post文件的地址
            postGoodsReturnPostEntitys(
        @PartMap map: Map<*, *>,
        @Part parts: List<*>
    ): Observable<*>


    // 检测记录上报
    @Headers("Content-Type: application/json")
    @Multipart
    @POST("uploadRecord")
    fun uploadIcon(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): Observable<ResponseBody>


}
