package com.alight.android.aoa_launcher.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alight.android.aoa_launcher.common.bean.CallArBean;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.utils.AccountUtil;
import com.alight.android.aoa_launcher.utils.SPUtils;
import com.google.gson.Gson;

import cn.jpush.android.api.JPushInterface;

/**
 * 接收用户登出的广播
 */
public class JPushDefaultReceiver extends BroadcastReceiver {
    private String TAG = "JPushDefaultReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive - " + intent.getAction());
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收 Registration Id : " + regId);
            SPUtils.syncPutData(AppConstants.REGISTRATION_ID, regId);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));         // 自定义消息不会展示在通知栏，完全要开发者写代码去处理     } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {         Log.d(TAG, "收到了通知");         // 在这里可以做些统计，或者做些其他工作     } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {         Log.d(TAG, "用户点击打开了通知");         // 在这里可以自己写代码去定义用户点击后的行为         Intent i = new Intent(context, TestActivity.class);  //自定义打开的界面         i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         context.startActivity(i);     } else {         Log.d(TAG, "Unhandled intent - " + intent.getAction());    } }
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            String json = bundle.getString(JPushInterface.EXTRA_EXTRA);
            try {
                CallArBean callArBean = new Gson().fromJson(json, CallArBean.class);
                Intent intent2 = new Intent("com.alight.trtcav.WindowActivity");
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("parentId", callArBean.getMessage().getFromUserInfo().toString());
                intent2.putExtra("parentName", callArBean.getMessage().getFromUserInfo().getName());
                intent2.putExtra("parentAvatar", callArBean.getMessage().getFromUserInfo().getAvatar());
                intent2.putExtra("roomId", callArBean.getMessage().getRoomId());
                intent2.putExtra("childId", AccountUtil.INSTANCE.getCurrentUser().getUserId() + "");
                intent2.putExtra("called", 2);
                intent2.putExtra("token", AccountUtil.INSTANCE.getCurrentUser().getToken());
                intent2.putExtra("callType", callArBean.getMessage().getType());
                intent2.putExtra("isCallAr", true);
                context.startActivity(intent2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}