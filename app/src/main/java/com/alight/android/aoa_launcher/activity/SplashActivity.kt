package com.alight.android.aoa_launcher.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.event.CheckUpdateEvent
import com.alight.android.aoa_launcher.common.event.SplashEvent
import com.alight.android.aoa_launcher.common.event.SplashStepEvent
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.SplashUserAdapter
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.SocketTimeoutException
import java.util.*


/**
 * 开机引导页
 */
class SplashActivity : BaseActivity(), View.OnClickListener {

    private lateinit var splashViews: List<View>;
    private var wifiFlag = false

    /*
        private val userSplashBgList = arrayListOf(
            R.drawable.launcher_splash1,
            R.drawable.launcher_splash2,
            R.drawable.launcher_splash3,
            R.drawable.launcher_splash4,
            R.drawable.launcher_splash5,
            R.drawable.launcher_splash6,
        )
    */
    private val splashVideoList = arrayListOf(
        R.raw.splash1,
        R.raw.splash2,
        R.raw.splash3,
        R.raw.splash4
    )
    private var isForceUpdate = false
    private var isRebinding = false
    private var userSplashNumber = 0
    private val USER_LOGIN_ACTION = "com.alight.android.user_login" // 自定义ACTION
    private var onlySplash = false
    private var closeSplash = false
    private var cdk: String? = null;
    private var autoFetchCDKTimerTask: TimerTask? = null;
    private var _refreshTimerTask: TimerTask? = null;
    private var refreshTimerTask: TimerTask? = null;
    private var fetchCDKRemain: Int = 0;
    private var mUserId = -1

//    private val surfaceView: SurfaceView? = null

    //MediaPlayer对象
//    private var mediaPlayer: MediaPlayer? = null

    //初始化控件
    override fun initView() {
        tv_download_app.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        bind_code_text.paintFlags = bind_code_text.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        splashViews = listOf<View>(
            fl_splash1,
            ll_splash2,
            ll_splash3,
            fl_splash4,
            ll_splash5
        )


    }

    /**
     * @param activeIndex 需要跳转的引导页码
     */
    private fun showSplash(activeIndex: Int) {
        val realIndex = activeIndex - 1
        runOnUiThread {
            for (index in splashViews.indices) {
                val view = splashViews[index]
                if (realIndex == index) {
                    view.visibility = View.VISIBLE;
                }
                if (realIndex != index) {
                    view.visibility = View.GONE
                }
            }
        }
    }

    override fun setListener() {
        fl_splash1.setOnClickListener(this)
        ll_splash2.setOnClickListener(this)
        fl_splash4.setOnClickListener(this)
        tv_next_launcher_splash.setOnClickListener(this)
        tv_next_launcher_splash2.setOnClickListener(this)
        tv_skip_splash.setOnClickListener(this)
        ll_no_child_splash.setOnClickListener(this)
        tv_download_app.setOnClickListener(this)
        fl_wifi_module.setOnClickListener(this)
        btn_code_renew.setOnClickListener(this)

        ll_child_mode.setOnClickListener(this)
        ll_student_mode.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        getPresenter().sendMenuEnableBroadcast(this, false)
        iv_wifi_module.setImageResource(getPresenter().getCurrentWifiDrawable(this))
    }

    override fun onPause() {
        super.onPause()
        lastSplashClose()
        if (!isForceUpdate)
            getPresenter().sendMenuEnableBroadcast(this, true)
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        Log.i("SplashActivity", "DSN: ${AccountUtil.getDSN()}")
        //仅重选用户
        val onlyShowSelectChild = SPUtils.getData("onlyShowSelectChild", false) as Boolean
        //开启用户引导
        val openUserSplash = intent.getBooleanExtra("openUserSplash", false)
        //重新绑定
        isRebinding = SPUtils.getData("rebinding", false) as Boolean
        getSystemDate()
        when {
            isRebinding -> showSplash3(isRebinding)
            openUserSplash -> {   //直接跳转到用户引导
                onlySplash = true
                showNewUserSplash()
            }
            onlyShowSelectChild -> showChildUser()
        }
        refreshNetWorkIcon()
        /* RxTimerUtil.interval(5000) {
             iv_wifi_module.setImageResource(getPresenter().getCurrentWifiDrawable(this@SplashActivity))
         }*/
    }

