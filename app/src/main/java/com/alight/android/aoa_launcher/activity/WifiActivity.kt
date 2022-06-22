package com.alight.android.aoa_launcher.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.WifiBean
import com.alight.android.aoa_launcher.common.event.SplashStepEvent
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.WifiListAdapter
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class WifiActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "WifiActivity"
    private var wifiListAdapter: WifiListAdapter? = null
    private var wifiConfigList: List<WifiConfiguration>? = null
    private var mWifiAdmin: WifiAdmin? = null
    private var realWifiList: ArrayList<WifiBean> = ArrayList()
    private var adbBackdoorFlag = 0
    private var wifiBackdoorFlag = 0
//    private var wifiLock = false

    private var mWifiBean: WifiBean? = null

    //true 表示经历了连接过程
    private var connecting = false

    //true 表示进入页面时自动开启wifi
    private var startWifi = false

    //true 表示主动触发了一次连接
    private var activeConnect = false

    private var isConnected = false

    val wifiManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val connectivityManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val wifiScanReceiver: WifiReceiver? = null

/*
    val wifiScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }
*/

    val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "onReceive: intent action" + intent.action)
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                Log.i(TAG, "onReceive: SCAN_RESULTS_AVAILABLE_ACTION")
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
                //当扫描结果后，进行刷新列表
                /* refreshLocalWifiListData()
                 if (null != listener) {
                     listener.onScanResultAvailable()
                 }*/
            } else if (intent.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) { //wifi连接网络状态变化
                Log.i(TAG, "onReceive: NETWORK_STATE_CHANGED_ACTION")
//                sortScaResult()
                val info: NetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)!!
                Log.d(TAG, "--NetworkInfo--$info")
                if (NetworkInfo.State.DISCONNECTED == info.state) { //wifi没连接上

                    Log.d(TAG, "wifi没连接上")
//                    Toast.makeText(this@WifiActivity, "wifi没连接上", Toast.LENGTH_SHORT).show()
//                    hidingProgressBar()
                    for (i in realWifiList.indices) { //没连接上将 所有的连接状态都置为“未连接”
                        realWifiList[i].state = 3
                    }
                    wifiListAdapter?.notifyDataSetChanged()
                    //经历过连接
                    if (connecting) {
                        ToastUtils.showShort(this@WifiActivity, "连接失败，请重试！")
                        //主动连接且未连接成功，提示弹窗
                        if (mWifiBean != null && activeConnect) {
                            showWifiDialog(mWifiBean!!)
                            activeConnect = false
                        }
                        connecting = false
                    }
                } else if (NetworkInfo.State.CONNECTED == info.state) { //wifi连接上了
                    Log.d(TAG, "wifi连接上了")
                    //连接成功 跳转界面 传递ip地址
//                    hidingProgressBar()
                    val connectedWifiInfo: WifiInfo = wifiManager.connectionInfo
//                    val ping = InternetUtil.ping()
                    //经历过连接
                    if (connecting) {
                        isConnected = true
                        ToastUtil.showToast(
                            LauncherApplication.getContext(),
                            "连接成功！"
//                            "连接成功！realWifiList size = ${realWifiList.size}"
                        )
                        if (realWifiList.size == 0) {
                            RxTimerUtil.interval(500) {
                                if (wifiManager.scanResults != null && wifiManager.scanResults.size > 0) {
                                    RxTimerUtil.cancel()
                                    sortScaResult()
                                    val connectType = 1
                                    wifiListSet(connectedWifiInfo.ssid, connectType)
                                }
                            }
                        }
                        connecting = false
                        activeConnect = false
                        if (startWifi) {
                            //直接跳转到步骤2
                            EventBus.getDefault().post(SplashStepEvent.getInstance(2))
                            finish()
                        }
                    }
                    /*else if (!ping) {
                        ToastUtils.showShort(this@WifiActivity, "请重新联网或切换WiFi！")
                        connecting = false
                        activeConnect = false
                    }*/
                    val connectType = 1
                    wifiListSet(connectedWifiInfo.ssid, connectType)
                } else if (NetworkInfo.State.CONNECTING == info.state) { //正在连接
                    Log.d(TAG, "wifi正在连接")
                    //经历了连接
                    connecting = true
//                    showProgressBar()
                    val connectedWifiInfo: WifiInfo = wifiManager.connectionInfo
                    val connectType = 2
                    wifiListSet(connectedWifiInfo.ssid, connectType)
                }
                /*    refreshLocalWifiListData()
                    val state =
                        (intent.getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO) as NetworkInfo?)!!.detailedState
                    if (null != listener) {
                        listener.onNetWorkStateChanged(state, mSSID)
                    }*/
            } else if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION) { //wifi状态变化
                Log.i(TAG, "onReceive: WIFI_STATE_CHANGED_ACTION")
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
                when (state) {
                    WifiManager.WIFI_STATE_DISABLED -> {
                        Log.d(TAG, "已经关闭")
                    }
                    WifiManager.WIFI_STATE_DISABLING -> {
                        Log.d(TAG, "正在关闭")
                    }
                    WifiManager.WIFI_STATE_ENABLED -> {
                        Log.d(TAG, "已经打开")
                        sortScaResult()
                    }
                    WifiManager.WIFI_STATE_ENABLING -> {
                        Log.d(TAG, "正在打开")
                    }
                    WifiManager.WIFI_STATE_UNKNOWN -> {
                        Log.d(TAG, "未知状态")
                    }
                }

//                val wifiState = intent.getIntExtra(
//                    WifiManager.EXTRA_WIFI_STATE,
//                    WifiManager.WIFI_STATE_DISABLED
//                )
                /*  if (null != listener) {
                      listener.onWiFiStateChanged(wifiState)
                  }*/
            } else if (intent.action == WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) {
                Log.i(TAG, "onReceive: SUPPLICANT_STATE_CHANGED_ACTION")
                val state = intent.getParcelableExtra<SupplicantState>(WifiManager.EXTRA_NEW_STATE)
                val error = intent.getIntExtra(EXTRA_SUPPLICANT_ERROR, 0)
                if (intent.hasExtra(EXTRA_SUPPLICANT_ERROR)) {
                    //failed to connect
//                    ToastUtils.showShort(this@WifiActivity, "连接失败！")
                } else if (error == WifiManager.ERROR_AUTHENTICATING) {
//                    ToastUtils.showShort(this@WifiActivity, "连接失败，请重试！")
                }


                /*if (null != listener) {
                    if (error == WifiManager.ERROR_AUTHENTICATING) {

                    //这里可以获取到监听连接wifi密码错误的时候进行回调
                        listener.onWifiPasswordFault()
                    }*/
            }
        }
    }

    /**
     * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
     * @param wifiName
     * @param type
     */
    fun wifiListSet(wifiName: String, type: Int) {
        var index = -1;
        var wifiInfo = WifiBean()
        if (realWifiList == null || realWifiList.size == 0) {
            return
        }
        for (i in realWifiList.indices) {
            realWifiList[i].state = 3
        }
        realWifiList.sort()//根据信号强度排序
        for (i in realWifiList.indices) {
            var wifiBean = realWifiList.get(i);
            if (index == -1 && ("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {
                index = i;
                wifiInfo.setLevel(wifiBean.getLevel());
                wifiInfo.setWifiName(wifiBean.getWifiName());
                wifiInfo.setCapabilities(wifiBean.getCapabilities());
                if (type == 1) {
                    wifiInfo.state = 1
                } else {
                    wifiInfo.state = 2
                }
            }
        }
        if (index != -1) {
            realWifiList.removeAt(index);
            realWifiList.add(0, wifiInfo);
            GlobalScope.launch(Dispatchers.Main) {
                wifiListAdapter?.notifyDataSetChanged();
            }
        }
    }

    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     */
    private fun sortScaResult() {
        if (isConnected) return
        val scanResults = WifiBeanUtil.noSameName(wifiManager.scanResults)
        realWifiList.clear()
        if (scanResults != null && scanResults.size > 0) {
            for (i in scanResults.indices) {
                val wifiBean = WifiBean()
                wifiBean.wifiName = scanResults[i].SSID
                wifiBean.state = 3 //只要获取都假设设置成未连接，真正的状态都通过广播来确定
                wifiBean.capabilities = scanResults[i].capabilities
                wifiBean.level = scanResults[i].level
                realWifiList.add(wifiBean)
                //排序
                realWifiList.sort()
                if (wifiListAdapter?.data == null || wifiListAdapter?.data!!.size == 0) {
                    wifiListAdapter?.setNewInstance(realWifiList)
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        wifiListAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun scanSuccess() {
//        val results = wifiManager.scanResults
//        sortScaResult()
//        val filterScanResult = filterScanResult(results)
//        filterScanResult?.sort { scanResult, scanResult2 ->
//        }
//        filterScanResult.sort()
//        Arrays.sort(rssi,new Comparator() {
//            @Override
//
//            public int compare(String[] str1, String[] str2) {
//                final String lv1= str1[0];
//
//                final String lv2= str2[0];
//
//                return lv1.compareTo(lv2);
//
//            }
        //wifi列表排序
//        val sortScanResult = sortByLevel(filterScanResult)
//        wifiListAdapter?.setNewInstance(sortScanResult)
//        ... use new scan results ...
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
//        ... potentially use older scan results ...
    }

/*
    private fun setNetStateListener() {
        var request = NetworkRequest.Builder().build()
        var connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerNetworkCallback(request, object : NetStateUtil() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                sortScaResult()

                val connectedWifiInfo: WifiInfo = wifiManager.connectionInfo
                //连接成功 跳转界面 传递ip地址
                val connectType = 1
                wifiListSet(connectedWifiInfo.ssid, connectType)

                ToastUtils.showShort(this@WifiActivity, "网络连接成功！")
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                GlobalScope.launch(Dispatchers.Main) {
                    ToastUtils.showShort(this@WifiActivity, "连接失败2！")
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                for (i in realWifiList.indices) { //没连接上将 所有的连接状态都置为“未连接”
                    realWifiList[i].state = 3
                }
                GlobalScope.launch(Dispatchers.Main) {
                    wifiListAdapter?.notifyDataSetChanged()
                    ToastUtils.showShort(this@WifiActivity, "连接失败1！")
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                GlobalScope.launch(Dispatchers.Main) {
                    ToastUtils.showShort(this@WifiActivity, "网络连接超时或者网络连接不可达！")
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                // 表明此网络连接成功验证
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        // 使用WI-FI
                        val connectedWifiInfo: WifiInfo = wifiManager.connectionInfo
                        //连接成功 跳转界面 传递ip地址
                        val connectType = 1
                        wifiListSet(connectedWifiInfo.ssid, connectType)
//                LogUtil.instance.d("当前在使用WiFi上网")
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        // 使用数据网络
//                LogUtil.instance.d("当前在使用数据网络上网")
                    } else {
//                LogUtil.instance.d("当前在使用其他网络")
                        // 未知网络，包括蓝牙、VPN等
                    }
                }
            }
        })
    }
*/

    override fun initData() {
//        setNetStateListener()

        registerWifiReceiver()

//        val PERMS_INITIAL = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
//        requestPermissions(PERMS_INITIAL, 127)
        mWifiAdmin = WifiAdmin(this);
        startWifi = intent.getBooleanExtra("startWifi", false)
        val wifiEnabled = wifiManager.isWifiEnabled
        if (startWifi && !wifiEnabled) {
            openWifiAndScan()
        } else {
            switch_wifi.isChecked = wifiEnabled
        }
//        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
//        Log.i(TAG, "当前设备可以感知wifi: $hasSystemFeature")
        if (wifiEnabled) {
            mWifiAdmin?.startScan(this)
        }

/*
        if (InternetUtil.isNetworkAvalible(this)) {
            sortScaResult()

            val connectedWifiInfo: WifiInfo = wifiManager.connectionInfo
            //连接成功 跳转界面 传递ip地址
            val connectType = 1
            wifiListSet(connectedWifiInfo.ssid, connectType)
        }
*/

//        val success = wifiManager.startScan()
//        Log.i(TAG, "开始扫描: $success")
//        if (!success) {
        // scan failure handling
//            scanFailure()
//        }
//        getConfiguration()
    }

    private fun registerWifiReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)//监听wifi是开关变化的状态
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)//监听wifiwifi连接状态广播
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)//监听wifi列表变化（开启一个热点或者关闭一个热点）
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(wifiScanReceiver)
    }

    override fun onResume() {
        super.onResume()
        /*  val intentFilter = IntentFilter()
          intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)//监听wifi是开关变化的状态
          intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)//监听wifiwifi连接状态广播
          intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)//监听wifi列表变化（开启一个热点或者关闭一个热点）
          intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
          registerReceiver(wifiScanReceiver, intentFilter)*/
    }

    override fun onPause() {
        super.onPause()
//        unregisterReceiver(wifiScanReceiver)
    }


    override fun initView() {
        if (wifiListAdapter == null) {
            rv_wifi_list.layoutManager = LinearLayoutManager(this)
            wifiListAdapter = WifiListAdapter()
            wifiListAdapter?.setEmptyView(View.inflate(this, R.layout.item_wifi_empty, null))
            rv_wifi_list.adapter = wifiListAdapter
        }
    }

    override fun setListener() {
//        iv_setting_wifi.setOnClickListener(this)
//        iv_adb_wifi.setOnClickListener(this)
        fl_adb_backdoor.setOnClickListener(this)
        fl_wifi_backdoor.setOnClickListener(this)
        ll_back.setOnClickListener(this)
        switch_wifi.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                openWifiAndScan()
            } else {
                isConnected = false
                mWifiAdmin?.closeWifi(this, switch_wifi)
                realWifiList.clear()
                wifiListAdapter?.notifyDataSetChanged()
            }
            /* buttonView.isEnabled = false
             GlobalScope.launch(Dispatchers.Main) {
                 delay(2000)
                 buttonView.isEnabled = true
             }*/
        }
        wifiListAdapter?.setOnItemChildClickListener { adapter, view, position ->
            //忽略此网络
            if (view.id == R.id.tv_ignore_network) {
                val wifiBean = adapter.data[position] as WifiBean
                showWifiIgnoreDialog(wifiBean)
            }
        }
        wifiListAdapter?.setOnItemClickListener { adapter, view, position ->
            if (connecting) return@setOnItemClickListener
            val wifiBean = adapter.data[position] as WifiBean
            this.mWifiBean = wifiBean
            val savePwd = SPUtils.getData("wifi" + wifiBean.wifiName, true) as Boolean
            var wifiConfiguration: WifiConfiguration? = null
            //正在连接的点击不做处理
            if (wifiBean.state == 2)
                return@setOnItemClickListener
            if (savePwd) {
                wifiConfiguration = mWifiAdmin?.IsExsits(wifiBean.wifiName)
            }
            /*   ToastUtils.showShort(
                   this,
                   "wifiConfiguration null = ${wifiConfiguration == null} savePwd = $savePwd"
               )*/
//            val index = mWifiAdmin?.getConfigIndex(wifiBean.wifiName)!!
            if (wifiBean.state == 1) {
                //已连接的wifi
            } else if (wifiConfiguration != null && savePwd) {
                activeConnect = true
                //有记录的wifi 无需输入密码 直接连接
                // 连接配置好的指定ID的网络
//                wifiManager.enableNetwork(
//                    wifiConfiguration.networkId,
//                    true
//                )
                val isConnected = mWifiAdmin?.addNetwork(wifiConfiguration)
//                if (isConnected!!) {
//                    ToastUtils.showShort(this@WifiActivity, "连接成功！")
//                } else {
//                    ToastUtils.showShort(this@WifiActivity, "连接失败，请重试！")
//                    showWifiDialog(wifiBean)
//                }
            } else {
                activeConnect = true
                //未连接的wifi
                showWifiDialog(wifiBean)
            }
        }
    }

    /**
     * 忽略网络的确认框
     */
    private fun showWifiIgnoreDialog(wifiBean: WifiBean) {
        val customDialog = CustomDialog(this, R.layout.dialog_wifi_ignore)
        val tvWifiName = customDialog.findViewById<TextView>(R.id.tv_wifi_name_dialog)
        tvWifiName.text = wifiBean.wifiName
        customDialog.findViewById<TextView>(R.id.confirm).setOnClickListener {
            val wifiConfiguration = mWifiAdmin?.IsExsits(wifiBean.wifiName)
            mWifiAdmin?.removeWifi(wifiConfiguration?.networkId!!)
            SPUtils.syncPutData("wifi" + wifiBean.wifiName, false)
            ToastUtils.showShort(this, "正在忽略此网络并断开连接")
            customDialog.dismiss()
        }
        customDialog.findViewById<TextView>(R.id.cancel).setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun openWifiAndScan() {
        mWifiAdmin?.openWifi(this, switch_wifi)
        ToastUtils.showShort(this, "wifi已开启，正在扫描wifi请稍等")
        RxTimerUtil.interval(500) {
            if (wifiManager.scanResults != null && wifiManager.scanResults.size > 0) {
                RxTimerUtil.cancel()
                sortScaResult()
            }
        }
    }

    private fun showWifiDialog(wifiBean: WifiBean) {
        val powerDialog = CustomDialog(this, R.layout.dialog_wifi_connect)
        val tvWifiName = powerDialog.findViewById<TextView>(R.id.tv_wifi_name_dialog)
        val cbRecordPwd = powerDialog.findViewById<CheckBox>(R.id.cb_record_pwd)
        tvWifiName.text = wifiBean.wifiName
        val etWifiPwd = powerDialog.findViewById<EditText>(R.id.et_wifi_pwd)
        powerDialog.findViewById<CheckBox>(R.id.cb_show_pwd)
            .setOnCheckedChangeListener { buttonView, isChecked ->
                //显示密码
                if (isChecked) {
                    etWifiPwd.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    etWifiPwd.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                etWifiPwd.setSelection(etWifiPwd.text.toString().length)
            }
        cbRecordPwd
            .setOnCheckedChangeListener { buttonView, isChecked ->
                //记录密码
                if (isChecked) {
                    SPUtils.syncPutData("wifi" + wifiBean.wifiName, true)
                } else {
                    SPUtils.syncPutData("wifi" + wifiBean.wifiName, false)
                }
            }
        powerDialog.findViewById<TextView>(R.id.tv_connect_cancel).setOnClickListener {
            //取消
            powerDialog.dismiss()
        }
        powerDialog.findViewById<TextView>(R.id.tv_connect_wifi).setOnClickListener {
            if (etWifiPwd.text.toString().length < 8) {
                Toast.makeText(this, "密码至少8位", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            //                    SPUtils.syncPutData("wifi" + scanResult.SSID, true)
            //记录密码
            if (cbRecordPwd.isChecked) {
                SPUtils.syncPutData("wifi" + wifiBean.wifiName, true)
            } else {
                SPUtils.syncPutData("wifi" + wifiBean.wifiName, false)
            }
            //加入（连接wifi）
            val isConnected = mWifiAdmin?.addNetwork(
                mWifiAdmin?.CreateWifiInfo(
                    wifiBean.wifiName,
                    etWifiPwd.text.toString(),
                    getWifiType(wifiBean)
                )
            )
//            if (isConnected!!) {
            powerDialog.dismiss()
//                ToastUtils.showShort(this@WifiActivity, "连接成功！")
//            } else {
//                ToastUtils.showShort(this@WifiActivity, "连接失败，请重试！")
//            }
        }
        powerDialog.show()
    }

    private fun getWifiType(wifiBean: WifiBean): Int {
        var TYPE: Int
        val capabilities = wifiBean.capabilities
        if (capabilities.isEmpty()) {
            TYPE = 1
        } else if (capabilities.contains("WEP")) {
            TYPE = 2
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains(
                "WPS"
            )
        ) {
            TYPE = 3
        } else {
            TYPE = 1
        }
        return TYPE
    }

    override fun initPresenter(): PresenterImpl {
        return PresenterImpl()
    }

    override fun getLayout(): Int {
        return R.layout.activity_wifi
    }

    override fun onSuccess(any: Any) {
    }

    override fun onError(error: String) {
    }

    override fun onClick(v: View) {
        when (v.id) {
            //adb后门
            R.id.fl_adb_backdoor -> {
                if (adbBackdoorFlag < 10) {
                    adbBackdoorFlag++
                } else {
                    adbBackdoorFlag = 0
                    getPresenter().showAdbWifi()
                }
            }
            //wifi后门
            R.id.fl_wifi_backdoor -> {
                if (wifiBackdoorFlag < 10) {
                    wifiBackdoorFlag++
                } else {
                    wifiBackdoorFlag = 0
                    getPresenter().showWifiSetting(this)
                }
            }
            R.id.ll_back -> {
                finish()
            }
        }
    }

}