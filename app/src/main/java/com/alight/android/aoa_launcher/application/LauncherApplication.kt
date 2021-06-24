package com.alight.android.aoa_launcher.application

import android.app.Application
import android.content.Context
import android.util.Log
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.tencent.mmkv.MMKV
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.listener.OnUpdateFailureListener
import com.xuexiang.xupdate.proxy.IUpdateHttpService
import com.xuexiang.xupdate.utils.UpdateUtils


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
            .isWifiOnly(true) //默认设置只在wifi下检查版本更新
            .isGet(true) //默认设置使用get请求检查版本
            .isAutoMode(false) //默认设置非自动模式，可根据具体使用配置
            .param("versionCode", UpdateUtils.getVersionCode(this)) //设置默认公共请求参数
            .param("appKey", packageName)
            .setOnUpdateFailureListener { error ->

                //设置版本更新出错的监听
                if (error.code !== CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                    ToastUtils.showShort(this@LauncherApplication, error.toString())
                }
            }
            .supportSilentInstall(true) //设置是否支持静默安装，默认是true
            .setIUpdateHttpService(object : IUpdateHttpService {
                override fun download(
                    url: String,
                    path: String,
                    fileName: String,
                    callback: IUpdateHttpService.DownloadCallback
                ) {
                    TODO("Not yet implemented")
                }

                override fun asyncGet(
                    url: String,
                    params: MutableMap<String, Any>,
                    callBack: IUpdateHttpService.Callback
                ) {
                    TODO("Not yet implemented")
                }

                override fun cancelDownload(url: String) {
                    TODO("Not yet implemented")
                }

                override fun asyncPost(
                    url: String,
                    params: MutableMap<String, Any>,
                    callBack: IUpdateHttpService.Callback
                ) {
                    TODO("Not yet implemented")
                }
            }) //这个必须设置！实现网络请求功能。
            .init(this) //这个必须初始化
    }

    companion object {
        var context: Context? = null
            private set
    }
}