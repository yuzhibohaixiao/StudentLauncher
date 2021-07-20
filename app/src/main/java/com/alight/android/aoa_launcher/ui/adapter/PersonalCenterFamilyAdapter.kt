package com.alight.android.aoa_launcher.ui.adapter

import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.Data2
import com.alight.android.aoa_launcher.common.bean.Parent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.item_family.view.*

class PersonalCenterFamilyAdapter :
    BaseQuickAdapter<Parent, BaseViewHolder>(R.layout.item_family) {

    override fun convert(holder: BaseViewHolder, parent: Parent) {
        //家长头像
        Glide.with(context).load(parent.avatar)
            .error(if (parent.role_type == 1) R.drawable.father else R.drawable.mather)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(holder.itemView.iv_icon_family_item);

        //家长身份
        holder.setText(
            R.id.tv_name_family_item, when (parent.role_type) {
                0 -> "妈妈"
                1 -> "爸爸"
                2 -> "其他家长"
                else -> ""
            }
        )
    }

    /**
     * 刷新家长的在线状态
     */
    fun setOnlineState(data: Data2?) {
        if (data == null) return
        for (position in this.data.indices) {
            if (this.data[position].user_id == data.user_id) {
                var onlineState =
                    getViewByPosition(position, R.id.iv_online_state_family_item) as ImageView
                onlineState.setImageResource(
                    if (data.value) R.drawable.online_state_green else R.drawable.online_state_gray
                )
            }
        }
    }
}