package com.alight.android.aoa_launcher.common.event;

/**
 * 安装成功后的事件
 *
 * @author wangzhe
 */
public class UpdateEvent {

    public final String packageName;

    public static UpdateEvent getInstance(String packageName) {
        return new UpdateEvent(packageName);
    }

    private UpdateEvent(String packageName) {
        this.packageName = packageName;
    }
}