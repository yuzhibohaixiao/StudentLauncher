package com.alight.android.aoa_launcher.activity

import android.content.Intent
import android.provider.Settings
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.PersonalCenterFamilyAdapter
import com.alight.android.aoa_launcher.utils.SPUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_personal_center.*

class PersonCenterActivity : BaseActivity(), View.OnClickListener {

    private var tokenPair: TokenPair? = null
    private lateinit var familyAdapter: PersonalCenterFamilyAdapter
    private var familyId: Int? = null
    private val USER_LOGOUT_ACTION = "com.alight.android.user_logout" // 自定义ACTION
//    private val abilityManager = AbilityManager("launcher", "3", "123")
//    private var calibrationAbility: CalibrationAbility? = null

    override fun initView() {
        familyAdapter = PersonalCenterFamilyAdapter()
        rv_family_info.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_family_info.adapter = familyAdapter

    }

    override fun initData() {
        val userInfo = intent.getSerializableExtra("userInfo")
        if (userInfo != null) {
            tokenPair = userInfo as TokenPair
        }
        tokenPair?.apply {
            Glide.with(this@PersonCenterActivity)
                .load(if (tokenPair?.gender == 2) R.drawable.splash_boy else R.drawable.splash_girl)
//                .apply(RequestOptions.bitmapTransform(CircleCrop()))
//                .error()
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
        getPresenter().getModel(
            Urls.FAMILY_INFO,
            HashMap(),
            FamilyInfoBean::class.java
        )
//        calibrationAbility =
//            abilityManager.getAbility(CalibrationAbility::class.java, true, applicationContext)
//        calibrationAbility?.bindLooper(Looper.myLooper()!!)
    }

    override fun setListener() {
        ll_back_personal_center.setOnClickListener(this)
        ll_exit_personal_center.setOnClickListener(this)
        tv_focus.setOnClickListener(this)
        tv_wifi.setOnClickListener(this)
        tv_set.setOnClickListener(this)
        tv_splash.setOnClickListener(this)
    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_personal_center
    }

    override fun onSuccess(any: Any) {
        when (any) {
            is FamilyInfoBean -> {
                familyId = any.data.id
                familyAdapter.addData(any.data.parents)
                any.data.parents.forEach {
                    getPresenter().getModel(
                        Urls.PARENT_ONLINE_STATE,
                        hashMapOf("user_id" to it.user_id),
                        ParentOnlineState::class.java
                    )
                }
            }
            is ParentOnlineState -> {
                familyAdapter.setOnlineState(any.data)
            }
            is DeviceRelationBean -> {
                ToastUtils.showShort(this, any.data)
                //重新绑定
                var intent = Intent(this, SplashActivity::class.java)
                intent.putExtra("rebinding", true)
                startActivity(intent)
            }
            is UpdateBean -> {
                if (familyId!=null)
                getPresenter().showUpdateDialog(any, familyId!!, this)
            }
        }
    }

    override fun onError(error: String) {
        ToastUtils.showShort(this, error)
    }

    /**
     * 发送用户登出的广播
     */
    private fun sendSendUserLogoutBroadcast() {
        val intent = Intent()
        intent.action = USER_LOGOUT_ACTION
        intent.putExtra("message", "用户登出！") // 设置广播的消息
        sendBroadcast(intent)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_back_personal_center ->
                finish()
            //用户登出
            R.id.ll_exit_personal_center -> {
                sendSendUserLogoutBroadcast()
                finish()
                SPUtils.syncPutData("onlyShowSelectChild", true)
                startActivity(Intent(this, SplashActivity::class.java))
            }
            R.id.tv_focus -> {
//                calibrationAbility?.startCalibration()
            }
            R.id.tv_wifi ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
            R.id.tv_set -> {
                getPresenter().getModel(Urls.UPDATE, hashMapOf(), UpdateBean::class.java)
                /*
                //todo 待解绑验证码接口完善后完成
                val unbindDialog = CustomDialog(this, R.layout.dialog_unbind)
                 unbindDialog.show()
                 val tvUnbind = unbindDialog.findViewById<TextView>(R.id.tv_unbind_dialog)
                 val tvBack = unbindDialog.findViewById<ImageView>(R.id.iv_back_unbind)
                 val tvClose = unbindDialog.findViewById<ImageView>(R.id.iv_close_unbind)
                 tvUnbind.setOnClickListener {
                     val verifyDialog =
                         CustomDialog(this, R.layout.dialog_unbind_verification)
                     verifyDialog.show()
                     val back = verifyDialog.findViewById<ImageView>(R.id.iv_back_verify)
                     val close = verifyDialog.findViewById<ImageView>(R.id.iv_close_verify)
                     back.setOnClickListener {
                         verifyDialog.dismiss()
                     }
                     close.setOnClickListener {
                         verifyDialog.dismiss()
                         updateDialog.dismiss()
                         unbindDialog.dismiss()
                     }
                 }
                 tvBack.setOnClickListener {
                     unbindDialog.dismiss()
                 }
                 tvClose.setOnClickListener {
                     unbindDialog.dismiss()
                     updateDialog.dismiss()
                 }*/

            }
            R.id.tv_splash -> {
                //直接打开用户引导
                val intent = Intent(this, SplashActivity::class.java)
                intent.putExtra("openUserSplash", true)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        abilityManager.onStop()
    }
}