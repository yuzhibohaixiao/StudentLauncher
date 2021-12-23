package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTypeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LauncherCenterAdapter :
    BaseQuickAdapter<AppTypeBean, BaseViewHolder>(R.layout.item_study_type) {

    private val appTypeList1: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.yxkw, "", ""),
        AppTypeBean(R.drawable.tx, "com.jxw.handwrite", "com.jxw.handwrite.ZymsActivity"),
        AppTypeBean(R.drawable.xpy, "com.jxw.learnchinesepinyin", "com.jxw.learnchinesepinyin.activity.MainActivity"),
        AppTypeBean(
            R.drawable.ktbsp,
            "com.jxw.mskt.video",
            "com.jxw.mskt.filelist.activity.FileListActivity"
        ),
        AppTypeBean(R.drawable.xbh, "com.jxw.bihuamingcheng", "com.example.viewpageindicator.MainActivity"),
        AppTypeBean(R.drawable.xbs, "com.jxw.bishunguize", "com.example.viewpageindicator.MainActivity"),
        AppTypeBean(
            R.drawable.xsz,
            "com.jxw.characterlearning",
            "com.jxw.characterlearning.MainActivity"
        ),
        AppTypeBean(R.drawable.bgs, "", ""),
        AppTypeBean(
            R.drawable.aicp,
            "com.jxw.examcenter.activity",
            "com.jxw.examcenter.activity.AppStartActivity"
        )
    )
    private val appTypeList2: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.yxjc, "", ""),
        AppTypeBean(R.drawable.rssz, "com.jxw.studydigital", "com.jxw.studydigital.StuDydigitalActivity"),
        AppTypeBean(R.drawable.szys, "com.jxw.jxwcalculator", "com.jxw.jxwcalculator.MainActivity"),
        AppTypeBean(
            R.drawable.ktbsp,
            "com.jxw.mskt.video",
            "com.jxw.mskt.filelist.activity.FileListActivity"
        ),
        AppTypeBean(R.drawable.sskj, "com.example.arithmeticformula", "com.example.arithmeticformula.MainActivity"),
        AppTypeBean(R.drawable.sxgs, "", ""),
        AppTypeBean(R.drawable.zzxl, "com.jxw.schultegrid", "com.jxw.schultegrid.SettingActivity"),
        AppTypeBean(R.drawable.qwsx, "", ""),
        AppTypeBean(
            R.drawable.aicp,
            "com.jxw.examcenter.activity",
            "com.jxw.examcenter.activity.AppStartActivity"
        ),
    )
    private val appTypeList3: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.yxkw, "", ""),
        AppTypeBean(R.drawable.kycp, "com.jxw.singsound", "com.jxw.singsound.ui.MainActivity"),
        AppTypeBean(R.drawable.tlxl, "", ""),
        AppTypeBean(
            R.drawable.ktbsp,
            "com.jxw.mskt.video",
            "com.jxw.mskt.filelist.activity.FileListActivity"
        ),
        AppTypeBean(R.drawable.yyzm, "", ""),
        AppTypeBean(R.drawable.gjyb, "", ""),
        AppTypeBean(R.drawable.wwjdc, "com.jxw.wuweijidanci", "com.jxw.wuweijidanci.MainActivity"),
        AppTypeBean(R.drawable.lccj, "", ""),
        AppTypeBean(R.drawable.yyyf, "", ""),
        AppTypeBean(
            R.drawable.aicp,
            "com.jxw.examcenter.activity",
            "com.jxw.examcenter.activity.AppStartActivity"
        )
    )

    fun setShowType(launcherType: String) {
        when (launcherType) {
            AppConstants.LAUNCHER_TYPE_CHINESE -> {
                setNewInstance(appTypeList1)
            }
            AppConstants.LAUNCHER_TYPE_MATHEMATICS -> {
                setNewInstance(appTypeList2)
            }
            AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                setNewInstance(appTypeList3)
            }
//            AppConstants.LAUNCHER_TYPE_QUALITY -> {
//                setNewInstance(appTypeList4)
//            }
        }
    }


    override fun convert(holder: BaseViewHolder, item: AppTypeBean) {
        holder.setImageResource(R.id.iv_study_type_item, item.appIcon)
    }

}