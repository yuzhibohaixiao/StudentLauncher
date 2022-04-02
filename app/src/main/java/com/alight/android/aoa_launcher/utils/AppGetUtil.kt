package com.alight.android.aoa_launcher.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.bean.NewAppTypeBean

/**
 * 获取全部应用
 */
object AppGetUtil {
    val TAG = "AppGetUtil"
    fun getAppData(): List<NewAppTypeBean> {
        val datas: MutableList<NewAppTypeBean> = ArrayList()
        var packageManager = LauncherApplication.getContext().packageManager
        var mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        var apps = packageManager.queryIntentActivities(mainIntent, 0)

        //获取应用 只有当类型和包名都相同时才去进行添加
        for (position in apps.indices) {
            val resolveInfo = apps[position]
            val packageName = resolveInfo.activityInfo.applicationInfo.packageName
            Log.i(
                "TAG",
                "getAppData: ${resolveInfo.loadLabel(packageManager)} packageName${packageName} "
            )
            datas.add(
                NewAppTypeBean(
                    resolveInfo.loadLabel(packageManager).toString(),
                    R.drawable.quality_default_icon,
                    packageName, null, null
                )
            )
        }
        return datas
    }
}