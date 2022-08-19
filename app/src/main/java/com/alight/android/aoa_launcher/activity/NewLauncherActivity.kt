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
import kotlinx.coroutines.*
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * @author wangzhe
 * 新的Launcher主页
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
    private var interactionAbility: InteractionAbility? = null //交互模式相关
    private var panelAbility: PanelAbility? = null //交互模式相关(无动画)
    private var audioAbility: AudioAbility? = null //语音助手相关
    private var touchAbility: TouchAbility? = null //触控乱点相关
    private val abilityManager = AbilityManager("launcher", "5", "234")
    private var abilityInitSuccessful = false
    private var touchAbilityInitSuccessful = false
    private var audioInitSuccessful = false
    private var stopHeart = false
    private var playTimeBean: PlayTimeBean? = null
    private var shutdownReceiver: ShutdownReceiver? = null
    private var isStartHeart = false
    private var onresumeFlag = false
    private var featureAbility: FeatureAbility? = null //内存 护眼相关
    private var isFeatureAbilityInit = false
    private var mUserId = -1
    private var mode = "student"
    private var isRefresh = false
    private var heartCoroutineScope: Job? = null

    private lateinit var launcherPagerAdapter: LauncherPagerAdapter


    private var selectBook: AppTypeBean = AppTypeBean(
        R.drawable.yxkw, "com.jxw.pedu.clickread",
        "com.jxw.pedu.clickread.MainActivity",
        mapOf("StartArgs" to "英语")
    )

    /**
     * 语文和英语跳转课本点读，数学跳转课本辅导
     */
    private val bookList = arrayListOf(
        AppTypeBean
            (
            R.drawable.yxkw, "com.jxw.pedu.clickread",
            "com.jxw.pedu.clickread.MainActivity",
            mapOf("StartArgs" to "英语")
        ), AppTypeBean(
            R.drawable.yxkw, "com.jxw.online_study",
            "com.jxw.online_study.activity.BookCaseWrapperActivity",
            mapOf("StartArgs" to "d:/同步学习/数学|e:JWFD")
        ), AppTypeBean(
            R.drawable.yxkw, "com.jxw.pedu.clickread",
            "com.jxw.pedu.clickread.MainActivity",
            mapOf("StartArgs" to "语文")
        )
    )

    //引导过用户升级为 true
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
                    Log.i(TAG, "数据发生变化")
