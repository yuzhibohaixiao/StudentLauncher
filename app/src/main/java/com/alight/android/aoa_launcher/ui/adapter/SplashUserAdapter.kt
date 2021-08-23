package com.alight.android.aoa_launcher.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.TokenPair
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.System.load

class SplashUserAdapter : BaseQuickAdapter<TokenPair, BaseViewHolder>(R.layout.item_child_splash) {

    override fun convert(holder: BaseViewHolder, tokenPair: TokenPair) {
        //孩子头像
        val childIcon = holder.getView<ImageView>(R.id.iv_child_icon_splash)
        //孩子名称
        val childName = holder.getView<TextView>(R.id.tv_child_name_splash)
//        "gender": 0, // 0 -> 未知， 1 -> male 2-> female
        //未知和女性都使用女性头像，否则使用男头像

        Glide.with(context)
            .load(tokenPair.avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .error(if (tokenPair.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
            .into(childIcon)

        childName.text = tokenPair.name
    }

}