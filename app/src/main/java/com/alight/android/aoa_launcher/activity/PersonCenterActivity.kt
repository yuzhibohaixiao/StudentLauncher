package com.alight.android.aoa_launcher.activity

import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import kotlinx.android.synthetic.main.activity_personal_center.*

class PersonCenterActivity : BaseActivity(), View.OnClickListener {

    override fun initView() {
    }

    override fun initData() {
    }

    override fun setListener() {
        ll_back_personal_center.setOnClickListener(this)
        ll_exit_personal_center.setOnClickListener(this)
    }

    override fun initPresenter(): PresenterImpl? {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_personal_center
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_back_personal_center ->
                finish()
            R.id.ll_exit_personal_center ->
                finish()
        }
    }
}