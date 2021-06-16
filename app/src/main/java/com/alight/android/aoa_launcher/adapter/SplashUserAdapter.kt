package com.alight.android.aoa_launcher.adapter

import android.widget.ImageView
import android.widget.TextView
import com.alight.android.aoa_launcher.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SplashUserAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_child_splash) {

    override fun convert(holder: BaseViewHolder, item: String) {
        //孩子头像
        val childIcon = holder.getView<ImageView>(R.id.iv_child_icon_splash)
        //孩子名称
        val childName = holder.getView<TextView>(R.id.tv_child_name_splash)
    }

}