package com.alight.android.aoa_launcher.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.ahwcx.ahwsdk.AbilityManager
import com.alight.ahwcx.ahwsdk.abilities.CalibrationAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility.HardwareStatusHandler
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.event.NetMessageEvent
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.PersonalCenterFamilyAdapter
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.activity_personal_center.*
import kotlinx.android.synthetic.main.activity_personal_center.tv_dialog_launcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PersonCenterActivity : BaseActivity(), View.OnClickListener {

    private val TAG: String = "PersonCenterActivity"
    private var tokenPair: TokenPair? = null
    private lateinit var familyAdapter: PersonalCenterFamilyAdapter
    private var familyId: Int? = null
    private val abilityManager = AbilityManager("launcher", "3", "123")
    private var calibrationAbility: CalibrationAbility? = null
    private var panelAbility: PanelAbility? = null
    private var music: MediaPlayer? = null
    private var netState = 1

    override fun initView() {
        familyAdapter = PersonalCenterFamilyAdapter()
        rv_family_info.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_family_info.adapter = familyAdapter
    }

    override fun initData() {
        EventBus.getDefault().register(this)
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
        calibrationAbility =
            abilityManager.getAbility(CalibrationAbility::class.java, true, applicationContext)
        calibrationAbility?.bindLooper(Looper.myLooper()!!)
        panelAbility =
            abilityManager.getAbility(PanelAbility::class.java, true, applicationContext)
        panelAbility?.bindLooper(Looper.myLooper()!!)

        GlobalScope.launch {
            delay(500)
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        netState = intent.getIntExtra("netState", 0)
        getNetStateShowUI(netState)

        try {
            val mmkv = LauncherApplication.getMMKV()
            val playTimeJson = mmkv.decodeString(AppConstants.PLAY_TIME)
            val playTimeBean = Gson().fromJson(playTimeJson, PlayTimeBean::class.java)
            tv_control_today.text = "今天：" + if (playTimeBean.data.is_rest_day) "休息日" else "上学日"
            tv_control_time.text =
                playTimeBean.data.playtime.start_playtime + "-" + playTimeBean.data.playtime.stop_playtime
            tv_control_total_time.text = TimeUtils.timeDifference(
                playTimeBean.data.playtime.start_playtime,
                playTimeBean.data.playtime.stop_playtime
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        /* ApkController.slienceInstallWithSysSign(
             LauncherApplication.getContext(),
             Environment.getExternalStorageDirectory().path + "/"+ "launcher.apk"
         )*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetNetEvent(event: NetMessageEvent) {
        //网络正常 刷新UI
        getNetStateShowUI(event.netState)
    }

    /**
     * 根据网络状态展示不同的UI效果
     */
    private fun getNetStateShowUI(netState: Int) {
        this.netState = netState
        if (netState == 1) {
            if (familyAdapter.data.size == 0 && InternetUtil.isNetworkAvalible(this)) {
                getPresenter().getModel(
                    Urls.FAMILY_INFO,
                    HashMap(),
                    FamilyInfoBean::class.java
                )
            }
            rv_family_info.visibility = View.VISIBLE
            ll_family_info_offline.visibility = View.GONE
            tv_set.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.setting, 0, 0)
            resources.getColor(R.color.person_center_text_black)
            ll_exit_personal_center.visibility = View.VISIBLE
        } else if (netState == 0) {
            rv_family_info.visibility = View.GONE
            ll_family_info_offline.visibility = View.VISIBLE
            tv_set.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.setting_no_network, 0, 0)
            tv_set.setTextColor(Color.parseColor("#50ffffff"))
            ll_exit_personal_center.visibility = View.GONE
            Log.e(TAG, "onNetChanged:没有网络 ")
        }
    }

    override fun setListener() {
        ll_family_info_offline.setOnClickListener(this)
        ll_back_personal_center.setOnClickListener(this)
        ll_exit_personal_center.setOnClickListener(this)
        tv_shutdown_personal_center.setOnClickListener(this)
        tv_focus.setOnClickListener(this)
        tv_wifi.setOnClickListener(this)
        tv_set.setOnClickListener(this)
        tv_splash.setOnClickListener(this)
        tv_pen_touch.setOnClickListener(this)
        tv_hand_touch.setOnClickListener(this)
        tv_dialog_launcher.setOnClickListener(this)

        familyAdapter.setOnItemClickListener { adapter, view, position ->
            val status = familyAdapter.data[position].status
            if (status.jpush_online == 0) {
                ToastUtils.showShort(this, "当前用户离线")
            } else if (status.jpush_online == 1 && status.av == 0) {
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
            intent.putExtra("childId", tokenPair?.userId.toString())
            intent.putExtra("token", tokenPair?.token)
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
                    //https转换
//                    any.data.parents.forEach {
//                        it.avatar.replace("https", "http")
//                    }
                    familyAdapter.data.clear()
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
//                if (any.code == 200) {
                //重新绑定
                ToastUtils.showShort(this, any.data)
                SPUtils.syncPutData("onlyShowSelectChild", false)
                SPUtils.syncPutData("rebinding", true)
                //让引导再次开启
                SPUtils.syncPutData("splashClose", false)
                finish()
                var intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
//                } else {
                //重新绑定
//                    ToastUtils.showShort(this, "解绑失败")
//                }
            }
            is UpdateBean -> {
                if (familyId != null)
                    getPresenter().showUpdateDialog(any, familyId!!, this)
            }
        }
    }

    override fun onError(error: String) {
        Log.i("PersonCenter", "onError: $error")
        ToastUtils.showShort(this, "网络请求错误")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_back_personal_center ->
                finish()
            //用户注销
            R.id.ll_exit_personal_center -> {
                SPUtils.syncPutData("onlyShowSelectChild", true)
                setResult(AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER)
                finish()
            }
            //关机
            R.id.tv_shutdown_personal_center -> {
//                val intent = Intent()
//                intent.action = Intent.ACTION_SHUTDOWN
//                sendBroadcast(intent)

                val intent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
                intent.putExtra("android.intent.extra.KEY_CONFIRM", true)
                //其中false换成true,会弹出是否关机的确认窗口
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            R.id.tv_focus -> {
                calibrationAbility?.startCalibration()
            }
            R.id.tv_wifi ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
            R.id.ll_family_info_offline -> {
                showOfflineDialog()
            }
            R.id.tv_set -> {
                if (netState == 1) {
                    getPresenter().getModel(
                        Urls.UPDATE,
                        hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
                        UpdateBean::class.java
                    )
                } else {
                    showOfflineDialog()
                }
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
                playClickMusic()
            }
            R.id.tv_hand_touch -> {
                tv_pen_touch.isSelected = false
                tv_hand_touch.isSelected = true
                panelAbility?.setTouchMode(PanelAbility.TouchMode.FINGER_MODE)
                playClickMusic()
            }
            R.id.tv_dialog_launcher -> {
                getPresenter().showSelectGradeDialog(this, tv_dialog_launcher)
            }
        }
    }

    private fun showOfflineDialog() {
        val customDialog = CustomDialog(this, R.layout.dialog_offline)
        val tvOffline = customDialog.findViewById<TextView>(R.id.tv_offline)
        val spannableString = SpannableString(tvOffline.text)
        spannableString.setSpan(UnderlineSpan(), 7, 11, Paint.UNDERLINE_TEXT_FLAG)
        tvOffline.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //直接进入手机中的wifi网络设置界面
        }
        GlobalScope.launch(Dispatchers.Main) {
            customDialog.show()
            delay(3000)
            customDialog.dismiss()
        }
    }

    /**
     * 播放按键音
     */
    private fun playClickMusic() {
        if (music == null)
            music = MediaPlayer.create(this, R.raw.click)
        music?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        abilityManager.onStop()
        EventBus.getDefault().unregister(this)
    }
}