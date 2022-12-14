package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.content.*
import android.content.Intent.ACTION_SHUTDOWN
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import cn.jpush.android.api.JPushInterface
import com.alight.ahwcx.ahwsdk.AbilityManager
import com.alight.ahwcx.ahwsdk.abilities.*
import com.alight.ahwcx.ahwsdk.common.AbilityConnectionHandler
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.*
import com.alight.android.aoa_launcher.common.broadcast.HomeWatcherReceiver
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.event.CheckUpdateEvent
import com.alight.android.aoa_launcher.common.event.HomeKeyEvent
import com.alight.android.aoa_launcher.common.event.NetMessageEvent
import com.alight.android.aoa_launcher.common.event.ParentControlEvent
import com.alight.android.aoa_launcher.common.i.LauncherListener
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.net.INetEvent
import com.alight.android.aoa_launcher.net.NetTools
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.LauncherCenterAdapter
import com.alight.android.aoa_launcher.ui.adapter.LauncherPagerAdapter
import com.alight.android.aoa_launcher.ui.adapter.LauncherRightAdapter
import com.alight.android.aoa_launcher.ui.adapter.QualityHorizontalAdapter
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.view_stub_center_launcher.*
import kotlinx.coroutines.*
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat


/**
 * @author wangzhe
 * ??????Launcher??????
 */
class NewLauncherActivity : BaseActivity(), View.OnClickListener, LauncherListener, INetEvent {

    private var netState = 1
    private var tokenPair: TokenPair? = null
    private var launcherCenterAdapter: LauncherCenterAdapter? = null
    private var launcherRightAdapter: LauncherRightAdapter? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var TAG = "NewLauncherActivity"
    private var uri: Uri? = null
    private var splashCloseFlag = false
    private var qualityHorizontalAdapter: QualityHorizontalAdapter? = null
    private var interactionAbility: InteractionAbility? = null //??????????????????
    private var panelAbility: PanelAbility? = null //??????????????????(?????????)
    private var audioAbility: AudioAbility? = null //??????????????????
    private var touchAbility: TouchAbility? = null //??????????????????
    private val abilityManager = AbilityManager("launcher", "5", "234")
    private var abilityInitSuccessful = false
    private var touchAbilityInitSuccessful = false
    private var audioInitSuccessful = false
    private var stopHeart = false
    private var playTimeBean: PlayTimeBean? = null
    private var shutdownReceiver: ShutdownReceiver? = null
    private var isStartHeart = false
    private var onresumeFlag = false
    private var featureAbility: FeatureAbility? = null //?????? ????????????
    private var isFeatureAbilityInit = false
    private var mUserId = -1
    private var mode = "student"
    private var isRefresh = false
    private var heartCoroutineScope: Job? = null

    private lateinit var launcherPagerAdapter: LauncherPagerAdapter
    private var vsCenterInflate: View? = null


    private var selectBook: AppTypeBean = AppTypeBean(
        R.drawable.yxkw, "com.jxw.pedu.clickread",
        "com.jxw.pedu.clickread.MainActivity",
        mapOf("StartArgs" to "??????")
    )

    /**
     * ????????????????????????????????????????????????????????????
     */
    private val bookList = arrayListOf(
        AppTypeBean
            (
            R.drawable.yxkw, "com.jxw.pedu.clickread",
            "com.jxw.pedu.clickread.MainActivity",
            mapOf("StartArgs" to "??????")
        ), AppTypeBean(
            R.drawable.yxkw, "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf("StartArgs" to "d:/????????????/??????|e:JWFD")
        ), AppTypeBean(
            R.drawable.yxkw, "com.jxw.pedu.clickread",
            "com.jxw.pedu.clickread.MainActivity",
            mapOf("StartArgs" to "??????")
        )
    )

    //???????????????????????? true
    private var guideUserUpdate = false

    companion object {
        lateinit var mINetEvent: INetEvent
//        var isRegister: Boolean = false
    }

