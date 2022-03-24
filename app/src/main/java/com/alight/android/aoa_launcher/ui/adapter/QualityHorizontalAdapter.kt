package com.alight.android.aoa_launcher.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.alight.android.aoa_launcher.common.bean.AppTreblePackDataBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class QualityHorizontalAdapter :
    BaseQuickAdapter<AppTreblePackDataBean, BaseViewHolder>(R.layout.item_quality_horizontal) {
    private val appList1: ArrayList<AppTrebleDataBean> = arrayListOf(
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
    )

    init {
        val appTreblePackDataList =
            arrayListOf(
                AppTreblePackDataBean(R.drawable.quality_art, appList1),
                AppTreblePackDataBean(R.drawable.quality_thinking, appList2),
                AppTreblePackDataBean(R.drawable.quality_happy_kids, appList3),
                AppTreblePackDataBean(R.drawable.quality_happy_study, appList4)
            )
        setNewInstance(appTreblePackDataList)
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * 实现此方法，并使用 helper 完成 item 视图的操作
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: BaseViewHolder, item: AppTreblePackDataBean) {
        holder.itemView.setBackgroundResource(item.drawable)
        val recyclerView = holder.getView<RecyclerView>(R.id.rv_quality_app_content)
        recyclerView.layoutManager = LinearLayoutManager(context)
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
        qualityAdapter.setNewInstance(item.appTrebleDataBean)
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