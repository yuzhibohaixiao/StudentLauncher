package com.alight.android.aoa_launcher.utils

import java.text.SimpleDateFormat

/**
 *  时间工具类
 */
object TimeUtils {

    /**
     * 判断时间是否在区间之内 返回true表示在区间内
     */
    fun inTimeInterval(startTime: String, endTime: String, time: String): Boolean {
        val format = SimpleDateFormat("mm:ss")
        try {

            var startTime = format.parse(startTime).time
            var endTime = format.parse(endTime).time
            var time = format.parse(time).time
            return time > startTime && startTime < endTime

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}