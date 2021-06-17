package com.alight.android.aoa_launcher

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.adapter.SplashUserAdapter
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.bean.TokenManagerException
import com.alight.android.aoa_launcher.bean.TokenPair
import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.utils.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


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
                ll_splash2.visibility = View.VISIBLE
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
        ll_splash2.setOnClickListener(this)
        fl_splash3.setOnClickListener(this)
    }

    override fun initData() {
        getSystemDate()
        //获取用户信息之前必须调用的初始化方法
        AccountUtil.run()
    }

    fun startQRCode() {
        AccountUtil.getValidToken()

    }

    private fun showChildUser() {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val allUser = AccountUtil.getAllToken() as MutableList<TokenPair>
                GlobalScope.launch(Dispatchers.Main) {
                    if (allUser.isNullOrEmpty()) {
                        ll_no_child_splash.visibility = View.VISIBLE
                        rv_select_child_splash.visibility = View.GONE
                    } else {
                        rv_select_child_splash.layoutManager = LinearLayoutManager(
                            this@SplashActivity,
                            RecyclerView.HORIZONTAL, false
                        )
                        val splashUserAdapter = SplashUserAdapter()
                        //点击事件
                        splashUserAdapter.setOnItemClickListener { adapter, view, position ->
                            ToastUtils.showShort(this@SplashActivity, "你点到我了！$position")
                        }
                        rv_select_child_splash.adapter = splashUserAdapter
                        //第一次添加数据
                        splashUserAdapter.setNewInstance(allUser)
                    }
                }
            }
        } catch (tokenManagerException: TokenManagerException) {

        }
    }

    private fun getSystemDate() {
        GlobalScope.launch(Dispatchers.IO) {
            var calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault();//默认当前时区
            var year = calendar.get(Calendar.YEAR)// 获取当前年份
            var month = calendar.get(Calendar.MONTH) + 1// 获取当前月份
            var day = calendar.get(Calendar.DAY_OF_MONTH)// 获取当前月份的日期号码
            var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
            var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
            GlobalScope.launch(Dispatchers.Main) {
                tv_date_splash.text =
                    "${year}/${month}/${day} " + DateUtil.getDayOfWeek(calendar) + "$hour:" + if (minute >= 10) minute else "0$minute"
            }
            delay(10000)
            getSystemDate()
        }
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
                    ll_splash2.visibility = View.VISIBLE
                } else {
                    ToastUtils.showLong(this, getString(R.string.splash_network_connections))
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
                }
            }
            R.id.ll_splash2 -> {
                ll_splash2.visibility = View.GONE
                fl_splash3.visibility = View.VISIBLE
                showChildUser()
            }
            R.id.fl_splash3 -> {
                //系统引导设置完毕，关闭引导页
                closeSplash()
            }
        }
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
