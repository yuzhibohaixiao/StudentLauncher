package com.alight.android.aoa_launcher.common.bean

data class StudyPlanBean(
    val code: Int,
    val data: StudyPlanBeanData,
    val msg: String,
    val request: String
)

data class StudyPlanBeanData(
    val plan_complete_total: Int,
    val plan_total: Int
)