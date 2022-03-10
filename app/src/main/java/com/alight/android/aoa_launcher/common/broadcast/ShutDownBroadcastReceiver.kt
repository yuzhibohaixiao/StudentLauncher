package com.alight.android.aoa_launcher.common.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alight.android.aoa_launcher.common.bean.BaseBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.NetUtils
import com.alight.android.aoa_launcher.utils.toJson
import com.tencent.mmkv.MMKV
import okhttp3.RequestBody

/**
 * 用户自定义接收推送广播
 */
/*
class ShutDownBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "ShutDownBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("TAG", "即将关机")
        val mmkv = MMKV.defaultMMKV()
        mmkv.encode(AppConstants.SHUTDOWN, true)
//        Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
        NetUtils.intance.postInfo(
            Urls.SHUTDOWN, RequestBody.create(
                null,
                mapOf(
                    "dsn" to AccountUtil.getDSN()
                ).toJson()
            ), BaseBean::class.java, object : NetUtils.NetCallback {
                override fun onSuccess(any: Any) {
                    Log.i(TAG, "onSuccess: 关机接口调用")
                }

                override fun onError(error: String) {

                }
            }
        );
    }
}*/
