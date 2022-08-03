package com.alight.android.aoa_launcher.presenter

import android.Manifest
import android.app.Activity
import android.app.Notification.FLAG_NO_CLEAR
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alight.ahwcx.ahwsdk.abilities.InteractionAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.activity.LauncherActivity
import com.alight.android.aoa_launcher.activity.UpdateActivity
import com.alight.android.aoa_launcher.activity.WifiActivity
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BasePresenter
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.constants.AppConstants.Companion.EXTRA_IMAGE_PATH
import com.alight.android.aoa_launcher.common.constants.AppConstants.Companion.OLD_AOA_PACKAGE_NAME
import com.alight.android.aoa_launcher.common.constants.AppConstants.Companion.SYSTEM_ZIP_FULL_PATH
import com.alight.android.aoa_launcher.common.event.SplashEvent
import com.alight.android.aoa_launcher.common.listener.DownloadListener
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.net.contract.IContract
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.ui.adapter.GradeDialogAdapter
import com.alight.android.aoa_launcher.ui.adapter.HorizontalChildParentAdapter
import com.alight.android.aoa_launcher.ui.adapter.HorizontalParentAdapter
import com.alight.android.aoa_launcher.ui.adapter.HorizontalScrollAdapter
import com.alight.android.aoa_launcher.ui.view.ConfirmDialog
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.zhouwei.library.CustomPopWindow
import com.google.gson.Gson
import com.qweather.sdk.bean.base.Code
import com.qweather.sdk.bean.base.Lang
import com.qweather.sdk.bean.base.Unit
import com.qweather.sdk.bean.geo.GeoBean
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.qweather.sdk.view.HeConfig
import com.qweather.sdk.view.QWeather
import com.qweather.sdk.view.QWeather.OnResultGeoListener
import com.qweather.sdk.view.QWeather.OnResultWeatherNowListener
import com.viewpagerindicator.CirclePageIndicator
import com.xuexiang.xupdate.entity.UpdateEntity
import com.xuexiang.xupdate.listener.IUpdateParseCallback
import com.xuexiang.xupdate.proxy.IUpdateParser
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.dialog_update.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Runnable
import java.util.*


/**
 * launcher业务处理类
 * @author wangzhe
 * Created on 2021/5/12
 */
class PresenterImpl : BasePresenter<IContract.IView>() {

    private var mCustomPopWindow: CustomPopWindow? = null

    private var TAG = "PresenterImpl"
    override fun <T> getModel(url: String, map: HashMap<String, Any>, cls: Class<T>) {
        //调用model
        getModel().getNetInfo(url, map, cls, object : NetUtils.NetCallback {
            //model层回调给Presenter层级
            override fun onSuccess(any: Any) {
                //希望在View层进行视图的刷新
                getView()?.onSuccess(any)
            }

            override fun onError(error: String) {
                getView()?.onError(error)
            }
        })
    }

    fun <T> postModel(url: String, requestBody: RequestBody, cls: Class<T>) {
        //调用model
        getModel().postNetInfo(url, requestBody, cls, object : NetUtils.NetCallback {
            //model层回调给Presenter层级
            override fun onSuccess(any: Any) {
                //希望在View层进行视图的刷新
                getView()?.onSuccess(any)
            }

            override fun onError(error: String) {
                getView()?.onError(error)
            }
        })
    }


    fun <T> deleteModel(requestBody: RequestBody, cls: Class<T>) {
        //调用model
        getModel().deleteNetInfo(requestBody, cls, object : NetUtils.NetCallback {
            //model层回调给Presenter层级
            override fun onSuccess(any: Any) {
                //希望在View层进行视图的刷新
                getView()?.onSuccess(any)
            }

            override fun onError(error: String) {
                getView()?.onError(error)
            }
        })
    }


    /**
     *  初始化天气服务
     */
    private fun initWeather() {
        // param publicId  appKey
        HeConfig.init("HE2105171641531090", "49fba87b52944fe08ba36e5c74dfb4a1")
        //切换至开发版服务
        HeConfig.switchToDevService()
        //切换至商业版服务
//        HeConfig.switchToBizService();
    }

