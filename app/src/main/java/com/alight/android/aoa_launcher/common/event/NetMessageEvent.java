package com.alight.android.aoa_launcher.common.event;

/**
 * @author wangzhe
 */
public class NetMessageEvent {

    /**
     * netState表示网络状态 0为异常（无网络） 1为正常
     */
    public final int netState;
    public final String message;

    public static NetMessageEvent getInstance(int netState, String message) {
        return new NetMessageEvent(netState, message);
    }

    private NetMessageEvent(int netState, String message) {
        this.netState = netState;
        this.message = message;
    }
}