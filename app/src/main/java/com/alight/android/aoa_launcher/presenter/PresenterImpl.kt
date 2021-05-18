package com.alight.android.aoa_launcher.presenter

import android.content.Context
import android.util.Log
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
import java.util.*
import kotlin.collections.HashMap

/**
 * @author wangzhe
 * Presenter的实现
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
                getView().onSuccess(any)
            }

            override fun onError(error: String) {
                getView().onError(error)
            }
        })
    }

    private fun initWeather() {
        //  key
        HeConfig.init("HE2105171641531090", "49fba87b52944fe08ba36e5c74dfb4a1")
        //切换至开发版服务
        HeConfig.switchToDevService()
        //切换至商业版服务
//        HeConfig.switchToBizService();
    }

    override fun getWeather(context: Context) {
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
            context,
            "116.41,39.92",
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
                                    getView().onWeather(geoBean.locationBean[0].adm1, weatherBean)
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


}