package com.alight.android.aoa_launcher

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
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
    //初始化并弹出对话框方法


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

    private fun showDialog() {
        val view =
            LayoutInflater.from(LauncherApplication.getContext()).inflate(
                R.layout.dialog_app_launcher,
                null,
                false
            )
        val dialog =
            AlertDialog.Builder(LauncherApplication.getContext())
                .setView(view).create()

        /*  Button btn_cancel_high_opion = view.findViewById(R.id.btn_cancel_high_opion);
        Button btn_agree_high_opion = view.findViewById(R.id.btn_agree_high_opion);

        btn_cancel_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUnitls.setParam(getApplicationContext(), "HighOpinion", "false");
                //... To-do
                dialog.dismiss();
            }
        });

        btn_agree_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //... To-do
                dialog.dismiss();
            }
        });

        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout(
                (ScreenUtils.getScreenWidth(this) / 4 * 3),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );*/
    }


    override fun onClick(view: View) {
        when (view.id) {
            //视频
            R.id.iv_video_launcher -> showDialog()
            //游戏
            R.id.iv_game_launcher -> ""
            //其他
            R.id.iv_other_launcher -> ""
            //教育
            R.id.iv_education_launcher -> ""
            //设置
            R.id.iv_setting_launcher -> ""
            //应用市场（安智）
            R.id.iv_app_store -> ""
        }
    }


}
