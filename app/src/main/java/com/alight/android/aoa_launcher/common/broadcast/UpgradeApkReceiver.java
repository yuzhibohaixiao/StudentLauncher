package com.alight.android.aoa_launcher.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alight.android.aoa_launcher.activity.LauncherActivity;
import com.alight.android.aoa_launcher.common.bean.BaseBean;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.event.UpdateEvent;
import com.alight.android.aoa_launcher.net.urls.Urls;
import com.alight.android.aoa_launcher.utils.AccountUtil;
import com.alight.android.aoa_launcher.utils.AppUtils;
import com.alight.android.aoa_launcher.utils.NetUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import okhttp3.RequestBody;

/**
 * 接收用户安装的广播
 */
public class UpgradeApkReceiver extends BroadcastReceiver {

    // context 上下文对象 intent 接收的意图对象
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        Log.i("UpgradeApkReceiver", "安装完成: packageName = " + packageName);
        EventBus.getDefault().post(UpdateEvent.getInstance(packageName));
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            Drawable drawable = applicationInfo.loadIcon(context.getPackageManager());
            String appName = applicationInfo.loadLabel(context.getPackageManager()).toString();
            postInstallApk(appName, packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            if (packageName.equals(AppConstants.AHWCX_PACKAGE_NAME)) {
                Intent myIntent = new Intent();
                ComponentName componentName =
                        new ComponentName(AppConstants.AHWCX_PACKAGE_NAME, AppConstants.AHWCX_SERVICE_NAME);
                myIntent.setComponent(componentName);
                context.startService(myIntent);
            } else if (packageName.equals(AppConstants.LAUNCHER_PACKAGE_NAME)) {
                try {
                    Intent myIntent = new Intent(context, LauncherActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(myIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void postInstallApk(String appName, String packageName) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("package_name", packageName);
        hashMap.put("app_name", appName);
        hashMap.put("logo", "logo");
        hashMap.put("dsn", AccountUtil.INSTANCE.getDSN());
        NetUtils.Companion.getIntance().postInfo(
                Urls.DEVICE_INSTALL,
                RequestBody.create(null, new Gson().toJson(hashMap)), BaseBean.class, new NetUtils.NetCallback() {
                    @Override
                    public void onSuccess(@NonNull Object any) {

                    }

                    @Override
                    public void onError(@NonNull String error) {

                    }
                });
    }
}