package com.alight.android.aoa_launcher.ui.adapter

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.bean.WifiBean
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


/**
 *  年级选择的适配器
 */
class WifiListAdapter : BaseQuickAdapter<WifiBean, BaseViewHolder>(R.layout.item_wifi) {
    val connectivityManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    init {
        addChildClickViewIds(R.id.tv_ignore_network)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun convert(holder: BaseViewHolder, item: WifiBean) {

        val ivTick = holder.getView<ImageView>(R.id.iv_wifi_tick)
        val ivWifiConnecting = holder.getView<ImageView>(R.id.iv_wifi_connecting)
        val line = holder.getView<View>(R.id.view_wifi_item)
        //可以忽略此网络
        val tvIgnoreNetwork = holder.getView<View>(R.id.tv_ignore_network)
        //wifi是否加密
        val ivLock = holder.getView<ImageView>(R.id.iv_wifi_lock_item)
        //wifi名称
        holder.itemView.visibility = View.VISIBLE
        holder.setText(R.id.tv_wifi_name, item.wifiName)
//        val currentWifiSsid = getWifiSsid()
        when (item.state) {
            //当前连接的wifi
            1 -> {
                ivTick.visibility = View.VISIBLE
                line.visibility = View.VISIBLE
                ivWifiConnecting.visibility = View.GONE
                tvIgnoreNetwork.visibility = View.VISIBLE
                ivLock.visibility = View.GONE
            }
            //正在连接的wifi
            2 -> {
                ivTick.visibility = View.GONE
                line.visibility = View.GONE
                Glide.with(context).asGif().load(R.drawable.wifi_connecting).into(ivWifiConnecting);
                ivWifiConnecting.visibility = View.VISIBLE
                tvIgnoreNetwork.visibility = View.GONE
                ivLock.visibility =
                    if (getWifiCipher(item.capabilities)) View.VISIBLE else View.GONE
            }
            //未连接
            3 -> {
                ivTick.visibility = View.GONE
                line.visibility = View.GONE
                ivWifiConnecting.visibility = View.GONE
                tvIgnoreNetwork.visibility = View.GONE
                ivLock.visibility =
                    if (getWifiCipher(item.capabilities)) View.VISIBLE else View.GONE
            }
        }
/*
        if (item.wifiName.isNotEmpty()) {
            holder.itemView.visibility = View.VISIBLE
            holder.setText(R.id.tv_wifi_name, item.wifiName)
            val currentWifiSsid = getWifiSsid()
            if (currentWifiSsid.isNotEmpty()) {
                //当前连接的wifi
                if (currentWifiSsid == item.wifiName) {
                    ivTick.visibility = View.VISIBLE
                    line.visibility = View.VISIBLE
                } else {
                    ivTick.visibility = View.INVISIBLE
                    line.visibility = View.GONE
                }
            } else {
                ivTick.visibility = View.INVISIBLE
                line.visibility = View.GONE
            }

        } else {
            holder.itemView.visibility = View.GONE
            */
/*    val wifiInfo: NetworkInfo? =
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                var wifiName = wifiInfo?.extraInfo?.get(holder.layoutPosition-1).toString()
                if (wifiName != null && wifiName.startsWith("\"")) {
                    wifiName = wifiName.substring(1, wifiName.length)
                }
                if (wifiName != null && wifiName.endsWith("\"")) {
                    wifiName = wifiName.substring(0, wifiName.length - 1)
                }
                holder.setText(R.id.tv_wifi_name, wifiName)*//*

        }
*/
        //wifi信号强度 又称RSSI
        val wifi = item.level
//        val wifi = WifiManager.calculateSignalLevel(level,4)
        if (wifi > -70 && wifi < 0) {//最强
            holder.setImageResource(R.id.iv_wifi_signal_item, R.drawable.wifi_connect_big)
        } else if (wifi > -80 && wifi <= -70) {//较强
            holder.setImageResource(R.id.iv_wifi_signal_item, R.drawable.wifi_connect_middle)
        } else {//较弱
            holder.setImageResource(R.id.iv_wifi_signal_item, R.drawable.wifi_connect_small)
        }

        /*    if (wifi > -50 && wifi < 0) {//最强

                Log.e(TAG, "最强");

            } else if (wifi > -70 && wifi < -50) {//较强

                Log.e(TAG, "较强");

            } else if (wifi > -80 && wifi < -70) {//较弱

                Log.e(TAG, "较弱");

            } else if (wifi > -100 && wifi < -80) {//微弱

                Log.e(TAG, "微弱");

            }

        } else {
    //无连接

            Log.e(TAG, "无wifi连接");

        }*/

    }

    /**
     * 获取当前连接的wifi名
     */
    private fun getWifiSsid(): String {

        var ssid = ""

        var networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo?.isConnected!!) {
            var wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            var connectionInfo = wifiManager.connectionInfo;

            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {

                ssid = connectionInfo.ssid;

            }

        }

        return ssid.replace("\"", "")

    }

    /**
     * 判断wifi热点是否加密
     */
    private fun getWifiCipher(capabilities: String): Boolean {
        return if (capabilities.contains("WEP")) {
            true
        } else capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains(
            "WPS"
        )
        /*if (capabilities.isEmpty()) {
            return WifiCipherType.WIFICIPHER_INVALID;
        } else if (capabilities.contains("WEP")) {
            return WifiCipherType.WIFICIPHER_WEP;
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains(
                "WPS"
            )
        ) {
            return WifiCipherType.WIFICIPHER_WPA;
        } else {
            return WifiCipherType.WIFICIPHER_NOPASS;
        }*/
    }
}