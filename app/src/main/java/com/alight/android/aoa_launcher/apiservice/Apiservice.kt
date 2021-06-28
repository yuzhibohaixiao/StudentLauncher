package com.alight.android.aoa_launcher.apiservice

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface Apiservice {

    @GET
    fun getAllInfo(@Url url: String, @QueryMap map: HashMap<String, Any>): Observable<ResponseBody>

}