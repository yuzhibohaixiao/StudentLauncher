package com.alight.android.aoa_launcher.ui.adapter

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.alight.android.aoa_launcher.utils.StringUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.item_quality_launcher.view.*

class QualityAdapter :
    BaseQuickAdapter<AppTrebleDataBean, BaseViewHolder>(R.layout.item_quality_launcher) {

    override fun convert(holder: BaseViewHolder, item: AppTrebleDataBean) {
        if (!StringUtils.isEmpty(item.appPackName1)) {
            holder.setImageDrawable(R.id.iv_quality_launcher_item1, getIcon(item.appPackName1))
        } else if (item.appIcon1 != 0) {
            holder.setImageResource(R.id.iv_quality_launcher_item1, item.appIcon1)
        } else {
            holder.getView<ImageView>(R.id.iv_quality_launcher_item1).visibility =
                View.INVISIBLE
        }
        if (!StringUtils.isEmpty(item.appPackName2)) {
            holder.setImageDrawable(R.id.iv_quality_launcher_item2, getIcon(item.appPackName2))
        } else if (item.appIcon2 != 0) {
            holder.setImageResource(R.id.iv_quality_launcher_item2, item.appIcon2)
        } else {
            holder.getView<ImageView>(R.id.iv_quality_launcher_item2).visibility = View.INVISIBLE
        }
        if (!StringUtils.isEmpty(item.appPackName3)) {
            holder.setImageDrawable(R.id.iv_quality_launcher_item3, getIcon(item.appPackName3))
        } else if (item.appIcon3 != 0) {
            holder.setImageResource(R.id.iv_quality_launcher_item3, item.appIcon3)
        } else {
            holder.getView<ImageView>(R.id.iv_quality_launcher_item3).visibility = View.INVISIBLE
        }

        holder.setText(R.id.tv_quality_app_name_item1, item.appName1)
        holder.setText(R.id.tv_quality_app_name_item2, item.appName2)
        holder.setText(R.id.tv_quality_app_name_item3, item.appName3)
        holder.itemView.iv_quality_launcher_item1.setOnClickListener {
            startApp(item.appPackName1)
        }
        holder.itemView.tv_quality_app_name_item1.setOnClickListener {
            startApp(item.appPackName1)
        }
        holder.itemView.iv_quality_launcher_item2.setOnClickListener {
            startApp(item.appPackName2)
        }
        holder.itemView.tv_quality_app_name_item2.setOnClickListener {
            startApp(item.appPackName2)
        }
        holder.itemView.iv_quality_launcher_item3.setOnClickListener {
            startApp(item.appPackName3)
        }
        holder.itemView.tv_quality_app_name_item3.setOnClickListener {
            startApp(item.appPackName3)
        }
    }

    private fun startApp(appPackName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(appPackName)
            context.startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.showShort(context, "该应用缺失，请安装后重试")
            e.printStackTrace()
        }
    }

    private fun getIcon(packName: String): Drawable? {
        val pm: PackageManager = context.packageManager
        try {
            var appInfo = pm.getApplicationInfo(packName, PackageManager.GET_META_DATA)
            // 应用名称
            // pm.getApplicationLabel(appInfo)

            //应用图标
            return pm.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }


}