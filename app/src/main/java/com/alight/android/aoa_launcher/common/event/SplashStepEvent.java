package com.alight.android.aoa_launcher.common.event;

/**
 * @author wangzhe
 */
public class SplashStepEvent {

    public final int step;

    public static SplashStepEvent getInstance(int step) {
        return new SplashStepEvent(step);
    }

    private SplashStepEvent(int step) {
        this.step = step;
    }
}