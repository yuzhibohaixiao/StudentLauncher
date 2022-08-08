package com.alight.android.aoa_launcher.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.NewAppTypeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.utils.AppGetUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.ArrayList

class QualityHorizontalAdapter :
    BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_quality_horizontal) {

    private var typeTextList = arrayListOf(
        R.drawable.text_thinking_enlightenment,
        R.drawable.text_art,
        R.drawable.text_language_enlightenment,
        R.drawable.text_fun_learning
    )

    private val appList1: ArrayList<NewAppTypeBean> = arrayListOf(
        NewAppTypeBean(
            "天天练",
            R.drawable.wjdnxyy,
            "com.leleketang.SchoolFantasy",
            null,
            null
        ),
        NewAppTypeBean(
            "嘟嘟数学",
            R.drawable.quality_default_icon,
            "com.enuma.todomathcn",
            null,
            null,
        ),
        NewAppTypeBean(
            "宝宝巴士",
            R.drawable.quality_default_icon, "com.sinyee.babybus.recommendapp",
            null,
            null
        ),
        NewAppTypeBean(
            "科学实验",
            R.drawable.quality_default_icon, "org.pbskids.playandlearnscience",
            null,
            null
        ),
        NewAppTypeBean(
            "中国象棋",
            R.drawable.quality_default_icon, "com.cnvcs.xiangqi",
            null,
            null
        ),
        NewAppTypeBean(
            "黑白棋",
            R.drawable.quality_default_icon, "com.popoko.reversijp",
            null,
            null
        ),
    )

    private val appList2: ArrayList<NewAppTypeBean> = arrayListOf(
        NewAppTypeBean(
            "架子鼓",
            R.drawable.quality_default_icon, "com.gamestar.xdrum",
            null,
            null
        ),
        NewAppTypeBean(
            "完美钢琴",
            R.drawable.quality_default_icon, "com.gamestar.perfectpiano",
            null,
            null
        ),
        NewAppTypeBean(
            "宝宝乐器",
            R.drawable.quality_default_icon, "com.sinyee.babybus.songIV",
            null,
            null
        ),
        NewAppTypeBean(
            "儿童画画填色",
            R.drawable.quality_default_icon, "com.melestudio.paintd",
            null,
            null
        ),
    )
    private val appList3: ArrayList<NewAppTypeBean> = arrayListOf(
        NewAppTypeBean(
            "洪恩识字",
            R.drawable.quality_default_icon, "com.hongen.app.word",
            null,
            null
        ),
        NewAppTypeBean(
            "阿布睡前故事",
            R.drawable.quality_default_icon, "com.android.abustory",
            null,
            null
        ),
        NewAppTypeBean(
            "儿歌点点",
            R.drawable.quality_default_icon, "com.mampod.ergedd",
            null,
            null
        ),
        NewAppTypeBean(
            "kada故事",
            R.drawable.quality_default_icon, "com.hhdd.kada",
            null,
            null
        ),
        NewAppTypeBean(
            "喜马拉雅儿童",
            R.drawable.quality_default_icon,
            "com.ximalayaos.pad.tingkid",
            null,
            null,
        ),
        NewAppTypeBean(
            "熊猫博士识字",
            R.drawable.quality_default_icon, "com.drpanda.chineseacademy.b2b",
            null,
            null
        ),
    )
    private val appList4: ArrayList<NewAppTypeBean> = arrayListOf(

        NewAppTypeBean(
            "科魔大战",
            R.drawable.quality_default_icon, "com.lotfun.svmAndroid",
            null,
            null
        ),
        NewAppTypeBean(
            "作业帮",
            R.drawable.quality_default_icon, "com.baidu.homework",
            null,
            null
        ),
        NewAppTypeBean(
            "猿辅导",
            R.drawable.quality_default_icon, "com.yuantiku.tutor",
            null,
            null
        ),
        NewAppTypeBean(
            "星尘浏览器",
            R.drawable.science, "com.chaozhuo.browser",
            null,
            null
        ),
        NewAppTypeBean(
            "科学",
            R.drawable.science, "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf<String, Any>("StartArgs" to "d:/同步学习/科学|e:JWFD")
        ),
        NewAppTypeBean(
            "思想品德",
            R.drawable.moral, "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf<String, Any>("StartArgs" to "d:/同步学习/政治|e:JWFD")
        ),
    )

    private val tempList: ArrayList<NewAppTypeBean> = arrayListOf()

    /**
     * 过滤应用
     */
    private val filterList: ArrayList<NewAppTypeBean> = arrayListOf(
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            AppConstants.LAUNCHER_PACKAGE_NAME,
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            AppConstants.AOA_PACKAGE_NAME,
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            AppConstants.OLD_AOA_PACKAGE_NAME,
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "android.rk.RockVideoPlayer",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.camera2",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.contacts",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.calendar",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.deskclock",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.email",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.gallery3d",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.music",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.settings",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.soundrecorder",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.coolapk.market",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com,sohu.inputmethod.sogou",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com,jxw.launcher",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.calculator2",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.documentsui",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.quicksearchbox",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.android.rk",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "teaonly.rk.droidipcam",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.iflytek.speechcloud",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.jxw.huiben",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.google.android.inputmethod.latin",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.oirsdfg89.flg",
            null,
            null
        ),
        NewAppTypeBean(
            "",
            R.drawable.quality_default_icon,
            "com.jxw.pedu.clickread",
            null,
            null
        ),
        NewAppTypeBean(
            "alook-浏览器",
            R.drawable.science, "alook.browser",
            null,
            null
        ),
        NewAppTypeBean(
            "浏览器",
            R.drawable.science, "com.android.browser",
            null,
            null
        ),
    )


    /* private val appList1: ArrayList<AppTrebleDataBean> = arrayListOf(
         AppTrebleDataBean(
             R.drawable.moral, "com.jxw.online_study", "思想品德",
             "com.jxw.online_study.activity.BookCaseWrapperActivity",
             mapOf<String, Any>("StartArgs" to "d:/同步学习/政治|e:JWFD"),
             R.drawable.science, "com.jxw.online_study", "科学",
             "com.jxw.online_study.activity.BookCaseWrapperActivity",
             mapOf<String, Any>("StartArgs" to "d:/同步学习/科学|e:JWFD"),//StartArgs -> d:/同步学习/科学|e:JWFD
             R.drawable.launcher_quality_think,
             "com.zane.childdraw",
             "儿童启蒙画画",
             null, null
         ), AppTrebleDataBean(
             R.drawable.launcher_quality_think,
             "com.gamestar.xdrum",
             "X架子鼓", null, null,
             R.drawable.launcher_quality_think,
             "com.gamestar.perfectpiano",
             "完美钢琴", null, null,
             R.drawable.launcher_quality_think,
             "com.honghesoft.calligrapher",
             "大书法家", null, null
         )
     )

     private val appList2: ArrayList<AppTrebleDataBean> = arrayListOf(
         AppTrebleDataBean(
             R.drawable.launcher_quality_think,
             "com.hongen.app.word",
             "洪恩识字", null, null,
             R.drawable.launcher_quality_think,
             "com.qinlin.ahaschool",
             "ahakid", null, null,
             R.drawable.launcher_quality_think,
             "com.ubestkid.sightwords.a",
             "贝乐虎英语", null, null
         ), AppTrebleDataBean(
             R.drawable.launcher_quality_think,
             "com.leleketang.SchoolFantasy",
             "天天练", null, null,
             R.drawable.launcher_quality_think,
             "com.duwo.reading",
             "伴鱼绘本", null, null,
             0,
             "",
             "", null, null
         )
     )
     private val appList3: ArrayList<AppTrebleDataBean> = arrayListOf(
         AppTrebleDataBean(
             R.drawable.launcher_quality_think,
             "com.android.abustory",
             "阿布睡前故事", null, null,
             R.drawable.launcher_quality_think,
             "com.mampod.ergedd",
             "儿歌点点", null, null,
             R.drawable.launcher_quality_think,
             "com.lotfun.svmAndroid",
             "科魔大战", null, null
         )
     )
     private val appList4: ArrayList<AppTrebleDataBean> = arrayListOf(
         AppTrebleDataBean(
             R.drawable.launcher_quality_think,
             "com.yangcong345.android.phone",
             "洋葱学园", null, null,
             R.drawable.launcher_quality_think,
             "com.youdao.course",
             "有道精品课", null, null,
             R.drawable.launcher_quality_think,
             "com.yuantiku.tutor",
             "猿辅导", null, null
         ), AppTrebleDataBean(
             R.drawable.launcher_quality_think,
             "com.xueersi.parentsmeeting",
             "学而思网课", null, null,
             R.drawable.launcher_quality_think,
             "com.zjy.zjyeduandroid",
             "人教智能教辅HD", null, null,
             0,
             "",
             "", null, null
         )
     )*/

    init {
        tempList.addAll(appList4)
        setNewInstance(typeTextList)
        resetAppNotifyAdapter()
    }

    @DelicateCoroutinesApi
    @Synchronized
    fun resetAppNotifyAdapter() {
        GlobalScope.launch(Dispatchers.IO) {
            val appDatas = AppGetUtil.getAppData()
            //过滤掉不需要的应用（素质拓展和系统应用）
            val appFilter = appFilter(appDatas)
            appList4.clear()
            appList4.addAll(tempList)
            appList4.addAll(appFilter)
            GlobalScope.launch(Dispatchers.Main) {
                notifyItemChanged(itemCount - 1)
            }
        }
    }

    /**
     * 过滤不需要的应用（包括系统应用和部分第三方应用）
     */
    private fun appFilter(appDatas: ArrayList<NewAppTypeBean>): List<NewAppTypeBean> {
        var allQualityList = arrayListOf<NewAppTypeBean>()
        allQualityList.addAll(appList1)
        allQualityList.addAll(appList2)
        allQualityList.addAll(appList3)
        allQualityList.addAll(tempList)
        allQualityList.addAll(filterList)

        var removeList = arrayListOf<NewAppTypeBean>()
        for (appData in appDatas) {
            val appPackName = appData.appPackName
            for (qualityApp in allQualityList) {
                val qualityAppPackName = qualityApp.appPackName
                if (appPackName.contains(qualityAppPackName)) {
                    removeList.add(appData)
                    continue
                }
            }
        }
        appDatas.removeAll(removeList)
        return appDatas
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * 实现此方法，并使用 helper 完成 item 视图的操作
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: BaseViewHolder, item: Int) {
//        holder.itemView.setBackgroundResource(item.drawable)
        holder.setImageResource(R.id.iv_quality_app_name, typeTextList[holder.layoutPosition])
        val recyclerView = holder.getView<RecyclerView>(R.id.rv_quality_app_content)
        val gridLayoutManager = GridLayoutManager(context, 3)
        gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
        recyclerView.layoutManager = gridLayoutManager
        val qualityAdapter = QualityAdapter()
        qualityAdapter.setOnItemClickListener(object : QualityAdapter.OnItemClickListener {
            override fun onItemClick(
                packName: String,
                className: String?,
                params: Map<String, Any>?
            ) {
                if (onItemClickListener != null) {
                    onItemClickListener?.onItemClick(packName, className, params)
                }
            }

        })
        when (holder.layoutPosition) {
            0 -> {
                qualityAdapter.setNewInstance(appList1)
            }
            1 -> {
                qualityAdapter.setNewInstance(appList2)
            }
            2 -> {
                qualityAdapter.setNewInstance(appList3)
            }
            3 -> {
                qualityAdapter.setNewInstance(appList4)
            }
        }

        recyclerView.adapter = qualityAdapter
    }

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(packName: String, className: String?, params: Map<String, Any>?)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}