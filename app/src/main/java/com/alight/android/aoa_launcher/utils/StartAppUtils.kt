package com.alight.android.aoa_launcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.alight.android.aoa_launcher.common.bean.PlayTimeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * 跳转任意类型应用的封装工具类
 */
object StartAppUtils {

    /**
     * 开启AOA的模块
     */
    fun startAoaApp(context: Context, appId: Int, route: String) {
        try {
            var intent = Intent("com.alight.android.aoax.entry")
            intent.putExtra("action", "aos.app.open")
            intent.putExtra("appId", appId)
            intent.putExtra("route", route)
            intent.putExtra("params", "{}")
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 开启一个应用，支持传参
     */
    fun startActivity(
        context: Context,
        packName: String,
        className: String,
        params: Map<String, Any>?
    ) {
        try {
            val mmkv = MMKV.defaultMMKV()
            val playTimeJson = mmkv.decodeString(AppConstants.PLAY_TIME)
            val playTimeBean = Gson().fromJson(playTimeJson, PlayTimeBean::class.java)

            var calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault();//默认当前时区
            var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
            var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
            var sysTime = "$hour:" + if (minute >= 10) minute else "0$minute"
            var startTime = playTimeBean.data.playtime.start_playtime
            var endTime = playTimeBean.data.playtime.stop_playtime
//            var tempString = " {StartArgs:f:/ansystem/固化数据/小学古诗词.JXW}"
//            val split = tempString.split(":", "}")

            playTimeBean.data.app_manage.forEach {
                if (packName == it.app_info.package_name && className == it.class_name && (params == null || params.values.indexOf(
                        it.args
                    ) != -1)
                ) {
                    if ((it.app_permission == 3)) {
                        ToastUtils.showLong(context, "该应用已被禁用")
                        return@startActivity
                    } else if (it.app_permission == 2 && !TimeUtils.inTimeInterval(
                            startTime,
                            endTime,
                            sysTime
                        )
                    ) {
                        //限时禁用
                        ToastUtils.showLong(context, "该应用已被限时禁用")
                        return@startActivity
                    }
                    return@forEach
                }
            }

            val intent = Intent()
            val componentName =
                ComponentName(packName, className)
            params?.forEach {
                when (it.value) {
                    is String -> {
                        intent.putExtra(it.key, it.value.toString())
                    }
                    is Boolean -> {
                        intent.putExtra(it.key, it.value as? Boolean)
                    }
                    is Int -> {
                        intent.putExtra(it.key, it.value as? Int)
                    }
                }
            }
            intent.component = componentName
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            GlobalScope.launch(Dispatchers.Main) {
                ToastUtils.showLong(context, "该应用正在开发中，敬请期待！")
            }
            e.printStackTrace()
        }
    }

}