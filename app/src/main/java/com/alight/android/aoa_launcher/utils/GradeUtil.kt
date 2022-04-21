package com.alight.android.aoa_launcher.utils

import java.util.*

/**
 * 年级工具类，根据gradeType返回年级字符串
 */
object GradeUtil {
    fun getCurrentGrade(gradeType: Int): String? {
        val gradeMap = hashMapOf<Int, String>(
            1 to "早教",
            2 to "小班",
            3 to "中班",
            4 to "大班",
            5 to "学龄前",
            6 to "一年级",
            7 to "二年级",
            8 to "三年纪",
            9 to "四年级",
            10 to "五年级",
            11 to "六年级",
            12 to "初中",
            13 to "初中",
            14 to "初中",
            15 to "高中",
            16 to "高中",
            17 to "高中"
        )
        return gradeMap[gradeType]
    }
}