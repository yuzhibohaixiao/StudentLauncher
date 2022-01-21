package com.alight.android.aoa_launcher.common.event;

/**
 * @author wangzhe
 */
public class SplashEvent {

    /**
     * netState表示网络状态 0为异常（无网络） 1为正常
     */
    public final Boolean showSelectChild;

    public static SplashEvent getInstance(Boolean showSelectChild) {
        return new SplashEvent(showSelectChild);
    }

    private SplashEvent(Boolean showSelectChild) {
        this.showSelectChild = showSelectChild;
    }
}