package com.alight.android.aoa_launcher.net.urls

object Urls {

            const val BASEURL = "http://api.alight-sys.com"   //正式服务（生产）
//    const val BASEURL =
//        "http://test.api.alight-sys.com" //测试服务

    const val HOT_MOVIE = "movie/v2/findHotMovieList"
    const val ZZ_MOVIE = "movie/v2/findReleaseMovieList"
    const val JJ_MOVIE = "movie/v2/findComingSoonMovieList"
    const val BANNER = "tool/v2/banner"
    const val BANNER2 = "tool/v2/banne"
    const val UPDATE = "/device/v1/app-version"

    //家庭信息
    const val FAMILY_INFO = "/family/v1/student/families"

    //家长在线状态
    const val PARENT_ONLINE_STATE = "/state/v1/state/online"

    //设备解绑
    const val DEVICE_RELATION = "/family/v1/device_relation"

    //查看设备绑定家庭 （二维码是否绑定）
    const val DEVICE_BIND = "/family/v1/student/device/check"
}