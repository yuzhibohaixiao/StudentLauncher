package com.alight.android.aoa_launcher

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import kotlinx.android.synthetic.main.activity_splash.*


/**
 * Launcher主页
 */
class SplashActivity : BaseActivity(), View.OnClickListener {

    //初始化控件
    override fun initView() {
    }

    override fun setListener() {
        fl_splash.setOnClickListener(this)
    }

    override fun initData() {

    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_splash
    }

    override fun onSuccess(any: Any) {

    }


    override fun onError(error: String) {
        Log.e("error", error)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fl_splash ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
        }
//                closeSplash()
    }


/*private fun closeSplash() {
    SPUtils.asyncPutData(AppConstants.NEW_USER, false)
    finish()
}*/

    /**
     * 屏蔽系统返回按钮
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            //do something.
            true;//系统层不做处理 就可以了
        } else {
            super.dispatchKeyEvent(event);
        }
    }

}
