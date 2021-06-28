package com.alight.android.aoa_launcher

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.bean.TokenMessage
import com.alight.android.aoa_launcher.bean.UpdateBean
import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.i.LauncherListener
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.urls.Urls
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.DateUtil
import com.alight.android.aoa_launcher.utils.SPUtils
import com.google.gson.Gson
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateEntity
import com.xuexiang.xupdate.listener.IUpdateParseCallback
import com.xuexiang.xupdate.proxy.IUpdateParser
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


/**
 * Launcher主页
 */
class LauncherActivity : BaseActivity(), View.OnClickListener, LauncherListener {
//    lateinit var mAdapter: MyAdapter

    //初始化控件
    override fun initView() {
//        main_recy.layoutManager = LinearLayoutManager(this)
//        mAdapter = MyAdapter(baseContext)
//        main_recy.adapter = mAdapter
        AccountUtil.register(this)

        //XUpdate 更新
//        promptThemeColor: 设置主题颜色
//        promptButtonTextColor: 设置按钮的文字颜色
//        promptTopResId: 设置顶部背景图片
//        promptWidthRatio: 设置版本更新提示器宽度占屏幕的比例，默认是-1，不做约束
//        promptHeightRatio: 设置版本更新提示器高度占屏幕的比例，默认是-1，不做约束
        XUpdate.newBuild(this)
            .updateUrl(Urls.BASEURL + Urls.UPDATE)
            //检查更新
            .updateChecker(object : DefaultUpdateChecker() {
                override fun onBeforeCheck() {
                    super.onBeforeCheck()
//                    CProgressDialogUtils.showProgressDialog(getActivity(), "查询中...")
                }

                override fun onAfterCheck() {
                    super.onAfterCheck()
//                    CProgressDialogUtils.cancelProgressDialog(getActivity())
                }
            })
            .promptThemeColor(resources.getColor(R.color.white))
            .promptButtonTextColor(Color.WHITE)
            .updateParser(CustomUpdateParser())
            .promptTopResId(R.mipmap.ic_launcher_round)
            .promptWidthRatio(0.7F)
            .update();
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

    override fun initData() {
        //如果是新用户则打开Splash
        val isNewUser = SPUtils.getData(AppConstants.NEW_USER, true) as Boolean
        if (true) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
        //初始化天气控件日期
        initWeatherDate()
        //定位后获取天气
        getPresenter().getLocationAndWeather()

//        var map = hashMapOf<String, Any>()
//        map.put("page", 1)
//        map.put("count", 10)
//        getPresenter().getModel(MyUrls.ZZ_MOVIE, map, ZZBean::class.java)
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
                tv_month_launcher.text = "${month}月${day}日 " + DateUtil.getDayOfWeek(calendar)
                tv_year_launcher.text = "${year}年"
                tv_time_launcher.text =
                    "$hour:" + if (minute >= 10) minute else "0$minute"
            }
            delay(10000)
            initWeatherDate()
        }

    }

    /**
     * 更新解析器
     */
    class CustomUpdateParser : IUpdateParser {
        override fun parseJson(json: String): UpdateEntity {
            val result: UpdateBean = Gson().fromJson(json, UpdateBean::class.java)
            return if (result != null) {
                val data = result.data[0]
                UpdateEntity()
                    .setHasUpdate(data.is_active)
                    .setIsIgnorable(data.app_force_upgrade)
                    .setVersionCode(data.version_code)
                    .setVersionName(data.version_name)
                    .setUpdateContent(data.content)
                    .setDownloadUrl(data.app_url)
            } else result
        }

        override fun parseJson(json: String, callback: IUpdateParseCallback) {
        }

        override fun isAsyncParser(): Boolean {
            return true
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
            R.id.iv_aoa_launcher -> getPresenter().showAOA()
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
            super.dispatchKeyEvent(event);
        }
    }

    override fun onReceive(message: TokenMessage) {

    }

    override fun onConnect() {

    }

    override fun onDisconnect() {

    }


}
