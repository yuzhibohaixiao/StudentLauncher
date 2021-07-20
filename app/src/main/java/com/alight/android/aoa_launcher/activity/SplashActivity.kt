package com.alight.android.aoa_launcher.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.ui.adapter.SplashUserAdapter
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.TokenManagerException
import com.alight.android.aoa_launcher.common.bean.TokenPair
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.networkbench.agent.impl.NBSAppAgent
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.*


/**
 * Launcher主页
 */
class SplashActivity : BaseActivity(), View.OnClickListener {

    private var wifiFlag = false
    private val userSplashBgList = arrayListOf(
        R.drawable.launcher_splash1,
        R.drawable.launcher_splash2,
        R.drawable.launcher_splash3,
        R.drawable.launcher_splash4
    )
    private var userSplashNumber = 0

    override fun onResume() {
        super.onResume()
//        showQRCode()
    }

    //初始化控件
    override fun initView() {

    }

    override fun setListener() {
        fl_splash1.setOnClickListener(this)
        ll_splash2.setOnClickListener(this)
        fl_splash3.setOnClickListener(this)
        tv_next_launcher_splash.setOnClickListener(this)
        tv_next_launcher_splash2.setOnClickListener(this)
        tv_skip_splash.setOnClickListener(this)
        ll_no_child_splash.setOnClickListener(this)
    }

    override fun initData() {
        //仅重选用户
        val onlyShowSelectChild = SPUtils.getData("onlyShowSelectChild", false) as Boolean
        //开启用户引导
        val openUserSplash = intent.getBooleanExtra("openUserSplash", false)
        //重新绑定
        val isRebinding = intent.getBooleanExtra("rebinding", false)
        when {
            isRebinding -> {
                showQRCode()
            }
            openUserSplash -> {   //直接跳转到用户引导
                fl_splash1.visibility = View.GONE
                openUserSplash()
            }
            onlyShowSelectChild -> {
                showChildUser()
                getSystemDate()
            }
            else -> {
                getSystemDate()
            }
        }
    }

    private fun showQRCode() {
        if (!wifiFlag) return
        //判断网络是否连接
        if (InternetUtil.isNetworkAvalible(this)) {
            fl_splash1.visibility = View.GONE
            ll_splash2.visibility = View.VISIBLE

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val qrCode = AccountUtil.getQrCode()
                    GlobalScope.launch(Dispatchers.Main) {
                        Glide.with(this@SplashActivity).load(qrCode).into(iv_qr_splash);
//                        val bitmap = BitmapFactory.decodeByteArray(qrCode, 0, qrCode.size)
//                        iv_qr_splash.setImageBitmap(bitmap)
//                        loadBitmapImage(iv_qr_splash, bitmap)
                    }
                } catch (e: SocketTimeoutException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } else {
            ToastUtils.showLong(this, getString(R.string.splash_reconnection))
        }
    }

