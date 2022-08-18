package com.alight.android.aoa_launcher.application

import android.app.Application
import android.content.Context
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.alibaba.ha.adapter.AliHaAdapter
import com.alibaba.ha.adapter.AliHaConfig
import com.alibaba.ha.adapter.Plugin
import com.alibaba.ha.adapter.service.tlog.TLogLevel
import com.alibaba.ha.adapter.service.tlog.TLogService
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.utils.SerialUtils.getCPUSerial
import com.liulishuo.okdownload.DownloadTask
import com.tencent.mmkv.MMKV
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.utils.UpdateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xutils.x
import java.util.*


class LauncherApplication : Application() {
    var TAG = "LauncherApplication"

    companion object {
        var downloadTaskHashMap = HashMap<String, DownloadTask>()
            private set
        private var context: Application? = null
        fun getContext(): Context {
            return context!!
        }

        private var mmkv: MMKV? = null
        fun getMMKV(): MMKV {
            return mmkv!!
        }

    }

    override fun onCreate() {
        super.onCreate()
        context = this
        x.Ext.init(this)
        init()
    }

    private fun init() {
        //初始化极光推送
        JPushInterface.init(this)
//        JPushInterface.setDebugMode(true)
        //初始化听云sdk
        //"Host" 为听云平台「Redirect」服务器地址，无需添加协议头
/*        NBSAppAgent.setLicenseKey("32c1f7c04eb64c3c95e1c4cd9625aa65")
            .setRedirectHost("wkrd.tingyun.com")
            .withLocationServiceEnabled(true).enableLogging(true).setStartOption(511)
//            .setHttpEnabled(true)
            .startInApplication(applicationContext)*/;//首次初始化开启全部功能
        //XUpdate全局初始化
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
            }.init(this) //这个必须初始化

        //MMKV
        val rootDir = MMKV.initialize(this, AppConstants.SYSTEM_MMKV_PATH)
        mmkv = MMKV.mmkvWithID(
            AppConstants.MMKV_MMAP_ID,
            MMKV.MULTI_PROCESS_MODE,
            AppConstants.MMKV_KEY
        )
        Log.i(TAG, "mmkv root: $rootDir")
        initHa()
    }

    private fun initHa() {
        CoroutineScope(Dispatchers.IO).launch {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val appVersionCode = packageInfo.versionCode
            val appVersionName = packageInfo.versionName

            val config = AliHaConfig()
            config.appKey = "333756021"
            config.appVersion = "[$appVersionCode]$appVersionName"
            config.appSecret = "e33ca63654744d53ac5af3f8e11ae405"
            config.channel = "mqc_test"
            config.userNick = getCPUSerial()
            config.application = this@LauncherApplication
            config.context = applicationContext
            config.isAliyunos = false
            config.rsaPublicKey =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCi76KrlhbPtobMHvTAK3Lk+JhdsApZnndPjLjXdBdG5LFL9RLG/PC0srSYlCtNMh8x+wQrJk1NmePuUkyHlmL6/zOkv3kSmDbUi2akLSbmAgHGe6FhWmelpeyLsxilfIF0wMCus8AD+pEUWPu3bk8KY6uiXKjy7xXXYsVvarVdFQIDAQAB";

            // 启动CrashReporter
            AliHaAdapter.getInstance().addPlugin(Plugin.crashreporter)
            AliHaAdapter.getInstance().addPlugin(Plugin.tlog)
            AliHaAdapter.getInstance().openDebug(false)
            AliHaAdapter.getInstance().start(config)
            TLogService.updateLogLevel(TLogLevel.INFO)
        }
    }

}