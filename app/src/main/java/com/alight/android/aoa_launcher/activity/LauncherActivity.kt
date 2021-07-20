package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.TokenMessage
import com.alight.android.aoa_launcher.common.bean.TokenPair
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.i.LauncherListener
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.DateUtil
import com.alight.android.aoa_launcher.utils.SPUtils
import com.bumptech.glide.Glide
import com.permissionx.guolindev.PermissionX
import com.qweather.sdk.bean.weather.WeatherNowBean
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_family.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


/**
 * Launcher主页
 */
class LauncherActivity : BaseActivity(), View.OnClickListener, LauncherListener {

    private var tokenPair: TokenPair? = null
    private var TAG = "LauncherActivity"

    override fun onResume() {
        super.onResume()
        //初始化用户工具及展示用户数据
        initAccountUtil()
    }

    override fun initData() {
        //获取用户信息之前必须调用的初始化方法
        AccountUtil.run()
        //初始化权限
        initPermission()
        //监听contentProvider是否被操作
        contentResolver.registerContentObserver(
            LauncherContentProvider.URI,
            true,
            contentObserver
        )
        //如果是新用户则打开Splash
//        val isNewUser = SPUtils.getData(AppConstants.NEW_USER, true) as Boolean
//        if (isNewUser) {
        startActivity(Intent(this, SplashActivity::class.java))
//        }
        //初始化天气控件日期
        initWeatherDate()
        //定位后获取天气
        getPresenter().getLocationAndWeather()
        //获取App和系统固件更新
        getPresenter().updateAppAndSystem()
    }

    private var uri: Uri? = null
    private val handler =
        Handler(Handler.Callback { msg ->
            if (msg.what == 0x123) {
                if (uri != null) {
                    val cursor =
                        contentResolver.query(uri!!, null, null, null, null)
                    if (null != cursor) {
                        cursor.moveToNext()
                        val id =
                            cursor.getInt(cursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN))
                        val name =
                            cursor.getString(cursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_NAME))
                        val token =
                            cursor.getString(cursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN))
                        /*Toast.makeText(
                            this@LauncherActivity,
                            "ID=$id ; name=$name; token=$token",
                            Toast.LENGTH_SHORT
                        ).show()*/
                        Log.i(TAG, "ID=$id ; name=$name; token=$token")

                    }
                } else {
                    Log.i(TAG, "数据发生变化")
//                    Toast.makeText(this@LauncherActivity, "数据发生变化", Toast.LENGTH_SHORT).show()
                }
            }
            false
        })

    /**
     * 内容提供者监听类
     */
    private val contentObserver: ContentObserver = object : ContentObserver(handler) {
        override fun deliverSelfNotifications(): Boolean {
            Log.i(TAG, "deliverSelfNotifications")
            return super.deliverSelfNotifications()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.i(TAG, "onChange-------->")
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.i(TAG, "onChange-------->$uri")
            handler.sendEmptyMessage(0x123)
        }
    }

    //初始化控件
    override fun initView() {
//        main_recy.layoutManager = LinearLayoutManager(this)
//        mAdapter = MyAdapter(baseContext)
//        main_recy.adapter = mAdapter
    }

    private fun initPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Log.i(TAG, "initPermission: All permissions are granted")
                } else {
                    Log.i(TAG, "initPermission: These permissions are denied: $deniedList")
                }
            }
    }


    private fun initAccountUtil() {

        AccountUtil.register(this)
        val userId = SPUtils.getData(AppConstants.USER_ID, -1) as Int
        if (userId != -1) {
            GlobalScope.launch(Dispatchers.IO) {
                val allToken = AccountUtil.getAllToken()
                allToken.forEach {
                    if (it.userId == userId) {
                        AccountUtil.selectUser(it.userId)
                    }
                }
                //设置用户信息
                getPresenter().setPersonInfo(this@LauncherActivity)
            }

        }
    }


    override fun setListener() {
        iv_education_launcher.setOnClickListener(this)
        iv_game_launcher.setOnClickListener(this)
        iv_other_launcher.setOnClickListener(this)
        iv_video_launcher.setOnClickListener(this)

        iv_setting_launcher.setOnClickListener(this)
        iv_app_store.setOnClickListener(this)
        iv_aoa_launcher.setOnClickListener(this)
        ll_personal_center.setOnClickListener(this)
    }

    /**
     *  初始化天气控件的日期和时间 异步获取天气和时间 每10秒刷新一次
     */
    private fun initWeatherDate() {
        GlobalScope.launch(Dispatchers.IO) {
            //获取当前系统时间
            var calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault();//默认当前时区
            var year = calendar.get(Calendar.YEAR)// 获取当前年份
            var month = calendar.get(Calendar.MONTH) + 1// 获取当前月份
            var day = calendar.get(Calendar.DAY_OF_MONTH)// 获取当前月份的日期号码
            var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
            var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
            GlobalScope.launch(Dispatchers.Main) {
                tv_month_launcher.text =
                    "${month}月${day}日 " + DateUtil.getDayOfWeek(calendar)
                tv_year_launcher.text = "${year}年"
                tv_time_launcher.text =
                    "$hour:" + if (minute >= 10) minute else "0$minute"
            }
            delay(10000)
            initWeatherDate()
        }

    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }
//初始化并弹出对话框方法


    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun onSuccess(any: Any) {
        GlobalScope.launch(Dispatchers.Main) {
            //网络请求成功后的结果 让对应视图进行刷新
            if (any is TokenPair) {
                tokenPair = any
                Glide.with(this@LauncherActivity)
                    .load(if (tokenPair?.gender == 2) R.drawable.splash_boy else R.drawable.splash_girl)
//                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
//                    .error()
                    .into(iv_user_icon_launcher)
                tv_user_name_launcher.text = any.name
            }
        }
    }


    override fun onError(error: String) {
        Log.e("error", error)
    }

    /**
     * 天气处理
     */
    fun onWeather(
        city: String,
        weatherNowBean: WeatherNowBean,
        weatherIcon: Int
    ) {
        tv_temperature_launcher.text = weatherNowBean.now.temp + "°C"
        iv_weather_launcher.setImageResource(weatherIcon)
    }

    override fun onClick(view: View) {
        when (view.id) {
            //教育
            R.id.iv_education_launcher -> getPresenter().showDialog(AppConstants.EDUCATION_APP)
            //游戏
            R.id.iv_game_launcher -> getPresenter().showDialog(AppConstants.GAME_APP)
            //其他
            R.id.iv_other_launcher -> getPresenter().showDialog(AppConstants.OTHER_APP)
            //音视频
            R.id.iv_video_launcher -> getPresenter().showDialog(AppConstants.MEDIA_APP)
            //设置
            R.id.iv_setting_launcher -> getPresenter().showSystemSetting()
            // 打开应用市场（安智）
            R.id.iv_app_store -> getPresenter().showAZMarket()
            //打开aoa星仔伴学
            R.id.iv_aoa_launcher ->
                getPresenter().showAOA()
            //个人中心
            R.id.ll_personal_center -> {
                var intent = Intent(this, PersonCenterActivity::class.java)
                intent.putExtra("userInfo", tokenPair)
                startActivity(intent)
            }
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
            super.dispatchKeyEvent(event)
        }
    }

    override fun onReceive(message: TokenMessage) {
        // todo 1 服务端发给我消息 发一个广播给其他的应用接收

        // todo 2 其他应用给我发消息 我需要调用AccountUtil.postMessage()发给服务端
    }

    override fun onConnect() {

    }

    override fun onDisconnect() {

    }


}