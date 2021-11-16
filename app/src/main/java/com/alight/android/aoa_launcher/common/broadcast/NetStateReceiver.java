package com.alight.android.aoa_launcher.common.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.alight.android.aoa_launcher.activity.LauncherActivity;
import com.alight.android.aoa_launcher.net.INetEvent;
import com.alight.android.aoa_launcher.net.NetTools;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class NetStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetStateReceiver";
    private INetEvent mINetEvent = LauncherActivity.mINetEvent;

    private static long WIFI_TIME = 0;
    private static long ETHERNET_TIME = 0;
    private static long NONE_TIME = 0;

    private static int LAST_TYPE = -3;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: " + intent.getAction());
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            long time = getTime();
            if (time != WIFI_TIME && time != ETHERNET_TIME && time != NONE_TIME) {
                final int netWorkState = NetTools.getNetWorkState(context);
                if (netWorkState == 0 && LAST_TYPE != 0) {
                    WIFI_TIME = time;
                    LAST_TYPE = netWorkState;
                    if (mINetEvent != null) {
                        mINetEvent.onNetChange(NetTools.getNetWorkState(context));
                    }
                } else if (netWorkState == 1 && LAST_TYPE != 1) {
                    ETHERNET_TIME = time;
                    LAST_TYPE = netWorkState;
                    if (mINetEvent != null) {
                        mINetEvent.onNetChange(NetTools.getNetWorkState(context));
                    }
                } else if (netWorkState == -1 && LAST_TYPE != -1) {
                    NONE_TIME = time;
                    LAST_TYPE = netWorkState;
                    if (mINetEvent != null) {
                        mINetEvent.onNetChange(NetTools.getNetWorkState(context));
                    }
                }
            }
        }
    }

    public long getTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String date = sDateFormat.format(new java.util.Date());
        return Long.valueOf(date);
    }

}