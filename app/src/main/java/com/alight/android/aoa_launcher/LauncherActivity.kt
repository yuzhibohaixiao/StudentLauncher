package com.alight.android.aoa_launcher

import Data
import UpdateBean
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.bean.TokenMessage
import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.constants.AppConstants.Companion.EXTRA_IMAGE_PATH
import com.alight.android.aoa_launcher.constants.AppConstants.Companion.SYSTEM_ZIP_PATH
import com.alight.android.aoa_launcher.i.LauncherListener
import com.alight.android.aoa_launcher.listener.DownloadListener
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.DateUtil
import com.alight.android.aoa_launcher.utils.DownloadUtil
import com.alight.android.aoa_launcher.utils.SPUtils
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.xuexiang.xupdate.entity.UpdateEntity
import com.xuexiang.xupdate.listener.IUpdateParseCallback
import com.xuexiang.xupdate.proxy.IUpdateParser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


/**
 * Launcher主页
 */
class LauncherActivity : BaseActivity(), View.OnClickListener, LauncherListener {

    private var TAG = "LauncherActivity"

    override fun initData() {
        //初始化权限
        initPermission()
        //初始化用户获取工具
        initAccountUtil()
        //监听contentProvider是否被操作
        contentResolver.registerContentObserver(
            LauncherContentProvider.URI,
            true,
            contentObserver
        )
        //如果是新用户则打开Splash
        val isNewUser = SPUtils.getData(AppConstants.NEW_USER, true) as Boolean
        if (isNewUser) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
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

    /**
     * 更新解析器
     */
    inner class CustomUpdateParser : IUpdateParser {
        override fun parseJson(json: String): UpdateEntity? {
            /*  val updateBean = Gson().fromJson(json, UpdateBean::class.java)
              val data = updateBean.data
              var updateData: Any
              for (position in updateBean.data.indices) {
                  if (updateData(position))
              }
              if (data != null) {
                  return UpdateEntity()
                      .setHasUpdate(data.is_active)
                      .setSize(data.apk_size)
                      .setIsAutoInstall(true)
                      .setMd5(data.apk_md5)
                      .setIsIgnorable(true)
  //                .setForce(data.app_force_upgrade == 1)
                      .setVersionCode(data.version_code)
                      .setVersionName(data.version_name)
                      .setUpdateContent(data.content)
                      .setDownloadUrl(data.app_url)
              }*/
            return null
        }

        override fun parseJson(json: String, callback: IUpdateParseCallback) {
            val result: UpdateBean = Gson().fromJson(json, UpdateBean::class.java)
            val data = result.data
            var systemApp: Data? = null
            var launcherApp: Data? = null
            for (position in data.indices) {
                when (data[position].app_name) {
                    //系统升级
                    "system" -> systemApp = data[position]
                    "test2_apk" -> launcherApp = data[position]
                }
            }
            launcherApp?.let {
                var launcherEntity = UpdateEntity()
                    .setHasUpdate(launcherApp.is_active)
                    .setSize(launcherApp.apk_size)
                    .setIsAutoInstall(true)
                    .setMd5(launcherApp.apk_md5)
                    .setIsIgnorable(true)
//                    .setForce(launcherApp.app_force_upgrade == 1)
                    .setVersionCode(launcherApp.version_code)
                    .setVersionName(launcherApp.version_name)
                    .setUpdateContent(launcherApp.content)
                    .setDownloadUrl(launcherApp.app_url)
                Log.i("XUpdate", "parseJson: $launcherApp")
                callback.onParseResult(launcherEntity)
            }
            DownloadUtil.download(systemApp?.app_url, SYSTEM_ZIP_PATH, object : DownloadListener {

                override fun onStart() {
                    //运行在子线程
                }

                override fun onProgress(progress: Int) {
                    //运行在子线程
                    Log.i("TAG", "onProgress: $progress")
                }

                override fun onFinish(path: String?) {
                    Log.i("TAG", "onProgress: 下载完成，尝试提示安装")
                    //运行在子线程
                    checkSystemUpdate()
                }

                override fun onFail(errorInfo: String?) {
                    //运行在子线程
                }
            })

            //todo 可设置多个回调 从而处理多个应用更新
//            callback.onParseResult(updateEntity)
        }

        override fun isAsyncParser(): Boolean {
            return true
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
    }


    /**
     * 获取升级包
     *
     * @return 升级包file
     */
    private fun getUpdateFile(): File? {
        var updateFile: File? = null
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val updatePath: String =
                (Environment.getExternalStorageDirectory().absolutePath
                        + File.separator) + "update.zip"
            updateFile = File(updatePath)
            val isExists: Boolean = updateFile.exists()
            Log.i(TAG, "\"是否存在升级包：$isExists")
            if (isExists) {
                return updateFile
            }
        }
        return null
    }

    /**
     * 检测系统升级
     */
    private fun checkSystemUpdate() {
        val intent = Intent()
        intent.component = ComponentName(
            "android.rockchip.update.service",
            "android.rockchip.update.service.FirmwareUpdatingActivity"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(
            EXTRA_IMAGE_PATH,
            SYSTEM_ZIP_PATH
        )
        startActivity(intent)
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
        /*   //网络请求成功后的结果 让对应视图进行刷新
           if (any is BannerBean) {
               mAdapter.setBannerData(any.result)
           }
        */
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
//                queryUserInfo()

        }
    }

    private fun queryUserInfo() {
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
