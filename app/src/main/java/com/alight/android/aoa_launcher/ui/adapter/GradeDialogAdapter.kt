package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *  年级选择的适配器
 */
class GradeDialogAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_grade_dialog) {

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tv_grade_dialog, item)
    }

}