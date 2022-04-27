package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.CallArBean
import com.alight.android.aoa_launcher.utils.TimestampUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *  通知中心的适配器
 */
class NotifyCenterAdapter :
    BaseQuickAdapter<CallArBean, BaseViewHolder>(R.layout.item_notify_center) {

    init {
        //新版添加子控件点击事件
        addChildClickViewIds(R.id.tv_av_callback)
    }

    override fun convert(holder: BaseViewHolder, item: CallArBean) {
        holder.setText(R.id.tv_date_notify_item, TimestampUtils.transToString(item.message.time))
        holder.setText(R.id.tv_date_notify_content, item.body)
    }

}