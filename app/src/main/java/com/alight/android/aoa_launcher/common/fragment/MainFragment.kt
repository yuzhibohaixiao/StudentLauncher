package com.alight.android.aoa_launcher.common.fragment

import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.presenter.PresenterImpl

class MainFragment : BaseFragment(), View.OnClickListener {
    override fun initData() {
    }

    override fun initView() {
    }

    override fun getLayout(): Int {
        return R.layout.fragment_main
    }

    override fun setListener() {
//        getInflateView().iv_game_launcher.setOnClickListener(this)
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
        /*  when (v.id) {
              R.id.iv_game_launcher -> {
                  context?.let { getPresenter().showDialog(it, AppConstants.GAME_APP) }
              }
              R.id.iv_video_launcher -> {
                  context?.let { getPresenter().showDialog(it, AppConstants.VIDEO_APP) }
              }
              R.id.iv_work_launcher -> {
                  //切换到工作Fragment
                  (activity as LauncherActivity).switchPager(0)
              }
              R.id.iv_education_launcher -> {
                  context?.let { getPresenter().showDialog(it, AppConstants.EDUCATION_APP) }
              }
              R.id.iv_app_store -> {
                  getPresenter().showKAMarket()
              }
              R.id.iv_all_app_launcher -> {
                  context?.let { getPresenter().showDialog(it, AppConstants.ALL_APP) }
              }
              R.id.iv_setting -> {
                  getPresenter().showSystemSetting()
              }*/
    }

}