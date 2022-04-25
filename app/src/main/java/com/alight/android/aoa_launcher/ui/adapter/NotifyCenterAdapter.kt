package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *  通知中心的适配器
 */
class NotifyCenterAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_notify_center) {

    override fun convert(holder: BaseViewHolder, item: String) {
//        holder.setText(R.id.tv_grade_dialog, item)
    }

}