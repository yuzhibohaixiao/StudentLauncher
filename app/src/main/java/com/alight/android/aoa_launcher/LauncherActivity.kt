package com.alight.android.aoa_launcher

import android.util.Log
import android.view.View
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Launcher主页
 */
class LauncherActivity : BaseActivity(), View.OnClickListener {
//    lateinit var mAdapter: MyAdapter

    //初始化控件
    override fun initView() {
//        main_recy.layoutManager = LinearLayoutManager(this)
//        mAdapter = MyAdapter(baseContext)
//        main_recy.adapter = mAdapter
    }

    override fun setListener() {
        iv_video_launcher.setOnClickListener(this)
        iv_game_launcher.setOnClickListener(this)
        iv_other_launcher.setOnClickListener(this)
        iv_education_launcher.setOnClickListener(this)
        iv_setting_launcher.setOnClickListener(this)
        iv_app_store.setOnClickListener(this)
    }

    override fun initData() {

        var map = hashMapOf<String, Any>()
//        map.put("page", 1)
//        map.put("count", 10)
//        getPresenter().getModel(MyUrls.ZZ_MOVIE, map, ZZBean::class.java)
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_video_launcher -> ""
            R.id.iv_game_launcher -> ""
            R.id.iv_other_launcher -> ""
            R.id.iv_education_launcher -> ""
            R.id.iv_setting_launcher -> ""
            R.id.iv_app_store -> ""
        }
    }


}
