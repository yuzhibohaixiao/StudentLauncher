package com.alight.android.aoa_launcher.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.alight.android.aoa_launcher.common.bean.AppTreblePackDataBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.fragment.QualityFragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class QualityHorizontalAdapter :
    BaseQuickAdapter<AppTreblePackDataBean, BaseViewHolder>(R.layout.item_quality_horizontal) {
    private val appList1: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )

    private val appList2: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )
    private val appList3: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )
    private val appList4: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )

    init {
        val drawableList = arrayListOf(
            R.drawable.quality_art,
            R.drawable.quality_thinking,
            R.drawable.quality_happy_kids,
            R.drawable.quality_happy_study
        )
        var appTreblePackDataList =
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
        qualityAdapter.setNewInstance(item.appTrebleDataBean)
        recyclerView.adapter = qualityAdapter
    }
}