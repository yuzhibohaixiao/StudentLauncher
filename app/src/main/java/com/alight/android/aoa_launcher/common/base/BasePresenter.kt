package com.alight.android.aoa_launcher.common.base

import com.alight.android.aoa_launcher.net.contract.IContract
import com.alight.android.aoa_launcher.net.model.ModelImpl
import java.lang.ref.WeakReference

/**
 * @author wangzhe
 * Created on 2021/5/12
 */
abstract class BasePresenter<V : IContract.IView> : IContract.IPresenter {

    //私有化
    private var mModel = ModelImpl()

    //弱引用 希望他被回收 防止内存泄漏
    lateinit var weakReference: WeakReference<V>

    //跟View试图进行绑定
    fun onAttach(v: V) {
        weakReference = WeakReference(v)
    }

    //当activity和fragment被销毁时 需要进行解绑
    fun onDetach() {
        if (weakReference.get() != null) {
            //说明持有View引用 清理持有的引用
            weakReference.clear()
        }
    }

    //返回一个Model引用
    fun getModel(): ModelImpl {
        return mModel
    }

    //返回一个View的引用
    fun getView(): V? {
        return if (weakReference.get() != null) {
            weakReference.get()!!
        } else null
    }

}