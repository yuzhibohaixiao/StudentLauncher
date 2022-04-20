package com.alight.android.aoa_launcher.common.broadcast;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver class which shows notifications when the Device Administrator status
 * of the application changes.
 */
public class ScreenOffAdminReceiver extends DeviceAdminReceiver {
    private void showToast(Context context, String msg) {
//        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
//        showToast(context,
//                "设备管理器使能");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
//        showToast(context,
//                "设备管理器没有使能");
    }

}