    private fun refreshNetWorkIcon() {
        GlobalScope.launch(Dispatchers.IO) {
            delay(5000)
            GlobalScope.launch(Dispatchers.Main) {
                iv_wifi_module.setImageResource(getPresenter().getCurrentWifiDrawable(this@SplashActivity))
            }
            refreshNetWorkIcon()
        }
    }

    /**
     * 屏蔽系统返回按钮
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                lastSplashClose()
                true;//系统层不做处理 就可以了
            }
            KeyEvent.KEYCODE_MENU -> {//MENU键
                //监控/拦截菜单键
                return true;
            }
            else -> {
                super.dispatchKeyEvent(event);
            }
        }
    }

    /**
     * 电源键处理
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            lastSplashClose()
        }
        return super.onKeyDown(keyCode, event)
    }


    /**
     * 发送用户登出的广播
     */
    private fun sendUserLoginBroadcast() {
        val intent = Intent()
        intent.action = USER_LOGIN_ACTION
        intent.putExtra("message", "用户登陆") // 设置广播的消息
        sendBroadcast(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetSplashEvent(event: SplashEvent) {
        if (event.showSelectChild) {
            //网络正常 展示选择孩子
            showChildUser()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetSplashEvent(event: SplashStepEvent) {
        //step是指需要跳转到步骤几
        if (event.step == 2) {
            fl_splash1.visibility = View.GONE
            ll_splash2.visibility = View.VISIBLE
            ll_splash3.visibility = View.GONE
            fl_splash4.visibility = View.GONE
            iv_splash_progress.setImageResource(R.drawable.splash2_progress)
            showSplash2QRCode()
        }
    }

    /**
     * Activity是否已被销毁
     * @return
     */
    fun isActivityEnable(): Boolean {
        return !(this == null || isDestroyed || isFinishing)
    }

    /**
     * 扫描二维码绑定
     */
    private fun showSplash3(isRebinding: Boolean) {
        if (!isRebinding && !wifiFlag) {
            return
        }
        //判断网络是否连接
        if (InternetUtil.isNetworkAvalible(this)) {
            GlobalScope.launch(Dispatchers.Main) {
                showSplash(3)
                iv_splash_progress.setImageResource(R.drawable.splash3_progress)
            }
            loadQRCode(iv_qr_splash, true)
            fetchCDK()
        } else {
            ToastUtils.showLong(this, getString(R.string.splash_reconnection))
        }
    }

    private var fetchSDKThrottleTimeLock: Long = 0;
    private var fetchCDK_CD = 15;
    private var fetchCDK_failed_CD = 3;

    @SuppressLint("SetTextI18n")
    private fun fetchCDK() {
        // 倒计时
        if (fetchCDKRemain > 0) return;
        if (cdk == null) {
            bind_code_text.text = "获取中";
        }
        // 加锁
        var failed = true;
        GlobalScope.launch(Dispatchers.IO) {
            try {
                startRenewCodeBtnTimeCounter(fetchCDK_CD)
                cdk = AccountUtil.getCDK()
                failed = false
//                autoFetchCDKTimerTask?.cancel()
//                autoFetchCDKTimerTask = object : TimerTask() {
//                    override fun run() {
//                        fetchCDK()
//                    }
//                }
//                // 5分钟自动刷新
//                Timer().scheduleAtFixedRate(autoFetchCDKTimerTask, 5 * 60 * 1000,5 * 60 * 1000)

            } catch (e: Exception) {
                e.printStackTrace()
//                cdk = "获取失败，点击按钮重试"
                failed = true
                startRenewCodeBtnTimeCounter(fetchCDK_failed_CD)
                GlobalScope.launch(Dispatchers.Main) {
                    ToastUtils.showLong(this@SplashActivity, "获取失败，稍后点击按钮重试")
                }
            }
            if (!failed) {
                GlobalScope.launch(Dispatchers.Main) {
                    bind_code_text.text = cdk
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    fun startRenewCodeBtnTimeCounter(cd: Int = fetchCDK_CD) {
        // 灰掉
        fetchCDKRemain = cd
        runOnUiThread {
            disableRenewCodeBtn()
            btn_code_renew.text = "刷新($cd)";
        }
        // 先取消之前的
        refreshTimerTask?.cancel()
        refreshTimerTask = null
        refreshTimerTask = object : TimerTask() {
            override fun run() {
                fetchCDKRemain--;
                if (fetchCDKRemain <= 0) {
                    fetchCDKRemain = 0;
                    runOnUiThread {
                        btn_code_renew.text = "刷新";
                        enableRenewCodeBtn()
                    }
                    refreshTimerTask?.cancel()
                    refreshTimerTask = null;
                    return;
                }
                runOnUiThread {
                    btn_code_renew.text = "刷新($fetchCDKRemain)";
                }
            }
        }
        Timer().scheduleAtFixedRate(refreshTimerTask, 1000, 1 * 1000)

    }

    private fun disableRenewCodeBtn() {
        btn_code_renew.setTextColor(getColor(R.color.splash_renew_btn_color_disabled))
        btn_code_renew.background = getDrawable(R.drawable.btn_gray_bg)
    }

    private fun enableRenewCodeBtn() {
        btn_code_renew.setTextColor(getColor(R.color.splash_renew_btn_color))
        btn_code_renew.background = getDrawable(R.drawable.grade_white_bg)
    }

    private fun loadQRCode(view: ImageView, isShowChild: Boolean) {
        if (!isActivityEnable()) return

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val qrCode = AccountUtil.getQrCode()
                GlobalScope.launch(Dispatchers.Main) {
                    val loader = Glide.with(this@SplashActivity).load(qrCode)
                    loader.listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any,
                            target: Target<Drawable>, isFirstResource: Boolean
                        ): Boolean {
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(2000L)
                                loadQRCode(view, true)
                            }
                            //加载失败
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable, model: Any, target: Target<Drawable>,
                            dataSource: DataSource, isFirstResource: Boolean
                        ): Boolean {
                            if (isShowChild) {
                                //加载成功
                                getPresenter().getModel(
                                    Urls.DEVICE_BIND,
                                    hashMapOf("dsn" to AccountUtil.getDSN()),
                                    DeviceBindBean::class.java
                                )
                            }
                            return false
                        }
                    }).into(view)
                }
            } catch (e: Exception) {
                delay(2000L)
                loadQRCode(view, true)
                e.printStackTrace()
            }
        }
    }

    private fun showSplash2QRCode() {
        loadQRCode(iv_qr_download_splash, false)
    }

    private fun showChildUser() {
        try {
            GlobalScope.launch(Dispatchers.Main) {
                fl_splash1.visibility = View.GONE
                ll_splash2.visibility = View.GONE
                ll_splash3.visibility = View.GONE
                fl_splash4.visibility = View.VISIBLE
                iv_splash_progress.setImageResource(R.drawable.splash4_progress)
            }
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val allUser = AccountUtil.getAllToken() as MutableList<TokenPair>
                    GlobalScope.launch(Dispatchers.Main) {
                        if (allUser.isNullOrEmpty()) {
                            ll_no_child_splash.visibility = View.VISIBLE
                            rv_select_child_splash.visibility = View.GONE
                            tv_splash4_title.visibility = View.GONE
                        } else {
                            ll_no_child_splash.visibility = View.GONE
                            rv_select_child_splash.visibility = View.VISIBLE
                            tv_splash4_title.visibility = View.VISIBLE
                            rv_select_child_splash.layoutManager = LinearLayoutManager(
                                this@SplashActivity,
                                RecyclerView.HORIZONTAL, false
                            )
                            val splashUserAdapter = SplashUserAdapter()
                            //点击事件
                            splashUserAdapter.setOnItemClickListener { adapter, view, position ->
                                if (InternetUtil.isNetworkAvalible(this@SplashActivity)) {
                                    try {
                                        GlobalScope.launch(Dispatchers.IO) {
                                            val tokenPair = allUser[position]
//                                            NBSAppAgent.setUserIdentifier(tokenPair.userId.toString())
                                            SPUtils.syncPutData(
                                                AppConstants.USER_ID,
                                                tokenPair.userId
                                            )
                                            SPUtils.syncPutData(
                                                AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN,
                                                tokenPair.token
                                            )
                                            SPUtils.syncPutData("rebinding", false)
                                            mUserId = tokenPair.userId
                                            //新替换的登录接口
                                            getPresenter().postModel(
                                                Urls.STUDENT_LOGIN,
                                                RequestBody.create(
                                                    null,
                                                    mapOf(
                                                        "user_id" to tokenPair.userId,
                                                        "dsn" to AccountUtil.getDSN()
                                                    ).toJson()
                                                ), BaseBean::class.java
                                            )
//                                            AccountUtil.selectUser(tokenPair.userId)
                                            GlobalScope.launch(Dispatchers.Main) {
                                                //保存用户信息
                                                writeUserInfo(tokenPair)
                                                //发送用户登陆的广播
                                                sendUserLoginBroadcast()
                                                //展示模式切换
                                                iv_splash_progress.setImageResource(R.drawable.splash5_progress)
                                                showSplash(5)
                                                /* val onlyShowSelectChild =
                                                     SPUtils.getData(
                                                         "onlyShowSelectChild",
                                                         false
                                                     ) as Boolean
                                                 if (onlyShowSelectChild) {
                                                     //仅展示用户选择
                                                     finishSplash()
                                                 } else {
                                                     showNewUserSplash()
                                                 }*/

                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    ToastUtils.showShort(this@SplashActivity, "当前网络异常，请联网后重试")
                                }
                            }
                            rv_select_child_splash.adapter = splashUserAdapter
                            //第一次添加数据
                            splashUserAdapter.setNewInstance(allUser)
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    delay(2000L)
                    showChildUser()
                    e.printStackTrace()
                } catch (e: Exception) {
                    delay(2000L)
                    showChildUser()
                    e.printStackTrace()
                }
            }
        } catch (tokenManagerException: TokenManagerException) {

        }
    }

    private fun finishSplash() {
        getPresenter().sendMenuEnableBroadcast(this, true)
        val syncPutData = SPUtils.syncPutData("splashClose", true)
        if (syncPutData) {
            setResult(AppConstants.RESULT_CODE_SELECT_USER_BACK)
            finish()
        }
    }

    private fun showNewUserSplash() {
        fl_wifi_module.visibility = View.GONE
//        fl_splash.setBackgroundResource(userSplashBgList[0])
        fl_splash1.visibility = View.GONE
        sc_next_launcher_splash.visibility = View.VISIBLE
        tv_date_splash.visibility = View.GONE
        tv_skip_splash.visibility = View.VISIBLE
        rv_select_child_splash.visibility = View.GONE
        ll_progress_splash.visibility = View.GONE
        ll_splash5.visibility = View.GONE
        //增加控制浮窗
//        val mediaController = MediaController(this@SplashActivity)
//        //VideoView与MediaController建立关联
//        videoView.setMediaController(mediaController)
        videoView.setOnPreparedListener {
            it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            it.setOnInfoListener { mp, what, extra ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    simple_video_bg.visibility = View.GONE
                    videoView.setBackgroundColor(Color.TRANSPARENT)
                }
                return@setOnInfoListener true
            }
            videoView.start()
        }

        userSplashNumber = 0
        videoView.visibility = View.VISIBLE
        playSplashVideo()
    }

    private fun backNewUserSplash() {
        if (userSplashNumber != 0) {
            userSplashNumber--
        }
        if (userSplashNumber == 0) {
            sc_next_launcher_splash.visibility = View.VISIBLE
            tv_date_splash.visibility = View.GONE
            tv_skip_splash.visibility = View.VISIBLE
            rv_select_child_splash.visibility = View.GONE
            ll_progress_splash.visibility = View.GONE
            fl_splash4.visibility = View.GONE
            sc_next_launcher_splash2.visibility = View.GONE
            tv_next_launcher_splash.text = "下一页"
        } else {
            sc_next_launcher_splash.visibility = View.VISIBLE
            sc_next_launcher_splash2.visibility = View.VISIBLE
            tv_next_launcher_splash.text = "上一页"
            tv_next_launcher_splash2.text = "下一页"
        }
//        fl_splash.setBackgroundResource(userSplashBgList[userSplashNumber])

        playSplashVideo()
    }

    private fun nextNewUserSplash() {
        if (lastSplashClose()) return
        if (splashVideoList.size - 1 > userSplashNumber) {
            userSplashNumber++
        }
        //显示launcher引导
        if (userSplashNumber == 0) {
            sc_next_launcher_splash.visibility = View.VISIBLE
            tv_date_splash.visibility = View.GONE
            tv_skip_splash.visibility = View.VISIBLE
            rv_select_child_splash.visibility = View.GONE
            ll_progress_splash.visibility = View.GONE
            fl_splash4.visibility = View.GONE
        } else {
            sc_next_launcher_splash2.visibility = View.VISIBLE
            sc_next_launcher_splash.visibility = View.VISIBLE
            tv_next_launcher_splash.text = "上一页"
        }
//        fl_splash.setBackgroundResource(userSplashBgList[userSplashNumber])
        /*  Glide.with(this)
              .load(userSplashBgList[userSplashNumber])
              .into(object : SimpleTarget<Drawable?>() {
                  override fun onResourceReady(
                      resource: Drawable,
                      transition: Transition<in Drawable?>?
                  ) {
                      fl_splash.background = resource
                  }
              })*/
        if (userSplashNumber == splashVideoList.size - 1) {
            tv_next_launcher_splash2.text = "进入系统"
        }
        playSplashVideo()
    }

    private fun playSplashVideo() {
        simple_video_bg.visibility = View.VISIBLE
        videoView.setBackgroundColor(Color.WHITE)
        try {
            val uri =
                Uri.parse("android.resource://$packageName/${splashVideoList[userSplashNumber]}")//“xxxx”为视频名称，视频资源在res目录下新建raw，在raw文件夹中放入视频
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                videoView.setVideoURI(uri)
            }
            videoView.requestFocus()
            videoView.seekTo(1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        videoView.start()
    }

    private fun lastSplashClose(): Boolean {
        if (userSplashNumber == splashVideoList.size - 1 && !closeSplash) {
            //开机引导进入launcher时检测更新
            if (!onlySplash) {
                closeSplash = true
                EventBus.getDefault().post(CheckUpdateEvent.getInstance())
            }
            //关闭引导
            closeSplash()
            return true
        }
        return false
    }

    /**
     * 往Provider中写入数据
     */
    private fun writeUserInfo(tokenPair: TokenPair) {
        //插入数据前清除之前的数据
//        contentResolver.delete(LauncherContentProvider.URI, null, null)
        val contentValues = ContentValues()
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME, tokenPair.expireTime)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE, tokenPair.gradeType)
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
                AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME,
                AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE
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
                    ) + "  grade:" + boyCursor.getInt(
                        boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE)
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
        when (any) {
            is DeviceBindBean -> {
                if (any.data.exists) {
                    showChildUser()
                    //检测系统更新
                    /*  if (isRebinding) {
                          showChildUser()
                      } else {
                          //绕过升级直接进入选择用户
    //                        showChildUser()
                          getPresenter().getModel(
                              Urls.UPDATE,
                              hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
                              UpdateBean::class.java
                          )
                      }*/

                } else {
                    GlobalScope.launch {
                        try {
                            delay(2000)
                            getPresenter().getModel(
                                Urls.DEVICE_BIND,
                                hashMapOf("dsn" to AccountUtil.getDSN()),
                                DeviceBindBean::class.java
                            )
                        } catch (e: NullPointerException) {
                            Log.e("SplashActivity", e.stackTraceToString())
                        } catch (e: Exception) {
                            Log.e("SplashActivity", e.stackTraceToString())
                        }

                    }
                }
            }
            is UpdateBean -> {
                isForceUpdate = true
                if (!isRebinding)
                //展示系统固件更新
                    getPresenter().splashStartUpdateActivity(
                        true,
                        any,
                        this
                    )
            }
            is BaseBean -> {
                if (any.code == 201) {
                    if (mUserId != -1) {
                        AccountUtil.currentUserId = mUserId
                    }
                } else {
                    //重新尝试调用登录接口
                    if (mUserId != -1) {
                        getPresenter().postModel(
                            Urls.STUDENT_LOGIN,
                            RequestBody.create(
                                null,
                                mapOf(
                                    "user_id" to mUserId,
                                    "dsn" to AccountUtil.getDSN()
                                ).toJson()
                            ), BaseBean::class.java
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    override fun onError(error: String) {
        Log.e("error", error)
    }

    /**
     * 根据页码展示不同的引导页
     */
/*
    private fun showPositionSplash(position: Int) {
        when (position) {
            1 -> {
                fl_splash1.visibility = View.VISIBLE
                ll_splash2.visibility = View.GONE
                ll_splash3.visibility = View.GONE
                fl_splash4.visibility = View.GONE
            }
            2 -> {
                fl_splash1.visibility = View.GONE
                ll_splash2.visibility = View.VISIBLE
                ll_splash3.visibility = View.GONE
                fl_splash4.visibility = View.GONE
            }
            3 -> {
                showSplash3()
            }
            4 -> {
            }
        }

    }
*/

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fl_splash1 -> {
                wifiFlag = true
                //开始判断网络状态
                if (InternetUtil.isNetworkAvalible(this)) {
                    fl_splash1.visibility = View.GONE
                    ll_splash2.visibility = View.VISIBLE
                    iv_splash_progress.setImageResource(R.drawable.splash2_progress)
                    showSplash2QRCode()
                } else {
                    ToastUtils.showShort(this, getString(R.string.splash_network_connections))
                    getPresenter().startWifiModule(true)
//                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
                }
            }
            R.id.tv_download_app -> {
                iv_splash_progress.setImageResource(R.drawable.splash3_progress)
                showSplash3(true)
            }
//            R.id.ll_splash2 -> {
            //跳过二维码后门
//                showChildUser()
//            }
//            R.id.fl_splash3 -> {
            //系统引导设置完毕，关闭引导页
//                closeSplash()
//            }
            R.id.tv_next_launcher_splash -> {
                if (BtnClickUtil.isFastShow()) return
                //第一页
                if (userSplashNumber == 0) {
                    nextNewUserSplash()
                } else {
                    backNewUserSplash()
                }
            }
            R.id.tv_next_launcher_splash2 -> {
                if (BtnClickUtil.isFastShow()) return
                nextNewUserSplash()
            }
            R.id.tv_skip_splash -> {
                closeSplash()
                if (!onlySplash) {
                    EventBus.getDefault().post(CheckUpdateEvent.getInstance())
                }
            }
            R.id.ll_no_child_splash -> {
                ll_no_child_splash.visibility = View.GONE
                showChildUser()
            }
            R.id.btn_code_renew -> {
                fetchCDK()
            }
            R.id.fl_wifi_module -> {
                getPresenter().startWifiModule(false)
            }
            //儿童模式
            R.id.ll_child_mode -> {
                val mmkv = LauncherApplication.getMMKV()
                mmkv.encode("mode", "child")
                selectMode()
            }
            //学生模式
            R.id.ll_student_mode -> {
                val mmkv = LauncherApplication.getMMKV()
                mmkv.encode("mode", "student")
                selectMode()
            }
        }
    }

    private fun selectMode() {
        val onlyShowSelectChild =
            SPUtils.getData(
                "onlyShowSelectChild",
                false
            ) as Boolean
        if (onlyShowSelectChild) {
            //仅展示用户选择
            finishSplash()
        } else {
            showNewUserSplash()
        }
    }


    private fun closeSplash() {
        //新用户的状态设为false
        SPUtils.asyncPutData(AppConstants.NEW_USER, false)
        SPUtils.asyncPutData("onlyShowSelectChild", true)
        finishSplash()
    }

}
