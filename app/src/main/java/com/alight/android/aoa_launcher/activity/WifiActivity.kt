package com.alight.android.aoa_launcher.activity

import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl

class WifiActivity : BaseActivity() {
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
        return R.layout.activity_wifi
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }
}