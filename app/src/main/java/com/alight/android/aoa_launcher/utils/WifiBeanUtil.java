package com.alight.android.aoa_launcher.utils;

import android.net.wifi.ScanResult;
import android.text.TextUtils;

import com.alight.android.aoa_launcher.common.bean.WifiBean;

import java.util.ArrayList;
import java.util.List;

public class WifiBeanUtil {
    /**
     * 去除同名WIFI
     *
     * @param oldSr 需要去除同名的列表
     * @return 返回不包含同命的列表
     */
    public static List<ScanResult> noSameName(List<ScanResult> oldSr) {
        List<ScanResult> newSr = new ArrayList<ScanResult>();
        for (ScanResult result : oldSr) {
            if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID))
                newSr.add(result);
        }
        return newSr;
    }

    /**
     * 判断一个扫描结果中，是否包含了某个名称的WIFI
     *
     * @param sr   扫描结果
     * @param name 要查询的名称
     * @return 返回true表示包含了该名称的WIFI，返回false表示不包含
     */
    public static boolean containName(List<ScanResult> sr, String name) {
        for (ScanResult result : sr) {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(name))
                return true;
        }
        return false;
    }

//    List<ScanResult> scanResults = noSameName(getWifiScanResult(this));

    //
    List<WifiBean> realWifiList = new ArrayList<>();

}
