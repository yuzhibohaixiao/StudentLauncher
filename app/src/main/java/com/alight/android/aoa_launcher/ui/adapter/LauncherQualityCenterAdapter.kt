package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTypeBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LauncherQualityCenterAdapter :
    BaseQuickAdapter<AppTypeBean, BaseViewHolder>(R.layout.item_quality_art) {

    private val appList: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.launcher_quality_art, ""),
        AppTypeBean(R.drawable.launcher_quality_art, ""),
        AppTypeBean(R.drawable.launcher_quality_art, ""),
        AppTypeBean(R.drawable.launcher_quality_art, ""),
        AppTypeBean(R.drawable.launcher_quality_art, "")
    )

    init {
        setNewInstance(appList)
    }

    override fun convert(holder: BaseViewHolder, item: AppTypeBean) {
        holder.setImageResource(R.id.iv_quality_art_item, item.appIcon)
    }

}