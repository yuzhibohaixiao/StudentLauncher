package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.content.*
import android.database.ContentObserver
import android.graphics.Paint
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import cn.jpush.android.api.JPushInterface
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.event.NetMessageEvent
import com.alight.android.aoa_launcher.common.i.LauncherListener
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.net.INetEvent
import com.alight.android.aoa_launcher.net.NetTools
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import com.qweather.sdk.bean.weather.WeatherNowBean
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_family.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.*


/**
 * @author wangzhe
 * Launcher主页
 */
class LauncherActivity : BaseActivity(), View.OnClickListener, LauncherListener, INetEvent {

    private var netState = 1
    private var tokenPair: TokenPair? = null
    private var TAG = "LauncherActivity"
    private var dialog: CustomDialog? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var splashCloseFlag = false

    companion object {
        lateinit var mINetEvent: INetEvent
    }

    override fun onResume() {
        super.onResume()
        val splashClose = SPUtils.getData("splashClose", false) as Boolean
        Log.i(TAG, "splashClose = $splashClose splashCloseFlag = $splashCloseFlag")
        val rebinding = SPUtils.getData("rebinding", false) as Boolean
        //用户重新绑定
        if (rebinding) {
            initAccountUtil()
            SPUtils.syncPutData("rebinding", false)
        }
        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
        if (!splashClose && !splashCloseFlag && tokenPairCache.isNullOrEmpty()) {
            Log.i(TAG, "展示引导页")
//        如果未展示过引导则展示引导页
            activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
        }
        if (!splashCloseFlag && tv_user_name_launcher.text.isNullOrEmpty()) {
            Glide.with(this@LauncherActivity)
                .load(tokenPair?.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .error(if (tokenPair?.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
                .into(iv_user_icon_launcher)
            tv_user_name_launcher.text = tokenPair?.name
        }
        splashCloseFlag = false
    }


    override fun initData() {
        mINetEvent = this
        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
        if (tokenPairCache.isNotEmpty()) {
            tokenPair = Gson().fromJson(tokenPairCache, TokenPair::class.java)
            writeUserInfo(tokenPair!!)
        }
        //获取用户信息之前必须调用的初始化方法
        AccountUtil.run()
//        initAccountUtil()
        //初始化权限
        initPermission()
        //监听contentProvider是否被操作
        contentResolver.registerContentObserver(
            LauncherContentProvider.URI,
            true,
            contentObserver
        )
        //初始化天气控件日期
        initWeatherDate()
        //定位后获取天气
        getPresenter().getLocationAndWeather()
        startHardwareControl()
        Log.i(TAG, "DSN: ${AccountUtil.getDSN()}")
        SPUtils.syncPutData("splashClose", false)
        Log.i(TAG, "splashClose initData")
        //注册splash的返回数据回调
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.i(TAG, "registerForActivityResult")
            when (it.resultCode) {
                //选择用户后的用户刷新逻辑
                AppConstants.RESULT_CODE_SELECT_USER_BACK -> {
                    splashCloseFlag = true
                    val syncPutData = SPUtils.syncPutData("splashClose", true)
                    Log.i(TAG, "initAccountUtil")
                    //初始化用户工具及展示用户数据
                    initAccountUtil()
                }
                //使用Launcher调起选择用户
                AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER -> {
                    activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
                }
            }
        }
        //展示无网络UI
        noNetworkInit()
        // 用户绑定极光推送
        getPresenter().postModel(
            Urls.BIND_PUSH,
            RequestBody.create(
                null,
                mapOf(
                    AppConstants.REGISTRATION_ID to JPushInterface.getRegistrationID(this@LauncherActivity)
                ).toJson()
            ),
            JPushBindBean::class.java
        )
    }

    private fun noNetworkInit() {
        if (!InternetUtil.isNetworkAvalible(this)) {
            netState = 0
            iv_aoa_launcher.setImageResource(R.drawable.launcher_aoa_offline)
        }
    }

    /**
     * 开启硬件控制
     */
    private fun startHardwareControl() {
        val intent = Intent()
        val componentName =
            ComponentName(AppConstants.AHWCX_PACKAGE_NAME, AppConstants.AHWCX_SERVICE_NAME)
        intent.component = componentName
        startService(intent)
    }

    private var uri: Uri? = null
    private val handler =
        Handler { msg ->
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
        }

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
//        activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
    }

    private fun initPermission() {
//        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
//        startActivity(intent)
        PermissionUtils.isGrantExternalRW(this, 1)
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
                try {
                    val allToken = AccountUtil.getAllToken()
                    allToken.forEach {
                        if (it.userId == userId) {
                            AccountUtil.selectUser(it.userId)
                        }
                    }
                    //设置用户信息
                    getPresenter().setPersonInfo(this@LauncherActivity)
                    // 用户绑定极光推送
                    getPresenter().postModel(
                        Urls.BIND_PUSH,
                        RequestBody.create(
                            null,
                            mapOf(
                                AppConstants.REGISTRATION_ID to JPushInterface.getRegistrationID(
                                    this@LauncherActivity
                                )
                            ).toJson()
                        ),
                        JPushBindBean::class.java
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                if (tokenPair != null) {
                    writeUserInfo(tokenPair!!)
                    SPUtils.syncPutData("tokenPair", Gson().toJson(tokenPair))
                }
                Glide.with(this@LauncherActivity)
                    .load(tokenPair?.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .error(if (tokenPair?.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
                    .into(iv_user_icon_launcher)
                tv_user_name_launcher.text = any.name
                if (StringUtils.isEmpty(tokenPair?.token)) {
                    getPresenter().getLocationAndWeather()
                }
            } else if (any is JPushBindBean) {
                if (any.code == 201) {
                    heartbeat()
                }
            }
        }
    }

    private fun heartbeat() {
        GlobalScope.launch(Dispatchers.IO) {
            getPresenter().getModel(Urls.HEART_BEAT, hashMapOf(), BaseBean::class.java)
            //每15秒调用一次打点接口
            delay(1000 * 15)
            heartbeat()
        }
    }

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
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE, tokenPair.gradeType)
        //将登陆的用户数据插入保存
        contentResolver.insert(LauncherContentProvider.URI, contentValues)
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

    override fun onClick(view: View) {    //弹出自定义dialog
        when (view.id) {
            //教育
            R.id.iv_education_launcher -> getPresenter().showDialog(
                AppConstants.EDUCATION_APP
            )
            //游戏
            R.id.iv_game_launcher -> getPresenter().showDialog(AppConstants.GAME_APP)
/*
            R.id.iv_game_launcher -> {
                var intent = Intent("com.alight.trtcav.WindowActivity")
                var json = "{\n" +
                        "    \"extras\":\"\",\n" +
                        "    \"id\":9180820315017280000000000000000000000,\n" +
                        "    \"intent_url\":\"87://ar\",\n" +
                        "    \"message\":{\n" +
                        "        \"fromUserId\":301,\n" +
                        "        \"fromUserInfo\":{\n" +
                        "            \"avatar\":\"https://thirdwx.qlogo.cn/mmopen/vi_32/RqGtKcj2577SAw82p0hibopIpmXtPicKibEDhiakpeu9DXpS7ViaibaAUfaZmRKM3XW2DeezcmuRoE6KzicHtGfPQ4CLQ/132\",\n" +
                        "            \"birthday\":0,\n" +
                        "            \"city\":\"朝阳\",\n" +
                        "            \"country\":\"中国\",\n" +
                        "            \"create_time\":\"2021-09-05 21:59:24\",\n" +
                        "            \"gender\":2,\n" +
                        "            \"id\":301,\n" +
                        "            \"is_active\":true,\n" +
                        "            \"language\":null,\n" +
                        "            \"name\":\"@\",\n" +
                        "            \"phone\":\"17336332639\",\n" +
                        "            \"province\":\"北京\",\n" +
                        "            \"update_time\":\"2021-10-27 15:52:17\"\n" +
                        "        },\n" +
                        "        \"roomId\":9,\n" +
                        "        \"type\":\"video\",\n" +
                        "        \"userId\":330\n" +
                        "    },\n" +
                        "    \"title\":\"@ 打来了电话!\",\n" +
                        "    \"trace\":{\n" +
                        "        \"dst_id\":-1,\n" +
                        "        \"dst_platform\":-1,\n" +
                        "        \"src_id\":-1,\n" +
                        "        \"src_platform\":-1\n" +
                        "    },\n" +
                        "    \"type\":0\n" +
                        "}"
                val callArBean = Gson().fromJson(json, CallArBean::class.java)
                intent.putExtra("parentId", callArBean.message.fromUserId.toString())
                intent.putExtra("parentName", callArBean.message.fromUserInfo.name)
                intent.putExtra("parentAvatar", callArBean.message.fromUserInfo.avatar)
                intent.putExtra("roomId", callArBean.message.roomId)
                intent.putExtra("childId", AccountUtil.getCurrentUser().userId.toString())
                intent.putExtra("called", 2)
                intent.putExtra("token", AccountUtil.getCurrentUser().token)
                intent.putExtra("callType", callArBean.message.type)
                startActivity(intent)
            }
*/
            //其他
            R.id.iv_other_launcher -> getPresenter().showDialog(AppConstants.OTHER_APP)
            //音视频
            R.id.iv_video_launcher -> getPresenter().showDialog(AppConstants.MEDIA_APP)
            //设置　
            R.id.iv_setting_launcher -> getPresenter().showSystemSetting()
            // 打开应用市场（安智）
            R.id.iv_app_store -> getPresenter().showKAMarket()
            //打开aoa星仔伴学
            R.id.iv_aoa_launcher -> {
                if (netState == 1) {
                    getPresenter().showAOA()
                } else {
                    val customDialog = CustomDialog(this, R.layout.dialog_offline)
                    val tvOffline = customDialog.findViewById<TextView>(R.id.tv_offline)
                    val spannableString = SpannableString(tvOffline.text)
                    spannableString.setSpan(UnderlineSpan(), 7, 11, Paint.UNDERLINE_TEXT_FLAG)
                    tvOffline.setOnClickListener {
                        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        customDialog.show()
                        delay(3000)
                        customDialog.dismiss()
                    }
//                    ToastUtils.showLong(this, "当前无网络，请点击此处链接wifi")
                }
            }

            //个人中心
            R.id.ll_personal_center -> {
                if (tokenPair == null) return
                var intent = Intent(this, PersonCenterActivity::class.java)
                intent.putExtra("userInfo", tokenPair)
                intent.putExtra("netState", netState)
                activityResultLauncher?.launch(intent)
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

    override fun onReceive(tokenMessage: TokenMessage) {
        Log.i(TAG, "onReceive message: ${tokenMessage.message}")
        //家长打来视频通话
        if (tokenMessage.message.type == "video" || tokenMessage.message.type == "audio") {
            startCalledWindow(tokenMessage)
        }
        // todo 1 服务端发给我消息 发一个广播给其他的应用接收

        // todo 2 其他应用给我发消息 我需要调用AccountUtil.postMessage()发给服务端
    }

    private fun startCalledWindow(tokenMessage: TokenMessage) {
        val intent = Intent("com.alight.trtcav.WindowActivity")
        if (tokenMessage != null) {
            intent.putExtra("parentId", tokenMessage.message.fromUserId.toString())
            intent.putExtra("parentName", tokenMessage.message.fromUserInfo.name)
            intent.putExtra("parentAvatar", tokenMessage.message.fromUserInfo.avatar)
            intent.putExtra("roomId", tokenMessage.message.roomId)
            intent.putExtra("childId", AccountUtil.getCurrentUser().userId.toString())
            intent.putExtra("called", 2)
            intent.putExtra("token", AccountUtil.getCurrentUser().token)
            intent.putExtra("callType", tokenMessage.message.type)
        }
        try {
            this.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.stackTraceToString()
        }
    }

    override fun onConnect() {

    }

    override fun onDisconnect() {

    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    override fun onNetChange(netWorkState: Int) {
        when (netWorkState) {
            NetTools.NETWORK_NONE -> {
                netState = 0
                iv_aoa_launcher.setImageResource(R.drawable.launcher_aoa_offline)
                EventBus.getDefault().post(NetMessageEvent.getInstance(netState, "网络异常"));
                Log.e(TAG, "onNetChanged:没有网络 ")
            }
            NetTools.NETWORK_MOBILE, NetTools.NETWORK_WIFI -> {
                netState = 1
                iv_aoa_launcher.setImageResource(R.drawable.launcher_aoa)
                initAccountUtil()
                EventBus.getDefault().post(NetMessageEvent.getInstance(netState, "网络恢复正常"));
                Log.e(TAG, "onNetChanged:网络正常 ")
            }
        }
    }

}
