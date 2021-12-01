package com.alight.android.aoa_launcher.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alight.android.aoa_launcher.activity.LauncherActivity;
import com.alight.android.aoa_launcher.common.constants.AppConstants;

/**
 * 接收用户安装替换的广播
 */
public class UpgradeApkReceiver extends BroadcastReceiver {

    // context 上下文对象 intent 接收的意图对象
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            String packageName = intent.getComponent().getPackageName();
            Log.i("UpgradeApkReceiver", "安装完成: packageName = " + packageName);
            if (packageName.equals(AppConstants.AHWCX_PACKAGE_NAME)) {
//                Intent myIntent = new Intent();
//                ComponentName componentName =
//                        new ComponentName(AppConstants.AHWCX_PACKAGE_NAME, AppConstants.AHWCX_SERVICE_NAME);
//                myIntent.setComponent(componentName);
//                context.startService(myIntent);
                Context localContext = context.getApplicationContext();
                Intent mIntent = new Intent(AppConstants.AHWCX_SERVICE_NAME);
                mIntent.setPackage(AppConstants.AHWCX_PACKAGE_NAME);
                localContext.startService(mIntent);

            } else if (packageName.equals(AppConstants.LAUNCHER_PACKAGE_NAME)) {
//                Intent myIntent = new Intent(context, LauncherActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(myIntent);
            }
        }
    }
}