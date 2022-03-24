package com.alight.android.aoa_launcher.common.broadcast

import android.content.Context
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver

class JPushReceiver : JPushMessageReceiver() {
    var TAG = "JPushReceiver"
    override fun onNotifyMessageArrived(p0: Context?, p1: NotificationMessage?) {
        super.onNotifyMessageArrived(p0, p1)
        Log.i(TAG, "onNotifyMessageArrived: ")
    }

    override fun onMessage(p0: Context?, p1: CustomMessage?) {
        super.onMessage(p0, p1)
        Log.i(TAG, "onMessage: ")
    }
}