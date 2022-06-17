package com.alight.android.aoa_launcher.common.constants

import com.alight.android.aoa_launcher.BuildConfig

class AppConstants {
    companion object {
        const val MEDIA_APP = "media_app"
        const val GAME_APP = "game_app"
        const val EDUCATION_APP = "education_app"
        const val OTHER_APP = "other_app"
        const val NEW_USER = "new_user"
        const val ALL_APP = "all_app"

        const val PLAY_TIME = "play_time"

        const val REGISTRATION_ID = "registration_id"

        const val RESULT_CODE_SELECT_USER_BACK = 100
        const val RESULT_CODE_LAUNCHER_START_SELECT_USER = 101

        //家庭id
        const val FAMILY_ID = "family_id"

        const val USER_ID = "user_id"

        //AOA星仔伴学旧包名
        const val OLD_AOA_PACKAGE_NAME = "com.alight.android.aoa"

        //AOA星仔伴学新包名
        const val AOA_PACKAGE_NAME = "com.alight.android.aoax"

        //硬件控制包名
        const val AHWCX_PACKAGE_NAME = "com.alight.ahwcx"

        const val ADB_WIFI_PACKAGE_NAME = "com.rair.adbwifi"

        const val AHWCX_SERVICE_NAME = "com.alight.ahwcx.startup.services.StartupService"

        //Launcher 包名
        const val LAUNCHER_PACKAGE_NAME = "com.alight.android.aoa_launcher"

        const val AV_PACKAGE_NAME = "com.tencent.trtcav"

        //安智市场包名
        const val KA_PACKAGE_NAME = "com.coolapk.market"

        //sqlite 用户信息存储 key
        const val AOA_LAUNCHER_USER_INFO_TOKEN = "aoa_launcher_user_info_token"
        const val AOA_LAUNCHER_USER_INFO_AVATAR = "aoa_launcher_user_info_avatar"
        const val AOA_LAUNCHER_USER_INFO_NAME = "aoa_launcher_user_info_name"
        const val AOA_LAUNCHER_USER_INFO_USER_ID = "aoa_launcher_user_info_user_id"
        const val AOA_LAUNCHER_USER_INFO_GENDER = "aoa_launcher_user_info_gender"
        const val AOA_LAUNCHER_USER_INFO_EXPIRE_TIME = "aoa_launcher_user_info_expire_time"
        const val AOA_LAUNCHER_USER_INFO_GRADE_TYPE = "aoa_launcher_user_info_grade_type"
        const val AOA_LAUNCHER_USER_INFO_LOCAL_GRADE_TYPE = "aoa_launcher_user_info_local_grade_type"

        const val EXTRA_IMAGE_PATH = "android.rockchip.update.extra.IMAGE_PATH"

        //系统固件升级包下载｜安装路径
        const val SYSTEM_ZIP_FULL_PATH = "/data/media/0/update.zip"
        const val SYSTEM_ZIP_PATH = "/data/media/0/"

        const val SYSTEM_MMKV_PATH = BuildConfig.MMKV_PATH

        const val APP_DOWNLOAD_PATH = "/storage/emulated/0/"

        //配置文件的解压路径
        const val CONFIG_PATH = "/data/media/0/ansystem"

        const val LAUNCHER_TYPE_AR = "launcher_type_ar"
        const val LAUNCHER_TYPE_CHINESE = "launcher_type_chinese"
        const val LAUNCHER_TYPE_MATHEMATICS = "launcher_type_mathematics"
        const val LAUNCHER_TYPE_ENGLISH = "launcher_type_english"
        const val LAUNCHER_TYPE_QUALITY = "launcher_type_quality"

        const val MMKV_MMAP_ID = "shared_aoa_launcher"

        const val MMKV_KEY = "ALIGHT_CRYPTO"


        const val SHUTDOWN = "shutdown"
    }
}