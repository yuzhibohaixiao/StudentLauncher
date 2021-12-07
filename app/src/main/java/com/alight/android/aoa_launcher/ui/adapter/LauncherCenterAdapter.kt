package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTypeBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LauncherCenterAdapter :
    BaseQuickAdapter<AppTypeBean, BaseViewHolder>(R.layout.item_study_type) {

    private val appTypeList1: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.launcher_yxkw, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_xpy, ""),
        AppTypeBean(R.drawable.launcher_ktbsp, ""),
        AppTypeBean(R.drawable.launcher_aicp, "")
    )
    private val appTypeList2: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.launcher_szys, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_aicp, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, "")
    )
    private val appTypeList3: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, "")
    )
    private val appTypeList4: ArrayList<AppTypeBean> = arrayListOf(
        AppTypeBean(R.drawable.launcher_yxkw, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, ""),
        AppTypeBean(R.drawable.launcher_tx, "")
    )

    fun setShowType(launcherType: String) {
        when (launcherType) {
            AppConstants.LAUNCHER_TYPE_CHINESE -> {
                setNewInstance(appTypeList1)
            }
            AppConstants.LAUNCHER_TYPE_MATHEMATICS -> {
                setNewInstance(appTypeList2)
            }
            AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                setNewInstance(appTypeList3)
            }
            AppConstants.LAUNCHER_TYPE_QUALITY -> {
                setNewInstance(appTypeList4)
            }
        }
    }


    override fun convert(holder: BaseViewHolder, item: AppTypeBean) {
        holder.setImageResource(R.id.iv_study_type_item, item.appIcon)
    }

}