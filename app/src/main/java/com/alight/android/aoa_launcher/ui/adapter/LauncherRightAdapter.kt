package com.alight.android.aoa_launcher.ui.adapter

import android.content.ComponentName
import android.content.Intent
import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.bean.AppTypeBean
import com.alight.android.aoa_launcher.common.bean.PlayTimeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.utils.TimeUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.gson.Gson
import java.util.*

class LauncherRightAdapter :
    BaseQuickAdapter<AppTypeBean, BaseViewHolder>(R.layout.item_right_launcher) {
    private val englishAppList: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(
            R.drawable.wjdnxyy,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            mapOf("StartArgs" to "外教带你学英语"),
        ),
        AppTypeBean(
            R.drawable.kyjj, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学口语交际.JXW")
        ),
    )
    private val mathAppList: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(
            R.drawable.assp,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            mapOf("StartArgs" to "超越奥数"),
        ),
        AppTypeBean(
            R.drawable.asxl, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学奥数训练.JXW")
        ),
        AppTypeBean(
            R.drawable.yytxl,
            "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学应用题训练.JXW"),
        ),
        AppTypeBean(
            R.drawable.sdys, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学数的运算.JXW")
        ),
        AppTypeBean(
            R.drawable.qwsx, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学趣味数学.JXW")
        )
    )

    private val languageAppList: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(
            R.drawable.sysp,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            mapOf("StartArgs" to "语文阅读与写作", "tag" to "小学"),
        ),
        AppTypeBean(
            R.drawable.ctwh,
            "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学中华宝典.JXW"),
        ),
        AppTypeBean(
            R.drawable.zwsx,
            "com.jxw.yuwenxiezuo",
            "com.jxw.yuwenxiezuo.MainActivity", null,
        ),
        AppTypeBean(
            R.drawable.jxyd,
            "com.jxw.jxwbook",
            "com.jxw.jxwbook.MainActivity", null
        ),
        AppTypeBean(
            R.drawable.jfyc, "com.jxw.jinfangyici",
            "com.jxw.jinfangyici.MainActivity",
            null
        ),
    )


