package com.alight.android.aoa_launcher.model

import com.alight.android.aoa_launcher.contract.IContract
import com.alight.android.aoa_launcher.utils.NetUtils

/**
 * model层的实现
 * @author wangzhe
 * Created on 2021/5/12
 */
class ModelImpl : IContract.IModel {

    //不实现接口的话可以自己来写
    override fun <T> getNetInfo(url: String, map: HashMap<String, Any>, cls: Class<T>, callback: NetUtils.NetCallback) {
        //调用NetUtil中的网络请求
        NetUtils.intance.getInfo(url, map, cls, object : NetUtils.NetCallback {
            override fun onSuccess(any: Any) {
                //回调到Presenter层
                callback.onSuccess(any)
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

}