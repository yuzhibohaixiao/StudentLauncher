package com.alight.android.aoa_launcher.common.fragment

import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import kotlinx.android.synthetic.main.fragment_app_select.view.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class AppSelectFragment : BaseFragment(), View.OnClickListener {
    override fun initData() {
    }

    override fun initView() {
    }

    override fun getLayout(): Int {
        return R.layout.fragment_app_select
    }

    override fun setListener() {
        getInflateView().iv_sxqm_child_launcher.setOnClickListener(this)
        getInflateView().iv_kxts_child_launcher.setOnClickListener(this)
        getInflateView().iv_msqm_child_launcher.setOnClickListener(this)
        getInflateView().iv_hbyd_child_launcher.setOnClickListener(this)
        getInflateView().iv_yzyx_child_launcher.setOnClickListener(this)
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
            //数学启蒙
            R.id.iv_sxqm_child_launcher -> {
                getPresenter().startApp(requireContext(), "com.enuma.todomathcn")
            }
            //科学实验
            R.id.iv_kxts_child_launcher -> {
                getPresenter().startApp(requireContext(), "org.pbskids.playandlearnscience")
            }
            //蒙氏启蒙
            R.id.iv_msqm_child_launcher -> {
                getPresenter().startApp(requireContext(), "com.sagosago.World.googleplay")
            }
            //绘本阅读
            R.id.iv_hbyd_child_launcher -> {
                getPresenter().startApp(requireContext(), "com.jojoread.huiben")
            }
            //益智游戏
            R.id.iv_yzyx_child_launcher -> {
                getPresenter().startApp(requireContext(), "com.popoko.reversijp")
            }

        }
    }
}