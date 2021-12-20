package com.alight.android.aoa_launcher.ui.adapter

import android.view.View
import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class QualityAdapter :
    BaseQuickAdapter<AppTrebleDataBean, BaseViewHolder>(R.layout.item_quality_launcher) {

    override fun convert(holder: BaseViewHolder, item: AppTrebleDataBean) {
        if (item.appIcon1 != 0) {
            holder.setImageResource(R.id.iv_quality_launcher_item1, item.appIcon1)
        }else{
            holder.getView<ImageView>(R.id.iv_quality_launcher_item1).visibility = View.INVISIBLE
        }
        if (item.appIcon2 != 0) {
            holder.setImageResource(R.id.iv_quality_launcher_item2, item.appIcon2)
        }else{
            holder.getView<ImageView>(R.id.iv_quality_launcher_item2).visibility = View.INVISIBLE
        }
        if (item.appIcon3 != 0) {
            holder.setImageResource(R.id.iv_quality_launcher_item3, item.appIcon3)
        }else{
            holder.getView<ImageView>(R.id.iv_quality_launcher_item3).visibility = View.INVISIBLE
        }

        holder.setText(R.id.tv_quality_app_name_item1, item.appName1)
        holder.setText(R.id.tv_quality_app_name_item2, item.appName2)
        holder.setText(R.id.tv_quality_app_name_item3, item.appName3)
    }

}