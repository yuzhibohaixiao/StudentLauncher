package com.alight.android.aoa_launcher.ui.adapter

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.NewAppTypeBean
import com.alight.android.aoa_launcher.utils.StringUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.item_single_quality.view.*


class QualityAdapter :
    BaseQuickAdapter<NewAppTypeBean, BaseViewHolder>(R.layout.item_single_quality) {

    override fun convert(holder: BaseViewHolder, item: NewAppTypeBean) {
        val ivQualityIcon = holder.getView<ImageView>(R.id.iv_quality_icon)
//        val imageView2 = holder.getView<ImageView>(R.id.iv_quality_launcher_item2)
//        val imageView3 = holder.getView<ImageView>(R.id.iv_quality_launcher_item3)
        if (item.iconUrl != null && item.iconUrl!!.isNotEmpty()) {
            /*    val requestOptions: RequestOptions = RequestOptions()
                    .placeholder(R.drawable.testphoto_1)
                    .error(R.drawable.testphoto_2)
                    .fallback(R.drawable.testphoto_3)
                    .override(100, 100)*/
            Glide.with(context).load(item.iconUrl).skipMemoryCache(true).diskCacheStrategy(
                DiskCacheStrategy.NONE
            )
                .error(ivQualityIcon.drawable)
                .placeholder(ivQualityIcon.drawable)
                .into(ivQualityIcon)
        } else if (item.params != null && !StringUtils.isEmpty(item.className)) {
            ivQualityIcon.setImageResource(item.appIcon!!)
        } else if (!StringUtils.isEmpty(item.appPackName)) {
            setRoundImage(getIcon(item.appPackName), ivQualityIcon)
        } else if (item.appIcon != null && item.appIcon != 0) {
            ivQualityIcon.setImageResource(item.appIcon!!)
        } else {
            ivQualityIcon.visibility =
                View.INVISIBLE
        }
        /*    if (item.params2 != null && !StringUtils.isEmpty(item.className2)) {
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
            }*/

        holder.setText(R.id.iv_quality_app_name, item.appName)
//        holder.setText(R.id.tv_quality_app_name_item2, item.appName2)
//        holder.setText(R.id.tv_quality_app_name_item3, item.appName3)
        holder.itemView.iv_quality_icon.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName, item.className, item.params)
            }
        }
        holder.itemView.iv_quality_app_name.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener?.onItemClick(item.appPackName, item.className, item.params)
            }
        }
        /*  holder.itemView.iv_quality_launcher_item2.setOnClickListener {
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
          }*/
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
            .error(R.drawable.quality_default_icon)
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