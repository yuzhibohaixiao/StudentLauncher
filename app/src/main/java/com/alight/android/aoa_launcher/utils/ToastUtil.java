package com.alight.android.aoa_launcher.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * desc ToastUtil
 * <p>
 * Created on 17/11/16.
 * author: zhangpanzhao
 */

public class ToastUtil {

    /**
     * 两次点击按钮之间的点击间隔不能少于500毫秒
     */
    private static final int MIN_SHOW_DELAY_TIME = 300;
    private static long lastShowTime;

    /**
     * 按钮在指定时间内是否被连续点击
     *
     * @return
     */
    public static boolean isFastShow() {
        boolean flag = false;
        long curShowkTime = System.currentTimeMillis();
        if ((curShowkTime - lastShowTime) < MIN_SHOW_DELAY_TIME) {
            flag = true;
        }
        lastShowTime = curShowkTime;
        return flag;
    }

    private static Toast toast;

    /**
     * 限制弹出次数
     *
     * @param context
     * @param content
     */
    public static void showToast(Context context, String content) {
        if (context == null) return;
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

}

