package com.alight.android.aoa_launcher.common.event;

/**
 * 安装成功后的事件
 *
 * @author wangzhe
 */
public class CheckUpdateEvent {

    private static CheckUpdateEvent checkUpdateEvent = new CheckUpdateEvent();

    private CheckUpdateEvent() {

    }

    public static CheckUpdateEvent getInstance() {
        return checkUpdateEvent;
    }
}