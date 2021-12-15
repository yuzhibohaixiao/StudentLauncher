package com.alight.android.aoa_launcher.common.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.common.bean.AppTrebleDataBean
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.QualityAdapter

class QualityFragment : BaseFragment(), View.OnClickListener {

    private var qualityAdapter: QualityAdapter? = null

    private val appList1: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )

    private val appList2: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )
    private val appList3: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )
    private val appList4: ArrayList<AppTrebleDataBean> = arrayListOf(
        AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "国家大剧院",
            R.drawable.launcher_quality_think,
            "",
            "X架子鼓",
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
        ), AppTrebleDataBean(
            R.drawable.launcher_quality_think,
            "",
            "大书法家",
            R.drawable.launcher_quality_think,
            "",
            "儿童启蒙画画",
            0,
            "",
            "",
        )
    )

    override fun initData() {

    }

    override fun initView() {
        val rvQuality = getInflateView().findViewById<RecyclerView>(R.id.rv_quality);
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvQuality.layoutManager = linearLayoutManager
        qualityAdapter = QualityAdapter()
        rvQuality.adapter = qualityAdapter
    }

    fun setType(qualityType: String) {
        when (qualityType) {
            AppConstants.LAUNCHER_TYPE_QUALITY1 -> {
                qualityAdapter?.setNewInstance(appList1)
            }
            AppConstants.LAUNCHER_TYPE_QUALITY2 -> {
                qualityAdapter?.setNewInstance(appList2)
            }
            AppConstants.LAUNCHER_TYPE_QUALITY3 -> {
                qualityAdapter?.setNewInstance(appList3)
            }
            AppConstants.LAUNCHER_TYPE_QUALITY4 -> {
                qualityAdapter?.setNewInstance(appList4)
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_quality
    }

    override fun setListener() {
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        when (v.id) {
        }

    }

}