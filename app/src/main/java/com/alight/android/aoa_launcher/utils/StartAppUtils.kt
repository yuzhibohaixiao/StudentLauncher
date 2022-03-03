package com.alight.android.aoa_launcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 跳转任意类型应用的封装工具类
 */
object StartAppUtils {

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


    fun startNormalApp(
        context: Context,
        packName: String,
        className: String,
        params: Map<String, Any>?
    ) {
        try {
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