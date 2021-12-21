package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jpush.android.api.JPushInterface
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.BaseBean
import com.alight.android.aoa_launcher.common.bean.JPushBindBean
import com.alight.android.aoa_launcher.common.bean.TokenMessage
import com.alight.android.aoa_launcher.common.bean.TokenPair
import com.alight.android.aoa_launcher.common.constants.AppConstants
import com.alight.android.aoa_launcher.common.event.NetMessageEvent
import com.alight.android.aoa_launcher.common.i.LauncherListener
import com.alight.android.aoa_launcher.common.provider.LauncherContentProvider
import com.alight.android.aoa_launcher.net.INetEvent
import com.alight.android.aoa_launcher.net.NetTools
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.LauncherCenterAdapter
import com.alight.android.aoa_launcher.ui.adapter.LauncherRightAdapter
import com.alight.android.aoa_launcher.ui.adapter.QualityHorizontalAdapter
import com.alight.android.aoa_launcher.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus


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


    companion object {
        lateinit var mINetEvent: INetEvent
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

        tv_cdc_launcher.setOnClickListener(this)
        tv_czc_launcher.setOnClickListener(this)
        tv_translate_launcher.setOnClickListener(this)
        tv_seek_help_launcher.setOnClickListener(this)
        tv_answer_launcher.setOnClickListener(this)
        tv_read_book_launcher.setOnClickListener(this)
        tv_fun_card_launcher.setOnClickListener(this)
        tv_wrong_topic_launcher.setOnClickListener(this)
        tv_kspg_launcher.setOnClickListener(this)
        tv_ywzwpg_launcher.setOnClickListener(this)
        tv_yyzwpg_launcher.setOnClickListener(this)
        tv_favorite_launcher.setOnClickListener(this)
        iv_study_plan.setOnClickListener(this)
        iv_call_parent.setOnClickListener(this)

        iv_user_icon_new_launcher.setOnClickListener(this)
        tv_user_name_new_launcher.setOnClickListener(this)
        iv_all_app_launcher.setOnClickListener(this)
        tv_dialog_launcher.setOnClickListener(this)
        tv_read_book_launcher.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        val splashClose = SPUtils.getData("splashClose", false) as Boolean
        Log.i(TAG, "splashClose = $splashClose splashCloseFlag = $splashCloseFlag")
        val rebinding = SPUtils.getData("rebinding", false) as Boolean
        //用户重新绑定
        if (rebinding) {
            initAccountUtil()
            SPUtils.syncPutData("rebinding", false)
        }
        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
        if (!splashClose && !splashCloseFlag && tokenPairCache.isNullOrEmpty()) {
            Log.i(TAG, "展示引导页")
//        如果未展示过引导则展示引导页
            activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
        }
        if (!splashCloseFlag && tv_user_name_new_launcher.text.isNullOrEmpty()) {
            Glide.with(this@NewLauncherActivity)
                .load(tokenPair?.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .error(if (tokenPair?.gender == 1) R.drawable.splash_boy else R.drawable.splash_girl)
                .into(iv_user_icon_new_launcher)
            tv_user_name_new_launcher.text = tokenPair?.name
        }
        splashCloseFlag = false
    }

    private fun initAccountUtil() {
        if (AccountUtil.currentUserId != null)
            return
        AccountUtil.register(this)
        val userId = SPUtils.getData(AppConstants.USER_ID, -1) as Int
        if (userId != -1) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val allToken = AccountUtil.getAllToken()
                    allToken.forEach {
                        if (it.userId == userId) {
                            AccountUtil.selectUser(it.userId)
                        }
                    }
                    //设置用户信息
                    getPresenter().setPersonInfo(this@NewLauncherActivity)
                    // 用户绑定极光推送
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

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }


    override fun initData() {
        mINetEvent = this
        val tokenPairCache = SPUtils.getData("tokenPair", "") as String
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
                    splashCloseFlag = true
                    val syncPutData = SPUtils.syncPutData("splashClose", true)
                    Log.i(TAG, "initAccountUtil")
                    //初始化用户工具及展示用户数据
                    initAccountUtil()
                }
                //使用Launcher调起选择用户
                AppConstants.RESULT_CODE_LAUNCHER_START_SELECT_USER -> {
                    activityResultLauncher?.launch(Intent(this, SplashActivity::class.java))
                }
            }
        }
        // 用户绑定极光推送
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
        if (!InternetUtil.isNetworkAvalible(this)) {
            netState = 0
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
        //插入数据前清除之前的数据
        contentResolver.delete(LauncherContentProvider.URI, null, null)
        val contentValues = ContentValues()
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME, tokenPair.expireTime)
        contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE, tokenPair.gradeType)
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
                    heartbeat()
                }
            }
        }
    }

    private fun heartbeat() {
        GlobalScope.launch(Dispatchers.IO) {
            getPresenter().getModel(Urls.HEART_BEAT, hashMapOf(), BaseBean::class.java)
            //每15秒调用一次打点接口
            delay(1000 * 15)
            heartbeat()
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
            //书本指读（AR指读）
            R.id.tv_read_book_launcher -> {
                getPresenter().startActivity(
                    this,
                    "com.jxw.huiben",
                    "com.jxw.huiben.activity.SplashActivity"
                )
            }
            //查单词
            R.id.tv_cdc_launcher -> {
                getPresenter().startAoaApp(this, 135, "/home")
            }
            //查字词
            R.id.tv_czc_launcher -> {
                getPresenter().startAoaApp(this, 134, "/home")
            }
            //翻译-AOA翻译
            R.id.tv_translate_launcher -> {
                getPresenter().startAoaApp(this, 138, "/app/138/home")
            }
            //求助老师-AOA 的远程辅导页面
            R.id.tv_seek_help_launcher -> {
                getPresenter().startAoaApp(this, 140, "/home/140")
            }
            //答题-AOA搜题
            R.id.tv_answer_launcher -> {
                getPresenter().startAoaApp(this, 139, "/apps/139/main")
            }
            //书本指读-九学王-AR指读
            R.id.tv_read_book_launcher -> {
            }
            //趣味卡牌-自己做的卡牌游戏
            R.id.tv_fun_card_launcher -> {
            }
            //错题本-AOA收藏夹
            R.id.tv_wrong_topic_launcher -> {
                getPresenter().startAoaApp(this, 141, "/app/141/home")
            }
            //口算批改
            R.id.tv_kspg_launcher -> {
                getPresenter().startAoaApp(this, 142, "/app/142/home")
            }
            //语文作文批改
            R.id.tv_ywzwpg_launcher -> {
                getPresenter().startAoaApp(this, 143, "/app/143/home")
            }
            //英语作文批改
            R.id.tv_yyzwpg_launcher -> {
                getPresenter().startAoaApp(this, 144, "/app/144/home")
            }
            //收藏夹-AOA收藏夹
            R.id.tv_favorite_launcher -> {
                getPresenter().startAoaApp(this, 141, "/app/141/home")
            }
            //学习计划-AOA学习计划
            R.id.iv_study_plan
            -> {
                getPresenter().startAoaApp(this, 33, "/home")
            }
            //呼叫家长
            R.id.iv_call_parent, R.id.iv_user_icon_new_launcher, R.id.tv_user_name_new_launcher
            -> {
                if (tokenPair == null) return
                var intent = Intent(this, PersonCenterActivity::class.java)
                intent.putExtra("userInfo", tokenPair)
                intent.putExtra("netState", netState)
                activityResultLauncher?.launch(intent)
            }
            R.id.iv_all_app_launcher -> {
                getPresenter().showDialog(AppConstants.ALL_APP)
            }
            //选年级
            R.id.tv_dialog_launcher -> {
                getPresenter().showSelectGradeDialog(this, tv_dialog_launcher)

            }
        }
    }

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
                ll_ar_launcher1.visibility = View.VISIBLE
                ll_ar_launcher2.visibility = View.VISIBLE
                ll_ar_launcher3.visibility = View.VISIBLE
                iv_ip_image.visibility = View.VISIBLE
                fl_center_launcher.visibility = View.GONE
                fl_right_launcher.visibility = View.GONE
                rv_quality_launcher.visibility = View.GONE
            }
            AppConstants.LAUNCHER_TYPE_CHINESE, AppConstants.LAUNCHER_TYPE_MATHEMATICS, AppConstants.LAUNCHER_TYPE_ENGLISH -> {
                ll_ar_launcher1.visibility = View.GONE
                ll_ar_launcher2.visibility = View.GONE
                ll_ar_launcher3.visibility = View.GONE
                iv_ip_image.visibility = View.GONE
                fl_center_launcher.visibility = View.VISIBLE
                fl_right_launcher.visibility = View.VISIBLE
                rv_quality_launcher.visibility = View.GONE
                fl_center_launcher.setBackgroundResource(R.drawable.launcher_syn_learn_bg)
                fl_right_launcher.setBackgroundResource(R.drawable.launcher_instruction_after_class_bg)
                setAdapterUI(launcherType)
                setRightAdapter(launcherType)
            }
            AppConstants.LAUNCHER_TYPE_QUALITY -> {
                ll_ar_launcher1.visibility = View.GONE
                ll_ar_launcher2.visibility = View.GONE
                ll_ar_launcher3.visibility = View.GONE
                iv_ip_image.visibility = View.GONE
                fl_center_launcher.visibility = View.GONE
                fl_right_launcher.visibility = View.GONE
                rv_quality_launcher.visibility = View.VISIBLE
                if (qualityHorizontalAdapter == null) {
                    qualityHorizontalAdapter = QualityHorizontalAdapter()
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
                val appPackName = launcherCenterAdapter!!.data[position].appPackName
                val className = launcherCenterAdapter!!.data[position].className
                if (!StringUtils.isEmpty(appPackName) && !StringUtils.isEmpty(className)) {
                    getPresenter().startActivity(this, appPackName, className)
                }
            }
        }
        rv_center_launcher.layoutManager = GridLayoutManager(this, 3)
        rv_center_launcher.adapter = launcherCenterAdapter
        launcherCenterAdapter?.setShowType(launcherType)
    }

    private fun setRightAdapter(launcherType: String) {
        if (launcherRightAdapter == null) {
            val linearLayoutManager = LinearLayoutManager(this)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            rv_right_launcher.layoutManager = linearLayoutManager
            launcherRightAdapter = LauncherRightAdapter()
            rv_right_launcher.adapter = launcherRightAdapter
        }
        launcherRightAdapter?.setShowType(launcherType)
    }

    private fun showLeftSelectUI(id: Int) {
        iv_ar_launcher.visibility = if (id == R.id.tv_ar_launcher) View.VISIBLE else View.GONE
        iv_chinese_launcher.visibility =
            if (id == R.id.tv_chinese_launcher) View.VISIBLE else View.GONE
        iv_mathematics_launcher.visibility =
            if (id == R.id.tv_mathematics_launcher) View.VISIBLE else View.GONE
        iv_english_launcher.visibility =
            if (id == R.id.tv_english_launcher) View.VISIBLE else View.GONE
        iv_quality_launcher.visibility =
            if (id == R.id.tv_quality_launcher) View.VISIBLE else View.GONE
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
            intent.putExtra("childId", AccountUtil.getCurrentUser().userId.toString())
            intent.putExtra("called", 2)
            intent.putExtra("token", AccountUtil.getCurrentUser().token)
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
                Log.e(TAG, "onNetChanged:网络正常 ")
            }
        }
    }


    override fun onConnect() {
    }

    override fun onDisconnect() {
    }


}
