package com.alight.android.aoa_launcher.ui.adapter

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


/**
 *  年级选择的适配器
 */
class WifiListAdapter : BaseQuickAdapter<ScanResult, BaseViewHolder>(R.layout.item_wifi) {
    val connectivityManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


    override fun convert(holder: BaseViewHolder, item: ScanResult) {
        if (item.SSID.isNotEmpty()) {
            holder.setText(R.id.tv_wifi_name, item.SSID)
        } else {
            val wifiInfo: NetworkInfo? =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            var wifiName = wifiInfo?.extraInfo
            if (wifiName != null && wifiName.startsWith("\"")) {
                wifiName = wifiName.substring(1, wifiName.length);
            }
            if (wifiName != null && wifiName.endsWith("\"")) {
                wifiName = wifiName.substring(0, wifiName.length - 1);
            }
            holder.setText(R.id.tv_wifi_name, wifiName)
        }

    }

}