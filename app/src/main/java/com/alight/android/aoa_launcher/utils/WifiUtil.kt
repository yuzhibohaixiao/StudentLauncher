package com.alight.android.aoa_launcher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.TextUtils
import java.lang.reflect.Method


object WifiUtil {
    /**
     * 开始扫描wifi
     */
    fun startScanWifi(manager: WifiManager?) {
        manager?.startScan()
    }


    /**
     * 获取wifi列表
     */
    fun getWifiList(mWifiManager: WifiManager): List<ScanResult> {
        return mWifiManager.scanResults
    }


    /**
     * 保存网络
     */
    fun saveNetworkByConfig(manager: WifiManager?, config: WifiConfiguration?) {
        if (manager == null) {
            return
        }
        try {
            val save: Method? = manager.javaClass.getDeclaredMethod(
                "save",
                WifiConfiguration::class.java,
                Class.forName("android.net.wifi.WifiManager\$ActionListener")
            )
            if (save != null) {
                save.setAccessible(true)
                save.invoke(manager, config, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 断开连接
     */
    fun disconnectNetwork(manager: WifiManager?): Boolean {
        return manager != null && manager.disconnect()
    }


    /**
     * 获取当前wifi名字
     *
     * @return
     */
    fun getWiFiName(manager: WifiManager): String {
        val wifiInfo = manager.connectionInfo
        val name = wifiInfo.ssid
        return name.replace("\"", "")
    }

    /**
     * 获取当前WIFI信号强度
     *
     * @param manager
     * @return
     */
    fun getWiFiLevel(manager: WifiManager): Int {
        val wifiInfo = manager.connectionInfo
        return wifiInfo.rssi
    }

    /**
     * 获取wifi加密方式
     */
    fun getEncrypt(mWifiManager: WifiManager?, scanResult: ScanResult): String? {
        if (mWifiManager != null) {
            val capabilities: String = scanResult.capabilities
            if (!TextUtils.isEmpty(capabilities)) {
                return if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                    "WPA"
                } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                    "WEP"
                } else {
                    "没密码"
                }
            }
        }
        return "获取失败"
    }

    /**
     * 是否开启wifi，没有的话打开wifi
     */
    fun openWifi(mWifiManager: WifiManager) {
        if (!mWifiManager.isWifiEnabled) {
            mWifiManager.isWifiEnabled = true
        }
    }

    /**
     * 关闭wifi
     */
    fun closeWifi(mWifiManager: WifiManager) {
        mWifiManager.isWifiEnabled = false
    }

    /**
     * 有密码连接
     *
     * @param ssid
     * @param pws
     */
    fun connectWifiPws(mWifiManager: WifiManager, ssid: String, pws: String) {
        mWifiManager.disableNetwork(mWifiManager.connectionInfo.networkId)
        val netId = mWifiManager.addNetwork(getWifiConfig(mWifiManager, ssid, pws, true))
        mWifiManager.enableNetwork(netId, true)
    }

    /**
     * wifi设置
     *
     * @param ssid
     * @param pws
     * @param isHasPws
     */
    private fun getWifiConfig(
        mWifiManager: WifiManager,
        ssid: String,
        pws: String,
        isHasPws: Boolean
    ): WifiConfiguration? {
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + ssid + "\""
        val tempConfig = isExist(ssid, mWifiManager)
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId)
        }
        if (isHasPws) {
            config.preSharedKey = "\"" + pws + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        return config
    }

    /**
     * 得到配置好的网络连接
     *
     * @param ssid
     * @return
     */
    @SuppressLint("MissingPermission")
    private fun isExist(ssid: String, mWifiManager: WifiManager): WifiConfiguration? {
        val configs = mWifiManager.configuredNetworks
        for (config in configs) {
            if (config.SSID == "\"" + ssid + "\"") {
                return config
            }
        }
        return null
    }

    // 获取当前热点最新的信号强度
    fun getCurrentNetworkRssi(mContext: Context): Int {
        var wifiManager =
            mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo = wifiManager.connectionInfo
        return wifiInfo.rssi
    }

}