/*    private val appList1: ArrayList<AppRightDoubleDataBean> = arrayListOf(
        AppRightDoubleDataBean(
            R.drawable.launcher_small_video,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            mapOf("StartArgs" to "语文阅读与写作", "tag" to "小学"),
            "精选素养视频",
            R.drawable.jxyd,
            "com.jxw.jxwbook",
            "com.jxw.jxwbook.MainActivity", "精选阅读", null

        ), AppRightDoubleDataBean(
            R.drawable.zwsx,
            "com.jxw.yuwenxiezuo",
            "com.jxw.yuwenxiezuo.MainActivity", null,
            "作文赏析",
            R.drawable.jfyc, "com.jxw.jinfangyici",
            "com.jxw.jinfangyici.MainActivity",
            "近反义词",
            null
        ), AppRightDoubleDataBean(
            R.drawable.zhbd,
            "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学中华宝典.JXW"),
            "中华宝典",
            0,
            "",
            "",
            "",
            null
        )
    )
    private val appList2: ArrayList<AppRightDoubleDataBean> = arrayListOf(
        AppRightDoubleDataBean(
            R.drawable.launcher_small_video,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            mapOf("StartArgs" to "超越奥数"),
            "精选奥数视频",
            R.drawable.asxl, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            "奥数训练",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学奥数训练.JXW")
        ), AppRightDoubleDataBean(
            R.drawable.yytxl,
            "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学应用题训练.JXW"),
            "应用题训练",
            R.drawable.sdys, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            "数的运算",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学数的运算.JXW")
        )
    )
    private val appList3: ArrayList<AppRightDoubleDataBean> = arrayListOf(
        AppRightDoubleDataBean(
            R.drawable.wjdnxyy,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            mapOf("StartArgs" to "外教带你学英语"),
            "外教带你学英语",
            R.drawable.kyjj, "com.jxw.online_study",
            "com.jxw.online_study.activity.XBookStudyActivity",
            "口语交际",
            mapOf("StartArgs" to "f:/ansystem/固化数据/小学口语交际.JXW")
        )
    )*/

    init {
        setNewInstance(languageAppList)
    }

    fun setShowType(launcherType: String) {
        when (launcherType) {
            AppConstants.LAUNCHER_TYPE_CHINESE -> {
                setNewInstance(languageAppList)
            }
            AppConstants.LAUNCHER_TYPE_MATHEMATICS -> {
                setNewInstance(mathAppList)
            }
            AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                setNewInstance(englishAppList)
            }
//            AppConstants.LAUNCHER_TYPE_QUALITY -> {
//                setNewInstance(appList4)
//            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: AppTypeBean) {
//        val textView1 = holder.getView<TextView>(R.id.tv_right_app_name_item1)
//        holder.setImageResource(R.id.iv_right_launcher_item1, item.appIcon1)
//        val textView2 = holder.getView<TextView>(R.id.tv_right_app_name_item2)
        //单独显示时的TextView
//        val textView3 = holder.getView<TextView>(R.id.tv_right_app_name_item3)
//        val llAppNameLayout1 = holder.getView<LinearLayout>(R.id.ll_right_app_name_item)
//        val llAppNameLayout2 = holder.getView<LinearLayout>(R.id.ll_right_app_name_item3)
        val ivIcon = holder.getView<ImageView>(R.id.iv_right_launcher_item1)
//            textView2.text = item.appName2
//            holder.setImageResource(R.id.iv_right_launcher_item2, item.appIcon2)
//            textView2.visibility = View.VISIBLE
//        llAppNameLayout1.visibility = View.VISIBLE
//        llAppNameLayout2.visibility = View.GONE
//        ivItem2.visibility = View.VISIBLE
        ivIcon.setImageResource(item.appIcon)

        holder.getView<ImageView>(R.id.iv_right_launcher_item1).setOnClickListener {
            startActivity(item.appPackName, item.className, item.params)
        }
    }

    private fun startActivity(packName: String, className: String, params: Map<String, Any>?) {
        try {
            val mmkv = LauncherApplication.getMMKV()
            val playTimeJson = mmkv.decodeString(AppConstants.PLAY_TIME)
            val playTimeBean = Gson().fromJson(playTimeJson, PlayTimeBean::class.java)

            var calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault();//默认当前时区
            var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
            var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
            var sysTime = "$hour:" + if (minute >= 10) minute else "0$minute"
            var startTime = playTimeBean.data.playtime.start_playtime
            var endTime = playTimeBean.data.playtime.stop_playtime

            playTimeBean.data.app_manage.forEach {
                if (it.app_info != null && it.app_info.package_name.isNotEmpty() && packName == it.app_info.package_name && className == it.class_name && (params == null || params.isEmpty() || params.values.indexOf(
                        it.args
                    ) != -1)
                ) {
                    if ((it.app_permission == 3)) {
                        ToastUtils.showLong(context, "该应用已被禁用")
                        return@startActivity
                    } else if (it.app_permission == 2 && !TimeUtils.inTimeInterval(
                            startTime,
                            endTime,
                            sysTime
                        )
                    ) {
                        //限时禁用
                        ToastUtils.showLong(context, "该应用已被限时禁用")
                        return@startActivity
                    }
                    return@forEach
                }
            }

            if ((params == null || params.isEmpty()) && className.isEmpty()) {
                val intent = context.packageManager.getLaunchIntentForPackage(packName)
                context.startActivity(intent)
            } else {
                val intent = Intent()
                val componentName =
                    ComponentName(packName, className)
                params?.forEach {
                    when (it.value) {
                        is String -> {
                            intent.putExtra(it.key, it.value.toString())
                        }
                        is Boolean -> {
                            intent.putExtra(it.key, it.value as? Boolean)
                        }
                        is Int -> {
                            intent.putExtra(it.key, it.value as? Int)
                        }
                    }
                }
                intent.component = componentName
                context.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            ToastUtils.showLong(context, "该应用正在开发中，敬请期待！")
            e.printStackTrace()
        }
    }
}