    private val handler =
        Handler { msg ->
            if (msg.what == 0x123) {
                if (uri != null) {
                    val cursor =
                        contentResolver.query(uri!!, null, null, null, null)
                    if (null != cursor) {
                        cursor.moveToNext()
                        val id =
                            cursor.getInt(cursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN))
                        val name =
                            cursor.getString(cursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_NAME))
                        val token =
                            cursor.getString(cursor.getColumnIndex(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN))
                        /*Toast.makeText(
                            this@LauncherActivity,
                            "ID=$id ; name=$name; token=$token",
                            Toast.LENGTH_SHORT
                        ).show()*/
                        Log.i(TAG, "ID=$id ; name=$name; token=$token")

                    }
                } else {
                    Log.i(TAG, "??????????????????")
//                    Toast.makeText(this@LauncherActivity, "??????????????????", Toast.LENGTH_SHORT).show()
                }
            }
            false
        }


    override fun setListener() {
        tv_ar_launcher.setOnClickListener(this)
        tv_chinese_launcher.setOnClickListener(this)
        tv_mathematics_launcher.setOnClickListener(this)
        tv_english_launcher.setOnClickListener(this)
        tv_quality_launcher.setOnClickListener(this)

        iv_query_word.setOnClickListener(this)
        iv_chinese_words.setOnClickListener(this)
        iv_english_translation.setOnClickListener(this)
        iv_title_query.setOnClickListener(this)
        iv_oral_correction.setOnClickListener(this)
        iv_article_correction.setOnClickListener(this)
        iv_favorites.setOnClickListener(this)
        iv_study_plan.setOnClickListener(this)
        iv_call_parent.setOnClickListener(this)

        iv_user_icon_new_launcher.setOnClickListener(this)
        tv_user_name_new_launcher.setOnClickListener(this)
//        iv_all_app_launcher.setOnClickListener(this)
//        tv_grade_person_center.setOnClickListener(this)
        iv_book_reading.setOnClickListener(this)
//        iv_az_store.setOnClickListener(this)
//        tv_task_challenges.setOnClickListener(this)
//        iv_av_launcher.setOnClickListener(this)
        vs_center_launcher.setOnInflateListener { stub, inflated ->
            tv_book_click.setOnClickListener(this)
            fl_classroom_sync.setOnClickListener(this)
        }
//        iv_ip_image.setOnClickListener(this)
        fl_wifi_module.setOnClickListener(this)
        iv_call_parent_child.setOnClickListener(this)

        iv_main_top_child_launcher.setOnClickListener(this)
        iv_app_select_top_child_launcher.setOnClickListener(this)
        iv_app_list_top_child_launcher.setOnClickListener(this)
        vp2_launcher.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectChildUI(position)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //???onResume????????????
        /* if (onresumeFlag) {
             return
         } else {
             onresumeFlag = true
         }*/
        /*    if (!isRefresh) {
                isRefresh = true
                Log.i(TAG, "onResume: ?????????")
                return
            }*/
        Log.i(TAG, "onResume: ?????????")
        val splashClose = SPUtils.getData("splashClose", false) as Boolean
        Log.i(TAG, "splashClose = $splashClose splashCloseFlag = $splashCloseFlag")

        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
        val rebinding = SPUtils.getData("rebinding", false) as Boolean
        if (!splashClose && !splashCloseFlag && tokenPairCache.isNullOrEmpty() || (rebinding && !splashClose)) {
            /*   ToastUtils.showShort(
                   this,
                   "??????????????? $splashClose $splashCloseFlag ${tokenPairCache.isNullOrEmpty()} $rebinding"
               )*/
            Log.i(TAG, "???????????????")
//        ??????????????????????????????????????????
            activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
        } else {
            initAccountUtil()
            if (audioInitSuccessful) {
                audioAbility?.startRecording()
            }
//            getPresenter().sendMenuEnableBroadcast(this, true)
            /*if (!splashCloseFlag && !guideUserUpdate)  //??????????????????
            {
                getPresenter().getModel(Urls.UPDATE, hashMapOf(), UpdateBean::class.java)
                //???????????????????????????
                guideUserUpdate = true
            }*/
        }
        //??????????????????
        if (rebinding) {
            initAccountUtil()
//            SPUtils.syncPutData("rebinding", false)
        }
        if (!splashCloseFlag && tv_user_name_new_launcher.text.isNullOrEmpty()) {
            Glide.with(this@NewLauncherActivity)
                .load(tokenPair?.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .error(if (tokenPair?.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
                .into(iv_user_icon_new_launcher)
            tv_user_name_new_launcher.text = tokenPair?.name
        }
        setInteraction()
        resetTouchAbility()
        // ??????????????????
        if (AccountUtil.currentUserId != null) {
            getPresenter().getModel(
                "${Urls.STUDY_PLAN}${AccountUtil.currentUserId}",
                hashMapOf(),
                StudyPlanBean::class.java
            )
        }
        splashCloseFlag = false
        qualityHorizontalAdapter?.resetAppNotifyAdapter()
        iv_wifi_module.setImageResource(getPresenter().getCurrentWifiDrawable(this))
        //?????????????????????????????????
        if (isFeatureAbilityInit) {
            featureAbility?.startTaskMemoryClean()
        }
        //??????????????????
//        getPresenter().resetInputType(this)
        setLauncherMode()
    }

    @Synchronized
    private fun setLauncherMode() {
        val mmkv = LauncherApplication.getMMKV()
        val newMode = mmkv.decodeString("mode")
        if (newMode != null && mode != newMode) {
            mode = newMode
            CoroutineScope(Dispatchers.Main).launch {
                if (mode == "child") {
                    ll_student_top.visibility = View.GONE
                    fl_student_main.visibility = View.GONE
                    iv_call_parent_child.visibility = View.VISIBLE
                    ll_child_top.visibility = View.VISIBLE
                    vp2_launcher.visibility = View.VISIBLE
                    //?????????Launcher
                    launcherPagerAdapter = LauncherPagerAdapter(this@NewLauncherActivity)
                    vp2_launcher.adapter = launcherPagerAdapter
                    fl_launcher.setBackgroundResource(R.drawable.child_launcher_bg)
/*
                Glide.with(this).load(R.drawable.child_launcher_bg).into(object :
                    CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        fl_launcher.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
*/
                } else {
                    iv_call_parent_child.visibility = View.GONE
                    ll_child_top.visibility = View.GONE
                    vp2_launcher.visibility = View.GONE
                    ll_student_top.visibility = View.VISIBLE
                    fl_student_main.visibility = View.VISIBLE
                    //?????????Launcher
                    fl_launcher.setBackgroundResource(R.drawable.launcher_bg_new)
/*
                Glide.with(this).load(R.drawable.launcher_bg_new).into(object :
                    CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        fl_launcher.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
*/
                }
            }
        }
    }

    /**
     * ????????????????????????
     */
    private fun resetTouchAbility() {
        if (touchAbility == null) {
            touchAbility =
                abilityManager.getAbility(TouchAbility::class.java, true, applicationContext)
            GlobalScope.launch(Dispatchers.Main) {
                touchAbilityInitSuccessful = touchAbility?.waitConnectionAsync()!!
                if (touchAbilityInitSuccessful) {
                    try {
                        Log.i(TAG, "????????????????????? enableDisorderlyPointChecker: ")
                        touchAbility?.enableDisorderlyPointChecker()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (touchAbilityInitSuccessful) {
            try {
                touchAbility?.enableDisorderlyPointChecker()
                Log.i(TAG, "????????????????????? enableDisorderlyPointChecker: ")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * ?????????????????????
     */
    private fun setInteraction() {
        if (panelAbility == null) {
            panelAbility =
                abilityManager.getAbility(PanelAbility::class.java, true, applicationContext)
            GlobalScope.launch(Dispatchers.Main) {
                if (panelAbility?.waitConnectionAsync()!!) {
                    panelAbility?.getOpticalEngineMode(object :
                        PanelAbility.OpticalEngineModeHandler {
                        override fun onError(result: Map<String, Any>) {
                        }

                        override fun onSuccess(mode: PanelAbility.OpticalEngineMode) {
                            val highFpsMode =
                                LauncherApplication.getMMKV().getBoolean("highFpsMode", false)
                            if (mode == PanelAbility.OpticalEngineMode.HIGH_FPS_MODE && !highFpsMode) {
                                panelAbility?.setOpticalEngineMode(PanelAbility.OpticalEngineMode.NORMAL_MODE)
                            }
                        }
                    })
                }
            }
        }
        if (interactionAbility == null) {
            interactionAbility =
                abilityManager.getAbility(
                    InteractionAbility::class.java,
                    true,
                    applicationContext
                )
            GlobalScope.launch(Dispatchers.Main) {
                abilityInitSuccessful = interactionAbility?.waitConnectionAsync()!!
                if (abilityInitSuccessful) {
                    try {
                        getPresenter().startInteractionWindow(
                            interactionAbility!!,
                            InteractionAbility.InteractiveMode.FINGER_TOUCH
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            //??????launcher?????????????????????
        } else if (interactionAbility != null && abilityInitSuccessful) {
            try {
                getPresenter().startInteractionWindow(
                    interactionAbility!!,
                    InteractionAbility.InteractiveMode.FINGER_TOUCH
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initAccountUtil() {
        if (AccountUtil.currentUserId != null && !splashCloseFlag && tv_user_name_new_launcher.text.toString()
                .isNotEmpty() && tokenPair == null
        )
            return
//        AccountUtil.register(this)
        val userId = SPUtils.getData(AppConstants.USER_ID, -1) as Int
        if (userId != -1) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val allToken = AccountUtil.getAllToken()
                    allToken.forEach {
                        if (it.userId == userId) {
                            mUserId = it.userId
                            //????????????????????????
                            getPresenter().postModel(
                                Urls.STUDENT_LOGIN,
                                RequestBody.create(
                                    null,
                                    mapOf(
                                        "user_id" to it.userId,
                                        "dsn" to AccountUtil.getDSN()
                                    ).toJson()
                                ), LoginBean::class.java
                            )
                            //?????????????????????????????????????????????
//                            AccountUtil.selectUser(it.userId)
//                            stopHeart = false
//                            isStartHeart = false
                            // ??????????????????
                            getPresenter().getModel(
                                "${Urls.STUDY_PLAN}${it.userId}",
                                hashMapOf(),
                                StudyPlanBean::class.java
                            )
                            // ????????????????????????
                            if (!isStartHeart)
                                getPresenter().postModel(
                                    Urls.BIND_PUSH,
                                    RequestBody.create(
                                        null,
                                        mapOf(
                                            AppConstants.REGISTRATION_ID to JPushInterface.getRegistrationID(
                                                this@NewLauncherActivity
                                            )
                                        ).toJson()
                                    ),
                                    JPushBindBean::class.java
                                )
                            getPresenter().getModel(
                                Urls.PLAY_TIME,
                                hashMapOf("user_id" to it.userId),
                                PlayTimeBean::class.java
                            )
                            /*    if (!splashCloseFlag && !guideUserUpdate)  //??????????????????
                                {
                                    getPresenter().getModel(
                                        Urls.UPDATE,
                                        hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
    //                                    hashMapOf("device_type" to "LAMP"),
                                        UpdateBean::class.java
                                    )
                                    //???????????????????????????
                                    guideUserUpdate = true
                                }*/
                        }
                    }
                    //??????????????????
                    getPresenter().setPersonInfo(this@NewLauncherActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }


    override fun initData() {
        mINetEvent = this
        EventBus.getDefault().register(this)
        CoroutineScope(Dispatchers.IO).launch {
            val tokenPairCache = SPUtils.getData("tokenPair", "") as String
            //??????????????????????????????
            if (tokenPairCache.isNotEmpty()) {
                tokenPair = Gson().fromJson(tokenPairCache, TokenPair::class.java)
                writeUserInfo(tokenPair!!)
            }
        }
        //??????????????????????????????????????????????????????
        AccountUtil.run()
        initAccountUtil()
        //???????????????
        initPermission()
        //??????contentProvider???????????????
        contentResolver.registerContentObserver(
            LauncherContentProvider.URI,
            true,
            contentObserver
        )
        startHardwareControl()
        startActivateService()
        Log.i(TAG, "DSN: ${AccountUtil.getDSN()}")
        SPUtils.syncPutData("splashClose", false)
        Log.i(TAG, "splashClose initData")
        //??????splash?????????????????????
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.i(TAG, "registerForActivityResult")
            when (it.resultCode) {
                //????????????????????????????????????
                AppConstants.RESULT_CODE_SELECT_USER_BACK -> {
                    stopHeart = false
                    isStartHeart = false
                    if (!isStartHeart)
                        getPresenter().postModel(
                            Urls.BIND_PUSH,
                            RequestBody.create(
                                null,
                                mapOf(
                                    AppConstants.REGISTRATION_ID to JPushInterface.getRegistrationID(
                                        this@NewLauncherActivity
                                    )
                                ).toJson()
                            ),
                            JPushBindBean::class.java
                        )
                    splashCloseFlag = true
                    guideUserUpdate = true
                    SPUtils.syncPutData("rebinding", false)
                    val syncPutData = SPUtils.syncPutData("splashClose", true)
                    Log.i(TAG, "initAccountUtil")
                    //??????????????????????????????????????????
                    initAccountUtil()
                }
                //??????Launcher??????????????????
                AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER -> {
                    stopHeart = true
                    isStartHeart = false
                    activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
                }
            }
        }
        // ????????????????????????
        if (!isStartHeart)
            getPresenter().postModel(
                Urls.BIND_PUSH,
                RequestBody.create(
                    null,
                    mapOf(
                        AppConstants.REGISTRATION_ID to JPushInterface.getRegistrationID(this@NewLauncherActivity)
                    ).toJson()
                ),
                JPushBindBean::class.java
            )
        getPresenter().getModel(
            Urls.PLAY_TIME,
            hashMapOf("user_id" to tokenPair?.userId.toString()),
            PlayTimeBean::class.java
        )
        if (!InternetUtil.isNetworkAvalible(this)) {
            netState = 0
        }
        initAbility()
        initReceiver()
        Log.i(TAG, "DSN: " + AccountUtil.getDSN())
        RxTimerUtil.interval(5000) {
            iv_wifi_module.setImageResource(getPresenter().getCurrentWifiDrawable(this))
        }
        val storageBean = StorageUtil.queryWithStorageManager(this)
        var remainSize = storageBean.remainSize
        val remainSizeMb = DecimalFormat("0").format(remainSize.toDouble() * 1024).toLong()
        //?????????????????????????????????
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
        /*getPresenter().getModel(
            Urls.DEVICE_INSTALL,
            hashMapOf("dsn" to AccountUtil.getDSN()),
            AppUninstallBean::class.java
        )*/
        //??????????????????
//        getPresenter().resetInputType(this)
        //????????????
//        MyAppManager.init(this)
        getPresenter().getModel(
            Urls.APPS_SORT + AccountUtil.getDSN(),
            hashMapOf(), AppSortBean::class.java
        )

    }

    private var mHomeKeyReceiver: HomeWatcherReceiver? = null

    private fun initReceiver() {
        if (shutdownReceiver == null) {
            shutdownReceiver = ShutdownReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_SHUTDOWN)
            registerReceiver(shutdownReceiver, intentFilter)
        }
        if (mHomeKeyReceiver == null) {
            mHomeKeyReceiver = HomeWatcherReceiver()
            val homeFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            registerReceiver(mHomeKeyReceiver, homeFilter)
//                isRegister = true
        }
        /*     val pm: PackageManager = packageManager
             val intent = Intent()
             intent.action = Intent.ACTION_CLOSE_SYSTEM_DIALOGS
             val resolveInfos = pm.queryBroadcastReceivers(intent, 0)
             if (resolveInfos == null || resolveInfos.isEmpty()) {*/
        //??????????????????BroadcastReceiver

        /*  var isContains = false
          resolveInfos.forEach {
              if (it.activityInfo.name == HomeWatcherReceiver::class.java.name) {
                  isContains = true
                  return@forEach
              }
          }*/
    }

    inner class ShutdownReceiver : BroadcastReceiver() {

        private val TAG = "ShutDownBroadcastReceiver"

        override fun onReceive(context: Context, intent: Intent) {
            AccountUtil.isShutdown = true
            Log.i("TAG", "????????????")
            val mmkv = LauncherApplication.getMMKV()
            mmkv.encode(AppConstants.SHUTDOWN, true)
//        Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            NetUtils.intance.postInfo(
                Urls.SHUTDOWN, RequestBody.create(
                    null,
                    mapOf(
                        "dsn" to AccountUtil.getDSN()
                    ).toJson()
                ), BaseBean::class.java, object : NetUtils.NetCallback {
                    override fun onSuccess(any: Any) {
                        Log.i(TAG, "onSuccess: ??????????????????")
                    }

                    override fun onError(error: String) {

                    }
                }
            );
        }
    }

    private fun initAbility() {
        if (featureAbility == null) {
            featureAbility =
                abilityManager.getAbility(
                    FeatureAbility::class.java,
                    true,
                    applicationContext
                )
            featureAbility?.bindLooper(Looper.myLooper()!!)
            featureAbility?.waitConnection(object : AbilityConnectionHandler {
                override fun onServerConnected() {
                    isFeatureAbilityInit = true
                }
            })
        }
        if (audioAbility == null) {
            audioAbility =
                abilityManager.getAbility(
                    AudioAbility::class.java,
                    true,
                    applicationContext
                )
            GlobalScope.launch(Dispatchers.Main) {
                audioAbility?.waitConnection(object : AbilityConnectionHandler {
                    override fun onServerConnected() {
                        audioAbility?.startRecording()
                        try {
                            audioInitSuccessful = true
                            audioAbility?.subOpenAppResult(object :
                                AudioAbility.OpenAppCallback {
                                override fun onReceive(appArgs: MutableList<AudioAbility.ToApp>?) {
                                    appArgs?.forEach {
                                        //AOA??????
                                        if (it.type == AudioAbility.ParamType.AOA) {
                                            val aoaParam = it.aoaParam
                                            if (!StringUtils.isEmpty(aoaParam?.appId)) {
                                                val startAoaApp =
                                                    getPresenter().startAoaApp(
                                                        this@NewLauncherActivity,
                                                        aoaParam?.appId!!.toInt(),
                                                        aoaParam.route!!
                                                    )
                                                if (it.touchMode != null && startAoaApp) {
                                                    getPresenter().startInteractionWindow(
                                                        interactionAbility!!,
                                                        it.touchMode!!
                                                    )
                                                }

                                            } else {
                                                GlobalScope.launch(Dispatchers.Main) {
                                                    ToastUtils.showLong(
                                                        this@NewLauncherActivity,
                                                        "??????????????????????????????????????????"
                                                    )
                                                }
                                            }
                                            //???????????????????????????
                                        } else if (it.type == AudioAbility.ParamType.THIRD) {
                                            val thirdPartyParam = it.thirdPartyParam
                                            if (!StringUtils.isEmpty(thirdPartyParam?.packetName)) {
                                                val startActivity =
                                                    getPresenter().startActivity(
                                                        this@NewLauncherActivity,
                                                        thirdPartyParam?.packetName!!,
                                                        thirdPartyParam.className!!,
                                                        thirdPartyParam.param
                                                    )
                                                if (it.touchMode != null && startActivity) {
                                                    getPresenter().startInteractionWindow(
                                                        interactionAbility!!,
                                                        it.touchMode!!
                                                    )
                                                }
                                            } else {
                                                GlobalScope.launch(Dispatchers.Main) {
                                                    ToastUtils.showLong(
                                                        this@NewLauncherActivity,
                                                        "??????????????????????????????????????????"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                })

//                audioInitSuccessful = audioAbility?.waitConnectionAsync()!!
/*
                if (abilityInitSuccessful) {
                    try {
                        audioAbility?.subOpenAppResult(object : AudioAbility.OpenAppCallback {
                            override fun onReceive(appArgs: MutableList<AudioAbility.ToApp>?) {
                                appArgs?.forEach {
                                    if (it.type == AudioAbility.ParamType.AOA) {
                                        val aoaParam = it.aoaParam
                                        getPresenter().startAoaApp(
                                            this@NewLauncherActivity,
                                            aoaParam?.appId!!.toInt(),
                                            aoaParam.route!!
                                        )
                                    } else if (it.type == AudioAbility.ParamType.THIRD) {
                                        val thirdPartyParam = it.thirdPartyParam
                                        getPresenter().startActivity(
                                            this@NewLauncherActivity,
                                            thirdPartyParam?.packetName!!,
                                            thirdPartyParam.className!!,
                                            thirdPartyParam.param
                                        )
                                    }
                                }
                            }
                        })
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
*/
            }
        }
    }


    private fun initPermission() {
        PermissionUtils.isGrantExternalRW(this, 1)
        PermissionX.init(this)
            .permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Log.i(TAG, "initPermission: All permissions are granted")
                } else {
                    Log.i(TAG, "initPermission: These permissions are denied: $deniedList")
                }
            }
    }

    private fun startHardwareControl() {
        val intent = Intent()
        val componentName =
            ComponentName(AppConstants.AHWCX_PACKAGE_NAME, AppConstants.AHWCX_SERVICE_NAME)
        intent.component = componentName
        startService(intent)
    }

    /**
     * ??????????????????????????????
     */
    private fun startActivateService() {
        val intent = Intent()
        intent.component =
            ComponentName("com.jxw.launcher", "com.jht.engine.platsign.PlatformService")
        startService(intent)
    }

    /**
     * ????????????????????????
     */
    private val contentObserver: ContentObserver = object : ContentObserver(handler) {
        override fun deliverSelfNotifications(): Boolean {
            Log.i(TAG, "deliverSelfNotifications")
            return super.deliverSelfNotifications()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.i(TAG, "onChange-------->")
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.i(TAG, "onChange-------->$uri")
            handler.sendEmptyMessage(0x123)
        }
    }


    private fun writeUserInfo(tokenPair: TokenPair) {
        //mmkv??????????????????
        val mmkv = LauncherApplication.getMMKV()
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender!!)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME, tokenPair.expireTime!!)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE, tokenPair.gradeType!!)

        //????????????????????????????????????
        contentResolver.delete(LauncherContentProvider.URI, null, null)
        val contentValues = ContentValues()
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender)
        contentValues.put(
            AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME,
            tokenPair.expireTime
        )
        contentValues.put(
            AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE,
            tokenPair.gradeType
        )
        //????????????????????????????????????
        contentResolver.insert(LauncherContentProvider.URI, contentValues)
    }

    override fun initView() {

    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_launcher
    }

    override fun onSuccess(any: Any) {
        //?????????????????????acitivity????????????????????????
        if (this == null || isDestroyed || isFinishing) {
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            //?????????????????????????????? ???????????????????????????
            if (any is TokenPair) {
                tokenPair = any
                if (tokenPair != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!UserDBUtil.isLocalChanged) {
                            getPresenter().setInitGrade(tokenPair!!.gradeType!!)
                        }
                        writeUserInfo(tokenPair!!)
                        SPUtils.syncPutData("tokenPair", Gson().toJson(tokenPair))
                    }
                }
                Glide.with(this@NewLauncherActivity)
                    .load(tokenPair?.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .error(if (tokenPair?.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
                    .into(iv_user_icon_new_launcher)
                tv_user_name_new_launcher.text = any.name
                if (StringUtils.isEmpty(tokenPair?.token)) {
                    getPresenter().getLocationAndWeather()
                }
            } else if (any is JPushBindBean) {
                if (any.code == 201) {
                    if (!isStartHeart) {
                        isStartHeart = true
                        heartbeat()
                    }
                }
            } else if (any is StudyPlanBean) {
                /* try {
                     val planCompleteTotal = any.data.plan_complete_total
                     val planTotal = any.data.plan_total
                     if (planTotal > 0) {
                         tv_study_plan_launcher.text =
                             "?????????${planTotal}???????????????????????????${planCompleteTotal}???????????????!"
                     }
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }*/
            } else if (any is BaseBean) {
                if (any.code == 401 && tokenPair != null) {
                    stopHeart = true
                    SPUtils.syncPutData("onlyShowSelectChild", true)
                    activityResultLauncher?.launch(
                        Intent(
                            this@NewLauncherActivity,
                            SplashActivity::class.java
                        )
                    )
                }
            } else if (any is UpdateBean) {
                //????????????
                val isRebinding = SPUtils.getData("rebinding", false) as Boolean
                if (!isRebinding)
                //????????????????????????
                    getPresenter().splashStartUpdateActivity(
                        false,
                        any,
                        this@NewLauncherActivity
                    )
            } else if (any is PlayTimeBean) {
                Log.i(TAG, "onSuccess: ")
                playTimeBean = any
                val mmkv = LauncherApplication.getMMKV()
                mmkv.encode(AppConstants.PLAY_TIME, Gson().toJson(playTimeBean))
            } else if (any is LoginBean) {
                //??????????????????
                if (any.code == 201) {
                    if (mUserId != -1) {
                        AccountUtil.currentUserId = mUserId
                    }
                } else {
                    //??????????????????????????????
                    if (mUserId != -1) {
                        getPresenter().postModel(
                            Urls.STUDENT_LOGIN,
                            RequestBody.create(
                                null,
                                mapOf(
                                    "user_id" to mUserId,
                                    "dsn" to AccountUtil.getDSN()
                                ).toJson()
                            ), LoginBean::class.java
                        )
                    }
                }
            } else if (any is AppUninstallBean) {
                val appUninstallBean: AppUninstallBean = any
                if (appUninstallBean?.data != null && appUninstallBean.data.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runBlocking {
                            appUninstallBean.data.forEach {
                                val result = MyAppManager.tryUninstall(
                                    this@NewLauncherActivity,
                                    it.package_name,
                                    10010
                                )
                            }
                        }
                    }
                }
            } else if (any is AppSortBean) {
                val toJson = Gson().toJson(any)
                LauncherApplication.getMMKV().encode("appSortList", toJson)
                Log.i(TAG, "onSuccess: $toJson")
            }
            /*else if (any is FamilyInfoBean) {
                familyInfoBean = any
                ToastUtils.showShort(this@NewLauncherActivity, "?????????????????????")
//                familyAdapter.addData(any.data.parents)
            }*/
        }
    }

    private fun heartbeat() {
        if (!stopHeart) {
            heartCoroutineScope?.cancel()
            heartCoroutineScope = GlobalScope.launch(Dispatchers.IO) {
                getPresenter().getModel(Urls.HEART_BEAT, hashMapOf(), HeartBean::class.java)
                //???15???????????????????????????
                delay(1000 * 15)
                heartbeat()
            }
        }
    }

    override fun onError(error: String) {
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_ar_launcher, R.id.tv_chinese_launcher, R.id.tv_mathematics_launcher, R.id.tv_english_launcher, R.id.tv_quality_launcher
            -> {
                showLeftSelectUI(v.id)
                showSelectUI(v.id)
            }
            //???????????????AR?????? ???????????????
            R.id.iv_book_reading -> {
                val startActivity = getPresenter().startActivity(
                    this,
                    "com.jxw.huiben",
                    "com.jxw.huiben.activity.SplashActivity",
                    mapOf("StartArgs" to "keben")       //????????????
//                    mapOf("StartArgs" to "huiben")    //????????????
                )
                if (startActivity && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }
            //?????????
            R.id.iv_query_word -> {
                val startAoaApp = getPresenter().startAoaApp(this, 135, "/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //?????????
            R.id.iv_chinese_words -> {
                val startAoaApp = getPresenter().startAoaApp(this, 134, "/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //??????-AOA??????
            R.id.iv_english_translation -> {
                val startAoaApp = getPresenter().startAoaApp(this, 138, "/app/138/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            /*  //????????????-AOA ?????????????????????
              R.id.tv_seek_help_launcher -> {
                  val startAoaApp = getPresenter().startAoaApp(this, 140, "/home/140")
                  if (startAoaApp && interactionAbility != null)
                      getPresenter().startInteractionWindow(
                          interactionAbility!!,
                          InteractionAbility.InteractiveMode.PEN_POINT
                      )
              }*/

            //??????-AOA?????? ????????????
            R.id.iv_title_query -> {
                val startAoaApp = getPresenter().startAoaApp(this, 139, "/apps/139/main")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            /*   //????????????-????????????????????????
               R.id.tv_fun_card_launcher -> {
                   ToastUtils.showLong(this, "??????????????????????????????????????????")
   //                getPresenter().startInteractionWindow(interactionAbility!!,InteractionAbility.InteractiveMode.FINGER_TOUCH)
               }
               //?????????-AOA?????????
               R.id.tv_wrong_topic_launcher -> {
                   ToastUtils.showLong(this, "??????????????????????????????????????????")
   //                getPresenter().startAoaApp(this, 141, "/app/141/home")
   //                getPresenter().startInteractionWindow(
   //                    interactionAbility!!,
   //                    InteractionAbility.InteractiveMode.PEN_POINT
   //                )
               }*/
            //????????????
            R.id.iv_oral_correction -> {
                val startAoaApp = getPresenter().startAoaApp(this, 142, "/app/142/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //??????????????????
            R.id.iv_article_correction -> {
                val startAoaApp = getPresenter().startAoaApp(this, 143, "/app/143/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //??????????????????
/*
            R.id.tv_yyzwpg_launcher -> {
                ToastUtils.showLong(this, "??????????????????????????????????????????")
//                getPresenter().startAoaApp(this, 144, "/app/144/home")
//                getPresenter().startInteractionWindow(
//                    interactionAbility!!,
//                    InteractionAbility.InteractiveMode.PEN_POINT
//                )
            }
*/
            //?????????-AOA?????????
            R.id.iv_favorites -> {
                val startAoaApp = getPresenter().startAoaApp(this, 141, "/app/141/home")
                if (startAoaApp && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }
            //????????????-AOA????????????

            R.id.iv_study_plan
            -> {
                val startAoaApp = getPresenter().startAoaApp(this, 33, "/home")
                if (startAoaApp && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }
            //????????????
            R.id.iv_call_parent -> {
                getPresenter().showAVDialog(this, interactionAbility)
            }
            R.id.iv_call_parent_child -> {
                getPresenter().showChildAVDialog(this)
            }
            //????????????
            R.id.iv_user_icon_new_launcher, R.id.tv_user_name_new_launcher
            -> {
                if (tokenPair == null) return
                var intent = Intent(this, PersonCenterActivity::class.java)
                intent.putExtra("userInfo", tokenPair)
                intent.putExtra("netState", netState)
                activityResultLauncher?.launch(intent)
            }
//            R.id.iv_all_app_launcher -> {
//                getPresenter().showDialog(AppConstants.ALL_APP)
//            }
//            //?????????
//            R.id.tv_grade_person_center -> {
//                getPresenter().showSelectGradeDialog(this, tv_grade_person_center)
//            }
//            //????????????
//            R.id.iv_az_store -> {
//                getPresenter().showKAMarket()
//            }
            /*  R.id.iv_av_launcher, R.id.tv_task_challenges -> {
                  ToastUtils.showLong(this, "??????????????????????????????????????????")
              }*/
            //??????????????????
            R.id.tv_book_click, R.id.fl_classroom_sync -> {
                StartAppUtils.startActivity(
                    this,
                    selectBook.appPackName,
                    selectBook.className,
                    selectBook.params
                )
            }
//            R.id.iv_ip_image -> {
            //?????????????????????????????????????????????
//                audioAbility?.wakeup()
//            }
            R.id.fl_wifi_module -> {
                getPresenter().startWifiModule(false)
            }
            R.id.iv_main_top_child_launcher -> {
                selectChildUI(0)
            }
            R.id.iv_app_select_top_child_launcher -> {
                selectChildUI(1)
            }
            R.id.iv_app_list_top_child_launcher -> {
                selectChildUI(2)
            }
        }
    }

    private fun selectChildUI(index: Int) {
        if (vp2_launcher.currentItem != index) {
            vp2_launcher.currentItem = index
        }
        iv_main_top_child_launcher.alpha = if (index == 0) 1f else 0.6f
        iv_app_select_top_child_launcher.alpha = if (index == 1) 1f else 0.6f
        iv_app_list_top_child_launcher.alpha = if (index == 2) 1f else 0.6f
    }

    override fun onPause() {
        super.onPause()
        /*if (!isRefresh) {
            Log.i(TAG, "onPause: ?????????")
            return
        }*/
        Log.i(TAG, "onPause: ?????????")
        if (audioInitSuccessful)
            audioAbility?.stopRecording()

    }

    /* override fun onAttachedToWindow() {
         this.window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD)
         super.onAttachedToWindow()
     }

     override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         if (KeyEvent.KEYCODE_HOME == keyCode) { //???????????????home???
             finish() //?????????activity
             return true
         }
         return super.onKeyDown(keyCode, event)
     }*/

    private fun showSelectUI(id: Int) {
        when (id) {
            R.id.tv_ar_launcher -> {
                showArUI(AppConstants.LAUNCHER_TYPE_AR)
            }
            R.id.tv_chinese_launcher -> {
                showArUI(AppConstants.LAUNCHER_TYPE_CHINESE)
            }
            R.id.tv_mathematics_launcher -> {
                showArUI(AppConstants.LAUNCHER_TYPE_MATHEMATICS)
            }
            R.id.tv_english_launcher -> {
                showArUI(AppConstants.LAUNCHER_TYPE_ENGLISH)
            }
            R.id.tv_quality_launcher -> {
                showArUI(AppConstants.LAUNCHER_TYPE_QUALITY)
            }
        }
    }

    private fun showArUI(launcherType: String) {
        when (launcherType) {
            AppConstants.LAUNCHER_TYPE_AR -> {
                inflateCenterView(false)
                iv_ip_image.visibility = View.VISIBLE
                rv_quality_launcher.visibility = View.GONE
                rl_ar_launcher.visibility = View.VISIBLE
            }
            AppConstants.LAUNCHER_TYPE_CHINESE, AppConstants.LAUNCHER_TYPE_MATHEMATICS, AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                inflateCenterView(true)
                iv_ip_image.visibility = View.GONE
                rv_quality_launcher.visibility = View.GONE
                rl_ar_launcher.visibility = View.GONE
                setAdapterUI(launcherType)
                setRightAdapter(launcherType)
                var launcherBg = R.drawable.launcher_english_bg
                when (launcherType) {
                    AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                        launcherBg = R.drawable.launcher_english_bg
                        iv_classroom_sync.setImageResource(R.drawable.text_classroom_sync)
                        iv_study_tools.setImageResource(R.drawable.text_study_tools)
                        iv_extracurricular_counselling.setImageResource(R.drawable.text_extracurricular_counselling)
                        fl_classroom_sync.setBackgroundResource(R.drawable.launcher_english_book)
                        tv_book_click.setBackgroundResource(R.drawable.english_text_bg_oval)
                        selectBook = bookList[0]
                    }
                    AppConstants.LAUNCHER_TYPE_MATHEMATICS -> {
                        launcherBg = R.drawable.launcher_math_bg
                        iv_classroom_sync.setImageResource(R.drawable.text_math_classroom_sync)
                        iv_study_tools.setImageResource(R.drawable.text_math_study_tools)
                        iv_extracurricular_counselling.setImageResource(R.drawable.text_math_extracurricular_counselling)
                        fl_classroom_sync.setBackgroundResource(R.drawable.launcher_math_book)
                        tv_book_click.setBackgroundResource(R.drawable.math_text_bg_oval)
                        selectBook = bookList[1]
                    }
                    AppConstants.LAUNCHER_TYPE_CHINESE -> {
                        launcherBg = R.drawable.launcher_language_bg
                        iv_classroom_sync.setImageResource(R.drawable.text_language_classroom_sync)
                        iv_study_tools.setImageResource(R.drawable.text_language_study_tools)
                        iv_extracurricular_counselling.setImageResource(R.drawable.text_language_extracurricular_counselling)
                        fl_classroom_sync.setBackgroundResource(R.drawable.launcher_language_book)
                        tv_book_click.setBackgroundResource(R.drawable.language_text_bg_oval)
                        selectBook = bookList[2]
                    }
                }
                fl_study_tools.setBackgroundResource(launcherBg)
                fl_extracurricular_counselling.setBackgroundResource(launcherBg)
            }
            AppConstants.LAUNCHER_TYPE_QUALITY -> {
                inflateCenterView(false)
                iv_ip_image.visibility = View.GONE
                rv_quality_launcher.visibility = View.VISIBLE
                rl_ar_launcher.visibility = View.GONE
                if (qualityHorizontalAdapter == null) {
                    qualityHorizontalAdapter = QualityHorizontalAdapter()
                    val appSortListString =
                        LauncherApplication.getMMKV().getString("appSortList", "")
                    if (appSortListString?.isNotEmpty()!!) {
                        qualityHorizontalAdapter?.clearAppList()
                        val appSortBean =
                            Gson().fromJson(appSortListString, AppSortBean::class.java)
                        val filter1 = appSortBean.data.filter {
                            it.app_label == 1
                        }
                        val filter2 = appSortBean.data.filter {
                            it.app_label == 2
                        }
                        val filter3 = appSortBean.data.filter {
                            it.app_label == 3
                        }
                        val filter4 = appSortBean.data.filter {
                            it.app_label == 4
                        }
                        filter1.forEach {
                            qualityHorizontalAdapter?.appList1?.add(
                                NewAppTypeBean(
                                    it.app_name,
                                    null,
                                    it.package_name,
                                    it.class_name,
                                    null,
                                    it.app_icon
                                )
                            )
                        }
                        filter2.forEach {
                            qualityHorizontalAdapter?.appList2?.add(
                                NewAppTypeBean(
                                    it.app_name,
                                    null,
                                    it.package_name,
                                    it.class_name,
                                    null,
                                    it.app_icon
                                )
                            )
                        }
                        filter3.forEach {
                            qualityHorizontalAdapter?.appList3?.add(
                                NewAppTypeBean(
                                    it.app_name,
                                    null,
                                    it.package_name,
                                    it.class_name,
                                    null,
                                    it.app_icon
                                )
                            )
                        }
                        filter4.forEach {
                            qualityHorizontalAdapter?.appList4?.add(
                                NewAppTypeBean(
                                    it.app_name,
                                    null,
                                    it.package_name,
                                    it.class_name,
                                    null,
                                    it.app_icon
                                )
                            )
                        }
                        qualityHorizontalAdapter?.tempList?.clear()
                        qualityHorizontalAdapter?.tempList?.addAll(qualityHorizontalAdapter?.appList4!!)
                        qualityHorizontalAdapter?.resetAppNotifyAdapter()
                    }
                    qualityHorizontalAdapter?.setOnItemClickListener(object :
                        QualityHorizontalAdapter.OnItemClickListener {
                        override fun onItemClick(
                            packName: String,
                            className: String?,
                            params: Map<String, Any>?
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (BtnClickUtil.isFastShow()) return@launch
                                if (!StringUtils.isEmpty(className) && params != null) {
                                    StartAppUtils.startActivity(
                                        this@NewLauncherActivity,
                                        packName,
                                        className!!,
                                        params
                                    )
                                } else {
                                    StartAppUtils.startApp(this@NewLauncherActivity, packName)
                                }
                                if (StartAppUtils.isNeedStopTouchPoint(packName) && touchAbilityInitSuccessful) {
                                    touchAbility?.disableDisorderlyPointChecker()
                                    Log.i(TAG, "????????????????????? disableDisorderlyPointChecker: ")
                                }
                            }
                        }
                    })
                    val linearLayoutManager = LinearLayoutManager(this)
                    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                    rv_quality_launcher.layoutManager = linearLayoutManager
                    rv_quality_launcher.adapter = qualityHorizontalAdapter
                }
            }

        }
    }

    /**
     * @param isInflate ??????????????????
     */
    private fun inflateCenterView(isVisible: Boolean) {
        //needInflate???true??????????????????????????????????????????
        val needInflate = if (vs_center_launcher == null && vsCenterInflate != null) {
            false
        } else {
            vs_center_launcher.parent != null
        }
        if (isVisible) {
            if (needInflate) {
                vsCenterInflate = vs_center_launcher.inflate()
            } else {
                vsCenterInflate?.visibility = View.VISIBLE
            }
        } else if (!needInflate) {
            vsCenterInflate?.visibility = View.GONE
        }
    }

    private fun setAdapterUI(launcherType: String) {
        if (launcherCenterAdapter == null) {
            launcherCenterAdapter = LauncherCenterAdapter()
            launcherCenterAdapter?.setOnItemClickListener { adapter, view, position ->
                if (BtnClickUtil.isFastShow()) return@setOnItemClickListener
                val appPackName = launcherCenterAdapter!!.data[position].appPackName
                val className = launcherCenterAdapter!!.data[position].className
                val params = launcherCenterAdapter!!.data[position].params
                if (!StringUtils.isEmpty(appPackName) && !StringUtils.isEmpty(className)
                ) {
                    val startActivity =
                        getPresenter().startActivity(this, appPackName, className, params)
                    if (interactionAbility != null) {
                        //?????????AI???????????????????????? ????????????????????????
                        if (appPackName == "com.jxw.examcenter.activity" || appPackName == "com.jxw.handwrite") {
                            if (startActivity)
                                getPresenter().startInteractionWindow(
                                    interactionAbility!!,
                                    InteractionAbility.InteractiveMode.PEN_POINT
                                )
                        } else {
                            if (startActivity)
                                getPresenter().startInteractionWindow(
                                    interactionAbility!!,
                                    InteractionAbility.InteractiveMode.FINGER_TOUCH
                                )
                        }
                    }
                } else {
                    ToastUtils.showLong(this, "??????????????????????????????????????????")
                }
            }
        }
        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
        rv_center_launcher.layoutManager = gridLayoutManager
        rv_center_launcher.adapter = launcherCenterAdapter
        launcherCenterAdapter?.setShowType(launcherType)
    }

    private fun setRightAdapter(launcherType: String) {
        if (launcherRightAdapter == null) {
            val gridLayoutManager = GridLayoutManager(this, 3)
            gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
            rv_right_launcher.layoutManager = gridLayoutManager
            launcherRightAdapter = LauncherRightAdapter()
            rv_right_launcher.adapter = launcherRightAdapter
        }
        launcherRightAdapter?.setShowType(launcherType)
    }

    private fun showLeftSelectUI(id: Int) {
        iv_ar_launcher.visibility =
            if (id == R.id.tv_ar_launcher) View.VISIBLE else View.GONE
        iv_chinese_launcher.visibility =
            if (id == R.id.tv_chinese_launcher) View.VISIBLE else View.GONE
        iv_mathematics_launcher.visibility =
            if (id == R.id.tv_mathematics_launcher) View.VISIBLE else View.GONE
        iv_english_launcher.visibility =
            if (id == R.id.tv_english_launcher) View.VISIBLE else View.GONE
        iv_quality_launcher.visibility =
            if (id == R.id.tv_quality_launcher) View.VISIBLE else View.GONE
        startScaleAnim(iv_ar_launcher)
        startScaleAnim(iv_chinese_launcher)
        startScaleAnim(iv_mathematics_launcher)
        startScaleAnim(iv_english_launcher)
        startScaleAnim(iv_quality_launcher)
    }

    private fun startScaleAnim(id: View) {
        if (id.isVisible) {
            val animator = ObjectAnimator.ofFloat(id, "scaleY", 0.3f, 1.0f)
            animator.setDuration(500).interpolator = OvershootInterpolator()
            animator.start()
        }
    }

    override fun onReceive(tokenMessage: TokenMessage) {
        Log.i(TAG, "onReceive message: ${tokenMessage.message}")
        //????????????????????????
        if (tokenMessage.message.type == "video" || tokenMessage.message.type == "audio") {
            startCalledWindow(tokenMessage)
        }
    }

    private fun startCalledWindow(tokenMessage: TokenMessage) {
        val intent = Intent("com.alight.trtcav.WindowActivity")
        if (tokenMessage != null) {
            intent.putExtra("parentId", tokenMessage.message.fromUserId.toString())
            intent.putExtra("parentName", tokenMessage.message.fromUserInfo.name)
            intent.putExtra("parentAvatar", tokenMessage.message.fromUserInfo.avatar)
            intent.putExtra("roomId", tokenMessage.message.roomId)
            intent.putExtra("childId", tokenPair?.userId.toString())
            intent.putExtra("called", 2)
            intent.putExtra("token", tokenPair?.token)
            intent.putExtra("callType", tokenMessage.message.type)
        }
        try {
            this.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.stackTraceToString()
        }
    }

    override fun onNetChange(netWorkState: Int) {
        when (netWorkState) {
            NetTools.NETWORK_NONE -> {
                netState = 0
                EventBus.getDefault().post(NetMessageEvent.getInstance(netState, "????????????"));
                Log.e(TAG, "onNetChanged:???????????? ")
            }
            NetTools.NETWORK_MOBILE, NetTools.NETWORK_WIFI -> {
                netState = 1
                val storageBean = StorageUtil.queryWithStorageManager(this)
                var remainSize = storageBean.remainSize
                val remainSizeMb = DecimalFormat("0").format(remainSize.toDouble() * 1024).toLong()
                //?????????????????????????????????
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
                getPresenter().getModel(
                    Urls.APPS_SORT + AccountUtil.getDSN(),
                    hashMapOf(), AppSortBean::class.java
                )
                getPresenter().getModel(
                    Urls.DEVICE_INSTALL,
                    hashMapOf("dsn" to AccountUtil.getDSN()),
                    AppUninstallBean::class.java
                )
                initAccountUtil()
                EventBus.getDefault().post(NetMessageEvent.getInstance(netState, "??????????????????"));
                // ????????????????????????
                if (!isStartHeart)
                    getPresenter().postModel(
                        Urls.BIND_PUSH,
                        RequestBody.create(
                            null,
                            mapOf(
                                AppConstants.REGISTRATION_ID to JPushInterface.getRegistrationID(
                                    this@NewLauncherActivity
                                )
                            ).toJson()
                        ),
                        JPushBindBean::class.java
                    )
                val splashClose = SPUtils.getData("splashClose", false) as Boolean
                Log.i(TAG, "splashClose = $splashClose splashCloseFlag = $splashCloseFlag")

                val tokenPairCache = SPUtils.getData("tokenPair", "") as String
                val rebinding = SPUtils.getData("rebinding", false) as Boolean
                //??????????????????????????????
                if (!splashClose && !splashCloseFlag && tokenPairCache.isNullOrEmpty() || rebinding && !splashClose) {
                } else if (!splashCloseFlag && !guideUserUpdate) {
                    //??????????????????
                    getPresenter().getModel(
                        Urls.UPDATE,
                        hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
//                hashMapOf("device_type" to "LAMP_AL"),
                        UpdateBean::class.java
                    )
                    //???????????????????????????
                    guideUserUpdate = true
                }
                /* if (!guideUserUpdate)  //??????????????????
                 {
                     getPresenter().getModel(
                         Urls.UPDATE,
                         hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
    //                                    hashMapOf("device_type" to "LAMP"),
                         UpdateBean::class.java
                     )
                     //???????????????????????????
                     guideUserUpdate = true
                 }*/
                Log.e(TAG, "onNetChanged:???????????? ")
            }
        }
    }

    /**
     * App????????????????????????
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onParentControlEvent(event: ParentControlEvent) {
        if (tokenPair != null)
            getPresenter().getModel(
                Urls.PLAY_TIME,
                hashMapOf("user_id" to tokenPair?.userId.toString()),
                PlayTimeBean::class.java
            )
    }

    /**
     * home??????
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHomeKeyEvent(event: HomeKeyEvent) {
        var isForeground =
            CommonUtil.isForeground(this@NewLauncherActivity, NewLauncherActivity::class.java.name)
        if (isForeground) {
            isRefresh = false
        } else {
            isRefresh = true
        }

    }

    /**
     * ??????????????????
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCheckUpdateEvent(event: CheckUpdateEvent) {
        getPresenter().getModel(
            Urls.UPDATE,
            hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
//                                    hashMapOf("device_type" to "LAMP"),
            UpdateBean::class.java
        )
    }

    override fun onConnect() {
    }

    override fun onDisconnect() {
    }

    /**
     * ????????????????????????
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            //do something.
            true;//????????????????????? ????????????
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        abilityManager.onStop()
        EventBus.getDefault().unregister(this)
        unregisterReceiver(shutdownReceiver)
        unregisterReceiver(mHomeKeyReceiver)
        heartCoroutineScope?.cancel()
//        isRegister = false
    }
}
