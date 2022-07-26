package com.alight.android.aoa_launcher.common.fragment

import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.utils.ToastUtils
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainFragment : BaseFragment(), View.OnClickListener {
    override fun initData() {
    }

    override fun initView() {
    }

    override fun getLayout(): Int {
        return R.layout.fragment_main
    }

    override fun setListener() {
        getInflateView().iv_hongen_py.setOnClickListener(this)
        getInflateView().iv_hongen_sz.setOnClickListener(this)
//        iv_hongen_bc.setOnClickListener(this)
//        iv_hongen_bc.setOnClickListener(this)
//        iv_hongen_bc.setOnClickListener(this)
//        iv_hongen_bc.setOnClickListener(this)
//        iv_hongen_bc.setOnClickListener(this)
//        iv_hongen_bc.setOnClickListener(this)
//        getInflateView().iv_video_launcher.setOnClickListener(this)
//        getInflateView().iv_education_launcher.setOnClickListener(this)
//        getInflateView().iv_work_launcher.setOnClickListener(this)
//        getInflateView().iv_app_store.setOnClickListener(this)
//        getInflateView().iv_all_app_launcher.setOnClickListener(this)
//        getInflateView().iv_setting.setOnClickListener(this)
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
            R.id.iv_hongen_py -> {
                ToastUtils.showShort(context, "别点我1！")
            }
            R.id.iv_hongen_sz -> {
                ToastUtils.showShort(context, "别点我2！")
            }
        }
    }
}