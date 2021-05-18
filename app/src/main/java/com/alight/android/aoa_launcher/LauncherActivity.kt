package com.alight.android.aoa_launcher

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alight.android.aoa_launcher.adapter.LauncherAppDialogAdapter
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.view.CustomDialog
import com.qweather.sdk.bean.weather.WeatherNowBean
import kotlinx.android.synthetic.main.activity_main.*
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
        setCurrentDate()
    }


    /**
     * 设置日期时间展示
     */
    private fun setCurrentDate() {
        var calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getDefault();//默认当前时区
        var year = calendar.get(Calendar.YEAR)// 获取当前年份
        var month = calendar.get(Calendar.MONTH) + 1// 获取当前月份
        var day = calendar.get(Calendar.DAY_OF_MONTH)// 获取当前月份的日期号码
        var hour = calendar.get(Calendar.HOUR_OF_DAY)// 获取当前小时
        var minute = calendar.get(Calendar.MINUTE)// 获取当前分钟
        tv_month_launcher.text = "${month}月${day}日"
        tv_year_launcher.text = "${year}年"
        tv_time_launcher.text = "$hour:$minute"
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
        //获取天气
        getPresenter().getWeather(this)
        var map = hashMapOf<String, Any>()
//        map.put("page", 1)
//        map.put("count", 10)
//        getPresenter().getModel(MyUrls.ZZ_MOVIE, map, ZZBean::class.java)
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
