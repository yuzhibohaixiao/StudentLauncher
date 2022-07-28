package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppBean
import com.alight.android.aoa_launcher.utils.AppUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *  幼教版Launcher应用列表的适配器
 */
class ChildAppListAdapter :
    BaseQuickAdapter<AppBean, BaseViewHolder>(R.layout.item_child_app_list) {

    override fun convert(holder: BaseViewHolder, item: AppBean) {
        if (item.appPackName.isNotEmpty()) {
            val icon = AppUtils.getIcon(context, item.appPackName)
            if (icon != null) {
                holder.setImageDrawable(R.id.iv_icon_app_list, icon)
            }
        }

        holder.setText(R.id.tv_app_name, item.appName)
    }

}