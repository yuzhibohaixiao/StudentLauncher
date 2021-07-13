package com.alight.android.aoa_launcher.activity

import android.content.Intent
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.base.BaseActivity
import com.alight.android.aoa_launcher.bean.TokenPair
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.utils.SPUtils
import com.alight.android.aoa_launcher.view.CustomDialog
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_personal_center.*

class PersonCenterActivity : BaseActivity(), View.OnClickListener {

    private var tokenPair: TokenPair? = null
    override fun initView() {
    }

    override fun initData() {
        val userInfo = intent.getSerializableExtra("userInfo")
        if (userInfo != null) {
            tokenPair = userInfo as TokenPair
        }
        tokenPair?.apply {
            Glide.with(this@PersonCenterActivity)
                .load(if (gender == 0 || gender == 1) R.drawable.splash_girl else R.drawable.splash_boy)
                .into(iv_icon_personal_center)
            tv_name_personal_center.text = name
            tv_grade_personal_center.text = "一年级"
            tv_gender_center.text = when (gender) {
                0 -> "未知性别"
                1 -> "女孩"
                else -> "男孩"
            }
            tv_gender_center.setCompoundDrawablesWithIntrinsicBounds(
                when (gender) {
                    1 -> resources.getDrawable(R.drawable.girl)
                    2 -> resources.getDrawable(R.drawable.boy)
                    else -> null
                },
                null,
                null,
                null
            )
        }
    }

    override fun setListener() {
        ll_back_personal_center.setOnClickListener(this)
        ll_exit_personal_center.setOnClickListener(this)
        tv_focus.setOnClickListener(this)
        tv_wifi.setOnClickListener(this)
        tv_set.setOnClickListener(this)
        tv_splash.setOnClickListener(this)
    }

    override fun initPresenter(): PresenterImpl? {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_personal_center
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_back_personal_center ->
                finish()
            //用户登出
            R.id.ll_exit_personal_center -> {
                finish()
                SPUtils.syncPutData("onlyShowSelectChild", true)
                startActivity(Intent(this, SplashActivity::class.java))
            }
            R.id.tv_focus ->
                finish()
            R.id.tv_wifi ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
            R.id.tv_set -> {
                //系统升级和解绑
                val updateDialog = CustomDialog(this, R.layout.dialog_update)
                updateDialog.show()
                val update = updateDialog.findViewById<FrameLayout>(R.id.fl_update)
                val close = updateDialog.findViewById<ImageView>(R.id.iv_close_update)
                update.setOnClickListener {
                    //获取App和系统固件更新
                    getPresenter().updateAppAndSystem()
                }
                close.setOnClickListener {
                    updateDialog.dismiss()
                }
            }
            R.id.tv_splash -> {
                //直接打开用户引导
                val intent = Intent(this, SplashActivity::class.java)
                intent.putExtra("openUserSplash", true)
                startActivity(intent)
            }
        }
    }
}