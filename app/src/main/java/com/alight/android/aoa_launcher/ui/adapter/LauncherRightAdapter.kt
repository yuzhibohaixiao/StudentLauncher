package com.alight.android.aoa_launcher.ui.adapter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppRightDoubleDataBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.utils.StringUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LauncherRightAdapter :
    BaseQuickAdapter<AppRightDoubleDataBean, BaseViewHolder>(R.layout.item_right_launcher) {

    private val appList1: ArrayList<AppRightDoubleDataBean> = arrayListOf(
        AppRightDoubleDataBean(
            R.drawable.launcher_small_video,
            "com.jxw.special.video",
            "com.jxw.special.activity.SpecialCateListActivity",
            "精选素养视频",
            R.drawable.jxyd,
            "com.jxw.jxwbook",
            "com.jxw.jxwbook.MainActivity", "精选阅读"
        ), AppRightDoubleDataBean(
            R.drawable.zwsx,
            "com.jxw.yuwenxiezuo",
            "com.jxw.yuwenxiezuo.MainActivity", "作文赏析",
            R.drawable.jfyc,
            "com.jxw.jinfangyici", "com.jxw.jinfangyici.MainActivity",
            "近反义词"
        ), AppRightDoubleDataBean(
            R.drawable.zhbd,
            "",
            "", "中华宝典",
            0,
            "",
            "", ""
        )
    )
    private val appList2: ArrayList<AppRightDoubleDataBean> = arrayListOf(
        AppRightDoubleDataBean(
            R.drawable.launcher_small_video,
            "com.jxw.special.video", "com.jxw.special.activity.SpecialCateListActivity",
            "精选奥数视频",
            R.drawable.asxl,
            "", "",
            "奥数训练"
        ), AppRightDoubleDataBean(
            R.drawable.yytxl,
            "", "",
            "应用题训练",
            R.drawable.sdys,
            "", "",
            "数的运算"
        )
    )
    private val appList3: ArrayList<AppRightDoubleDataBean> = arrayListOf(
        AppRightDoubleDataBean(
            R.drawable.wjdnxyy,
            "com.jxw.special.video", "com.jxw.special.activity.SpecialCateListActivity",
            "外教带你学英语",
            R.drawable.kyjj,
            "", "",
            "口语交际"
        )
    )

    init {
        setNewInstance(appList1)
    }

    fun setShowType(launcherType: String) {
        when (launcherType) {
            AppConstants.LAUNCHER_TYPE_CHINESE -> {
                setNewInstance(appList1)
            }
            AppConstants.LAUNCHER_TYPE_MATHEMATICS -> {
                setNewInstance(appList2)
            }
            AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                setNewInstance(appList3)
            }
//            AppConstants.LAUNCHER_TYPE_QUALITY -> {
//                setNewInstance(appList4)
//            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: AppRightDoubleDataBean) {
        holder.setImageResource(R.id.iv_right_launcher_item1, item.appIcon1)
        val textView1 = holder.getView<TextView>(R.id.tv_right_app_name_item1)
        val textView2 = holder.getView<TextView>(R.id.tv_right_app_name_item2)
        //单独显示时的TextView
        val textView3 = holder.getView<TextView>(R.id.tv_right_app_name_item3)
        val llAppNameLayout1 = holder.getView<LinearLayout>(R.id.ll_right_app_name_item)
        val llAppNameLayout2 = holder.getView<LinearLayout>(R.id.ll_right_app_name_item3)
        val ivItem2 = holder.getView<ImageView>(R.id.iv_right_launcher_item2)
        if (StringUtils.isEmpty(item.appName2)) {
            llAppNameLayout2.visibility = View.VISIBLE
            llAppNameLayout1.visibility = View.GONE
            ivItem2.visibility = View.GONE
            textView3.text = item.appName1
        } else {
            textView1.text = item.appName1
            textView2.text = item.appName2
            holder.setImageResource(R.id.iv_right_launcher_item2, item.appIcon2)
            textView2.visibility = View.VISIBLE
            llAppNameLayout1.visibility = View.VISIBLE
            llAppNameLayout2.visibility = View.GONE
            ivItem2.visibility = View.VISIBLE
        }
        holder.getView<ImageView>(R.id.iv_right_launcher_item1).setOnClickListener {
            startActivity(item.appPackName1, item.appClassName1)
        }
        holder.getView<ImageView>(R.id.iv_right_launcher_item2).setOnClickListener {
            startActivity(item.appPackName2, item.appClassName2)
        }
    }

    private fun startActivity(packName: String, className: String) {
        try {
            val intent = Intent()
            val componentName =
                ComponentName(packName, className)
            intent.component = componentName
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            ToastUtils.showLong(context, "该应用正在开发中，敬请期待！")
            e.printStackTrace()
        }
    }


}