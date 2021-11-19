package com.alight.android.aoa_launcher.ui.adapter

import android.widget.ProgressBar
import android.widget.TextView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.net.model.File
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class UpdateAdapter : BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_update) {

    init {
        //新版添加子控件点击事件
        addChildClickViewIds(R.id.tv_update_item)
    }

    override fun convert(holder: BaseViewHolder, file: File) {

        holder.setText(R.id.tv_app_name_update_item, file.fileName.split(".")[0])
        holder.setText(R.id.tv_app_size_update_item, file.sizeStr)
        var tvUpdate = holder.getView<TextView>(R.id.tv_update_item)
        var pbUpdate = holder.getView<ProgressBar>(R.id.pb_update_item)
        /* tvUpdate.setOnClickListener {
             pbUpdate.visibility = View.VISIBLE
             GlobalScope.launch(Dispatchers.IO) {
                 for (i in 0..100) {
                     delay(100)
                     GlobalScope.launch(Dispatchers.Main) {
                         if (i == 100) {
                             tvUpdate.text = "已完成"
                         } else {
                             tvUpdate.text = "$i%"
                         }
                         pbUpdate.progress = i
                     }
                 }
             }
 */
    }

}
