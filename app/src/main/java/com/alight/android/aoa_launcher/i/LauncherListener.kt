package com.alight.android.aoa_launcher.i

import com.alight.android.aoa_launcher.bean.TokenMessage

interface LauncherListener {


    //接收Server的消息
    fun onReceive(message: TokenMessage)

    // connect asf call
    fun onConnect()

    // disconnect
    fun onDisconnect()
}