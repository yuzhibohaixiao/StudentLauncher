package com.alight.android.aoa_launcher.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.TokenPair
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SplashUserAdapter : BaseQuickAdapter<TokenPair, BaseViewHolder>(R.layout.item_child_splash) {

    override fun convert(holder: BaseViewHolder, tokenPair: TokenPair) {
        //孩子头像
        val childIcon = holder.getView<ImageView>(R.id.iv_child_icon_splash)
        //孩子名称
        val childName = holder.getView<TextView>(R.id.tv_child_name_splash)
//        "gender": 0, // 0 -> 未知， 1 -> male 2-> female
        //未知和女性都使用女性头像，否则使用男头像
        childIcon.setImageResource(if (tokenPair.gender == 0 || tokenPair.gender == 1) R.drawable.splash_girl else R.drawable.splash_boy)
        childName.text = tokenPair.name
    }

}