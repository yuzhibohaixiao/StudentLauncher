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
        getInflateView().iv_hongen_sw.setOnClickListener(this)
        getInflateView().iv_hongen_abc.setOnClickListener(this)

        getInflateView().iv_hongen_fjyd.setOnClickListener(this)
        getInflateView().iv_hongen_cy.setOnClickListener(this)
        getInflateView().iv_hongen_sww.setOnClickListener(this)
        getInflateView().iv_hongen_bc.setOnClickListener(this)
        getInflateView().iv_hongen_syhb.setOnClickListener(this)
        getInflateView().iv_hongen_yd.setOnClickListener(this)
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
                getPresenter().startApp(requireContext(), "com.ihuman.pinyin")
            }
            R.id.iv_hongen_sz -> {
                getPresenter().startApp(requireContext(), "com.hongen.app.word")
            }
            R.id.iv_hongen_sw -> {
                getPresenter().startApp(requireContext(), "com.ihuman.imath")
            }
            R.id.iv_hongen_abc -> {
                getPresenter().startApp(requireContext(),  "com.ihuman.english")
            }


            R.id.iv_hongen_fjyd -> {
                getPresenter().startApp(requireContext(), "com.ihuman.oxford")
            }
            R.id.iv_hongen_cy -> {
                getPresenter().startApp(requireContext(), "com.ihuman.guoxue")
            }
            R.id.iv_hongen_sww -> {
                getPresenter().startApp(requireContext(), "com.ihuman.ibaike")
            }
            R.id.iv_hongen_bc -> {
                getPresenter().startApp(requireContext(), "com.ihuman.kaka")
            }
            R.id.iv_hongen_syhb -> {
                getPresenter().startApp(requireContext(), "com.ihuman.book")
            }
            R.id.iv_hongen_yd -> {
                getPresenter().startApp(requireContext(), "com.ihuman.yuedu")
            }
        }
    }
}