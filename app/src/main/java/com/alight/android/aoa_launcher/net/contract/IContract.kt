package com.alight.android.aoa_launcher.net.contract

import com.alight.android.aoa_launcher.utils.NetUtils
import okhttp3.RequestBody

/**
 * @author wangzhe
 * 契约类
 * Created on 2021/5/12
 */
interface IContract {
    //Model层 进行网络请求
    interface IModel {
        //url: String,网络请求地址 map: HashMap<String, Any>,请求参数 cls: Class<T>解析类, callback: NetCallback 网络请求回调
        fun <T> getNetInfo(
            url: String,
            map: HashMap<String, Any>,
            cls: Class<T>,
            callback: NetUtils.NetCallback
        )

        fun <T> deleteNetInfo(
            requestBody: RequestBody,
            cls: Class<T>,
            callback: NetUtils.NetCallback
        )

        fun <T> postNetInfo(
            url: String,
            requestBody: RequestBody,
            cls: Class<T>,
            callback: NetUtils.NetCallback
        )

        fun <T> putNetInfo(
            url: String,
            requestBody: RequestBody,
            cls: Class<T>,
            callback: NetUtils.NetCallback
        )
    }

    //View视图层
    interface IView {
        fun onSuccess(any: Any)
        fun onError(error: String)
    }

    // Presenter 逻辑处理层
    interface IPresenter {
        //url: String,网络请求地址 map: HashMap<String, Any>,请求参数 cls: Class<T>解析类, callback: NetCallback 网络请求回调
        fun <T> getModel(url: String, map: HashMap<String, Any>, cls: Class<T>)
    }

}