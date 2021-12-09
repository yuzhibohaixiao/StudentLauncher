package com.alight.android.aoa_launcher.presenter

import android.Manifest
import android.app.Activity
import android.app.Notification.FLAG_NO_CLEAR
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.viewpager.widget.ViewPager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.activity.LauncherActivity
import com.alight.android.aoa_launcher.activity.UpdateActivity
import com.alight.android.aoa_launcher.common.base.BasePresenter
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.constants.AppConstants.Companion.EXTRA_IMAGE_PATH
import com.alight.android.aoa_launcher.common.constants.AppConstants.Companion.SYSTEM_ZIP_FULL_PATH
import com.alight.android.aoa_launcher.common.listener.DownloadListener
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.net.contract.IContract
import com.alight.android.aoa_launcher.ui.adapter.HorizontalScrollAdapter
import com.alight.android.aoa_launcher.ui.view.ConfirmDialog
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
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
import kotlinx.android.synthetic.main.dialog_update.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.ArrayList


/**
 * launcher业务处理类
 * @author wangzhe
 * Created on 2021/5/12
 */
class PresenterImpl : BasePresenter<IContract.IView>() {

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
        activity: LauncherActivity,
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

    fun showDialog(dialog: CustomDialog, appType: String) {
        val activity = getView() as LauncherActivity
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

        val launcherActivity = getView() as LauncherActivity

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
     * 打开安智市场
     */
    fun showAZMarket() {
        var activity = getView() as LauncherActivity
        try {
            var intent =
                activity.packageManager.getLaunchIntentForPackage(AppConstants.AZ_PACKAGE_NAME)
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
        }

        if (updateDialog != null) {
            updateDialog.show()
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

    fun startAoaApp(context: Context, appId: String, route: String) {
        var intent = Intent("com.alight.android.aoa.entry")
        intent.putExtra("action", "aos.app.open")
        intent.putExtra("appId", appId)
        intent.putExtra("route", route)
        intent.putExtra("params", "")
        context.startActivity(intent)
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