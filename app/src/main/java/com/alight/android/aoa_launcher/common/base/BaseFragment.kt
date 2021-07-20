package com.alight.android.aoa_launcher.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alight.android.aoa_launcher.net.contract.IContract
import com.alight.android.aoa_launcher.presenter.PresenterImpl

/**
 * @author wangzhe
 * Created on 2021/5/12
 */
abstract class BaseFragment : Fragment(), IContract.IView {

    private var mPresenter: PresenterImpl? = null
    private lateinit var inflate: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflate = View.inflate(context, getLayout(), null)
        initView()
        return inflate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //初始化Presenter希望父类进行实现
        mPresenter = initPresenter()
        mPresenter!!.onAttach(this)
        initData()
    }

    abstract fun initPresenter(): PresenterImpl

    fun getInflateView(): View {
        return inflate
    }

    //网络请求 和数据初始化
    abstract fun initData()

    //控件的初始化
    abstract fun initView()

    abstract fun getLayout(): Int

    fun getPresenter(): PresenterImpl {
        return mPresenter!!
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.onDetach()
            mPresenter = null
        }
    }
}