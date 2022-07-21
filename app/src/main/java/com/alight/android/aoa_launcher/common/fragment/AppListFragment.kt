package com.alight.android.aoa_launcher.common.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.presenter.PresenterImpl

class AppListFragment : BaseFragment() {
    override fun initData() {
   /*     val appBeanList = mutableListOf<AppBean>()
        appBeanList.add(AppBean("Bound", "", resources.getDrawable(R.drawable.bound)))
        appBeanList.add(AppBean("todo list", "", resources.getDrawable(R.drawable.todo_list)))
        appBeanList.add(AppBean("note", "", resources.getDrawable(R.drawable.note)))
        appBeanList.add(AppBean("scan", "", resources.getDrawable(R.drawable.scan)))
        appBeanList.add(
            AppBean(
                "Chrome",
                "com.android.chrome",
                resources.getDrawable(R.drawable.google)
            )
        )
        appBeanList.add(AppBean("Youtube", "", resources.getDrawable(R.drawable.youtube)))
        appBeanList.add(AppBean("Amazon", "", resources.getDrawable(R.drawable.amazon)))
        appBeanList.add(AppBean("Yelp", "", resources.getDrawable(R.drawable.yelp)))
        appBeanList.add(AppBean("wiki", "", resources.getDrawable(R.drawable.wiki)))
        appBeanList.add(AppBean("Spotify", "", resources.getDrawable(R.drawable.spotify)))
        getInflateView().rv_work_dock.adapter = WorkDockAdapter(context, appBeanList)*/
    }

    override fun initView() {
//        val linearLayoutManager = LinearLayoutManager(context)
//        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
//        getInflateView().rv_work_dock.layoutManager = linearLayoutManager

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