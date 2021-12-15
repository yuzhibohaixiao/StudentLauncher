package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class QualityAdapter :
    BaseQuickAdapter<AppTrebleDataBean, BaseViewHolder>(R.layout.item_quality_launcher) {

    override fun convert(holder: BaseViewHolder, item: AppTrebleDataBean) {
        holder.setImageResource(R.id.iv_quality_launcher_item1, item.appIcon1)
        holder.setImageResource(R.id.iv_quality_launcher_item2, item.appIcon2)
        holder.setImageResource(R.id.tv_quality_app_name_item3, item.appIcon3)

        holder.setText(R.id.tv_quality_app_name_item1, item.appName1)
        holder.setText(R.id.tv_quality_app_name_item2, item.appName2)
        holder.setText(R.id.tv_quality_app_name_item3, item.appName3)
    }

}