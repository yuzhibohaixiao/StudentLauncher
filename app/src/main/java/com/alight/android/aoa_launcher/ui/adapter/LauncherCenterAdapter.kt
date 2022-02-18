package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTypeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LauncherCenterAdapter :
    BaseQuickAdapter<AppTypeBean, BaseViewHolder>(R.layout.item_study_type) {

    private val appTypeList1: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(
            R.drawable.yxkw,
            "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf("StartArgs" to "d:/同步学习/语文|e:JWFD")
        ),
        AppTypeBean(
            R.drawable.tx,
            "com.jxw.handwrite",
            "com.jxw.handwrite.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.xpy,
            "com.jxw.learnchinesepinyin",
            "com.jxw.learnchinesepinyin.activity.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.ktbsp,
            "com.jxw.mskt.video",
            "com.jxw.mskt.filelist.activity.FileListActivity",
            mapOf("StartArgs" to "d: 小学|e: 语文")
        ),
        AppTypeBean(
            R.drawable.xbh,
            "com.jxw.bihuamingcheng",
            "com.example.viewpageindicator.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.xbs,
            "com.jxw.bishunguize",
            "com.example.viewpageindicator.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.xsz,
            "com.jxw.characterlearning",
            "com.jxw.characterlearning.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.bgs, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学古诗词.JXW")
        ),
        AppTypeBean(
            R.drawable.aicp,
            "com.jxw.examcenter.activity",
            "com.jxw.examcenter.activity.AppStartActivity",
            null
        )
    )
    private val appTypeList2: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(
            R.drawable.yxjc, "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf("StartArgs" to "d:/同步学习/数学|e:JWFD")
        ),
        AppTypeBean(
            R.drawable.rssz,
            "com.jxw.studydigital",
            "com.jxw.studydigital.StuDydigitalActivity",
            null
        ),
        AppTypeBean(
            R.drawable.szys,
            "com.jxw.jxwcalculator",
            "com.jxw.jxwcalculator.LancherActivity",
            null
        ),
        AppTypeBean(
            R.drawable.ktbsp,
            "com.jxw.mskt.video",
            "com.jxw.mskt.filelist.activity.FileListActivity",
            mapOf("StartArgs" to "d: 小学|e: 数学")
        ),
        AppTypeBean(
            R.drawable.sskj,
            "com.example.arithmeticformula",
            "com.example.arithmeticformula.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.sxgs,
            "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学数学公式.JXW")
        ),
        AppTypeBean(
            R.drawable.zzxl,
            "com.jxw.schultegrid",
            "com.jxw.schultegrid.SettingActivity",
            null
        ),
        AppTypeBean(
            R.drawable.qwsx, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学趣味数学.JXW")
        ),
        AppTypeBean(
            R.drawable.aicp,
            "com.jxw.examcenter.activity",
            "com.jxw.examcenter.activity.AppStartActivity",
            null
        ),
    )
    private val appTypeList3: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(
            R.drawable.yxkw, "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf("StartArgs" to "d:/同步学习/英语|e:JWFD")
        ),
        AppTypeBean(
            R.drawable.kycp,
            "com.jxw.singsound",
            "com.jxw.singsound.ui.SplashActivity",
            null
        ),
        AppTypeBean(
            R.drawable.tlxl, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/中学听力训练.JXW")
        ),
        AppTypeBean(
            R.drawable.ktbsp,
            "com.jxw.mskt.video",
            "com.jxw.mskt.filelist.activity.FileListActivity",
            mapOf("StartArgs" to "d: 小学|e: 英语")
        ),
        AppTypeBean(
            R.drawable.yyzm,
            "com.jxw.letterstudynew",
            "com.jxw.letterstudynew.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.gjyb,
            "com.jxw.englishsoundmark",
            "com.jxw.englishsoundmark.Activity.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.wwjdc,
            "com.jxw.wuweijidanci",
            "com.jxw.wuweijidanci.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.lccj,
            "com.jxw.liancichengju",
            "com.jxw.liancichengju.MainActivity",
            null
        ),
        AppTypeBean(
            R.drawable.yyyf, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学英语语法.JXW")
        ),
        AppTypeBean(
            R.drawable.aicp,
            "com.jxw.examcenter.activity",
            "com.jxw.examcenter.activity.AppStartActivity",
            null
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