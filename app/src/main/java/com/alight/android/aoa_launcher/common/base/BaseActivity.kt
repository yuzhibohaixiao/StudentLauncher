package com.alight.android.aoa_launcher.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alight.android.aoa_launcher.net.contract.IContract
import com.alight.android.aoa_launcher.presenter.PresenterImpl

/**
 * BaseActivity的基类封装
 * @author wangzhe
 * Created on 2021/5/12
 */
abstract class BaseActivity : AppCompatActivity(), IContract.IView {

    private var mPresenter: PresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getLayout() != 0) {
            setContentView(getLayout())
            //Presenter的初始化 希望父类实现
            mPresenter = initPresenter()
            mPresenter?.onAttach(this)
            initView()
            setListener()
            initData()
        } else {
            throw IllegalStateException("this activity no layout")
        }
    }

    //设置监听器
    abstract fun setListener()

    //初始化数据 调用网络请求
    abstract fun initData()

    //初始化控件
    abstract fun initView()

    abstract fun initPresenter(): PresenterImpl?

    // 希望父类进行布局的实现
    abstract fun getLayout(): Int

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.onDetach()
            mPresenter = null
        }
    }

    fun getPresenter(): PresenterImpl {
        return mPresenter!!
    }

}