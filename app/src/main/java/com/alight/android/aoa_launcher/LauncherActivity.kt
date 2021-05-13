package com.alight.android.aoa_launcher

import android.util.Log
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl


class LauncherActivity : BaseActivity() {
//    lateinit var mAdapter: MyAdapter

    //初始化控件
    override fun initView() {
//        main_recy.layoutManager = LinearLayoutManager(this)
//        mAdapter = MyAdapter(baseContext)
//        main_recy.adapter = mAdapter
    }

    override fun initData() {

        var map = hashMapOf<String, Any>()

//        getPresenter().getModel(MyUrls.BANNER, map, BannerBean::class.java)
//        map.put("page", 1)
//        map.put("count", 10)
//        getPresenter().getModel(MyUrls.ZZ_MOVIE, map, ZZBean::class.java)
//        getPresenter().getModel(MyUrls.JJ_MOVIE, map, JJBean::class.java)
//        getPresenter().getModel(MyUrls.HOT_MOVIE, map, HotBean::class.java)
    }


    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun onSuccess(any: Any) {
        /*   //网络请求成功后的结果 让对应视图进行刷新
           if (any is BannerBean) {
               mAdapter.setBannerData(any.result)
           }
        */
    }


    override fun onError(error: String) {
        Log.e("error", error)
    }


}
