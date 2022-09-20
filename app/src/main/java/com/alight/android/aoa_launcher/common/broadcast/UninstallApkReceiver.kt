package com.alight.android.aoa_launcher.common.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alight.android.aoa_launcher.common.bean.BaseBean
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.NetUtils
import com.alight.android.aoa_launcher.utils.toJson
import okhttp3.RequestBody

/**
 * 卸载完成的接收广播
 */
class UninstallApkReceiver : BroadcastReceiver() {
    private val TAG = "UninstallApkReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals("android.intent.action.PACKAGE_REMOVED")) {
            val packageName = intent.data?.schemeSpecificPart
            Log.i("UninstallApkReceiver", "卸载完成: packageName = $packageName")
            NetUtils.intance.putInfo(Urls.DEVICE_INSTALL,
                RequestBody.create(
                    null,
                    mapOf(
                        "package_name" to packageName,
                        "dsn" to AccountUtil.getDSN()
                    ).toJson()
                ), BaseBean::class.java, object : NetUtils.NetCallback {
                    override fun onSuccess(any: Any) {
                        Log.i(TAG, "onSuccess: $any")
                    }

                    override fun onError(error: String) {
                        Log.i(TAG, "onError: $error")
                    }
                })

        }

    }
}