package com.alight.android.aoa_launcher.ui.adapter

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.net.model.File
import com.alight.android.aoa_launcher.utils.AppUtils
import com.alight.android.aoa_launcher.utils.StringUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class UpdateAdapter : BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_update) {

    private var appType = 1
    fun setAppType(appType: Int) {
        this.appType = appType
    }

    init {
        //新版添加子控件点击事件
        addChildClickViewIds(R.id.tv_update_item)
    }

    override fun convert(holder: BaseViewHolder, file: File) {
        val tvSize = holder.getView<TextView>(R.id.tv_app_size_update_item)
        val tvUpdate = holder.getView<TextView>(R.id.tv_update_item)
        var pbUpdate = holder.getView<ProgressBar>(R.id.pb_update_item)
        val tvVersionCode = holder.getView<TextView>(R.id.tv_app_code_update_item)
        if (appType == 1) {
            //下载安装包名称
            holder.setText(R.id.tv_app_name_update_item, file.fileName.split(".")[0])
            //下载安装包大小
            holder.setText(R.id.tv_app_size_update_item, file.sizeStr)
            if (file.iconState == 0) {
                if (!StringUtils.isEmpty(file.packName)) {
                    Glide.with(context)
                        .load(getIcon(file.packName))
                        .error(R.mipmap.ic_launcher)
                        .into(holder.getView(R.id.iv_app_icon_update_item))
                    val versionName = AppUtils.getVersionName(context, file.packName)
                    //版本号
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        if (versionName.isNullOrEmpty()) {
                            "未安装"
                        } else {
                            "版本：" + AppUtils.getVersionName(context, file.packName)
                        }
                    )
                } else {
                    //资源文件
                    Glide.with(context)
                        .load(R.mipmap.jxw_config_file)
                        .into(holder.getView(R.id.iv_app_icon_update_item))
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        "版本：" + file.versionCode + ".0.0"
                    )
                }
                //ota
                if (file.format == 3) {
                    tvSize.visibility = View.GONE
                    tvUpdate.text = "无需更新"
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        "版本：" + Build.DISPLAY
                    )
                    //版本已经为最新
                } else if (file.format == 4) {
                    tvSize.visibility = View.GONE
                    tvUpdate.text = "无需更新"
                } else {
                    tvUpdate.text = "更新"
                    tvSize.visibility = View.VISIBLE
                }
                //表示已经加载过图片
                data[holder.layoutPosition].iconState = 1
            }
            tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
            if (pbUpdate.progress == 100) {
                tvUpdate.text = "安装中"
                pbUpdate.visibility = View.GONE
                tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
            } else {
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
                        if (pbUpdate.progress != 100) {
//                            tvUpdate.setTextColor(Color.WHITE)
                        }
                        tvUpdate.text = "${pbUpdate.progress}%";
                    }
                    File.DOWNLOAD_ERROR ->//出错
                    {
                        tvUpdate.text = "下载出错"
                    }
                    File.DOWNLOAD_COMPLETE ->//完成
                    {
                        tvUpdate.text = "安装中"
                        pbUpdate.visibility = View.GONE
                        tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
                    }
                    File.DOWNLOAD_REDYA ->//准备下载 ->开始
                    {
//                pbUpdate.visibility = View.VISIBLE
//                pbUpdate.progress = file.progress
//                tvUpdate.setTextColor(Color.WHITE)
//                tvUpdate.text = "${pbUpdate.progress}%";
                    }
                }
            }
        } else {
            //下载安装包名称
            holder.setText(R.id.tv_app_name_update_item, file.fileName.split(".")[0])
            //下载安装包大小
            holder.setText(R.id.tv_app_size_update_item, file.sizeStr)
            if (file.iconState == 0) {
                if (!StringUtils.isEmpty(file.packName)) {
                    Glide.with(context)
                        .load(getIcon(file.packName))
                        .error(R.mipmap.ic_launcher)
                        .into(holder.getView(R.id.iv_app_icon_update_item))
                    val versionName = AppUtils.getVersionName(context, file.packName)
                    //版本号
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        if (versionName.isNullOrEmpty()) {
                            "未安装"
                        } else {
                            "版本：" + AppUtils.getVersionName(context, file.packName)
                        }
                    )
                } else {
                    Glide.with(context)
                        .load(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.getView(R.id.iv_app_icon_update_item))
                    val versionName = AppUtils.getVersionName(context, file.packName)
                    //版本号
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        if (versionName.isNullOrEmpty()) {
                            "未安装"
                        } else {
                            "版本：" + AppUtils.getVersionName(context, file.packName)
                        }
                    )
                }
                if (file.fileName == "system.zip") {
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        "版本：" + Build.DISPLAY
                    )
                }
                //ota
                if (file.format == 3) {
                    tvSize.visibility = View.GONE
                    tvUpdate.text = "无需更新"
                    tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
                    holder.setText(
                        R.id.tv_app_code_update_item,
                        "版本：" + Build.DISPLAY
                    )
                    //版本已经为最新
                } else if (file.format == 4) {
                    tvSize.visibility = View.GONE
                    tvUpdate.text = "无需更新"
                    tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
                } else {
                    tvUpdate.isEnabled = true
                    tvUpdate.text = "更新"
                    tvUpdate.setTextColor(context.getColor(R.color.person_center_text_green))
                    tvUpdate.setBackgroundResource(R.drawable.launcher_ota_update)
                    tvSize.visibility = View.VISIBLE
                }
                //表示已经加载过图片
                data[holder.layoutPosition].iconState = 1
            }
            if (pbUpdate.progress == 100) {
                tvUpdate.text = "安装中"
                pbUpdate.visibility = View.GONE
                tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
            } else {
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
                        if (pbUpdate.progress != 100) {
//                            tvUpdate.setTextColor(Color.WHITE)
                        }
                        tvUpdate.setBackgroundResource(R.drawable.update_oval_trans20)
                        tvUpdate.text = "${pbUpdate.progress}%";

                    }
                    File.DOWNLOAD_ERROR ->//出错
                    {
                        tvUpdate.text = "下载出错"
                    }
                    File.DOWNLOAD_COMPLETE ->//完成
                    {
                        tvUpdate.text = "安装中"
                        tvUpdate.isEnabled = false
                        tvUpdate.setTextColor(context.getColor(R.color.person_center_text_alpha_green))
                        pbUpdate.visibility = View.GONE
                    }
                    File.DOWNLOAD_REDYA ->//准备下载 ->开始
                    {
//                pbUpdate.visibility = View.VISIBLE
//                pbUpdate.progress = file.progress
//                tvUpdate.setTextColor(Color.WHITE)
//                tvUpdate.text = "${pbUpdate.progress}%";
                    }
                }

            }
            if (file.isInstalled)
                refreshInstallState(file, tvUpdate)
        }
    }

    private fun refreshInstallState(file: File, tvUpdate: TextView) {
        if (file.isInstalled && AppUtils.getVersionCode(
                context,
                file.packName
            ) >= file.versionCode
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                tvUpdate.text = "已完成"
                //安装完成后删除安装包
                CoroutineScope(Dispatchers.IO).launch {
                    val isUninstall = deleteSingleFile(file)
                    Log.i("UpdateAdapter", "删除安装包${file.path}:" + if (isUninstall) "成功" else "失败")
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000 * 1)
                refreshInstallState(file, tvUpdate)
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    private fun deleteSingleFile(file: File): Boolean {
        val file = java.io.File(file.path)
        return if (file.isFile && file.exists()) {
            file.delete()
        } else false
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun getIcon(packName: String): Drawable? {
        if (packName.isEmpty()) return null
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