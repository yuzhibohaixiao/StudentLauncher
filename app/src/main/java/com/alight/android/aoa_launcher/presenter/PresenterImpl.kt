package com.alight.android.aoa_launcher.presenter

import com.alight.android.aoa_launcher.base.BasePresenter
import com.alight.android.aoa_launcher.contract.IContract
import com.alight.android.aoa_launcher.utils.NetUtils

/**
 * @author wangzhe
 * Presenter的实现
 * Created on 2021/5/12
 */
class PresenterImpl : BasePresenter<IContract.IView>() {

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
}