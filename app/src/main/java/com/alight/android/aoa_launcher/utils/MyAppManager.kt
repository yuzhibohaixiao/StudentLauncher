package com.alight.android.aoa_launcher.utils


import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import com.alight.android.aoa_launcher.common.listener.MyEventBus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.xutils.common.util.LogUtil


object MyAppManager {
    private val action = "com.alight.uninstallFinish"
    private var actionRemoved = Intent.ACTION_PACKAGE_REMOVED
    public val codeEventBus = MyEventBus()

    fun getAllPackages(ctx: Activity): MutableList<PackageInfo> {
        ctx.packageManager
        val packages = ctx.packageManager.getInstalledPackages(0)
        return packages
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun tryUninstall(ctx: Activity, packageName: String, requestCode: Int) =
        suspendCancellableCoroutine<Boolean> { co ->
            val intent = Intent(ctx, ctx::class.java);
            intent.action = action
            val sender = PendingIntent.getBroadcast(
                ctx,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val mPackageInstaller = ctx.packageManager.packageInstaller
            try {
            /*    codeEventBus.once("uninstall:finish:$packageName") {
                    LogUtil.d("uninstall success")
                    co.resume(true, null)
                }*/
                mPackageInstaller.uninstall(packageName, sender.intentSender)
                LogUtil.d("try uninstall $packageName")
            } catch (e: Exception) {
                LogUtil.e(e.toString())
//                co.resume(false, null)
            }

        }

    fun init(ctx: Activity) {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        filter.addDataScheme("package")
        ctx.registerReceiver(UninstallBroadcastReceiver(), filter)
        LogUtil.d("注册完成初始化")
    }

}

public class UninstallBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action
        LogUtil.i("收到广播")
        when (action) {
            Intent.ACTION_PACKAGE_REMOVED -> {
                val packageName = intent.dataString?.replace("package:", "")
                LogUtil.d("删除完成 $packageName")
                MyAppManager.codeEventBus.emit("uninstall:finish:$packageName")
            }

            Intent.ACTION_PACKAGE_ADDED -> {
                val packageName = intent.dataString
                LogUtil.d("安装完成 $packageName")
                MyAppManager.codeEventBus.emit("install:finish:$packageName")
            }
        }
    }
}