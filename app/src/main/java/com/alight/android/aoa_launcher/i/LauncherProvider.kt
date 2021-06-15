package com.alight.android.aoa_launcher.i

import com.alight.android.aoa_launcher.bean.TokenMessage
import com.alight.android.aoa_launcher.bean.TokenPair

interface LauncherProvider {
    //获取all用户信息
    fun getAllToken():List<TokenPair>

    //获取当前的用户
    fun getCurrentUser():TokenPair

    //选择用户
    fun selectUser(userId: Int)

    //获取token
    fun getToken():TokenPair

    //发送消息
    fun postMessage(message: TokenMessage)

    // get dsn
    fun getDSN():String
}