package com.alight.android.aoa_launcher.application

import android.app.Application
import android.content.Context
import android.util.Log
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.tencent.mmkv.MMKV
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.DownloadEntity
import com.xuexiang.xupdate.entity.UpdateError
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.listener.OnInstallListener
import com.xuexiang.xupdate.listener.OnUpdateFailureListener
import com.xuexiang.xupdate.logs.ILogger
import com.xuexiang.xupdate.proxy.IUpdateHttpService
import com.xuexiang.xupdate.utils.UpdateUtils
import java.io.File


class LauncherApplication : Application() {
    var TAG = "LauncherApplication"
    override fun onCreate() {
        super.onCreate()
        context = this
        val rootDir = MMKV.initialize(this)
        Log.i(TAG, "mmkv root: $rootDir")
        init()
    }

    private fun init() {
        XUpdate.get()
            .debug(true)
            .isWifiOnly(false) //默认设置只在wifi下检查版本更新
            .isGet(true) //默认设置使用get请求检查版本
            .isAutoMode(false) //默认设置非自动模式，可根据具体使用配置
            .param("versionCode", UpdateUtils.getVersionCode(this)) //设置默认公共请求参数
            .param("appKey", packageName)
            .supportSilentInstall(false) //设置是否支持静默安装，默认是true
            /* .setOnInstallListener(object : OnInstallListener {
                 override fun onInstallApk(
                     context: Context,
                     apkFile: File,
                     downloadEntity: DownloadEntity
                 ): Boolean {
                     return true
                 }

                 override fun onInstallApkSuccess() {
                 }
             })*/

            .setILogger { priority, tag, message, t ->
                Log.i(TAG, "XUpdate ILogger: $message")
            }
            .setOnUpdateFailureListener { error ->

                //设置版本更新出错的监听
                if (error.code !== CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                    Log.i(TAG, "XUpdate ILogger: $error")
                }
            }
//            .setIUpdateHttpService(object : IUpdateHttpService {
//                override fun download(
//                    url: String,
//                    path: String,
//                    fileName: String,
//                    callback: IUpdateHttpService.DownloadCallback
//                ) {
//                }
//
//                override fun asyncGet(
//                    url: String,
//                    params: MutableMap<String, Any>,
//                    callBack: IUpdateHttpService.Callback
//                ) {
//                }
//
//                override fun cancelDownload(url: String) {
//                }
//
//                override fun asyncPost(
//                    url: String,
//                    params: MutableMap<String, Any>,
//                    callBack: IUpdateHttpService.Callback
//                ) {
//                }
//            }) //这个必须设置！实现网络请求功能。
            .init(this) //这个必须初始化
    }

    companion object {
        var context: Context? = null
            private set
    }
}