package com.alight.android.aoa_launcher.common.bean;

public class WifiBean implements Comparable<WifiBean> {
    private String wifiName;
    private int level;
    private int state;  //1已连接  2正在连接  3未连接 三种状态
    private String capabilities;//加密方式

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public int compareTo(WifiBean o) {
        int level1 = this.getLevel();
        int level2 = o.getLevel();
        return level2 - level1;
    }
}