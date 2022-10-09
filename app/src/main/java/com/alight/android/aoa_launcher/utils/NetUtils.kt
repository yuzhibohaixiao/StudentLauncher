package com.alight.android.aoa_launcher.utils

import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.bean.BaseBean
import com.alight.android.aoa_launcher.net.apiservice.Apiservice
import com.alight.android.aoa_launcher.net.urls.Urls
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


/**
 *  网络请求的封装类
 */
class NetUtils private constructor() {
    lateinit var apiService: Apiservice

    init {
        var log = HttpLoggingInterceptor()
        log.setLevel(HttpLoggingInterceptor.Level.BODY)

        /*    var ok = OkHttpClient.Builder()
            .addInterceptor(log)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .build()*/

        val okHttpClient = HTTPSUtils.getInstance(LauncherApplication.getContext())

        var retro = Retrofit.Builder()
            .baseUrl(Urls.BASEURL)
            .client(okHttpClient)
            //1 替换Factory CoroutineCallAdapterFactory()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retro.create(Apiservice::class.java)
    }

    companion object {
        val intance: NetUtils by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { NetUtils() }
    }

    //具体的网络请求实现类
    fun <T> getInfo(url: String, map: HashMap<String, Any>, cls: Class<T>, callback: NetCallback) {
        apiService.getAllInfo(url, map).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: ResponseBody) {
                    var gson = Gson()
                    /*     try {
                            var any = gson.fromJson(t.string(), cls)
                            if (callback != null && any != null) {
                                //回调到model层
                                callback.onSuccess(any)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }*/
                    try {
                        val string = t.string()
                        val baseBean = gson.fromJson(
                            string,
                            BaseBean::class.java
                        ) //拦截code非401情况
                        if (baseBean.code != 401) {
                            var any = gson.fromJson(string, cls)
                            if (callback != null && any != null) {
                                //回调到model层
                                callback.onSuccess(any)
                            }
                        } else {
                            //表示用户离线
                            callback.onSuccess(baseBean)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(e: Throwable) {
                    try {
                        if (callback != null) {
                            callback.onError(e.message!!)
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }

            })
    }

    fun <T> putInfo(
        url: String,
        requestBody: RequestBody,
        cls: Class<T>,
        callback: NetCallback
    ) {
        apiService.putAllInfo(url, requestBody).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: ResponseBody) {
                    var gson = Gson()
                    try {
                        var any = gson.fromJson(t.string(), cls)
                        if (callback != null && any != null) {
                            //回调到model层
                            callback.onSuccess(any)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(e: Throwable) {
                    if (callback != null) {
                        callback.onError(e.message!!)
                    }
                }

            })
    }

    fun <T> postInfo(
        url: String,
        requestBody: RequestBody,
        cls: Class<T>,
        callback: NetCallback
    ) {
        apiService.postAllInfo(url, requestBody).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: ResponseBody) {
                    var gson = Gson()
                    try {
                        var any = gson.fromJson(t.string(), cls)
                        if (callback != null && any != null) {
                            //回调到model层
                            callback.onSuccess(any)
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(e: Throwable) {
                    if (callback != null) {
                        callback.onError(e.message!!)
                    }
                }

            })
    }

    //具体的网络请求实现类
    fun <T> deleteInfo(
        requestBody: RequestBody,
        cls: Class<T>,
        callback: NetCallback
    ) {
        apiService.deleteAllInfo(requestBody).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: ResponseBody) {
                    var gson = Gson()
                    var any = gson.fromJson(t.string(), cls)
                    if (callback != null && any != null) {
                        //回调到model层
                        callback.onSuccess(any)
                    }
                }

                override fun onError(e: Throwable) {
                    if (callback != null) {
                        callback.onError(e.message!!)
                    }
                }

            })
    }

    fun <T> uploadIcon(
        url: String,
        filePath: String,
        cls: Class<T>,
        callback: NetCallback
    ) {
        val file = File(filePath) // picture是图片路径，通过路径生成file文件
        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), file)
        // avatar参数是一个自定义的名字，自己随便写
        val createFormData = MultipartBody.Part.createFormData("icon", file.name, requestBody)
        apiService.uploadIcon(url, createFormData).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: ResponseBody) {
                    //回调到model层
                    var gson = Gson()
                    var any = gson.fromJson(t.string(), cls)
                    if (callback != null && any != null) {
                        //回调到model层
                        callback.onSuccess(any)
                    }
                }

                override fun onError(e: Throwable) {
                    if (callback != null) {
                        callback.onError(e.message!!)
                    }
                }

            })
    }


    fun isNet(): Boolean {
        return false
    }

    /**
     * 网络请求的回调接口
     */
    interface NetCallback {
        //成功
        fun onSuccess(any: Any)

        //失败
        fun onError(error: String)
    }

}

inline fun <reified T : Any> T.toJson(): String {
    return Gson().toJson(this)
}
