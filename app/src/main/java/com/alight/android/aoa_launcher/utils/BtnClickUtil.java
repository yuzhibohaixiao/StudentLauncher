package com.alight.android.aoa_launcher.utils;

public class BtnClickUtil {

    /**
     * 两次点击按钮之间的点击间隔不能少于 MIN_SHOW_DELAY_TIME 毫秒
     */
    private static final int MIN_SHOW_DELAY_TIME = 2000;
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
}
