package com.alight.android.aoa_launcher

import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.utils.DateUtil
import com.qweather.sdk.bean.weather.WeatherNowBean
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


/**
 * Launcher主页
 */
class SplashActivity : BaseActivity(), View.OnClickListener {

    //初始化控件
    override fun initView() {
    }

    override fun setListener() {
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

        }
    }

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
