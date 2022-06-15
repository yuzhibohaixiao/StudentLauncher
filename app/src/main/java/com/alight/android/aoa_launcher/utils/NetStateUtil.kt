package com.alight.android.aoa_launcher.utils;

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities


open class NetStateUtil : ConnectivityManager.NetworkCallback() {

    //网络连接成功
    override fun onAvailable(network: Network) {
//        LogUtil.instance.d("网络连接成功")
        super.onAvailable(network)
    }

    //网络已断开连接
    override fun onLost(network: Network) {
//        LogUtil.instance.d("网络已断开连接")
        super.onLost(network)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
//        LogUtil.instance.d("网络正在断开连接")
        super.onLosing(network, maxMsToLive)
    }

    //无网络
    override fun onUnavailable() {
//        LogUtil.instance.d("网络连接超时或者网络连接不可达")
        super.onUnavailable()
    }

    //当网络状态修改（网络依然可用）时调用
    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
//        LogUtil.instance.d( "net status change! 网络连接改变")
    }

    //当访问的网络被阻塞或者解除阻塞时调用
    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
    }

    //当网络连接属性发生变化时调用
    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
    }
}