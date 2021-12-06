package com.alight.android.aoa_launcher.activity

import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import kotlinx.android.synthetic.main.activity_launcher.*


/**
 * Launcher主页
 */
class NewLauncherActivity : BaseActivity(), View.OnClickListener {

    override fun setListener() {
    }


    override fun initData() {

    }

    override fun initView() {
        tv_ar_launcher.setOnClickListener(this)
        tv_chinese_launcher.setOnClickListener(this)
        tv_mathematics_launcher.setOnClickListener(this)
        tv_english_launcher.setOnClickListener(this)
        tv_quality_launcher.setOnClickListener(this)
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
        when (v.id) {
            R.id.tv_ar_launcher, R.id.tv_chinese_launcher, R.id.tv_mathematics_launcher, R.id.tv_english_launcher, R.id.tv_quality_launcher
            -> {
                showLeftSelectUi(v.id)
            }
        }
    }

    private fun showLeftSelectUi(id: Int) {
        iv_ar_launcher.visibility = if (id == R.id.tv_ar_launcher) View.VISIBLE else View.GONE
        iv_chinese_launcher.visibility = if (id == R.id.tv_chinese_launcher) View.VISIBLE else View.GONE
        iv_mathematics_launcher.visibility = if (id == R.id.tv_mathematics_launcher) View.VISIBLE else View.GONE
        iv_english_launcher.visibility = if (id == R.id.tv_english_launcher) View.VISIBLE else View.GONE
        iv_quality_launcher.visibility = if (id == R.id.tv_quality_launcher) View.VISIBLE else View.GONE
    }


}
