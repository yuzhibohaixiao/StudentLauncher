package com.alight.android.aoa_launcher.common.i

import com.alight.android.aoa_launcher.common.bean.TokenMessage

interface LauncherListener {


    //接收Server的消息
    fun onReceive(message: TokenMessage)

    // connect asf call
    fun onConnect()

    // disconnect
    fun onDisconnect()
}