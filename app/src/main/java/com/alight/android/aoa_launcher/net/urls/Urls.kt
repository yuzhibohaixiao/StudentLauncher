package com.alight.android.aoa_launcher.net.urls

import com.alight.android.aoa_launcher.BuildConfig

object Urls {

    //          android 9 以上需要使用https
//    const val BASEURL = "https://api.alight-sys.com"   //正式服务（生产）
    const val BASEURL = "https://${BuildConfig.API_HOST}"

//    const val BASEURL = "https://test.api.alight-sys.com" //测试服务
//    const val BASEURL = "https://appotronics.api.alight-sys.com" //光峰

//  const val BASEURL = "http://api.alight-sys.com"   //正式服务（生产）
//  const val BASEURL = "http://test.api.alight-sys.com" //测试服务

    const val HOT_MOVIE = "movie/v2/findHotMovieList"
    const val ZZ_MOVIE = "movie/v2/findReleaseMovieList"
    const val JJ_MOVIE = "movie/v2/findComingSoonMovieList"
    const val BANNER = "tool/v2/banner"
    const val BANNER2 = "tool/v2/banne"
    const val UPDATE = "/device/v1/app-version"
    const val PLAY_TIME = "/device/v1/student/app/playtime"

    const val SHUTDOWN = "/device/v1/student/device/shutdown"

    //家庭信息
    const val FAMILY_INFO = "/family/v1/student/families"

    //家长在线状态
    const val PARENT_ONLINE_STATE = "/state/v1/state/online"

    //设备解绑
    const val DEVICE_RELATION = "/family/v1/device_relation"

    //查看设备绑定家庭 （二维码是否绑定）
    const val DEVICE_BIND = "/family/v1/student/device/check"

    //   POST 用户绑定极光推送
    const val BIND_PUSH = "/asf/v1/student/jpush"

    //  GET 在线状态打点
    const val HEART_BEAT = "/state/v1/heartbeat"

    //学习计划
    const val STUDY_PLAN = "/study_plan/v1/student/plan/status/"

    //登录
    const val STUDENT_LOGIN = "device/v1/student/login"

    //静默卸载相关
    const val DEVICE_INSTALL = "device/v1/student/app/install"


}