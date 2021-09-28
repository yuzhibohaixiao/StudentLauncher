package com.alight.android.aoa_launcher.activity

import android.content.Intent
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.ahwcx.ahwsdk.AbilityManager
import com.alight.ahwcx.ahwsdk.abilities.CalibrationAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility.HardwareStatusHandler
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.PersonalCenterFamilyAdapter
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.SPUtils
import com.alight.android.aoa_launcher.utils.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_personal_center.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class PersonCenterActivity : BaseActivity(), View.OnClickListener {

    private var tokenPair: TokenPair? = null
    private lateinit var familyAdapter: PersonalCenterFamilyAdapter
    private var familyId: Int? = null
    private val USER_LOGOUT_ACTION = "com.alight.android.user_logout" // 自定义ACTION
    private val abilityManager = AbilityManager("launcher", "3", "123")
    private var calibrationAbility: CalibrationAbility? = null
    private var panelAbility: PanelAbility? = null

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
                .load(tokenPair?.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .error(if (tokenPair?.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
                .into(iv_icon_personal_center)

            tv_name_personal_center.text = name
            tv_grade_personal_center.text = "一年级"
            tv_gender_center.text = when (gender) {
                0 -> "未知性别"
                1 -> "男孩"
                else -> "女孩"
            }
            tv_gender_center.setCompoundDrawablesWithIntrinsicBounds(
                when (gender) {
                    1 -> resources.getDrawable(R.drawable.boy)
                    2 -> resources.getDrawable(R.drawable.girl)
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
        calibrationAbility =
            abilityManager.getAbility(CalibrationAbility::class.java, true, applicationContext)
        calibrationAbility?.bindLooper(Looper.myLooper()!!)
        panelAbility =
            abilityManager.getAbility(PanelAbility::class.java, true, applicationContext)
        panelAbility?.bindLooper(Looper.myLooper()!!)

        GlobalScope.launch {
            delay(500)
            //初始化控制中心
            panelAbility?.getStatus(object : HardwareStatusHandler {
                override fun onError(result: Map<String, Any>) {
                }

                override fun onSuccess(hardwareStatus: PanelAbility.HardwareStatus) {
                    //触控模式
                    when (hardwareStatus.touchMode) {
                        PanelAbility.TouchMode.PEN_MODE -> {
                            tv_pen_touch.isSelected = true
                            tv_hand_touch.isSelected = false
                        }
                        PanelAbility.TouchMode.FINGER_MODE -> {
                            tv_pen_touch.isSelected = false
                            tv_hand_touch.isSelected = true
                        }
                    }
                    //亮度
                    bsb_light.setProgress(hardwareStatus.light.toFloat())
                    //音量
                    bsb_voice.setProgress(hardwareStatus.volume.toFloat())
                }

            })
        }
    }

    override fun setListener() {
        ll_back_personal_center.setOnClickListener(this)
        ll_exit_personal_center.setOnClickListener(this)
        tv_exit_personal_center.setOnClickListener(this)
        tv_focus.setOnClickListener(this)
        tv_wifi.setOnClickListener(this)
        tv_set.setOnClickListener(this)
        tv_splash.setOnClickListener(this)
        tv_pen_touch.setOnClickListener(this)
        tv_hand_touch.setOnClickListener(this)

        familyAdapter.setOnItemClickListener { adapter, view, position ->
            val status = familyAdapter.data[position].status
            if (status.online == 0) {
                ToastUtils.showShort(this, "当前用户离线")
            } else if (status.online == 1 && status.av == 0) {
                startPhoneWindow(position)
            } else {
                ToastUtils.showShort(this, "当前用户忙碌中")
            }
        }

        GlobalScope.launch {
            delay(500)
            panelAbility?.subStatus(object : HardwareStatusHandler {
                override fun onError(result: Map<String, Any>) {

                }

                override fun onSuccess(hardwareStatus: PanelAbility.HardwareStatus) {
                    //监听触控模式
                    when (hardwareStatus.touchMode) {
                        PanelAbility.TouchMode.PEN_MODE -> {
                            tv_pen_touch.isSelected = true
                            tv_hand_touch.isSelected = false
                        }
                        PanelAbility.TouchMode.FINGER_MODE -> {
                            tv_pen_touch.isSelected = false
                            tv_hand_touch.isSelected = true
                        }
                    }
                    //亮度
                    bsb_light.setProgress(hardwareStatus.light.toFloat())
                    //音量
                    bsb_voice.setProgress(hardwareStatus.volume.toFloat())
                }
            })

        }

        bsb_light.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
            }

            override fun getProgressOnActionUp(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float
            ) {
                panelAbility?.setLight(progress)
            }

            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
            }
        }
        bsb_voice.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
            }

            override fun getProgressOnActionUp(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float
            ) {
                panelAbility?.setVolume(progress)
            }

            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {

            }
        }

    }

    private fun startPhoneWindow(position: Int) {
        val parentInfo = familyAdapter.data[position]
        val intent = Intent("com.alight.trtcav.WindowActivity")
        if (parentInfo != null) {
            intent.putExtra("called", 1)    //主叫
            intent.putExtra("parentId", parentInfo.user_id.toString())
            intent.putExtra("parentName", parentInfo.name)
            intent.putExtra("parentAvatar", parentInfo.avatar)
            intent.putExtra("childId", AccountUtil.getCurrentUser().userId.toString())
            intent.putExtra("token", AccountUtil.getCurrentUser().token)
        }
        try {
            this.startActivity(intent)
        } catch (e: Exception) {
            e.stackTraceToString()
        }
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
                if (any.data == null) {
                    ToastUtils.showShort(this, "没有获取到家庭信息")
                } else {
                    familyId = any.data.id
                    familyAdapter.addData(any.data.parents)
/*
                    any.data.parents.forEach {
                        getPresenter().getModel(
                            Urls.PARENT_ONLINE_STATE,
                            hashMapOf("user_id" to it.user_id),
                            ParentOnlineState::class.java
                        )
                    }
*/
                }
            }
            is ParentOnlineState -> {
//                familyAdapter.setOnlineState(any.data)
            }
            is DeviceRelationBean -> {
                ToastUtils.showShort(this, any.data)
                //重新绑定
                SPUtils.syncPutData("rebinding", true)
                //让引导再次开启
                SPUtils.syncPutData("splashClose", false)
                finish()
                var intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
            }
            is UpdateBean -> {
                if (familyId != null)
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
        intent.putExtra("message", "用户退出") // 设置广播的消息
        sendBroadcast(intent)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_back_personal_center ->
                finish()
            //用户注销
            R.id.ll_exit_personal_center, R.id.tv_exit_personal_center -> {
                sendSendUserLogoutBroadcast()
                SPUtils.syncPutData("onlyShowSelectChild", true)
                setResult(AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER)
                finish()
            }
            R.id.tv_focus -> {
                calibrationAbility?.startCalibration()
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
            R.id.tv_pen_touch -> {
                tv_pen_touch.isSelected = true
                tv_hand_touch.isSelected = false
                panelAbility?.setTouchMode(PanelAbility.TouchMode.PEN_MODE)
            }
            R.id.tv_hand_touch -> {
                tv_pen_touch.isSelected = false
                tv_hand_touch.isSelected = true
                panelAbility?.setTouchMode(PanelAbility.TouchMode.FINGER_MODE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        abilityManager.onStop()
    }
}