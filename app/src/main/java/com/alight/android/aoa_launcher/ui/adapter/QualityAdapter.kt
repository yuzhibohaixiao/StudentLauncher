package com.alight.android.aoa_launcher.ui.adapter

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.alight.android.aoa_launcher.common.bean.PlayTimeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.utils.StartAppUtils
import com.alight.android.aoa_launcher.utils.StringUtils
import com.alight.android.aoa_launcher.utils.TimeUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_quality_launcher.view.*
import java.util.*


class QualityAdapter :
    BaseQuickAdapter<AppTrebleDataBean, BaseViewHolder>(R.layout.item_quality_launcher) {

    override fun convert(holder: BaseViewHolder, item: AppTrebleDataBean) {
        val imageView1 = holder.getView<ImageView>(R.id.iv_quality_launcher_item1)
        val imageView2 = holder.getView<ImageView>(R.id.iv_quality_launcher_item2)
        val imageView3 = holder.getView<ImageView>(R.id.iv_quality_launcher_item3)
        if (item.params1 != null && !StringUtils.isEmpty(item.className1)) {
            imageView1.setImageResource(item.appIcon1)
        } else if (!StringUtils.isEmpty(item.appPackName1)) {
            setRoundImage(getIcon(item.appPackName1), imageView1)
        } else if (item.appIcon1 != 0) {
            imageView1.setImageResource(item.appIcon1)
        } else {
            imageView1.visibility =
                View.INVISIBLE
        }
        if (item.params2 != null && !StringUtils.isEmpty(item.className2)) {
            imageView2.setImageResource(item.appIcon2)
        } else if (!StringUtils.isEmpty(item.appPackName2)) {
            setRoundImage(getIcon(item.appPackName2), imageView2)
        } else if (item.appIcon2 != 0) {
            imageView2.setImageResource(item.appIcon2)
        } else {
            imageView2.visibility = View.INVISIBLE
        }
        if (item.params3 != null && !StringUtils.isEmpty(item.className3)) {
            imageView3.setImageResource(item.appIcon3)
        } else if (!StringUtils.isEmpty(item.appPackName3)) {
            setRoundImage(getIcon(item.appPackName3), imageView3)
        } else if (item.appIcon3 != 0) {
            imageView3.setImageResource(item.appIcon3)
        } else {
            imageView3.visibility = View.INVISIBLE
        }

        holder.setText(R.id.tv_quality_app_name_item1, item.appName1)
        holder.setText(R.id.tv_quality_app_name_item2, item.appName2)
        holder.setText(R.id.tv_quality_app_name_item3, item.appName3)
        holder.itemView.iv_quality_launcher_item1.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName1,item.className1,item.params1)
            }
        }
        holder.itemView.tv_quality_app_name_item1.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName1,item.className1,item.params1)
            }
        }
        holder.itemView.iv_quality_launcher_item2.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName2,item.className2,item.params2)
            }
        }
        holder.itemView.tv_quality_app_name_item2.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName2,item.className2,item.params2)
            }
        }
        holder.itemView.iv_quality_launcher_item3.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName3,item.className3,item.params3)
            }
        }
        holder.itemView.tv_quality_app_name_item3.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName3,item.className3,item.params3)
            }
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

    private fun setRoundImage(drawable: Drawable?, imageView: ImageView) {
        //Glide设置图片圆角角度
        val roundedCorners = RoundedCorners(10)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        // RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(20, 20);
        val options = RequestOptions.bitmapTransform(roundedCorners)
        Glide.with(context)
            .load(drawable) //.placeholder(R.drawable.ic_default_image)
            .error(R.drawable.launcher_quality_think)
            .apply(options)
            .into(imageView)

    }

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(packName: String, className: String?, params: Map<String, Any>?)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }


}