    /**
     * 该方法先获取到用户的经纬度，通过经纬度获取到用户所在城市的id，
     * 最终通过城市id调用获取天气情况的方法
     */
    fun getLocationAndWeather() {
        //初始化天气sdk
        initWeather()
        val activity = getView() as LauncherActivity
        // 位置
        val locationManager: LocationManager
        val locationListener: LocationListener
        val location: Location?
        val contextService: String = Context.LOCATION_SERVICE
        val provider: String?
        var lat: Double
        var lon: Double
        locationManager = activity.getSystemService(contextService) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE // 高精度

        criteria.isAltitudeRequired = false // 不要求海拔

        criteria.isBearingRequired = false // 不要求方位

        criteria.isCostAllowed = true // 允许有花费

        criteria.powerRequirement = Criteria.POWER_LOW // 低功耗

        // 从可用的位置提供器中，匹配以上标准的最佳提供器
        provider = locationManager.getBestProvider(criteria, true)
        // 获得最后一次变化的位置
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        location = locationManager.getLastKnownLocation(provider!!)
        locationListener = object : LocationListener {
            override fun onStatusChanged(
                provider: String, status: Int,
                extras: Bundle
            ) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }

            override fun onLocationChanged(location: Location) {
                lat = location.latitude
                lon = location.longitude
                Log.e("android_lat", lat.toString())
                Log.e("android_lon", lon.toString())
                //获取天气信息
                getWeather(activity, location)
            }
        }
        // 监听位置变化，2秒一次，距离10米以上
        locationManager.requestLocationUpdates(
            provider, 2000, 10f,
            locationListener
        )
    }

    /**
     * 获取天气控件的所需参数 调用该方法前需要先调用initWeather()方法
     */
    private fun getWeather(activity: LauncherActivity, location: Location) {

        /**
         * 实况天气数据
         * @param location 所查询的地区，可通过该地区名称、ID、IP和经纬度进行查询经纬度格式：经度,纬度
         * （英文,分隔，十进制格式，北纬东经为正，南纬西经为负)
         * @param lang     (选填)多语言，可以不使用该参数，默认为简体中文
         * @param unit     (选填)单位选择，公制（m）或英制（i），默认为公制单位
         * @param listener 网络访问结果回调
         */
        QWeather.getGeoCityLookup(
            activity, location.longitude.toString()
                    + "," + location.latitude,
            object : OnResultGeoListener {
                override fun onError(throwable: Throwable) {

                }

                override fun onSuccess(geoBean: GeoBean) {
                    QWeather.getWeatherNow(
                        activity,
                        geoBean.locationBean[0].id,
                        Lang.ZH_HANS,
                        Unit.METRIC,
                        object : OnResultWeatherNowListener {
                            override fun onError(e: Throwable) {
                                Log.i(TAG, "getWeather onError: $e")
                            }

                            override fun onSuccess(weatherBean: WeatherNowBean) {
                                Log.i(
                                    TAG,
                                    "getWeather onSuccess: " + Gson().toJson(weatherBean)
                                )
                                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                                if (Code.OK == weatherBean.code) {
//                                        val now = weatherBean.now
//                                        tv_city.setText(
//                                            "城市:" + geoBean.locationBean[0].adm1
//                                        )
//                                        tv_tianqi.setText("当前天气:" + weatherBean.now.text)
//                                        tv_kongqi.setText("当前温度:" + weatherBean.now.temp)

                                    activity.onWeather(
                                        geoBean.locationBean[0].adm1,
                                        weatherBean,
                                        getWeatherIcon(weatherBean)
                                    )
                                } else {
                                    //在此查看返回数据失败的原因
                                    val code =
                                        weatherBean.code
                                    Log.i(TAG, "failed code: $code")
                                }
                            }
                        }
                    )
                }
            })
    }

    /**
     *  通过和风天气代码获取对应的图标
     */
    private fun getWeatherIcon(weatherNowBean: WeatherNowBean): Int {
        return when (weatherNowBean.now.icon) {
            "100", "150" -> R.drawable.sunny   //晴
            "101" -> R.drawable.cloudy  //多云
            "102" -> R.drawable.partly_cloudy   //少云
            "103", "153" -> R.drawable.cloudy  //晴间多云
            "104", "154" -> R.drawable.cloudy_day  //阴
            "300", "350" -> R.drawable.shower   //阵雨
            "301", "351" -> R.drawable.heavy_rain   //强阵雨
            "302" -> R.drawable.thundershower   //雷阵雨
            "303" -> R.drawable.thundershower   //强雷阵雨
            "304" -> R.drawable.rain_and_hail   //雷阵雨
            "305", "399" -> R.drawable.moderate_rain   //小雨
            "306" -> R.drawable.heavy_rain      //中雨
            "307" -> R.drawable.heavy_rain      //大雨
            "308" -> R.drawable.heavy_rain      //极端降雨
            "309" -> R.drawable.moderate_rain      //毛毛雨
            "310" -> R.drawable.rainstorm           //暴雨
            "311" -> R.drawable.big_rainstorm           //大暴雨
            "312" -> R.drawable.big_rainstorm           //特大暴雨
            "313" -> R.drawable.big_rainstorm           //冻雨
            "314", "408" -> R.drawable.heavy_rain           //小到中雨
            "315", "409" -> R.drawable.heavy_rain           //中到大雨
            "316", "410" -> R.drawable.rainstorm           //大到暴雨
            "317" -> R.drawable.rainstorm           //暴雨到大暴雨
            "318" -> R.drawable.big_rainstorm       //大暴雨到特大暴雨
            "400" -> R.drawable.light_snow
            "401", "499" -> R.drawable.moderate_snow
            "402" -> R.drawable.heavy_snow
            "403" -> R.drawable.blizzard
            "404", "405", "406", "407", "457" -> R.drawable.snow_shower
            "456" -> R.drawable.sleet   //雨夹雪
            "500", "501", "509", "510", "514", "515" -> R.drawable.fog  //雾
            "502" -> R.drawable.haze
            "503", "504", "507" -> R.drawable.sand_storm
            "508" -> R.drawable.heavy_sandstorm
            "511" -> R.drawable.moderate_haze
            "512" -> R.drawable.heavy_haze
            "513" -> R.drawable.severe_haze
            else -> R.drawable.sunny    //未知天气
        }


    }


    /**
     * 获取系统应用并封装
     */
    private fun getAppData(
        appType: String,
        activity: Activity,
        pageSize: Int = 12
    ): List<List<AppBean>> {
        val datas: MutableList<AppBean> = ArrayList()
        val maps: MutableList<List<AppBean>> = ArrayList()
        var packageManager = activity.packageManager
        var mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        var apps = packageManager.queryIntentActivities(mainIntent, 0)

//        Activity获取方法:resolve.activityInfo.name
//        Activity包名获取方法：resolve.activityInfo.packageName
//        App包名获取方法:resolve.activityInfo.applicationInfo.packageName
        val properties = ProperTiesUtil.getProperties(activity)
        //音视频
        val mediaAppPackageName: String = properties.getProperty(AppConstants.MEDIA_APP)
        //游戏
        val gameAppPackageName: String = properties.getProperty(AppConstants.GAME_APP)
        //教育
        val educationAppPackageName: String = properties.getProperty(AppConstants.EDUCATION_APP)

        val mediaAppPackageNames = mediaAppPackageName.split(",")
        val gameAppPackageNames = gameAppPackageName.split(",")
        val educationAppPackageNames = educationAppPackageName.split(",")
        //获取应用 只有当类型和包名都相同时才去进行添加
        for (position in apps.indices) {
            val resolveInfo = apps[position]
            val packageName = resolveInfo.activityInfo.applicationInfo.packageName
            Log.i(
                TAG,
                "getAppData: ${resolveInfo.loadLabel(packageManager)} packageName${packageName} "
            )
            when {
                appType == AppConstants.ALL_APP && packageName != OLD_AOA_PACKAGE_NAME -> {
                    datas.add(
                        AppBean(
                            resolveInfo.loadLabel(packageManager), packageName,
                            resolveInfo.loadIcon(packageManager)
                        )
                    )
                }
                appType == AppConstants.MEDIA_APP && mediaAppPackageNames.contains(packageName) -> {
                    datas.add(
                        AppBean(
                            resolveInfo.loadLabel(packageManager), packageName,
                            resolveInfo.loadIcon(packageManager)
                        )
                    )
                }
                appType == AppConstants.GAME_APP && gameAppPackageNames.contains(packageName) -> {
                    datas.add(
                        AppBean(
                            resolveInfo.loadLabel(packageManager), packageName,
                            resolveInfo.loadIcon(packageManager)
                        )
                    )
                }
                appType == AppConstants.EDUCATION_APP && educationAppPackageNames.contains(
                    packageName
                ) -> {
                    datas.add(
                        AppBean(
                            resolveInfo.loadLabel(packageManager), packageName,
                            resolveInfo.loadIcon(packageManager)
                        )
                    )
                }
                appType == AppConstants.OTHER_APP && !mediaAppPackageNames.contains(packageName)
                        && !gameAppPackageNames.contains(packageName) && !educationAppPackageNames.contains(
                    packageName
                ) && packageName != AppConstants.AOA_PACKAGE_NAME
                        && packageName != AppConstants.LAUNCHER_PACKAGE_NAME
                -> {
                    datas.add(
                        AppBean(
                            resolveInfo.loadLabel(packageManager), packageName,
                            resolveInfo.loadIcon(packageManager)
                        )
                    )
                }
            }

            Log.i("AppName", "" + resolveInfo.loadLabel(packageManager))
        }
        if (datas.isEmpty()) {
            return maps
        }
        val pageTempSize = datas.size / pageSize
        val pageRemainder = if (datas.size % pageSize != 0 || datas.size < pageSize) 1 else 0
        //实际总页数
        val totalPageSize = pageTempSize + pageRemainder
        var startPage = 0
        var pageItems: List<AppBean>
        //根据实际页数对数据进行封装
        for (pageNumber in 1..totalPageSize) {
            pageItems = if (pageNumber >= totalPageSize) {
                datas.subList(startPage, if (datas.size - 1 < 1) 1 else datas.size)
            } else {
                datas.subList(startPage, pageNumber * pageSize)
            }
            startPage = pageNumber * pageSize
            maps.add(pageItems)
        }
        return maps
    }

    fun showDialog(appType: String) {
        val activity = getView() as Activity
        var dialog = CustomDialog(activity, R.layout.dialog_app_launcher)
        val appBeans: List<List<AppBean>> = getAppData(appType, activity)
        if (appBeans.isEmpty()) {
            Toast.makeText(activity, R.string.no_app, Toast.LENGTH_LONG).show()
            return
        }
        val scrollAdapter =
            HorizontalScrollAdapter(
                activity,
                appBeans
            )

        val viewPager = dialog.findViewById<ViewPager>(R.id.horizontalScrollView)
        val circlePageIndicator = dialog.findViewById<CirclePageIndicator>(R.id.circleIndicator)
        viewPager.adapter = scrollAdapter
        circlePageIndicator.setViewPager(viewPager)
        dialog.show();
    }


    /**
     * 打开系统设置
     */
    fun showSystemSetting() {
        val activity = getView() as Activity
        val intent = Intent(Settings.ACTION_SETTINGS)
        activity.startActivity(intent)
    }

    /**
     * 打开AdbWifi
     */
    fun showAdbWifi() {
        val activity = getView() as Activity
        StartAppUtils.startApp(activity, AppConstants.ADB_WIFI_PACKAGE_NAME)
    }

    /**
     * 打开酷安市场
     */
    fun showKAMarket() {
        var activity = getView() as Activity
        try {
            var intent =
                activity.packageManager.getLaunchIntentForPackage(AppConstants.KA_PACKAGE_NAME)
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e("showAZMarket", "没有安装")
        }

    }

    /**
     * 打开AOA星仔办学
     */
    fun showAOA() {
        var activity = getView() as Activity
        try {
            var intent =
                activity.packageManager.getLaunchIntentForPackage(AppConstants.AOA_PACKAGE_NAME)
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e("showAOA", "没有安装")
        }

    }

    fun updateSystem(activity: Activity, updateBean: UpdateBean) {
        var localSystemVersionName = Build.DISPLAY
        var systemApp: UpdateBeanData? = null //系统固件
        for (position in updateBean.data.indices) {
            when (updateBean.data[position].app_name) {
                //系统升级
                "system" -> systemApp = updateBean.data[position]
            }
        }
        //系统版本名不同表示有升级
        if (systemApp?.version_name!! != localSystemVersionName && systemApp.app_force_upgrade == 1) {//pp_force_upgrade == 1表示强更
            var notificationManager =
                activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val builder: NotificationCompat.Builder = NotificationCompat.Builder(activity)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentInfo("下载中...")
                .setContentTitle("系统下载中")
            val notification = builder.build()
            notification.flags = notification.flags or FLAG_NO_CLEAR

            builder.setProgress(100, 0, true)
            notificationManager.notify(0x3, notification)
            //系统固件升级包下载
            DownloadUtil.download(
                systemApp.app_url,
                SYSTEM_ZIP_FULL_PATH,
                object : DownloadListener {

                    var oldProgress = 0
                    override fun onStart() {
                        //运行在子线程
                    }

                    override fun onProgress(progress: Int) {
                        if (oldProgress != progress) {
                            Runnable {
                                builder.setProgress(100, progress, false)
                            }
                            notificationManager.notify(0x3, builder.build())
                            //运行在子线程
                            Log.i("TAG", "onProgress: $progress")
                        }

                        oldProgress = progress
                    }

                    override fun onFinish(path: String?) {
                        builder.setContentTitle("下载完成")
                            .setContentInfo("下载完成")
                        Log.i("TAG", "onProgress: 下载完成，尝试提示安装")
                        //运行在子线程
                        val intent = Intent()
                        intent.component = ComponentName(
                            "android.rockchip.update.service",
                            "android.rockchip.update.service.FirmwareUpdatingActivity"
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(
                            EXTRA_IMAGE_PATH,
                            SYSTEM_ZIP_FULL_PATH
                        )
                        activity.startActivity(intent)
                    }

                    override fun onFail(errorInfo: String?) {
                        //运行在子线程
                    }
                })
        }
        //XUpdate 更新
        /*    EasyUpdate.create(launcherActivity, Urls.BASEURL + Urls.UPDATE)
                .updateHttpService(AriaDownloader.getUpdateHttpService(launcherActivity))
                .updateParser(CustomUpdateParser(launcherActivity))
                .update()*/
    }

    fun setPersonInfo(activity: Activity) {
        queryUserInfo(activity)
    }

    private fun queryUserInfo(activity: Activity) {
        val boyCursor = activity.contentResolver.query(
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
                getView()?.onSuccess(
                    TokenPair(
                        boyCursor.getInt(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID)),
                        boyCursor.getString(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN)),
                        boyCursor.getDouble(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME)),
                        boyCursor.getInt(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE)),
                        boyCursor.getInt(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER)),
                        boyCursor.getString(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR)),
                        boyCursor.getString(boyCursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_NAME))
                    )
                )
            }
            boyCursor.close()
        }
    }

    /**
     * @param isEnable true表示可用 false表示禁用
     * 发送菜单禁用的广播
     */
    fun sendMenuEnableBroadcast(context: Context, isEnable: Boolean) {
        val intent = Intent()
        intent.action = "com.alight.android.menu"
        intent.putExtra("state", isEnable);
        context.sendBroadcast(intent)
    }

    /**
     * 开机固件升级检测
     */
    fun splashStartUpdateActivity(
        isNewUser: Boolean, any: UpdateBean,
        activity: Activity
    ) {
        //系统应用
        var systemAppList: ArrayList<UpdateBeanData> = arrayListOf()
        //预置应用
        var otherAppList: ArrayList<UpdateBeanData> = arrayListOf()
        var systemApp: UpdateBeanData? = null
        if (any.data == null) return
        for (position in any.data.indices) {
            val updateBeanData = any.data[position]
            //把ota单独抽出放到最后
            if (updateBeanData.format == 3) {
                systemApp = updateBeanData
                continue
            }
            if (updateBeanData.app_info.type == 1) {
                systemAppList.add(updateBeanData)
            } else if (updateBeanData.app_info.type == 2) {
                otherAppList.add(updateBeanData)
            }
        }

        if (systemApp?.app_info?.type == 1) {
            systemAppList.add(systemApp)
        } else if (systemApp?.app_info?.type == 2) {
            otherAppList.add(systemApp)
        }
        //newSplash true表示使用新用户第一次开启ota升级
//        val newSplash = SPUtils.getData("new_splash", true) as Boolean
        var intent = Intent(activity, UpdateActivity::class.java)
        intent.putExtra("systemApp", systemAppList)
        intent.putExtra("otherApp", otherAppList)
        intent.putExtra("new_splash", isNewUser)
        var newOtaVersionName = ""
        systemAppList.forEach {
            if (it.format == 3) {
                newOtaVersionName = it.version_name
            }
        }
        var isHaveSystemUpdate = false;
        val configVersion = SPUtils.getData(
            "configVersion",
            1
        ) as Int
        for (i in systemAppList.indices) {
            val systemApp = systemAppList[i]
            //资源包
            if (systemApp.format == 1) {
                if (configVersion < systemApp.version_code)
                    isHaveSystemUpdate = true
                break
            } else if (AppUtils.getVersionCode(
                    activity,
                    systemApp.app_info.package_name
                ) < systemApp.version_code
            ) {
                isHaveSystemUpdate = true
                break
            }
        }

        val localSystemVersionName = Build.DISPLAY
        val mmkv = LauncherApplication.getMMKV()

        //强更标记
        var isStartOtaUpdate = mmkv.getBoolean("isStartOtaUpdate", false)
        if (newOtaVersionName.isNotEmpty() && !localSystemVersionName.equals(newOtaVersionName)) {
            intent.putExtra("source", "splash")
            activity.startActivity(intent)
        } else if (isStartOtaUpdate && isHaveSystemUpdate) {    //有强更新标记并且包含系统更新
            intent.putExtra("source", "splash")
            activity.startActivity(intent)
        } else if (isHaveSystemUpdate) {    //仅有系统更新
            intent.putExtra("source", "onlySystemUpdate")
            activity.startActivity(intent)
        } else {
            EventBus.getDefault().post(SplashEvent.getInstance(true))
        }

    }

    fun getPkgList(): List<String> {
        val packages: MutableList<String> = ArrayList()
        try {
            val p = Runtime.getRuntime().exec("pm list packages")
            val isr = InputStreamReader(p.inputStream, "utf-8")
            val br = BufferedReader(isr)
            var line: String = br.readLine()
            while (line != null) {
                line = line.trim { it <= ' ' }
                if (line.length > 8) {
                    val prefix = line.substring(0, 8)
                    if (prefix.equals("package:", ignoreCase = true)) {
                        line = line.substring(8).trim { it <= ' ' }
                        if (!TextUtils.isEmpty(line)) {
                            packages.add(line)
                        }
                    }
                }
                line = br.readLine()
            }
            br.close()
            p.destroy()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return packages
    }

    fun getAllAppSize(context: Context): Int {
        var queryIntentActivities = mutableListOf<ResolveInfo>()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            queryIntentActivities =
                context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        } else {
            queryIntentActivities = context.packageManager.queryIntentActivities(intent, 0)
        }
        return queryIntentActivities.size
    }

    fun showUpdateActivity(
        any: UpdateBean,
        activity: Activity
    ) {
        if (activity == null || activity.isDestroyed) {
            return
        }
        //系统应用
        var systemAppList: ArrayList<UpdateBeanData> = arrayListOf()
        //预置应用
        var otherAppList: ArrayList<UpdateBeanData> = arrayListOf()
        var systemApp: UpdateBeanData? = null
        if (any.data == null) return
        for (position in any.data.indices) {
            val updateBeanData = any.data[position]
            //把ota单独抽出放到最后
            if (updateBeanData.format == 3) {
                systemApp = updateBeanData
                continue
            }
            if (updateBeanData.app_info.type == 1) {
                systemAppList.add(updateBeanData)
            } else if (updateBeanData.app_info.type == 2) {
                otherAppList.add(updateBeanData)
            }
        }

        if (systemApp?.app_info?.type == 1) {
            systemAppList.add(systemApp)
        } else if (systemApp?.app_info?.type == 2) {
            otherAppList.add(systemApp)
        }
        var intent = Intent(activity, UpdateActivity::class.java)
        intent.putExtra("systemApp", systemAppList)
        intent.putExtra("otherApp", otherAppList)
        activity.startActivity(intent)
    }

    fun getIcon(context: Context, packName: String): Drawable? {
        val pm: PackageManager = context.packageManager
        try {
            var appInfo = pm.getApplicationInfo(packName, PackageManager.GET_META_DATA)
            // 应用名称
            // pm.getApplicationLabel(appInfo)

            //应用图标
            return pm.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun showUnbindDeviceDialog(
        familyId: Int,
        activity: Activity
    ) {
        //解绑二次确认弹窗
        val confirmDialog = ConfirmDialog(activity)
        confirmDialog.setOnItemClickListener(object :
            ConfirmDialog.OnItemClickListener {
            //点击确认
            override fun onConfirmClick() {
                deleteModel(
                    RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        mapOf(
                            "family_id" to familyId,
                            "dsn" to AccountUtil.getDSN()
                        ).toJson()
                    ),
                    DeviceRelationBean::class.java
                )
                confirmDialog.dismiss()
            }
        })
        confirmDialog.show()
    }


    fun showUpdateDialog(
        any: UpdateBean,
        familyId: Int,
        activity: Activity
    ) {
        if (activity == null || activity.isDestroyed) {
            return
        }
        //系统升级和解绑
        val updateDialog =
            CustomDialog(activity, R.layout.dialog_update_new)
        val close = updateDialog.findViewById<ImageView>(R.id.iv_close_update)
        val unbind = updateDialog.findViewById<TextView>(R.id.tv_unbind_dialog)
        val update = updateDialog.findViewById<TextView>(R.id.tv_update_dialog)

//        var systemApp: UpdateBeanData? = null //系统固件
//        var launcherApp: UpdateBeanData? = null   //launcher
//        var aoaApp: UpdateBeanData? = null    //aoa
//        var hardwareApp: UpdateBeanData? = null   //硬件
//        var avApp: UpdateBeanData? = null   //音视频

        //系统应用
        var systemAppList: ArrayList<UpdateBeanData> = arrayListOf()
        //预置应用
        var otherAppList: ArrayList<UpdateBeanData> = arrayListOf()
        var systemApp: UpdateBeanData? = null
        if (any.data == null) return
        for (position in any.data.indices) {
            val updateBeanData = any.data[position]
            //把ota单独抽出放到最后
            if (updateBeanData.format == 3) {
                systemApp = updateBeanData
                continue
            }
            if (updateBeanData.app_info.type == 1) {
                systemAppList.add(updateBeanData)
            } else if (updateBeanData.app_info.type == 2) {
                otherAppList.add(updateBeanData)
            }
        }

        if (systemApp?.app_info?.type == 1) {
            systemAppList.add(systemApp)
        } else if (systemApp?.app_info?.type == 2) {
            otherAppList.add(systemApp)
        }

        /* for (position in any.data.indices) {
             when (any.data[position].app_name) {
                 //系统升级
                 "system" -> systemApp = any.data[position]
                 "launcher" -> launcherApp = any.data[position]
                 "aoa" -> aoaApp = any.data[position]
                 "ahwc" -> hardwareApp = any.data[position]
                 "av" -> avApp = any.data[position]
             }
         }*/

        update.setOnClickListener {
            var intent = Intent(activity, UpdateActivity::class.java)
            intent.putExtra("systemApp", systemAppList)
            intent.putExtra("otherApp", otherAppList)
            activity.startActivity(intent)
            updateDialog.dismiss()
        }
        try {
            updateDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        unbind.setOnClickListener {
            //解绑二次确认弹窗
            val confirmDialog = ConfirmDialog(activity)
            confirmDialog.setOnItemClickListener(object :
                ConfirmDialog.OnItemClickListener {
                //点击确认
                override fun onConfirmClick() {
                    deleteModel(
                        RequestBody.create(
                            MediaType.get("application/json; charset=utf-8"),
                            mapOf(
                                "family_id" to familyId,
                                "dsn" to AccountUtil.getDSN()
                            ).toJson()
                        ),
                        DeviceRelationBean::class.java
                    )
                    confirmDialog.dismiss()
                }
            })
            confirmDialog.show()
        }
        close.setOnClickListener {
            updateDialog.dismiss()
        }
    }

    fun startApp(context: Context, appPackName: String) {
        try {
            val mmkv = LauncherApplication.getMMKV()
            val playTimeJson = mmkv.decodeString(AppConstants.PLAY_TIME)
            val playTimeBean = Gson().fromJson(playTimeJson, PlayTimeBean::class.java)

            var calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault();//默认当前时区
            var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
            var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
            var sysTime = "$hour:" + if (minute >= 10) minute else "0$minute"
            var startTime = playTimeBean.data.playtime.start_playtime
            var endTime = playTimeBean.data.playtime.stop_playtime

            for (it in playTimeBean.data.app_manage) {
                if (it.app_info.package_name.isNotEmpty() && appPackName == it.app_info.package_name
                ) {
                    if ((it.app_permission == 3)) {
                        ToastUtils.showLong(context, "该应用已被禁用")
                        return
                    } else if (it.app_permission == 2 && !TimeUtils.inTimeInterval(
                            startTime,
                            endTime,
                            sysTime
                        )
                    ) {
                        //限时禁用
                        ToastUtils.showLong(context, "该应用已被限时禁用")
                        return
                    }
                    break
                } else continue
            }

            val intent = context.packageManager.getLaunchIntentForPackage(appPackName)
            context.startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.showShort(context, "该应用缺失，请安装后重试")
            e.printStackTrace()
        }
    }

    fun startActivity(
        context: Context,
        packName: String,
        className: String,
        params: Map<String, Any>?
    ): Boolean {
        try {
            val mmkv = LauncherApplication.getMMKV()
            val playTimeJson = mmkv.decodeString(AppConstants.PLAY_TIME)
            val playTimeBean = Gson().fromJson(playTimeJson, PlayTimeBean::class.java)

            var calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault();//默认当前时区
            var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
            var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
            var sysTime = "$hour:" + if (minute >= 10) minute else "0$minute"
            var startTime = playTimeBean.data.playtime.start_playtime
            var endTime = playTimeBean.data.playtime.stop_playtime

            playTimeBean.data.app_manage.forEach {
                if (packName == it.app_info.package_name && (className == it.class_name || className.isEmpty()) && (params == null || params.isEmpty() || params.values.indexOf(
                        it.args
                    ) != -1)
                ) {
                    if ((it.app_permission == 3)) {
                        GlobalScope.launch(Dispatchers.Main) {
                            ToastUtils.showLong(context, "该应用已被禁用")
                        }
                        return false
                    } else if (it.app_permission == 2 && !TimeUtils.inTimeInterval(
                            startTime,
                            endTime,
                            sysTime
                        )
                    ) {
                        GlobalScope.launch(Dispatchers.Main) {
                            //限时禁用
                            ToastUtils.showLong(context, "该应用已被限时禁用")
                        }
                        return false
                    }
                    return@forEach
                }
            }
            //只带包名的开启方式
            if ((params == null || params.isEmpty()) && className.isEmpty()) {
                val intent = context.packageManager.getLaunchIntentForPackage(packName)
                context.startActivity(intent)
            } else {
                //通过包名和类名的开启方式
                val intent = Intent()
                val componentName =
                    ComponentName(packName, className)
                params?.forEach {
                    when (it.value) {
                        is String -> {
                            intent.putExtra(it.key, it.value.toString())
                        }
                        is Boolean -> {
                            intent.putExtra(it.key, it.value as? Boolean)
                        }
                        is Int -> {
                            intent.putExtra(it.key, it.value as? Int)
                        }
                    }
                }
                intent.component = componentName
                context.startActivity(intent)
            }
            return true
        } catch (e: java.lang.Exception) {
            GlobalScope.launch(Dispatchers.Main) {
                ToastUtils.showLong(context, "该应用正在开发中，敬请期待！")
            }
            e.printStackTrace()
            return false
        }
    }

    /**
     * 带有动画的切换触控模式
     * @param interactiveMode
     *  PEN_POINT 交互笔点读模式
     *  PEN_RECT 交互笔划选区模式
     *  FINGER_TOUCH 手触控模式
     *  UNKOWN 未知状态
     */
    fun startInteractionWindow(
        ability: InteractionAbility,
        interactiveMode: InteractionAbility.InteractiveMode
    ) {
        try {
            ability.setInteractiveMode(interactiveMode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 不带动画的切换触控模式
     */
    fun startInteractionWindowNoAnim(
        ability: PanelAbility,
        touchMode: PanelAbility.TouchMode
    ) {
        try {
            ability.setTouchMode(touchMode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAVDialog(context: Context, interactionAbility: InteractionAbility?) {
        //音视频dialog
        val avDialog =
            CustomDialog(context, R.layout.dialog_home_av)
        avDialog.window?.setGravity(Gravity.START or Gravity.TOP)
        val llHomeAvBg = avDialog.findViewById<LinearLayout>(R.id.ll_home_av_bg)
        val llAv = avDialog.findViewById<LinearLayout>(R.id.ll_av_select_dialog)
        val ar = avDialog.findViewById<ImageView>(R.id.iv_ar_dialog)
        val ivAudio = avDialog.findViewById<ImageView>(R.id.iv_audio_dialog)
        val ivVideo = avDialog.findViewById<ImageView>(R.id.iv_video_dialog)
        val ivClose = avDialog.findViewById<ImageView>(R.id.iv_close_dialog)
        val coroutineScopeIo = CoroutineScope(Dispatchers.IO)
        llHomeAvBg.setOnClickListener {
            avDialog.dismiss()
        }
        ivClose.setOnClickListener {
            avDialog.dismiss()
        }
        ar.setOnClickListener {
            avDialog.dismiss()
            val startAoaApp = startAoaApp(context, 36, "/mine")
            if (startAoaApp && interactionAbility != null) {
                startInteractionWindow(
                    interactionAbility,
                    InteractionAbility.InteractiveMode.PEN_POINT
                )
            }
        }
        ivAudio.setOnClickListener {
            coroutineScopeIo.cancel()
            showAvParentInfoDialog(context, avDialog, "audio")
            YoYo.with(Techniques.FadeOutDown)
                .duration(700)
                .playOn(llAv)

        }
        ivVideo.setOnClickListener {
            coroutineScopeIo.cancel()
            showAvParentInfoDialog(context, avDialog, "video")
            YoYo.with(Techniques.FadeOutDown)
                .duration(700)
                .playOn(llAv)
        }
        avDialog.show()
        avDialog.setOnDismissListener {
            //dialog关闭时同步销毁协程
            coroutineScopeIo.cancel()
        }
        //6s内不做操作自动关闭
        coroutineScopeIo.launch {
            delay(6000)
            avDialog.dismiss()
        }
    }


    fun showChildAVDialog(context: Context) {
        //音视频dialog
        val avDialog =
            CustomDialog(context, R.layout.dialog_av_child_launcher)
        avDialog.window?.setGravity(Gravity.START or Gravity.TOP)
        val llHomeAvBg = avDialog.findViewById<LinearLayout>(R.id.ll_home_av_bg)
        val llAv = avDialog.findViewById<LinearLayout>(R.id.ll_av_select_dialog)
        val ivAudio = avDialog.findViewById<ImageView>(R.id.iv_audio_dialog)
        val ivVideo = avDialog.findViewById<ImageView>(R.id.iv_video_dialog)
        val ivClose = avDialog.findViewById<ImageView>(R.id.iv_close_dialog)
        val coroutineScopeIo = CoroutineScope(Dispatchers.IO)
        llHomeAvBg.setOnClickListener {
            avDialog.dismiss()
        }
        ivClose.setOnClickListener {
            avDialog.dismiss()
        }
        ivAudio.setOnClickListener {
            coroutineScopeIo.cancel()
            showChildAvParentInfoDialog(context, avDialog, "audio")
            YoYo.with(Techniques.FadeOutDown)
                .duration(700)
                .playOn(llAv)
        }
        ivVideo.setOnClickListener {
            coroutineScopeIo.cancel()
            showChildAvParentInfoDialog(context, avDialog, "video")
            YoYo.with(Techniques.FadeOutDown)
                .duration(700)
                .playOn(llAv)
        }
        avDialog.show()
        avDialog.setOnDismissListener {
            //dialog关闭时同步销毁协程
            coroutineScopeIo.cancel()
        }
        //6s内不做操作自动关闭
        coroutineScopeIo.launch {
            delay(6000)
            avDialog.dismiss()
        }
    }

    private fun showChildAvParentInfoDialog(
        context: Context,
        avDialog: CustomDialog,
        callType: String
    ) {
        val llAvSelect = avDialog.findViewById<LinearLayout>(R.id.ll_av_select_dialog)
        val llParentInfo = avDialog.findViewById<LinearLayout>(R.id.ll_parent_info)
        val tvCallType = avDialog.findViewById<TextView>(R.id.tv_call_type)
        tvCallType.text = if (callType == "video") "视频通话" else "语音通话"
        var familyInfoBean: FamilyInfoBean? = null
        var splitFamilyList: List<List<Parent>>?

        //计时器
        val mhandle = Handler()
        var currentSecond: Long = 0 //当前毫秒数
        val timeRunable: Runnable = object : Runnable {
            override fun run() {
                currentSecond = currentSecond + 1000
                Log.i(TAG, "计时器: currentSecond = $currentSecond")
                //递归调用本runable对象，实现每隔一秒一次执行任务
                if (currentSecond >= 6000) {
                    currentSecond = 0
                    avDialog.dismiss()
                } else {
                    mhandle.postDelayed(this, 1000)
                }
            }
        }
        NetUtils.intance.getInfo(Urls.FAMILY_INFO,
            HashMap(),
            FamilyInfoBean::class.java, object : NetUtils.NetCallback {
                override fun onSuccess(any: Any) {
                    if (any is FamilyInfoBean) {
                        familyInfoBean = any
                    }
                    if (familyInfoBean != null && !familyInfoBean?.data?.parents!!.isNullOrEmpty()) {
                        splitFamilyList =
                            ListSplitUtil.splitList(
                                familyInfoBean?.data?.parents,
                                4
                            ) as List<List<Parent>>
                        val scrollAdapter =
                            HorizontalChildParentAdapter(
                                context,
                                splitFamilyList, callType, avDialog
                            )
                        timeRunable.run()
                        val viewPager = avDialog.findViewById<ViewPager>(R.id.horizontalScrollView)
                        val circlePageIndicator =
                            avDialog.findViewById<CirclePageIndicator>(R.id.circleIndicator)
                        viewPager.adapter = scrollAdapter
                        viewPager.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                            if (currentSecond >= 0) {
                                currentSecond = 0
                            }
                        }
                        circlePageIndicator.setViewPager(viewPager)
                        val coroutineScope = CoroutineScope(Dispatchers.Main)
                        coroutineScope.launch {
                            llAvSelect.visibility = View.GONE
                            llParentInfo.visibility = View.VISIBLE
                            coroutineScope.cancel()

                            YoYo.with(Techniques.FadeInUp)
                                .duration(700)
                                .playOn(llParentInfo)
                        }
                    } else {
                        ToastUtils.showShort(context, "您暂时还未设置家长")
                    }
                }

                override fun onError(error: String) {
                    ToastUtils.showShort(context, "没有获取到家庭信息")
                }
            })
    }

    private fun showAvParentInfoDialog(
        context: Context,
        avDialog: CustomDialog,
        callType: String
    ) {
        val llAvSelect = avDialog.findViewById<LinearLayout>(R.id.ll_av_select_dialog)
        val llParentInfo = avDialog.findViewById<LinearLayout>(R.id.ll_parent_info)
        val tvCallType = avDialog.findViewById<TextView>(R.id.tv_call_type)
        tvCallType.text = if (callType == "video") "视频通话" else "语音通话"
        var familyInfoBean: FamilyInfoBean? = null
        var splitFamilyList: List<List<Parent>>?

        //计时器
        val mhandle = Handler()
        var currentSecond: Long = 0 //当前毫秒数
        val timeRunable: Runnable = object : Runnable {
            override fun run() {
                currentSecond = currentSecond + 1000
                Log.i(TAG, "计时器: currentSecond = $currentSecond")
                //递归调用本runable对象，实现每隔一秒一次执行任务
                if (currentSecond >= 6000) {
                    currentSecond = 0
                    avDialog.dismiss()
                } else {
                    mhandle.postDelayed(this, 1000)
                }
            }
        }
        NetUtils.intance.getInfo(Urls.FAMILY_INFO,
            HashMap(),
            FamilyInfoBean::class.java, object : NetUtils.NetCallback {
                override fun onSuccess(any: Any) {
                    if (any is FamilyInfoBean) {
                        familyInfoBean = any
                    }
                    if (familyInfoBean != null && !familyInfoBean?.data?.parents!!.isNullOrEmpty()) {
                        splitFamilyList =
                            ListSplitUtil.splitList(
                                familyInfoBean?.data?.parents,
                                12
                            ) as List<List<Parent>>
                        val scrollAdapter =
                            HorizontalParentAdapter(
                                context,
                                splitFamilyList, callType, avDialog
                            )
                        timeRunable.run()
                        val viewPager = avDialog.findViewById<ViewPager>(R.id.horizontalScrollView)
                        val circlePageIndicator =
                            avDialog.findViewById<CirclePageIndicator>(R.id.circleIndicator)
                        viewPager.adapter = scrollAdapter
                        viewPager.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                            if (currentSecond >= 0) {
                                currentSecond = 0
                            }
                        }
                        circlePageIndicator.setViewPager(viewPager)
                        val coroutineScope = CoroutineScope(Dispatchers.Main)
                        coroutineScope.launch {
                            llAvSelect.visibility = View.GONE
                            llParentInfo.visibility = View.VISIBLE
                            coroutineScope.cancel()

                            YoYo.with(Techniques.FadeInUp)
                                .duration(700)
                                .playOn(llParentInfo)
                        }
                    } else {
                        ToastUtils.showShort(context, "您暂时还未设置家长")
                    }
                }

                override fun onError(error: String) {
                    ToastUtils.showShort(context, "没有获取到家庭信息")
                }
            })
    }

    fun startAoaApp(context: Context, appId: Int, route: String): Boolean {
        try {
            val mmkv = LauncherApplication.getMMKV()
            val playTimeJson = mmkv.decodeString(AppConstants.PLAY_TIME)
            val playTimeBean = Gson().fromJson(playTimeJson, PlayTimeBean::class.java)
            playTimeBean.data.ar_manage.forEach {
                if (it.aoa_id == appId && !it.app_ar_permission) {
                    GlobalScope.launch(Dispatchers.Main) {
                        ToastUtils.showLong(context, "该AR学应用已被禁用")
                    }
                    return false
                }
            }
            var intent = Intent("com.alight.android.aoax.entry")
            intent.putExtra("action", "aos.app.open")
            intent.putExtra("appId", appId)
            intent.putExtra("route", route)
            intent.putExtra("params", "{}")
            context.startActivity(intent)
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 处理弹出显示内容、点击事件等逻辑
     * @param contentView
     */
    private fun handleLogic(activity: Activity, contentView: View, tv_dialog_launcher: TextView) {
        var gradeDialogAdapter: GradeDialogAdapter? = null
        var preschoolList = arrayListOf("早教", "小班", "中班", "大班", "学龄前")
        var primarySchoolList = arrayListOf("一年级", "二年级", "三年级", "四年级", "五年级", "六年级")
        var juniorList = arrayListOf("初中", "高中", "其他")
        val rvGrade = contentView.findViewById<RecyclerView>(R.id.rv_grade_launcher)
        val listener = View.OnClickListener {
            if (mCustomPopWindow != null) {
                rvGrade.visibility = View.VISIBLE
            }
            when (it.id) {
                R.id.tv_preschool_dialog -> {
                    gradeDialogAdapter?.setNewInstance(preschoolList)
                }
                R.id.tv_primary_school_dialog -> {
                    gradeDialogAdapter?.setNewInstance(primarySchoolList)
                }
                R.id.tv_junior_dialog -> {
                    gradeDialogAdapter?.setNewInstance(juniorList)
                }
            }
        }
        contentView.findViewById<TextView>(R.id.tv_preschool_dialog).setOnClickListener(listener)
        contentView.findViewById<TextView>(R.id.tv_primary_school_dialog)
            .setOnClickListener(listener)
        contentView.findViewById<TextView>(R.id.tv_junior_dialog).setOnClickListener(listener)

        if (gradeDialogAdapter == null) {
            gradeDialogAdapter = GradeDialogAdapter()
            rvGrade.layoutManager = LinearLayoutManager(activity)
            rvGrade.adapter = gradeDialogAdapter
            gradeDialogAdapter.setOnItemClickListener { adapter, view, position ->
                UserDBUtil.isLocalChanged = true
                mCustomPopWindow?.dissmiss()
                UserDBUtil.CURRENT_GRADE = adapter.data[position].toString()
                tv_dialog_launcher.text = "${UserDBUtil.CURRENT_GRADE}      ▼"
                UserDBUtil.CURRENT_GRADE_ADD_TRIANGLE = tv_dialog_launcher.text.toString()
                val mmkv = LauncherApplication.getMMKV()
                mmkv.encode(
                    AppConstants.AOA_LAUNCHER_USER_INFO_LOCAL_GRADE_TYPE,
                    GradeUtil.getCurrentGradeInt(UserDBUtil.CURRENT_GRADE)!!
                )
                //记录选择的年级
                if (gradeDialogAdapter.data == primarySchoolList) {
                    UserDBUtil.keepLastRecord("小学", UserDBUtil.CURRENT_GRADE, -1, -1, "", null)
                    //六年级以上按六年级逻辑
                } else if (gradeDialogAdapter.data == juniorList) {
                    UserDBUtil.keepLastRecord("小学", "六年级", -1, -1, "", null)
                    //学龄前以上按一年级逻辑
                } else {
                    UserDBUtil.keepLastRecord("小学", "一年级", -1, -1, "", null)
                }
            }
        }
    }

    fun showSelectGradeDialog(activity: Activity, tv_dialog_launcher: TextView) {
        val contentView: View = LayoutInflater.from(activity).inflate(R.layout.pop_menu, null)
        //处理popWindow 显示内容
        handleLogic(activity, contentView, tv_dialog_launcher)
        //创建并显示popWindow
        mCustomPopWindow = CustomPopWindow.PopupWindowBuilder(activity)
            .setView(contentView)
            .setOnDissmissListener {
                tv_dialog_launcher.text = UserDBUtil.CURRENT_GRADE_ADD_TRIANGLE
                tv_dialog_launcher.setBackgroundResource(R.drawable.white_bg_oval)
                UserDBUtil.CURRENT_GRADE_ADD_TRIANGLE = tv_dialog_launcher.text.toString()
            }
            .create()
            .showAsDropDown(tv_dialog_launcher, 0, 0)
        tv_dialog_launcher.text = "${UserDBUtil.CURRENT_GRADE}      ▲"
        tv_dialog_launcher.setBackgroundResource(R.drawable.launcher_dialog_top)
    }

    fun showAppSetting(context: Context) {
        // 跳转应用程序列表界面
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
        context.startActivity(intent)
    }

    fun showStorage(context: Context) {
        // 跳转 存储设置 【记忆卡存储】
        val intent = Intent(Settings.ACTION_MEMORY_CARD_SETTINGS)
        context.startActivity(intent)
    }

    fun showWifiSetting(context: Context) {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
    }

    fun getWifiSsid(context: Context): String {

        var ssid = ""

        var connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo?.isConnected!!) {
            var wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            var connectionInfo = wifiManager.connectionInfo;

            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {

                ssid = connectionInfo.ssid;

            }

        }

        return ssid;

    }

    fun screenOff() {
        //在需要调用息屏的逻辑调用代码
        val root = AndroidRootUtils.checkDeviceRoot()
        if (root) {
            AndroidRootUtils.execRootCmd("input keyevent 223");
        }
    }

//息屏
/*
    fun closeScreen(context: Context) {
        var policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        ComponentName adminReceiver = new ComponentName(
            AppIdleActivity.this,
            ScreenOffAdminReceiver.class);
        boolean admin = policyManager . isAdminActive (adminReceiver);
        if (admin) {
            isScreenOn = false;
            policyManager.lockNow();
        } else {
            Toast.makeText(
                this, "没有设备管理权限",
                Toast.LENGTH_LONG
            ).show();
        }

        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
//            pm.goToSleep(SystemClock.uptimeMillis());
            val wakeLock: WakeLock = pm?.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG")!!
//            wakeLock.acquire()
//            wakeLock.release()
//            wakeLock.release();
//            wakeLock.reenableKeyguard();
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }
*/

    //关机
    fun shutdown(context: Context) {
        val intent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false)
        //其中false换成true,会弹出是否关机的确认窗口
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    //关机
    fun reboot(context: Context) {
        var intent = Intent(Intent.ACTION_REBOOT)
        intent.putExtra("nowait", 1)
        intent.putExtra("interval", 1)
        intent.putExtra("window", 0)
        context.sendBroadcast(intent)
    }

    fun setInitGrade(gradeType: Int, tv_grade_person_center: TextView) {
        val currentGrade = GradeUtil.getCurrentGrade(gradeType)
        if (currentGrade != null) {
            UserDBUtil.CURRENT_GRADE = currentGrade
        }
        val gradeString = "$currentGrade      ▼"
        tv_grade_person_center.text = gradeString
        UserDBUtil.CURRENT_GRADE_ADD_TRIANGLE = gradeString
        when {
            gradeType in 6..11 -> {
                UserDBUtil.keepLastRecord("小学", UserDBUtil.CURRENT_GRADE, -1, -1, "", null)
                //六年级以上按六年级逻辑
            }
            gradeType < 6 -> {
                UserDBUtil.keepLastRecord("小学", "一年级", -1, -1, "", null)
                //学龄前以上按一年级逻辑
            }
            else -> {
                UserDBUtil.keepLastRecord("小学", "六年级", -1, -1, "", null)
            }
        }
    }

    fun setInitGrade(gradeType: Int) {
        val currentGrade = GradeUtil.getCurrentGrade(gradeType)
        when {
            gradeType in 6..11 -> {
                UserDBUtil.keepLastRecord("小学", currentGrade, -1, -1, "", null)
                //六年级以上按六年级逻辑
            }
            gradeType < 6 -> {
                UserDBUtil.keepLastRecord("小学", "一年级", -1, -1, "", null)
                //学龄前以上按一年级逻辑
            }
            else -> {
                UserDBUtil.keepLastRecord("小学", "六年级", -1, -1, "", null)
            }
        }
    }

    /**
     * @param startWifi true wifi默认被开启
     */
    fun startWifiModule(startWifi: Boolean) {
        val activity = getView() as Activity
        val intent = Intent(activity, WifiActivity::class.java)
        intent.putExtra("startWifi", startWifi)
        activity.startActivity(intent)
    }

    fun getCurrentWifiDrawable(context: Context): Int {
        val networkAvalible = InternetUtil.isNetworkAvalible(context)
        if (!networkAvalible) return R.drawable.wifi_no_connect
        val wifi = WifiUtil.getCurrentNetworkRssi(context)
        return if (wifi > -70 && wifi < 0) {//最强
            R.drawable.wifi_connect_big
        } else if (wifi > -80 && wifi <= -70) {//较强
            R.drawable.wifi_connect_middle
        } else if (wifi > -95 && wifi <= -80) {//较弱
            R.drawable.wifi_connect_small
        } else {
            //网络极弱
            R.drawable.wifi_connect_minimum
        }
    }

    fun getCurrentWifiPersonDrawable(context: Context): Int {
        val networkAvalible = InternetUtil.isNetworkAvalible(context)
        if (!networkAvalible) return R.drawable.wifi_not_connected
        val wifi = WifiUtil.getCurrentNetworkRssi(context)
        return if (wifi > -70 && wifi < 0) {//最强
            R.drawable.wifi_connect_person_big
        } else if (wifi > -80 && wifi <= -70) {//较强
            R.drawable.wifi_connect_person_middle
        } else if (wifi > -95 && wifi <= -80) {//较弱
            R.drawable.wifi_connect_person_small
        } else {
            //网络极弱
            R.drawable.wifi_connect_person_minimum
        }
    }

    /**
     * 重置为谷歌输入法
     */
    fun resetInputType(activity: Activity) {
        InputMethodUtil.setDefaultInputMethod(activity)
        /*  var imm = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
          val list = imm.inputMethodList
          //        var inputTypeId = "com.android.inputmethod.latin/.LatinIME"
          //        var inputTypeId = "jp.co.omronsoft.openwnn/.OpenWnnJAJP"
          var inputTypeId =
              "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME"
          for (index in list.indices) {
              Log.i(TAG, "inputMethodList: " + list[index].id)
              if (list[index].id == inputTypeId) {
                  imm.setInputMethod(activity.currentFocus?.windowToken, inputTypeId)
                  break
              }
          }*/
    }

    fun showModeSetDialog(activity: Activity) {
        var dialog = CustomDialog(activity, R.layout.dialog_mode_set)
        val ivChildModeSet = dialog.findViewById<ImageView>(R.id.iv_child_mode_set)
        val ivStudentModeSet = dialog.findViewById<ImageView>(R.id.iv_student_mode_set)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val confirm = dialog.findViewById<TextView>(R.id.confirm)
        val tvChildModeSet = dialog.findViewById<TextView>(R.id.tv_child_mode_set)
        val tvStudentModeSet = dialog.findViewById<TextView>(R.id.tv_student_mode_set)
        ivChildModeSet.isSelected = true
        ivChildModeSet.setOnClickListener {
            ivChildModeSet.isSelected = true
            ivStudentModeSet.isSelected = false
            tvChildModeSet.setTextColor(Color.parseColor("#215558"))
            tvStudentModeSet.setTextColor(Color.parseColor("#50215558"))
        }
        ivStudentModeSet.setOnClickListener {
            ivStudentModeSet.isSelected = true
            ivChildModeSet.isSelected = false
            tvChildModeSet.setTextColor(Color.parseColor("#50215558"))
            tvStudentModeSet.setTextColor(Color.parseColor("#215558"))
        }
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            dialog.dismiss()
            if (ivChildModeSet.isSelected) {
                LauncherApplication.getMMKV().encode("mode", "child")
                activity.finish()
            } else {
                LauncherApplication.getMMKV().encode("mode", "student")
                activity.finish()
            }
        }
        dialog.show()
    }


    /**
     * 更新解析器
     */
    class CustomUpdateParser(var context: Context) : IUpdateParser {
        override fun parseJson(json: String): UpdateEntity? {
            return null
        }

        override fun parseJson(json: String, callback: IUpdateParseCallback) {
            val result: UpdateBean = Gson().fromJson(json, UpdateBean::class.java)
            val data = result.data
            var systemApp: UpdateBeanData? = null
            var launcherApp: UpdateBeanData? = null
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
//                    .setMd5(launcherApp.apk_md5.replace(":", ""))
                    .setIsIgnorable(true)
//                    .setForce(launcherApp.app_force_upgrade == 1)
                    .setVersionCode(launcherApp.version_code)
                    .setVersionName(launcherApp.version_name)
                    .setUpdateContent(launcherApp.content)
                    .setDownloadUrl(launcherApp.app_url)
                Log.i("XUpdate", "parseJson: $launcherApp")
                callback.onParseResult(launcherEntity)
            }
/*
            //系统固件升级包下载
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
                    context.startActivity(intent)
                }

                override fun onFail(errorInfo: String?) {
                    //运行在子线程
                }
            })
*/

            //todo 可设置多个回调 从而处理多个应用更新
//            callback.onParseResult(updateEntity)
        }

        override fun isAsyncParser(): Boolean {
            return true
        }
    }
}