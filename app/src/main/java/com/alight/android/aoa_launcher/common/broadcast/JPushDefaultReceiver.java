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

import com.alight.android.aoa_launcher.activity.PersonCenterActivity;
import com.alight.android.aoa_launcher.application.LauncherApplication;
import com.alight.android.aoa_launcher.common.bean.CallArBean;
import com.alight.android.aoa_launcher.common.bean.ParentControlBean;
import com.alight.android.aoa_launcher.common.bean.TokenPair;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.event.ParentControlEvent;
import com.alight.android.aoa_launcher.utils.SPUtils;
import com.alight.android.aoa_launcher.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

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
                //电话拨打过来
                if (callArBean.getIntent_url().equals("87://av")) {
                    JPushInterface.clearNotificationById(context, intent.getIntExtra(JPushInterface.EXTRA_NOTIFICATION_ID, 0));
//                    NotifyUtil.INSTANCE.setAvNotifyId(intent.getIntExtra(JPushInterface.EXTRA_NOTIFICATION_ID, 0));
                    //电话拨打超时
                } else if (callArBean.getIntent_url().equals("88://av")) {
                    MMKV mmkv = LauncherApplication.Companion.getMMKV();
                    String notifyInfo = mmkv.getString("notifyInfo", "");
                    if (StringUtils.isEmpty(notifyInfo)) {
                        List<CallArBean> callArBeanList = new ArrayList<>();
                        callArBeanList.add(callArBean);
                        String callArBeanListString = new Gson().toJson(callArBeanList);
                        mmkv.encode("notifyInfo", callArBeanListString);
                    } else {
                        String callArBeanListString = mmkv.getString("notifyInfo", "");
                        ArrayList<CallArBean> callArBeanList = new Gson().fromJson(callArBeanListString, new TypeToken<ArrayList<CallArBean>>() {
                        }.getType());
                        callArBeanList.add(callArBean);
                        mmkv.encode("notifyInfo", new Gson().toJson(callArBeanList));
                    }
//                    CallArBean callArBean2 = new Gson().fromJson(json, CallArBean.class);
//                    JPushInterface.clearNotificationById(context, NotifyUtil.INSTANCE.getAvNotifyId());
//                    CallArBean callArBean = new Gson().fromJson(json, CallArBean.class);
//                    TokenPair tokenPair = new Gson().fromJson(tokenPairCache, TokenPair.class);
//                    Intent intent1 = new Intent(context, PersonCenterActivity.class);
//                    intent1.putExtra("userInfo", tokenPair);
//                    intent1.putExtra("netState", 1);
//                    intent1.putExtra("notify", callArBean.getBody());
//                    intent1.putExtra("parentId", callArBean.getMessage().getFromUserId() + "");
//                    intent1.putExtra("parentName", callArBean.getMessage().getFromUserInfo().getName());
//                    intent1.putExtra("parentAvatar", callArBean.getMessage().getFromUserInfo().getAvatar());
//                    intent1.putExtra("roomId", callArBean.getMessage().getRoomId());
//                    intent1.putExtra("childId", callArBean.getMessage().getUserId() + "");
//                    intent1.putExtra("called", 1);
//                    intent1.putExtra("callType", callArBean.getMessage().getType());
                }
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
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "用户点击打开了通知");
            String tokenPairCache = (String) SPUtils.getData("tokenPair", "");
            if (!StringUtils.isEmpty(tokenPairCache)) {
                String json = bundle.getString(JPushInterface.EXTRA_EXTRA);
                try {
//                    CallArBean callArBean = new Gson().fromJson(json, CallArBean.class);
                    TokenPair tokenPair = new Gson().fromJson(tokenPairCache, TokenPair.class);
                    Intent intent1 = new Intent(context, PersonCenterActivity.class);
                    intent1.putExtra("userInfo", tokenPair);
                    intent1.putExtra("netState", 1);
//                    intent1.putExtra("notify", callArBean.getBody());
//                    intent1.putExtra("parentId", callArBean.getMessage().getFromUserId() + "");
//                    intent1.putExtra("parentName", callArBean.getMessage().getFromUserInfo().getName());
//                    intent1.putExtra("parentAvatar", callArBean.getMessage().getFromUserInfo().getAvatar());
//                    intent1.putExtra("roomId", callArBean.getMessage().getRoomId());
//                    intent1.putExtra("childId", callArBean.getMessage().getUserId() + "");
//                    intent1.putExtra("called", 1);
//                    intent1.putExtra("callType", callArBean.getMessage().getType());
                    context.startActivity(intent1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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