//                    Toast.makeText(this@LauncherActivity, "数据发生变化", Toast.LENGTH_SHORT).show()
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
        tv_seek_help_launcher.setOnClickListener(this)
        iv_title_query.setOnClickListener(this)
        tv_fun_card_launcher.setOnClickListener(this)
        tv_wrong_topic_launcher.setOnClickListener(this)
        iv_oral_correction.setOnClickListener(this)
        iv_article_correction.setOnClickListener(this)
        tv_yyzwpg_launcher.setOnClickListener(this)
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
        tv_book_click.setOnClickListener(this)
        fl_classroom_sync.setOnClickListener(this)
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
        //给onResume增加限制
        /* if (onresumeFlag) {
             return
         } else {
             onresumeFlag = true
         }*/
        /*    if (!isRefresh) {
                isRefresh = true
                Log.i(TAG, "onResume: 未刷新")
                return
            }*/
        Log.i(TAG, "onResume: 刷新了")
        val splashClose = SPUtils.getData("splashClose", false) as Boolean
        Log.i(TAG, "splashClose = $splashClose splashCloseFlag = $splashCloseFlag")

        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
        val rebinding = SPUtils.getData("rebinding", false) as Boolean
        if (!splashClose && !splashCloseFlag && tokenPairCache.isNullOrEmpty() || (rebinding && !splashClose)) {
            /*   ToastUtils.showShort(
                   this,
                   "展示引导页 $splashClose $splashCloseFlag ${tokenPairCache.isNullOrEmpty()} $rebinding"
               )*/
            Log.i(TAG, "展示引导页")
//        如果未展示过引导则展示引导页
            activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
        } else {
            initAccountUtil()
            if (audioInitSuccessful) {
                audioAbility?.startRecording()
            }
//            getPresenter().sendMenuEnableBroadcast(this, true)
            /*if (!splashCloseFlag && !guideUserUpdate)  //检测系统更新
            {
                getPresenter().getModel(Urls.UPDATE, hashMapOf(), UpdateBean::class.java)
                //表示引导过用户升级
                guideUserUpdate = true
            }*/
        }
        //用户重新绑定
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
        // 获取学习计划
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
        //每次回首页调用内存清理
        if (isFeatureAbilityInit) {
            featureAbility?.startTaskMemoryClean()
        }
        //将输入法重置
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
                    //幼教版Launcher
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
                    //常规版Launcher
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
     * 监控触控乱点重启
     */
    private fun resetTouchAbility() {
        if (touchAbility == null) {
            touchAbility =
                abilityManager.getAbility(TouchAbility::class.java, true, applicationContext)
            GlobalScope.launch(Dispatchers.Main) {
                touchAbilityInitSuccessful = touchAbility?.waitConnectionAsync()!!
                if (touchAbilityInitSuccessful) {
                    try {
                        Log.i(TAG, "乱点监控被启用 enableDisorderlyPointChecker: ")
                        touchAbility?.enableDisorderlyPointChecker()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (touchAbilityInitSuccessful) {
            try {
                touchAbility?.enableDisorderlyPointChecker()
                Log.i(TAG, "乱点监控被启用 enableDisorderlyPointChecker: ")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 交互模式初始化
     */
    private fun setInteraction() {
        if (panelAbility == null) {
            panelAbility =
                abilityManager.getAbility(PanelAbility::class.java, true, applicationContext)
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
            //回到launcher时自动切回手触
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
        AccountUtil.register(this)
        val userId = SPUtils.getData(AppConstants.USER_ID, -1) as Int
        if (userId != -1) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val allToken = AccountUtil.getAllToken()
                    allToken.forEach {
                        if (it.userId == userId) {
                            mUserId = it.userId
                            //新替换的登录接口
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
                            //重新选择用户的逻辑可以写在这里
//                            AccountUtil.selectUser(it.userId)
//                            stopHeart = false
//                            isStartHeart = false
                            // 获取学习计划
                            getPresenter().getModel(
                                "${Urls.STUDY_PLAN}${it.userId}",
                                hashMapOf(),
                                StudyPlanBean::class.java
                            )
                            // 用户绑定极光推送
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
                            /*    if (!splashCloseFlag && !guideUserUpdate)  //检测系统更新
                                {
                                    getPresenter().getModel(
                                        Urls.UPDATE,
                                        hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
    //                                    hashMapOf("device_type" to "LAMP"),
                                        UpdateBean::class.java
                                    )
                                    //表示引导过用户升级
                                    guideUserUpdate = true
                                }*/
                        }
                    }
                    //设置用户信息
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
        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
        //满足条件时展示引导页
        if (tokenPairCache.isNotEmpty()) {
            tokenPair = Gson().fromJson(tokenPairCache, TokenPair::class.java)
            writeUserInfo(tokenPair!!)
        }
        //获取用户信息之前必须调用的初始化方法
        AccountUtil.run()
        initAccountUtil()
        //初始化权限
        initPermission()
        //监听contentProvider是否被操作
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
        //注册splash的返回数据回调
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.i(TAG, "registerForActivityResult")
            when (it.resultCode) {
                //选择用户后的用户刷新逻辑
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
                    //初始化用户工具及展示用户数据
                    initAccountUtil()
                }
                //使用Launcher调起选择用户
                AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER -> {
                    stopHeart = true
                    isStartHeart = false
                    activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
                }
            }
        }
        // 用户绑定极光推送
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
        //将输入法重置
//        getPresenter().resetInputType(this)
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
        //查询到相应的BroadcastReceiver

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
            Log.i("TAG", "即将关机")
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
                        Log.i(TAG, "onSuccess: 关机接口调用")
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
                                        //AOA应用
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
                                                        "该应用正在开发中，敬请期待！"
                                                    )
                                                }
                                            }
                                            //九学网和第三方应用
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
                                                        "该应用正在开发中，敬请期待！"
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
     * 开启九学王的激活服务
     */
    private fun startActivateService() {
        val intent = Intent()
        intent.component =
            ComponentName("com.jxw.launcher", "com.jht.engine.platsign.PlatformService")
        startService(intent)
    }

    /**
     * 内容提供者监听类
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
        //mmkv存储用户数据
        val mmkv = LauncherApplication.getMMKV()
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender!!)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME, tokenPair.expireTime!!)
        mmkv.encode(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE, tokenPair.gradeType!!)

        //插入数据前清除之前的数据
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
        //将登陆的用户数据插入保存
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
        GlobalScope.launch(Dispatchers.Main) {
            //网络请求成功后的结果 让对应视图进行刷新
            if (any is TokenPair) {
                tokenPair = any
                if (tokenPair != null) {
                    if (!UserDBUtil.isLocalChanged) {
                        getPresenter().setInitGrade(tokenPair!!.gradeType!!)
                    }
                    writeUserInfo(tokenPair!!)
                    SPUtils.syncPutData("tokenPair", Gson().toJson(tokenPair))
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
                             "今天有${planTotal}项学习计划，已完成${planCompleteTotal}项，加油哟!"
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
                //重新绑定
                val isRebinding = SPUtils.getData("rebinding", false) as Boolean
                if (!isRebinding)
                //展示系统固件更新
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
                //登录接口处理
                if (any.code == 201) {
                    if (mUserId != -1) {
                        AccountUtil.currentUserId = mUserId
                    }
                } else {
                    //重新尝试调用登录接口
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
            }
            /*else if (any is FamilyInfoBean) {
                familyInfoBean = any
                ToastUtils.showShort(this@NewLauncherActivity, "获取到家长信息")
//                familyAdapter.addData(any.data.parents)
            }*/
        }
    }

    private fun heartbeat() {
        if (!stopHeart) {
            heartCoroutineScope?.cancel()
            heartCoroutineScope = GlobalScope.launch(Dispatchers.IO) {
                getPresenter().getModel(Urls.HEART_BEAT, hashMapOf(), HeartBean::class.java)
                //每15秒调用一次打点接口
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
            //书本指读（AR指读 学王指读）
            R.id.iv_book_reading -> {
                val startActivity = getPresenter().startActivity(
                    this,
                    "com.jxw.huiben",
                    "com.jxw.huiben.activity.SplashActivity",
                    mapOf("StartArgs" to "keben")       //课本指读
//                    mapOf("StartArgs" to "huiben")    //绘本指读
                )
                if (startActivity && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }
            //查单词
            R.id.iv_query_word -> {
                val startAoaApp = getPresenter().startAoaApp(this, 135, "/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //查字词
            R.id.iv_chinese_words -> {
                val startAoaApp = getPresenter().startAoaApp(this, 134, "/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //翻译-AOA翻译
            R.id.iv_english_translation -> {
                val startAoaApp = getPresenter().startAoaApp(this, 138, "/app/138/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //求助老师-AOA 的远程辅导页面
            R.id.tv_seek_help_launcher -> {
                val startAoaApp = getPresenter().startAoaApp(this, 140, "/home/140")
                if (startAoaApp && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }

            //答题-AOA搜题 题目查询
            R.id.iv_title_query -> {
                val startAoaApp = getPresenter().startAoaApp(this, 139, "/apps/139/main")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //趣味卡牌-自己做的卡牌游戏
            R.id.tv_fun_card_launcher -> {
                ToastUtils.showLong(this, "该应用正在开发中，敬请期待！")
//                getPresenter().startInteractionWindow(interactionAbility!!,InteractionAbility.InteractiveMode.FINGER_TOUCH)
            }
            //错题本-AOA收藏夹
            R.id.tv_wrong_topic_launcher -> {
                ToastUtils.showLong(this, "该应用正在开发中，敬请期待！")
//                getPresenter().startAoaApp(this, 141, "/app/141/home")
//                getPresenter().startInteractionWindow(
//                    interactionAbility!!,
//                    InteractionAbility.InteractiveMode.PEN_POINT
//                )
            }
            //口算批改
            R.id.iv_oral_correction -> {
                val startAoaApp = getPresenter().startAoaApp(this, 142, "/app/142/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //语文作文批改
            R.id.iv_article_correction -> {
                val startAoaApp = getPresenter().startAoaApp(this, 143, "/app/143/home")
                if (startAoaApp && panelAbility != null)
                    getPresenter().startInteractionWindowNoAnim(
                        panelAbility!!,
                        PanelAbility.TouchMode.PEN_MODE
                    )
            }
            //英语作文批改
            R.id.tv_yyzwpg_launcher -> {
                ToastUtils.showLong(this, "该应用正在开发中，敬请期待！")
//                getPresenter().startAoaApp(this, 144, "/app/144/home")
//                getPresenter().startInteractionWindow(
//                    interactionAbility!!,
//                    InteractionAbility.InteractiveMode.PEN_POINT
//                )
            }
            //收藏夹-AOA收藏夹
            R.id.iv_favorites -> {
                val startAoaApp = getPresenter().startAoaApp(this, 141, "/app/141/home")
                if (startAoaApp && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }
            //学习计划-AOA学习计划

            R.id.iv_study_plan
            -> {
                val startAoaApp = getPresenter().startAoaApp(this, 33, "/home")
                if (startAoaApp && interactionAbility != null)
                    getPresenter().startInteractionWindow(
                        interactionAbility!!,
                        InteractionAbility.InteractiveMode.PEN_POINT
                    )
            }
            //呼叫家长
            R.id.iv_call_parent -> {
                getPresenter().showAVDialog(this, interactionAbility)
            }
            R.id.iv_call_parent_child -> {
                getPresenter().showChildAVDialog(this)
            }
            //个人中心
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
//            //选年级
//            R.id.tv_grade_person_center -> {
//                getPresenter().showSelectGradeDialog(this, tv_grade_person_center)
//            }
//            //应用商店
//            R.id.iv_az_store -> {
//                getPresenter().showKAMarket()
//            }
            /*  R.id.iv_av_launcher, R.id.tv_task_challenges -> {
                  ToastUtils.showLong(this, "该应用正在开发中，敬请期待！")
              }*/
            //打开预习课文
            R.id.tv_book_click, R.id.fl_classroom_sync -> {
                StartAppUtils.startActivity(
                    this,
                    selectBook.appPackName,
                    selectBook.className,
                    selectBook.params
                )
            }
//            R.id.iv_ip_image -> {
            //由于容易误触，暂时取消语音唤醒
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
            Log.i(TAG, "onPause: 未刷新")
            return
        }*/
        Log.i(TAG, "onPause: 刷新了")
        if (audioInitSuccessful)
            audioAbility?.stopRecording()

    }

    /* override fun onAttachedToWindow() {
         this.window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD)
         super.onAttachedToWindow()
     }

     override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         if (KeyEvent.KEYCODE_HOME == keyCode) { //判断是否为home键
             finish() //结束该activity
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
                iv_ip_image.visibility = View.VISIBLE
                rv_quality_launcher.visibility = View.GONE
                ll_center_launcher.visibility = View.GONE
                rl_ar_launcher.visibility = View.VISIBLE
            }
            AppConstants.LAUNCHER_TYPE_CHINESE, AppConstants.LAUNCHER_TYPE_MATHEMATICS, AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                iv_ip_image.visibility = View.GONE
                rv_quality_launcher.visibility = View.GONE
                fl_book_launcher.visibility = View.VISIBLE
                ll_center_launcher.visibility = View.VISIBLE
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
                iv_ip_image.visibility = View.GONE
                rv_quality_launcher.visibility = View.VISIBLE
                ll_center_launcher.visibility = View.GONE
                rl_ar_launcher.visibility = View.GONE
                if (qualityHorizontalAdapter == null) {
                    qualityHorizontalAdapter = QualityHorizontalAdapter()
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
                                    Log.i(TAG, "乱点监控被禁用 disableDisorderlyPointChecker: ")
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
                        //听写和AI测评为笔点击模式 其他都为手触模式
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
                    ToastUtils.showLong(this, "该应用正在开发中，敬请期待！")
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
        //家长打来视频通话
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
                EventBus.getDefault().post(NetMessageEvent.getInstance(netState, "网络异常"));
                Log.e(TAG, "onNetChanged:没有网络 ")
            }
            NetTools.NETWORK_MOBILE, NetTools.NETWORK_WIFI -> {
                netState = 1
                initAccountUtil()
                EventBus.getDefault().post(NetMessageEvent.getInstance(netState, "网络恢复正常"));
                // 用户绑定极光推送
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
                //满足条件时展示引导页
                if (!splashClose && !splashCloseFlag && tokenPairCache.isNullOrEmpty() || rebinding && !splashClose) {
                } else if (!splashCloseFlag && !guideUserUpdate) {
                    //检测系统更新
                    getPresenter().getModel(
                        Urls.UPDATE,
                        hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
//                hashMapOf("device_type" to "LAMP_AL"),
                        UpdateBean::class.java
                    )
                    //表示引导过用户升级
                    guideUserUpdate = true
                }
                /* if (!guideUserUpdate)  //检测系统更新
                 {
                     getPresenter().getModel(
                         Urls.UPDATE,
                         hashMapOf("device_type" to Build.DEVICE.toUpperCase()),
 //                                    hashMapOf("device_type" to "LAMP"),
                         UpdateBean::class.java
                     )
                     //表示引导过用户升级
                     guideUserUpdate = true
                 }*/
                Log.e(TAG, "onNetChanged:网络正常 ")
            }
        }
    }

    /**
     * App端修改了家长管控
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
     * home监听
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
     * 进行检测更新
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
     * 屏蔽系统返回按钮
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            //do something.
            true;//系统层不做处理 就可以了
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
