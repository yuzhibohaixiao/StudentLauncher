package com.alight.android.aoa_launcher.presenter

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.base.BasePresenter
import com.alight.android.aoa_launcher.contract.IContract
import com.alight.android.aoa_launcher.utils.NetUtils
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
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.collections.HashMap

/**
 * @author wangzhe
 * Presenter的实现
 * Created on 2021/5/12
 */
class PresenterImpl : BasePresenter<IContract.IView>() {

    private var locationManager: LocationManager? = null
    private var locationProvider: String? = null
    private var TAG = "PresenterImpl"
    override fun <T> getModel(url: String, map: HashMap<String, Any>, cls: Class<T>) {
        //调用model
        getModel().getNetInfo(url, map, cls, object : NetUtils.NetCallback {
            //model层回调给Presenter层级
            override fun onSuccess(any: Any) {
                //希望在View层进行视图的刷新
                getView().onSuccess(any)
            }

            override fun onError(error: String) {
                getView().onError(error)
            }
        })
    }

    private fun initWeather() {
        // param publicId  appKey
        HeConfig.init("HE2105171641531090", "49fba87b52944fe08ba36e5c74dfb4a1")
        //切换至开发版服务
        HeConfig.switchToDevService()
        //切换至商业版服务
//        HeConfig.switchToBizService();
    }

    /**
     * 获取天气控件的所需参数
     */
    override fun getWeather(context: Context, location: Location) {
        //初始化天气服务
        initWeather()
        /**
         * 实况天气数据
         * @param location 所查询的地区，可通过该地区名称、ID、IP和经纬度进行查询经纬度格式：经度,纬度
         * （英文,分隔，十进制格式，北纬东经为正，南纬西经为负)
         * @param lang     (选填)多语言，可以不使用该参数，默认为简体中文
         * @param unit     (选填)单位选择，公制（m）或英制（i），默认为公制单位
         * @param listener 网络访问结果回调
         */
        QWeather.getGeoCityLookup(
            context, location.longitude.toString()
                    + "," + location.latitude,
            object : OnResultGeoListener {
                override fun onError(throwable: Throwable) {

                }

                override fun onSuccess(geoBean: GeoBean) {
                    QWeather.getWeatherNow(
                        context,
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

                                    getView().onWeather(
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


}