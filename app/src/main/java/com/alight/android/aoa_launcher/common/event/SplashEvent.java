package com.alight.android.aoa_launcher.common.event;

/**
 * @author wangzhe
 */
public class SplashEvent {

    public final Boolean showSelectChild;

    public static SplashEvent getInstance(Boolean showSelectChild) {
        return new SplashEvent(showSelectChild);
    }

    private SplashEvent(Boolean showSelectChild) {
        this.showSelectChild = showSelectChild;
    }
}