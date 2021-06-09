package com.alight.android.aoa_launcher.i

interface LauncherProvider {
    //获取用户信息
    fun getUserInfo(userInfo: Any)

    //获取当前的用户
    fun getCurrentUser(currentUser: Any)

    //选择用户
    fun selectUser(userId: Any)

    //获取token
    fun getToken(token: Any)

    //发送消息
    fun getMessage(message: Any)
}