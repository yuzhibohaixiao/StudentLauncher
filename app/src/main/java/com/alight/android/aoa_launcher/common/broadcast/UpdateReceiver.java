package com.alight.android.aoa_launcher.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alight.android.aoa_launcher.activity.LauncherActivity;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.event.UpdateEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 接收自定义消息用户安装的广播
 */
public class UpdateReceiver extends BroadcastReceiver {

    // context 上下文对象 intent 接收的意图对象
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra("packageName");
        Log.i("UpdateReceiver", "安装完成: packageName = " + packageName);
        EventBus.getDefault().post(UpdateEvent.getInstance(packageName));
    }
}