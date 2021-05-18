package com.alight.android.aoa_launcher

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.adapter.LauncherAppDialogAdapter
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.view.CustomDialog
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
class LauncherActivity : BaseActivity(), View.OnClickListener {
//    lateinit var mAdapter: MyAdapter

    //初始化控件
    override fun initView() {
//        main_recy.layoutManager = LinearLayoutManager(this)
//        mAdapter = MyAdapter(baseContext)
//        main_recy.adapter = mAdapter
    }

    override fun setListener() {
        iv_video_launcher.setOnClickListener(this)
        iv_game_launcher.setOnClickListener(this)
        iv_other_launcher.setOnClickListener(this)
        iv_education_launcher.setOnClickListener(this)
        iv_setting_launcher.setOnClickListener(this)
        iv_app_store.setOnClickListener(this)
    }

    override fun initData() {
        //初始化天气控件
        initWeatherDate()
        getLocation()
//        var map = hashMapOf<String, Any>()
//        map.put("page", 1)
//        map.put("count", 10)
//        getPresenter().getModel(MyUrls.ZZ_MOVIE, map, ZZBean::class.java)
    }

    /**
     *  初始化天气控件 异步获取天气和时间 每10秒刷新一次
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
                tv_month_launcher.text = "${month}月${day}日"
                tv_year_launcher.text = "${year}年"
                tv_time_launcher.text = "$hour:$minute"
            }
            delay(10000)
            initWeatherDate()
        }

    }

    private fun getLocation() {

        // 位置
        val locationManager: LocationManager
        val locationListener: LocationListener
        val location: Location?
        val contextService: String = Context.LOCATION_SERVICE
        val provider: String?
        var lat: Double
        var lon: Double
        locationManager = getSystemService(contextService) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE // 高精度

        criteria.isAltitudeRequired = false // 不要求海拔

        criteria.isBearingRequired = false // 不要求方位

        criteria.isCostAllowed = true // 允许有花费

        criteria.powerRequirement = Criteria.POWER_LOW // 低功耗

        // 从可用的位置提供器中，匹配以上标准的最佳提供器
        // 从可用的位置提供器中，匹配以上标准的最佳提供器
        provider = locationManager.getBestProvider(criteria, true)
        // 获得最后一次变化的位置
        // 获得最后一次变化的位置
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
                getPresenter().getWeather(this@LauncherActivity,location)
            }
        }
        // 监听位置变化，2秒一次，距离10米以上
        locationManager.requestLocationUpdates(
            provider, 2000, 10f,
            locationListener
        )
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
    override fun onWeather(
        city: String,
        weatherNowBean: WeatherNowBean,
        weatherIcon: Int
    ) {
        tv_temperature_launcher.text = weatherNowBean.now.temp + "°C"
        iv_weather_launcher.setImageResource(weatherIcon)

    }

    private fun showDialog() {
        //弹出自定义dialog
        var dialog = CustomDialog(this, R.layout.dialog_app_launcher)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rv_app_dialog_launcher)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        val appName = arrayListOf<String>()
        for (i in 1..9) {
            appName.add("第${i}个应用")
        }
        recyclerView.adapter = LauncherAppDialogAdapter(this, appName)
        dialog.show();
    }

    /**
     * 打开系统设置
     */
    private fun showSystemSetting() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
    }


    override fun onClick(view: View) {
        when (view.id) {
            //视频
            R.id.iv_video_launcher -> showDialog()
            //游戏
            R.id.iv_game_launcher -> showDialog()
            //其他
            R.id.iv_other_launcher -> showDialog()
            //教育
            R.id.iv_education_launcher -> showDialog()
            //设置
            R.id.iv_setting_launcher -> showSystemSetting()
            //应用市场（安智）
            R.id.iv_app_store -> ""
        }
    }


}
