package com.alight.android.aoa_launcher.common.fragment

import androidx.recyclerview.widget.GridLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.common.bean.AppBean
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.ChildAppListAdapter
import com.alight.android.aoa_launcher.ui.view.HDHeadItemDecoration
import com.alight.android.aoa_launcher.utils.ScreenUtils
import kotlinx.android.synthetic.main.fragment_app_list.view.*

class AppListFragment : BaseFragment() {

    private var childAppListAdapter: ChildAppListAdapter? = null

    override fun initData() {
        val appBeanList = mutableListOf<AppBean>()
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))
        appBeanList.add(AppBean("斑马英语", "", null))

        if (childAppListAdapter == null) {
            childAppListAdapter = ChildAppListAdapter()
            childAppListAdapter?.addData(appBeanList)
            getInflateView().rv_app_list.adapter = childAppListAdapter
            getInflateView().rv_app_list.addItemDecoration(
                HDHeadItemDecoration(
                    ScreenUtils.dp2px(
                        context,
                        -38f
                    )
                )
            )
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