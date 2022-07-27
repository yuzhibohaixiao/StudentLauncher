package com.alight.android.aoa_launcher.common.fragment

import androidx.recyclerview.widget.GridLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.common.bean.AppBean
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.ChildAppListAdapter
import com.alight.android.aoa_launcher.ui.view.OverlapItemDecoration
import com.alight.android.aoa_launcher.utils.ScreenUtils
import kotlinx.android.synthetic.main.fragment_app_list.view.*

class AppListFragment : BaseFragment() {

    private var childAppListAdapter: ChildAppListAdapter? = null

    override fun initData() {
        val appBeanList = mutableListOf<AppBean>()
        appBeanList.add(AppBean("中国象棋", "", null))
        appBeanList.add(AppBean("宝宝巴士", "", null))
        appBeanList.add(AppBean("蒙台梭利启蒙乐园", "", null))
        appBeanList.add(AppBean("天天练", "", null))
        appBeanList.add(AppBean("宝宝乐器", "", null))
        appBeanList.add(AppBean("喜马拉雅儿童", "", null))
        appBeanList.add(AppBean("熊猫博士识字", "", null))
        appBeanList.add(AppBean("阿布睡前故事", "", null))
        appBeanList.add(AppBean("科魔大战", "", null))
        appBeanList.add(AppBean("星尘浏览器", "", null))
        appBeanList.add(AppBean("少年得到", "", null))

        if (childAppListAdapter == null) {
            childAppListAdapter = ChildAppListAdapter()
            childAppListAdapter?.addData(appBeanList)
            getInflateView().rv_app_list.adapter = childAppListAdapter
            getInflateView().rv_app_list.addItemDecoration(
                OverlapItemDecoration(
                    ScreenUtils.dp2px(
                        context,
                        -38f
                    )
                )
            )
            childAppListAdapter?.setOnItemClickListener { adapter, view, position ->
                getPresenter().startApp(
                    requireContext(),
                    childAppListAdapter!!.data[position].appPackName
                )
            }
        }
    }

    override fun initView() {
        val gridLayoutManager = GridLayoutManager(context, 2)
        gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
        getInflateView().rv_app_list.layoutManager = gridLayoutManager
    }

    override fun getLayout(): Int {
        return R.layout.fragment_app_list
    }

    override fun setListener() {

    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

}