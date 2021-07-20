package com.alight.android.aoa_launcher.utils

import java.util.*

/**
 * 日期工具类
 */
object DateUtil {
    //获得今天是周几
    fun getDayOfWeek(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY
            -> "周日"
            Calendar.MONDAY
            -> "周一"
            Calendar.TUESDAY
            -> "周二"
            Calendar.WEDNESDAY
            -> "周三"
            Calendar.THURSDAY
            -> "周四"
            Calendar.FRIDAY
            -> "周五"
            Calendar.SATURDAY
            -> "周六"
            else -> ""
        }
    }
}