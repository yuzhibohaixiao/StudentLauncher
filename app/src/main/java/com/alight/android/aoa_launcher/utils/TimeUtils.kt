package com.alight.android.aoa_launcher.utils

import java.text.SimpleDateFormat

/**
 *  时间工具类
 */
object TimeUtils {

    /**
     * 判断时间是否在区间之内 返回true表示在区间内 (24小时内)
     */
    fun inTimeInterval(startTime: String, endTime: String, time: String): Boolean {
        val format = SimpleDateFormat("mm:ss")
        try {

            var startTime = format.parse(startTime).time
            var endTime = format.parse(endTime).time
            var time = format.parse(time).time
            return time > startTime && time < endTime

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    /**
     * 计算两个时间的差值（24小时以内） 返回时间差时和分的字符串
     */
    fun timeDifference(startTime: String, endTime: String): String {
        val dfs = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val begin = dfs.parse("2020-01-01 $startTime:00")

        val end = dfs.parse("2020-01-01 $endTime:00")

        val between = (end.time - begin.time) / 1000 //除以1000是为了转换成秒

        val day1 = between / (24 * 3600)

        val hour1 = between % (24 * 3600) / 3600

        val minute1 = between % 3600 / 60

        val second1 = between % 60 / 60

        println("" + day1 + "天" + hour1 + "小时" + minute1 + "分" + second1 + "秒")
        return "${hour1}h${minute1}min"
    }


}