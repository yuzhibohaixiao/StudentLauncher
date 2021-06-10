package com.alight.android.aoa_launcher

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.utils.InternetUtil
import com.alight.android.aoa_launcher.utils.SPUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_splash.*


/**
 * Launcher主页
 */
class SplashActivity : BaseActivity(), View.OnClickListener {

    private var wifiFlag = false
    override fun onResume() {
        super.onResume()
        if (wifiFlag) {
            //判断网络是否连接
            if (InternetUtil.isNetworkAvalible(this)) {
                fl_splash1.visibility = View.GONE
                fl_splash2.visibility = View.VISIBLE
            } else {
                ToastUtils.showLong(this, getString(R.string.splash_reconnection))
            }
        }
    }

    //初始化控件
    override fun initView() {
    }

    override fun setListener() {
        fl_splash1.setOnClickListener(this)
        fl_splash2.setOnClickListener(this)
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
            R.id.fl_splash1 -> {
                //开始判断网络状态
                wifiFlag = true
                if (InternetUtil.isNetworkAvalible(this)) {
                    fl_splash1.visibility = View.GONE
                    fl_splash2.visibility = View.VISIBLE
                } else {
                    ToastUtils.showLong(this, getString(R.string.splash_network_connections))
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
                }
            }
            R.id.fl_splash2 -> {
                //系统引导设置完毕，关闭引导页
                closeSplash()
            }
        }
//                closeSplash()
    }


    private fun closeSplash() {
        //新用户的状态设为false
        SPUtils.asyncPutData(AppConstants.NEW_USER, false)
        finish()
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
