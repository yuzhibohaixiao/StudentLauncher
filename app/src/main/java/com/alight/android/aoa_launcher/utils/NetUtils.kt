package com.alight.android.aoa_launcher.utils

import com.alight.android.aoa_launcher.net.apiservice.Apiservice
import com.alight.android.aoa_launcher.net.urls.HeaderInterceptor
import com.alight.android.aoa_launcher.net.urls.Urls
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 协程 类似于rxjava 是一个异步处理库
 *
 * kotlin_version 大于1.3
 * 使用方式：
 * 导入
 * implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1"
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1'
 */
class NetUtils private constructor() {
    lateinit var apiService: Apiservice

    init {
        var log = HttpLoggingInterceptor()
        log.setLevel(HttpLoggingInterceptor.Level.BODY)
        var ok = OkHttpClient.Builder()
            .addInterceptor(log)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .build()
        var retro = Retrofit.Builder()
            .baseUrl(Urls.BASEURL_TEST)
            .client(ok)
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

    /**
     * 通过CmyIP获取获取外网外网地址  需在异步线程中访问
     * @return 外网IP
     */
    fun getOuterNetFormCmyIP(): String? {
        val response = GetOuterNetIp("http://www.cmyip.com/")
        val pattern: Pattern = Pattern
            .compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))")
        val matcher: Matcher = pattern.matcher(response)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

    /**
     * 获取获取外网外网地址  需在异步线程中访问
     * @param ipaddr 提供外网服务的服务器ip地址
     * @return       外网IP
     */
    fun GetOuterNetIp(ipaddr: String?): String? {
        var infoUrl: URL? = null
        var inStream: InputStream? = null
        try {
            infoUrl = URL(ipaddr)
            val connection: URLConnection = infoUrl.openConnection()
            val httpConnection: HttpURLConnection = connection as HttpURLConnection
            val responseCode: Int = httpConnection.getResponseCode()
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream()
                val reader = BufferedReader(InputStreamReader(inStream, "utf-8"))
                val strber = StringBuilder()
                var line: String? = null
                while (reader.readLine().also({ line = it }) != null) strber.append(
                    """
                        $line
                        
                        """.trimIndent()
                )
                inStream.close()
                return strber.toString()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

}

inline fun <reified T : Any> T.toJson(): String {
    return Gson().toJson(this)

}