    private fun showChildUser() {
        fl_splash1.visibility = View.GONE
        ll_splash2.visibility = View.GONE
        fl_splash3.visibility = View.VISIBLE
        try {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val allUser = AccountUtil.getAllToken() as MutableList<TokenPair>
                    GlobalScope.launch(Dispatchers.Main) {
                        if (allUser.isNullOrEmpty()) {
                            ll_no_child_splash.visibility = View.VISIBLE
                            rv_select_child_splash.visibility = View.GONE
                        } else {
                            ll_no_child_splash.visibility = View.GONE
                            rv_select_child_splash.visibility = View.VISIBLE
                            rv_select_child_splash.layoutManager = LinearLayoutManager(
                                this@SplashActivity,
                                RecyclerView.HORIZONTAL, false
                            )
                            val splashUserAdapter = SplashUserAdapter()
                            //点击事件
                            splashUserAdapter.setOnItemClickListener { adapter, view, position ->
                                try {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        val tokenPair = allUser[position]
                                        NBSAppAgent.setUserIdentifier(tokenPair.userId.toString())
                                        SPUtils.syncPutData(
                                            AppConstants.USER_ID,
                                            tokenPair.userId
                                        )
                                        SPUtils.syncPutData(
                                            AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN,
                                            tokenPair.token
                                        )
                                        AccountUtil.selectUser(tokenPair.userId)
                                        GlobalScope.launch(Dispatchers.Main) {
                                            //保存用户信息
                                            writeUserInfo(tokenPair)
                                            val onlyShowSelectChild =
                                                SPUtils.getData(
                                                    "onlyShowSelectChild",
                                                    false
                                                ) as Boolean
                                            if (onlyShowSelectChild) {
                                                //仅展示用户选择
                                                finish()
                                            } else {
                                                openUserSplash()
                                            }

                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            rv_select_child_splash.adapter = splashUserAdapter
                            //第一次添加数据
                            splashUserAdapter.setNewInstance(allUser)
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (tokenManagerException: TokenManagerException) {

        }
    }

    private fun openUserSplash() {
        if (userSplashNumber == userSplashBgList.size) {
            //新用户的状态设为false
            SPUtils.asyncPutData(AppConstants.NEW_USER, false)
            getPresenter().showAOA()
            finish()
            return
        }
        //显示launcher引导
        if (userSplashNumber == 0) {
            sc_next_launcher_splash.visibility = View.VISIBLE
            tv_date_splash.visibility = View.GONE
            tv_skip_splash.visibility = View.VISIBLE
            iv_splash_earth.visibility = View.GONE
            rv_select_child_splash.visibility = View.GONE
            sc_next_launcher_splash.visibility = View.VISIBLE
        } else {
            tv_next_launcher_splash.text = "下一步"
        }
        Glide.with(this)
            .load(userSplashBgList[userSplashNumber])
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    fl_splash.background = resource
                }
            })
        userSplashNumber++
        if (userSplashNumber == userSplashBgList.size) {
            tv_next_launcher_splash.text = "开始学习"
            sc_next_launcher_splash2.visibility = View.VISIBLE
        }
    }

    /**
     * 往Provider中写入数据
     */
    private fun writeUserInfo(tokenPair: TokenPair) {
        //插入数据前清除之前的数据
        contentResolver.delete(LauncherContentProvider.URI, null, null)
        val contentValues = ContentValues()
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME, tokenPair.expireTime)
        //将登陆的用户数据插入保存
        contentResolver.insert(LauncherContentProvider.URI, contentValues)
        val boyCursor = contentResolver.query(
            LauncherContentProvider.URI,
            arrayOf(
                "_id",
                AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN,
                AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR,
                AppConstants.AOA_LAUNCHER_USER_INFO_NAME,
                AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID,
                AppConstants.AOA_LAUNCHER_USER_INFO_GENDER,
                AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME
            ),
            null,
            null,
            null
        )
        if (boyCursor != null) {
            while (boyCursor.moveToNext()) {
                Log.e(
                    "childInfo",
                    "ID:" + boyCursor.getInt(boyCursor.getColumnIndex("_id")) + "  token:" +
                            boyCursor.getString(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN)) + "  token:" + boyCursor.getString(
                        boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR)
                    )
                            + "  name:" + boyCursor.getString(
                        boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_NAME)
                    )
                            + "  userId:" + boyCursor.getInt(
                        boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID)
                    )
                            + "  gender:" + boyCursor.getInt(
                        boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER)
                    )
                            + "  expireTime:" + boyCursor.getDouble(
                        boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME)
                    )
                )
            }
            boyCursor.close()
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
                    showQRCode()
                } else {
                    ToastUtils.showLong(this, getString(R.string.splash_network_connections))
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
                }
            }
            R.id.ll_splash2 -> {
                showChildUser()
            }
//            R.id.fl_splash3 -> {
            //系统引导设置完毕，关闭引导页
//                closeSplash()
//            }
            R.id.tv_next_launcher_splash -> {
                openUserSplash()
            }
            R.id.tv_next_launcher_splash2, R.id.tv_skip_splash -> {
                closeSplash()
            }
            R.id.ll_no_child_splash -> {
                showChildUser()
            }
        }
    }


    private fun closeSplash() {
        //新用户的状态设为false
        SPUtils.asyncPutData(AppConstants.NEW_USER, false)
        SPUtils.asyncPutData("onlyShowSelectChild", true)
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
