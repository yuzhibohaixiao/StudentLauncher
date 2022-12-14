package com.alight.android.aoa_launcher.activity

import android.content.Intent
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.ahwcx.ahwsdk.AbilityManager
import com.alight.ahwcx.ahwsdk.abilities.CalibrationAbility
import com.alight.ahwcx.ahwsdk.abilities.FeatureAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility
import com.alight.ahwcx.ahwsdk.abilities.PanelAbility.HardwareStatusHandler
import com.alight.ahwcx.ahwsdk.common.AbilityConnectionHandler
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.event.NetMessageEvent
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.NotifyCenterAdapter
import com.alight.android.aoa_launcher.ui.adapter.PersonalCenterFamilyAdapter
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_personal_center.*
import kotlinx.android.synthetic.main.activity_personal_center.tv_grade_person_center
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * ???????????????
 */
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
    private var updateBean: UpdateBean? = null
    private var splitFamilyList: List<List<Parent>>? = null
    private var selectFamilyPosition = 0    //  ???????????????????????????
    private var maxFamilySize = 4 //?????????????????????????????????
    private var notifyCenterAdapter: NotifyCenterAdapter? = null
    private var notifyCenterList = arrayListOf<CallArBean>()
    private var featureAbility: FeatureAbility? = null //?????? ????????????
    private var isFeatureAbilityInit = false

    override fun onResume() {
        super.onResume()
        initWifiState()
        getPresenter().getModel(
            Urls.UPDATE,
            hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
//            hashMapOf("device_type" to "LAMP"),
            UpdateBean::class.java
        )
        iv_wifi_icon.setImageResource(getPresenter().getCurrentWifiPersonDrawable(this))
        UpdateActivity.selectPage = 0
    }

    private fun initWifiState() {
        val wifiSsid = getPresenter().getWifiSsid(this)
        if (wifiSsid.isEmpty()) {
//            iv_wifi_icon.setImageResource(R.drawable.wifi_not_connected)
            tv_wifi_state.text = "?????????"
            tv_wifi_name.text = "????????????Wi-Fi"
            tv_wifi_state.setTextColor(resources.getColor(R.color.person_center_text_gray))
        } else {
//            iv_wifi_icon.setImageResource(R.drawable.wifi_connect_person_big)
            tv_wifi_state.text = "?????????"
            tv_wifi_name.text = wifiSsid.substring(1, wifiSsid.length - 1)
            tv_wifi_state.setTextColor(resources.getColor(R.color.person_center_text_blue))
        }
    }

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
            tv_gender_center.text = when (gender) {
                0 -> "????????????"
                1 -> "??????"
                else -> "??????"
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
            if (gradeType != null && !UserDBUtil.isLocalChanged) {
                getPresenter().setInitGrade(gradeType!!, tv_grade_person_center)
            } else {
                tv_grade_person_center.text = UserDBUtil.CURRENT_GRADE_ADD_TRIANGLE
            }
        }
        calibrationAbility =
            abilityManager.getAbility(CalibrationAbility::class.java, true, applicationContext)
        calibrationAbility?.bindLooper(Looper.myLooper()!!)
        panelAbility =
            abilityManager.getAbility(PanelAbility::class.java, true, applicationContext)
        panelAbility?.bindLooper(Looper.myLooper()!!)
        if (featureAbility == null) {
            featureAbility =
                abilityManager.getAbility(
                    FeatureAbility::class.java,
                    true,
                    applicationContext
                )
            featureAbility?.bindLooper(Looper.myLooper()!!)
            GlobalScope.launch(Dispatchers.Main) {
                featureAbility?.waitConnection(object : AbilityConnectionHandler {
                    override fun onServerConnected() {
                        isFeatureAbilityInit = true
                        featureAbility?.getEyeProtectionMode(object :
                            FeatureAbility.EyeProtectionModeHandler {
                            override fun onError(result: Map<String, Any>) {

                            }

                            override fun onSuccess(eyeProtectionMode: Boolean) {
                                iv_eyeshield_mode.setImageResource(if (eyeProtectionMode) R.drawable.eyeshield_mode_open else R.drawable.eyeshield_mode_close)
                                tv_eyeshield_mode.text = if (eyeProtectionMode) "?????????" else "?????????"
                            }
                        })
                    }
                })
            }
        }
        GlobalScope.launch {
            delay(500)
            try {
                //?????????????????????
                panelAbility?.getStatus(object : HardwareStatusHandler {
                    override fun onError(result: Map<String, Any>) {
                    }

                    override fun onSuccess(hardwareStatus: PanelAbility.HardwareStatus) {
                        //????????????
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
                        //??????
                        bsb_light.setProgress(hardwareStatus.light.toFloat())
                        //??????
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
            tv_control_today.text = "?????????" + if (playTimeBean.data.is_rest_day) "?????????" else "?????????"
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
        val allAppSize = getPresenter().getAllAppSize(this)
        tv_all_app_size.text = "????????? $allAppSize ?????????"
        val storageBean = StorageUtil.queryWithStorageManager(this)
        var totalSize = storageBean.totalSize
        var useSize = storageBean.useSize
        var remainSize = storageBean.remainSize
        val remainSizeMb = DecimalFormat("0").format(remainSize.toDouble() * 1024)
        getPresenter().putModel(
            Urls.DEVICE_SPACE,
            RequestBody.create(
                null,
                mapOf(
                    "dsn" to AccountUtil.getDSN(),
                    "free_space" to remainSizeMb
                ).toJson()
            ), BaseBean::class.java
        )
        val format = DecimalFormat("0.00")
        totalSize = format.format(BigDecimal(totalSize))
        useSize = format.format(BigDecimal(useSize))
        remainSize = format.format(BigDecimal(remainSize))
        tv_storage.text = "??????${remainSize}GB/32.00GB"
        val progress = useSize.toFloat() * 100 / totalSize.toFloat()
        update_progress.progress = progress.toInt()
        if (notifyCenterAdapter == null) {
            rv_notify_center.layoutManager = LinearLayoutManager(this)
            notifyCenterAdapter = NotifyCenterAdapter()
            rv_notify_center.adapter = notifyCenterAdapter
            val mmkv = LauncherApplication.getMMKV()
            val callArBeanListString: String =
                mmkv.getString("notifyInfo", "")!!
            if (callArBeanListString.isNotEmpty()) {
                notifyCenterList = Gson().fromJson(
                    callArBeanListString,
                    object : TypeToken<ArrayList<CallArBean>>() {}.type
                )
                var endTime = System.currentTimeMillis()     //???????????????
                var removeBeanList = arrayListOf<CallArBean>()
                notifyCenterList.forEachIndexed { index, callArBean ->
                    var startTime = callArBean.message.time//???????????????
                    var timeDifference = endTime - startTime;
                    var second = timeDifference / 1000;    //?????????
                    var minute = second / 60
                    var hour = minute / 60
                    var day = hour / 24
                    if (day >= 7) {
                        removeBeanList.add(callArBean)
                    }
                }
                removeBeanList.forEach {
                    notifyCenterList.remove(it)
                }
                notifyCenterList.reverse()
                notifyCenterAdapter?.setNewInstance(notifyCenterList)
            }
            //???????????????????????????
            notifyCenterAdapter?.setOnItemChildClickListener { adapter: BaseQuickAdapter<*, *>?, view: View, position: Int ->
                if (view.id == R.id.tv_av_callback) {
                    val callArBean = notifyCenterAdapter?.data?.get(position)
                    startPhoneWindow(callArBean!!)
                }
            }
        }
        RxTimerUtil.interval(5000) {
            iv_wifi_icon.setImageResource(getPresenter().getCurrentWifiPersonDrawable(this))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetNetEvent(event: NetMessageEvent) {
        //???????????? ??????UI
        getNetStateShowUI(event.netState)
    }

    /**
     * ?????????????????????????????????UI??????
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
            resources.getColor(R.color.person_center_text_green)
            ll_exit_personal_center.visibility = View.VISIBLE
        } else if (netState == 0) {
            rv_family_info.visibility = View.GONE
            ll_family_info_offline.visibility = View.VISIBLE
            ll_exit_personal_center.visibility = View.GONE
            Log.e(TAG, "onNetChanged:???????????? ")
        }
    }

    override fun setListener() {
        ll_family_info_offline.setOnClickListener(this)
        ll_back_personal_center.setOnClickListener(this)
        ll_exit_personal_center.setOnClickListener(this)
        tv_shutdown_personal_center.setOnClickListener(this)
        tv_pen_touch.setOnClickListener(this)
        tv_hand_touch.setOnClickListener(this)
        tv_grade_person_center.setOnClickListener(this)

        ll_camera_calibration.setOnClickListener(this)
        ll_mode_set.setOnClickListener(this)
        ll_unbind_device.setOnClickListener(this)
        ll_splash.setOnClickListener(this)
        ll_about_deivce.setOnClickListener(this)
        ll_power.setOnClickListener(this)
        fl_all_app.setOnClickListener(this)
//        fl_storage.setOnClickListener(this)
        fl_wifi_set.setOnClickListener(this)
        fl_update_system.setOnClickListener(this)
        tv_family_back.setOnClickListener(this)
        tv_family_next.setOnClickListener(this)
        iv_notify_clear.setOnClickListener(this)

        fl_eyeshield_mode.setOnClickListener(this)
        fl_clear_memory.setOnClickListener(this)
        ll_lab.setOnClickListener(this)

        familyAdapter.setOnItemClickListener { adapter, view, position ->
            val status = familyAdapter.data[position].status
            if (status.jpush_online == 0) {
                ToastUtils.showShort(this, "??????????????????")
            } else if (status.jpush_online == 1 && status.av == 0) {
                startPhoneWindow(position)
            } else {
                ToastUtils.showShort(this, "?????????????????????")
            }
        }

        GlobalScope.launch {
            delay(500)
            panelAbility?.subStatus(object : HardwareStatusHandler {
                override fun onError(result: Map<String, Any>) {

                }

                override fun onSuccess(hardwareStatus: PanelAbility.HardwareStatus) {
                    //??????????????????
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
                    //??????
                    bsb_light.setProgress(hardwareStatus.light.toFloat())
                    //??????
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

    /**
     * ???????????????????????????????????????????????????????????????
     */
    private fun startPhoneWindow(callArBean: CallArBean) {
        val intent = Intent("com.alight.trtcav.WindowActivity")
        intent.putExtra("called", 1)    //??????
        intent.putExtra("parentId", callArBean.message.fromUserId.toString())
        intent.putExtra("parentName", callArBean.message.fromUserInfo.name)
        intent.putExtra("parentAvatar", callArBean.message.fromUserInfo.avatar)
        intent.putExtra("childId", callArBean.message.userId.toString())
        intent.putExtra("callType", callArBean.message.type)
        intent.putExtra("token", tokenPair?.token)
        intent.putExtra("isCallback", true)
        try {
            this.startActivity(intent)
        } catch (e: Exception) {
            e.stackTraceToString()
        }
    }


    private fun startPhoneWindow(position: Int) {
        val parentInfo = familyAdapter.data[position]
        val intent = Intent("com.alight.trtcav.WindowActivity")
        if (parentInfo != null) {
            intent.putExtra("called", 1)    //??????
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
                    ToastUtils.showShort(this, "???????????????????????????")
                } else {
                    familyId = any.data.id
                    //https??????
//                    any.data.parents.forEach {
//                        it.avatar.replace("https", "http")
//                    }
                    familyAdapter.data.clear()
                    familyAdapter.addData(any.data.parents)

                    splitFamilyList =
                        ListSplitUtil.splitList(
                            any.data.parents,
                            maxFamilySize
                        ) as List<List<Parent>>
                    setFamilyUI(selectFamilyPosition)
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
                //??????????????????
//                if (any.code == 200) {
                //????????????
                ToastUtils.showShort(this, any.data)
                SPUtils.syncPutData("onlyShowSelectChild", false)
                SPUtils.syncPutData("rebinding", true)
                //?????????????????????
                SPUtils.syncPutData("splashClose", false)
                //??????????????????
                val mmkv = LauncherApplication.getMMKV()
                mmkv.putString("notifyInfo", "")
                finish()
                var intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
//                } else {
                //????????????
//                    ToastUtils.showShort(this, "????????????")
//                }
            }
            is UpdateBean -> {
                updateBean = any
                //??????????????????????????????
                setUpdateBtn(any)
//                if (familyId != null)
//                    getPresenter().showUpdateDialog(any, familyId!!, this)
            }
        }
    }

    /**
     * ????????????????????????
     */
    private fun setFamilyUI(selectFamilyPosition: Int) {
        if (!splitFamilyList.isNullOrEmpty()) {
            setFamilyData(splitFamilyList!![selectFamilyPosition])
            //??????????????????
            if (selectFamilyPosition < splitFamilyList!!.size - 1) {
                tv_family_next.visibility = View.VISIBLE
            } else {
                tv_family_next.visibility = View.GONE
            }
            //??????????????????
            if (selectFamilyPosition != 0) {
                tv_family_back.visibility = View.VISIBLE
            } else {
                tv_family_back.visibility = View.GONE
            }
        }
    }

    private fun setFamilyData(parentList: List<Parent>) {

        var parent1: Parent? = null
        var parent2: Parent? = null
        var parent3: Parent? = null
        var parent4: Parent? = null

        when (parentList.size) {
            1 -> {
                parent1 = parentList[0]
                ll_family1.visibility = View.VISIBLE
                ll_family2.visibility = View.INVISIBLE
                ll_family3.visibility = View.INVISIBLE
                ll_family4.visibility = View.INVISIBLE
            }
            2 -> {
                parent1 = parentList[0]
                parent2 = parentList[1]
                ll_family1.visibility = View.VISIBLE
                ll_family2.visibility = View.VISIBLE
                ll_family3.visibility = View.INVISIBLE
                ll_family4.visibility = View.INVISIBLE
            }
            3 -> {
                parent1 = parentList[0]
                parent2 = parentList[1]
                parent3 = parentList[2]
                ll_family1.visibility = View.VISIBLE
                ll_family2.visibility = View.VISIBLE
                ll_family3.visibility = View.VISIBLE
                ll_family4.visibility = View.INVISIBLE
            }
            4 -> {
                parent1 = parentList[0]
                parent2 = parentList[1]
                parent3 = parentList[2]
                parent4 = parentList[3]
                ll_family1.visibility = View.VISIBLE
                ll_family2.visibility = View.VISIBLE
                ll_family3.visibility = View.VISIBLE
                ll_family4.visibility = View.VISIBLE
            }
        }
        if (parent1 != null) {
            //????????????
            Glide.with(this).load(parent1.avatar)
                .error(if (parent1.role_type == 1) R.drawable.father else R.drawable.mather)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(iv_icon_family_item)
            //????????????
            tv_name_family_item.text = parent1.name
            //????????????
            if (parent1.status.jpush_online == 0) {
                iv_online_dot_family_item.setImageResource(R.drawable.online_state_gray)
                iv_online_state_family_item.text = "??????"
            } else if (parent1.status.jpush_online == 1 && parent1.status.av == 0) {
                iv_online_dot_family_item.setImageResource(R.drawable.online_state_green)
                iv_online_state_family_item.text = "??????"
            } else {
                iv_online_dot_family_item.setImageResource(R.drawable.online_state_yellow)
                iv_online_state_family_item.text = "??????"
            }
        }
        if (parent2 != null) {
            Glide.with(this).load(parent2.avatar)
                .error(if (parent2.role_type == 1) R.drawable.father else R.drawable.mather)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(iv_icon_family_item2)
            tv_name_family_item2.text = parent2.name
            if (parent2.status.jpush_online == 0) {
                iv_online_dot_family_item2.setImageResource(R.drawable.online_state_gray)
                iv_online_state_family_item2.text = "??????"
            } else if (parent2.status.jpush_online == 1 && parent2.status.av == 0) {
                iv_online_dot_family_item2.setImageResource(R.drawable.online_state_green)
                iv_online_state_family_item2.text = "??????"
            } else {
                iv_online_dot_family_item2.setImageResource(R.drawable.online_state_yellow)
                iv_online_state_family_item2.text = "??????"
            }
        }
        if (parent3 != null) {
            Glide.with(this).load(parent3.avatar)
                .error(if (parent3.role_type == 1) R.drawable.father else R.drawable.mather)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(iv_icon_family_item3)
            tv_name_family_item3.text = parent3.name
            if (parent3.status.jpush_online == 0) {
                iv_online_dot_family_item3.setImageResource(R.drawable.online_state_gray)
                iv_online_state_family_item3.text = "??????"
            } else if (parent3.status.jpush_online == 1 && parent3.status.av == 0) {
                iv_online_dot_family_item3.setImageResource(R.drawable.online_state_green)
                iv_online_state_family_item3.text = "??????"
            } else {
                iv_online_dot_family_item3.setImageResource(R.drawable.online_state_yellow)
                iv_online_state_family_item3.text = "??????"
            }
        }
        if (parent4 != null) {
            Glide.with(this).load(parent4.avatar)
                .error(if (parent4.role_type == 1) R.drawable.father else R.drawable.mather)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(iv_icon_family_item4)
            tv_name_family_item4.text = parent4.name

            if (parent4.status.jpush_online == 0) {
                iv_online_dot_family_item4.setImageResource(R.drawable.online_state_gray)
                iv_online_state_family_item4.text = "??????"
            } else if (parent4.status.jpush_online == 1 && parent4.status.av == 0) {
                iv_online_dot_family_item4.setImageResource(R.drawable.online_state_green)
                iv_online_state_family_item4.text = "??????"
            } else {
                iv_online_dot_family_item4.setImageResource(R.drawable.online_state_yellow)
                iv_online_state_family_item4.text = "??????"
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private fun setUpdateBtn(any: UpdateBean) {
        var needUpdate = false
        for (position in 0 until any.data.size) {
            val data = any.data[position]
            Log.i(TAG, "setUpdateBtn: $data")
            if (data.format == 1 && SPUtils.getData(
                    "configVersion",
                    1
                ) as Int >= data.version_code || AppUtils.getVersionCode(
                    this,
                    data.app_info.package_name
                ) >= data.version_code || (data.format == 3 && data.version_name == Build.DISPLAY)
            ) {
            } else {
                needUpdate = true
                break
            }
        }
        if (needUpdate) {
            //????????????
            tv_update_state.text = "????????????"
//            iv_update_icon.setImageResource(R.drawable.system_need_update)
            tv_wifi_state.setTextColor(resources.getColor(R.color.person_center_text_blue))
        } else {
            //????????????
            tv_update_state.text = "??????????????????"
//            iv_update_icon.setImageResource(R.drawable.system_no_update)
            tv_wifi_state.setTextColor(resources.getColor(R.color.person_center_text_gray))
        }
    }

    override fun onError(error: String) {
        Log.i("PersonCenter", "onError: $error")
        ToastUtils.showShort(this, "??????????????????")
    }

    private fun showAboutDeviceDialog() {
        val powerDialog = CustomDialog(this, R.layout.dialog_about_device)
        val ivClose = powerDialog.findViewById<ImageView>(R.id.iv_close_dialog)
        ivClose.setOnClickListener { powerDialog.dismiss() }
        val deviceTupe = Build.DEVICE
        var deviceName: String? = null
        when (deviceTupe) {
            "LAMP", "LAMP_AL" -> {
                deviceName = "??????AR???????????????G9"
            }
            else -> {
                deviceName = "??????"
            }
        }
        powerDialog.findViewById<TextView>(R.id.tv_device_name).text = deviceName
        powerDialog.findViewById<TextView>(R.id.tv_device_type).text =
            deviceTupe //???????????? (LAMP LAMP_AL)
        powerDialog.findViewById<TextView>(R.id.tv_device_model).text = Build.MODEL
        powerDialog.findViewById<TextView>(R.id.tv_android_version).text = Build.VERSION.RELEASE
        powerDialog.findViewById<TextView>(R.id.tv_alight_os_version).text = Build.DISPLAY
        powerDialog.findViewById<TextView>(R.id.tv_launcher_version).text = "" +
                AppUtils.getVersionName(this, AppConstants.LAUNCHER_PACKAGE_NAME)
        powerDialog.findViewById<TextView>(R.id.tv_aoa_version).text = "" +
                AppUtils.getVersionName(this, AppConstants.AOA_PACKAGE_NAME)
        powerDialog.findViewById<TextView>(R.id.tv_av_version).text = "" +
                AppUtils.getVersionName(this, AppConstants.AV_PACKAGE_NAME)
        powerDialog.findViewById<TextView>(R.id.tv_ahwc_version).text = "" +
                AppUtils.getVersionName(this, AppConstants.AHWCX_PACKAGE_NAME)
        powerDialog.findViewById<TextView>(R.id.tv_device_nunber).text = AccountUtil.getDSN()
        powerDialog.show()
    }

    private fun showPowerDialog() {
        val powerDialog = CustomDialog(this, R.layout.dialog_power)
        val ivClose = powerDialog.findViewById<ImageView>(R.id.iv_close_dialog)
        ivClose.setOnClickListener { powerDialog.dismiss() }
        powerDialog.findViewById<View>(R.id.ll_standby).setOnClickListener { v: View? ->
            getPresenter().screenOff()
            powerDialog.dismiss()
        }
        powerDialog.findViewById<View>(R.id.ll_restart).setOnClickListener { v: View? ->
            val confirmDialog = CustomDialog(this, R.layout.dialog_confirm_new)
            val tvTitle = confirmDialog.findViewById<TextView>(R.id.tv_title_dialog)
            val tvContent = confirmDialog.findViewById<TextView>(R.id.tv_content_dialog)
            confirmDialog.findViewById<TextView>(R.id.confirm).setOnClickListener {
                //????????????
                getPresenter().reboot(this)
                powerDialog.dismiss()
            }
            confirmDialog.findViewById<TextView>(R.id.cancel).setOnClickListener {
                confirmDialog.dismiss()
            }
            tvTitle.text = "??????"
            tvContent.text = "?????????????????????"
            powerDialog.dismiss()
            confirmDialog.show()
        }
        powerDialog.findViewById<View>(R.id.ll_shutdown).setOnClickListener { v: View? ->
            val confirmDialog = CustomDialog(this, R.layout.dialog_confirm_new)
            val tvTitle = confirmDialog.findViewById<TextView>(R.id.tv_title_dialog)
            val tvContent = confirmDialog.findViewById<TextView>(R.id.tv_content_dialog)
            confirmDialog.findViewById<TextView>(R.id.confirm).setOnClickListener {
                //????????????
                getPresenter().shutdown(this)
                powerDialog.dismiss()
            }
            confirmDialog.findViewById<TextView>(R.id.cancel).setOnClickListener {
                confirmDialog.dismiss()
            }
            tvTitle.text = "??????"
            tvContent.text = "?????????????????????"
            powerDialog.dismiss()
            confirmDialog.show()
        }
        powerDialog.show()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_back_personal_center ->
                finish()
            //????????????
            R.id.ll_exit_personal_center -> {
//                getPresenter().deleteModel(Urls.BIND_PUSH, BaseBean::class.java)
                UserDBUtil.isLocalChanged = false
                SPUtils.syncPutData("onlyShowSelectChild", true)
                setResult(AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER)
                finish()
            }
            //??????
            R.id.ll_power -> {
                showPowerDialog()
            }
            R.id.ll_camera_calibration -> {
                calibrationAbility?.startCalibration()
            }
            R.id.fl_wifi_set ->
                getPresenter().startWifiModule(false)
            R.id.ll_family_info_offline -> {
                showOfflineDialog()
            }
            R.id.fl_update_system -> {
                if (updateBean != null)
                    getPresenter().showUpdateActivity(updateBean!!, this)
            }
            R.id.ll_unbind_device -> {
                if (familyId != null)
                    getPresenter().showUnbindDeviceDialog(familyId!!, this)
            }
            //????????????
            R.id.ll_about_deivce -> {
                showAboutDeviceDialog()
            }

            /*
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
                */
/*
                //todo ???????????????????????????????????????
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
                 }*//*


            }
*/
            R.id.ll_splash -> {
                //????????????????????????
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
            R.id.tv_grade_person_center -> {
                getPresenter().showSelectGradeDialog(this, tv_grade_person_center)
            }
            R.id.fl_all_app -> {
                getPresenter().showAppSetting(this)
            }
            R.id.fl_storage -> {
//                getPresenter().showStorage(this)
            }
            R.id.tv_family_back -> {
                selectFamilyPosition--
                setFamilyUI(selectFamilyPosition)
            }
            R.id.tv_family_next -> {
                selectFamilyPosition++
                setFamilyUI(selectFamilyPosition)
            }
            //??????????????????
            R.id.iv_notify_clear -> {
                //??????????????????
                notifyCenterList.clear()
                notifyCenterAdapter?.notifyDataSetChanged()
                LauncherApplication.getMMKV().remove("notifyInfo")
            }
            //????????????
            R.id.fl_clear_memory -> {
                if (isFeatureAbilityInit) {
                    featureAbility?.startUserMemoryClean()
                    ToastUtils.showShort(this, "???????????????")
                }
            }
            //????????????
            R.id.fl_eyeshield_mode -> {
                if (isFeatureAbilityInit) {
                    featureAbility?.getEyeProtectionMode(object :
                        FeatureAbility.EyeProtectionModeHandler {
                        override fun onError(result: Map<String, Any>) {

                        }

                        override fun onSuccess(eyeProtectionMode: Boolean) {
                            if (eyeProtectionMode) {
                                featureAbility?.closeEyeProtectionMode()
                            } else {
                                featureAbility?.openEyeProtectionMode()
                            }
                            iv_eyeshield_mode.setImageResource(if (!eyeProtectionMode) R.drawable.eyeshield_mode_open else R.drawable.eyeshield_mode_close)
                            tv_eyeshield_mode.text = if (!eyeProtectionMode) "?????????" else "?????????"
                        }
                    })
                }
            }
            R.id.ll_mode_set -> {
                getPresenter().showModeSetDialog(this)
            }
            R.id.ll_lab -> {
                if (panelAbility != null) {
                    getPresenter().showLabDialog(this, panelAbility!!)
                }
            }
        }
    }

    private fun showOfflineDialog() {
        val customDialog = CustomDialog(this, R.layout.dialog_offline)
        val tvOffline = customDialog.findViewById<TextView>(R.id.tv_offline)
        val spannableString = SpannableString(tvOffline.text)
        spannableString.setSpan(UnderlineSpan(), 7, 11, Paint.UNDERLINE_TEXT_FLAG)
        tvOffline.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) //????????????????????????wifi??????????????????
        }
        GlobalScope.launch(Dispatchers.Main) {
            customDialog.show()
            delay(3000)
            customDialog.dismiss()
        }
    }

    /**
     * ???????????????
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