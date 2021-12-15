package com.alight.android.aoa_launcher.ui.adapter

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class QualityHorizontalAdapter :
    BaseQuickAdapter<AppTrebleDataBean, BaseViewHolder>(R.layout.item_quality_launcher) {

    /*init {
        val qualityFragment1 = QualityFragment()
        val qualityFragment2 = QualityFragment()
        val qualityFragment3 = QualityFragment()
        val qualityFragment4 = QualityFragment()

        qualityFragment1.setType(AppConstants.LAUNCHER_TYPE_QUALITY1)
        qualityFragment2.setType(AppConstants.LAUNCHER_TYPE_QUALITY2)
        qualityFragment3.setType(AppConstants.LAUNCHER_TYPE_QUALITY3)
        qualityFragment4.setType(AppConstants.LAUNCHER_TYPE_QUALITY4)
    }
*/
    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * 实现此方法，并使用 helper 完成 item 视图的操作
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: BaseViewHolder, item: AppTrebleDataBean) {

    }
}