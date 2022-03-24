package com.alight.android.aoa_launcher.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alight.android.aoa_launcher.application.LauncherApplication;
import com.alight.android.aoa_launcher.common.bean.CallArBean;
import com.alight.android.aoa_launcher.common.bean.ParentControlBean;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.event.ParentControlEvent;
import com.alight.android.aoa_launcher.utils.AccountUtil;
import com.alight.android.aoa_launcher.utils.SPUtils;
import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import cn.jpush.android.api.JPushInterface;

/**
 * 接收用户登出的广播
 */
public class JPushDefaultReceiver extends BroadcastReceiver {
    private String TAG = "JPushDefaultReceiver";
    private String USER_USE_AV = "com.alight.android.use_av"; // 自定义ACTION

    Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive - " + intent.getAction());
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收 Registration Id : " + regId);
            SPUtils.syncPutData(AppConstants.REGISTRATION_ID, regId);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_TITLE));         // 自定义消息不会展示在通知栏，完全要开发者写代码去处理     } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {         Log.d(TAG, "收到了通知");         // 在这里可以做些统计，或者做些其他工作     } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {         Log.d(TAG, "用户点击打开了通知");         // 在这里可以自己写代码去定义用户点击后的行为         Intent i = new Intent(context, TestActivity.class);  //自定义打开的界面         i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         context.startActivity(i);     } else {         Log.d(TAG, "Unhandled intent - " + intent.getAction());    } }
            Log.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));         // 自定义消息不会展示在通知栏，完全要开发者写代码去处理     } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {         Log.d(TAG, "收到了通知");         // 在这里可以做些统计，或者做些其他工作     } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {         Log.d(TAG, "用户点击打开了通知");         // 在这里可以自己写代码去定义用户点击后的行为         Intent i = new Intent(context, TestActivity.class);  //自定义打开的界面         i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         context.startActivity(i);     } else {         Log.d(TAG, "Unhandled intent - " + intent.getAction());    } }
            Log.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_EXTRA));         // 自定义消息不会展示在通知栏，完全要开发者写代码去处理     } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {         Log.d(TAG, "收到了通知");         // 在这里可以做些统计，或者做些其他工作     } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {         Log.d(TAG, "用户点击打开了通知");         // 在这里可以自己写代码去定义用户点击后的行为         Intent i = new Intent(context, TestActivity.class);  //自定义打开的界面         i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         context.startActivity(i);     } else {         Log.d(TAG, "Unhandled intent - " + intent.getAction());    } }
            Log.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MSG_ID));         // 自定义消息不会展示在通知栏，完全要开发者写代码去处理     } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {         Log.d(TAG, "收到了通知");         // 在这里可以做些统计，或者做些其他工作     } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {         Log.d(TAG, "用户点击打开了通知");         // 在这里可以自己写代码去定义用户点击后的行为         Intent i = new Intent(context, TestActivity.class);  //自定义打开的界面         i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         context.startActivity(i);     } else {         Log.d(TAG, "Unhandled intent - " + intent.getAction());    } }
            try {
                ParentControlBean parentControlBean = new Gson().fromJson(bundle.getString(JPushInterface.EXTRA_EXTRA), ParentControlBean.class);
                //收到App家长管控修改
                if (parentControlBean.getIntent_url().equals("90://parental_controls")) {
                    EventBus.getDefault().post(ParentControlEvent.getInstance(bundle.getString("childId"), bundle.getString("parentId"), bundle.getString("title")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "收到了音视频。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            String json = bundle.getString(JPushInterface.EXTRA_EXTRA);
            try {
                CallArBean callArBean = new Gson().fromJson(json, CallArBean.class);
                Intent intent2 = new Intent();
                intent2.setComponent(new ComponentName(
                        "com.tencent.trtcav",
                        "com.alight.trtcav.activity.WindowActivity"
                ));
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("parentId", callArBean.getMessage().getFromUserId() + "");
                intent2.putExtra("parentName", callArBean.getMessage().getFromUserInfo().getName());
                intent2.putExtra("parentAvatar", callArBean.getMessage().getFromUserInfo().getAvatar());
                intent2.putExtra("roomId", callArBean.getMessage().getRoomId());
                intent2.putExtra("childId", callArBean.getMessage().getUserId() + "");
                intent2.putExtra("called", 2);
                intent2.putExtra("token", LauncherApplication.Companion.getMMKV().decodeString(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN));
                intent2.putExtra("callType", callArBean.getMessage().getType());
                if (callArBean.getIntent_url().contains("ar")) {
                    intent2.putExtra("isCallAr", true);
                } else {
                    intent2.putExtra("isCallAr", false);
                }
                sendUserUseAv(context);
                handler.postDelayed(() -> {
                    Log.d(TAG, "唤起音视频");
                    context.startActivity(intent2);
                }, 100);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 发送用户使用音视频的广播
     *
     * @param context
     */
    private void sendUserUseAv(Context context) {
        Intent intent = new Intent();
        intent.setAction(USER_USE_AV);
        intent.putExtra("state", true);
        intent.putExtra("message", "用户使用音视频");// 设置广播的消息
        context.sendBroadcast(intent);
    }

}