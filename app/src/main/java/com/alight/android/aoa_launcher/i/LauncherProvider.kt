package com.alight.android.aoa_launcher.i

interface LauncherProvider {
    //获取用户信息
    fun getUserInfo(userInfo: Any)

    //获取当前的用户
    fun getCurrentUser(currentUser: Any)

    //选择用户
    fun selectUser(userId: String)

    //获取token
    fun getToken(token: String)

    //发送消息
    fun getMessage(message: String)
}