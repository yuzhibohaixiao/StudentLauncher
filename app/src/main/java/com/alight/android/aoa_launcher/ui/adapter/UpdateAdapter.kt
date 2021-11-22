package com.alight.android.aoa_launcher.ui.adapter

import android.graphics.Color
import android.view.View
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
        //下载安装包名称
        holder.setText(R.id.tv_app_name_update_item, file.fileName)
        //下载安装包大小
        holder.setText(R.id.tv_app_size_update_item, file.sizeStr)
        var tvUpdate = holder.getView<TextView>(R.id.tv_update_item)
        var pbUpdate = holder.getView<ProgressBar>(R.id.pb_update_item)

        when (file.status) {
            File.DOWNLOAD_PAUSE ->//暂停->开始
            {
                pbUpdate.visibility = View.VISIBLE
                pbUpdate.progress = file.progress
            }
            File.DOWNLOAD_PROCEED -> //下载进行中
            {
                pbUpdate.visibility = View.VISIBLE
                pbUpdate.progress = file.progress
                tvUpdate.setTextColor(Color.WHITE)
                tvUpdate.text = "${pbUpdate.progress}%";

            }
            File.DOWNLOAD_ERROR ->//出错
            {
                tvUpdate.text = "下载出错"
            }
            File.DOWNLOAD_COMPLETE ->//完成
            {
                tvUpdate.text = "已完成"
                pbUpdate.visibility = View.GONE
                tvUpdate.setTextColor(Color.parseColor("#50ffffff"))
            }
            File.DOWNLOAD_REDYA ->//准备下载 ->开始
            {
                pbUpdate.visibility = View.VISIBLE
            }
        }

    }
}