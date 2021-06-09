package com.alight.android.aoa_launcher.i

interface LauncherListener {
    //把引用传递
    fun register(launcherProvider: LauncherProvider)

    //接收Server的消息
    fun onReceive(message: Any)
}