package com.alight.android.aoa_launcher.activity

import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl


/**
 * Launcher主页
 */
class NewLauncherActivity : BaseActivity(), View.OnClickListener {

    override fun setListener() {
    }


    override fun initData() {

    }

    override fun initView() {
    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_launcher
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
    }


}
