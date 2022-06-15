package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.*
import android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.common.bean.WifiBean
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.WifiListAdapter
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.*
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class WifiActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "WifiActivity"
    private var wifiListAdapter: WifiListAdapter? = null
    private var wifiConfigList: List<WifiConfiguration>? = null
    private var mWifiAdmin: WifiAdmin? = null
    private var realWifiList: ArrayList<WifiBean> = ArrayList()

    private var mWifiBean: WifiBean? = null

    //true 表示经历了连接过程
    private var connecting = false

    //true 表示进入页面时自动开启wifi
    private var startWifi = false

    //true 表示主动触发了一次连接
    private var activeConnect = false

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
                    val ping = InternetUtil.ping()
                    //经历过连接
                    if (connecting && ping) {
                        ToastUtils.showShort(this@WifiActivity, "连接成功！")
                        connecting = false
                        activeConnect = false
                        if (startWifi) {
                            finish()
                        }
                    } else if (!ping) {
                        ToastUtils.showShort(this@WifiActivity, "请重新联网或切换WiFi！")
                        connecting = false
                        activeConnect = false
                    }
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

        val PERMS_INITIAL = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(PERMS_INITIAL, 127)
        mWifiAdmin = WifiAdmin(this);
        startWifi = intent.getBooleanExtra("startWifi", false)
        if (startWifi && !wifiManager.isWifiEnabled) {
            openWifiAndScan()
        } else {
            switch_wifi.isChecked = wifiManager.isWifiEnabled
        }

        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        Log.i(TAG, "当前设备可以感知wifi: $hasSystemFeature")

        mWifiAdmin?.startScan(this)

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
        iv_setting_wifi.setOnClickListener(this)
        iv_adb_wifi.setOnClickListener(this)
        ll_back.setOnClickListener(this)
        switch_wifi.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                openWifiAndScan()
            } else {
                mWifiAdmin?.closeWifi(this)
                realWifiList.clear()
                wifiListAdapter?.notifyDataSetChanged()
            }
        }
        wifiListAdapter?.setOnItemChildClickListener { adapter, view, position ->
            //忽略此网络
            if (view.id == R.id.tv_ignore_network) {
                val wifiBean = adapter.data[position] as WifiBean
                val wifiConfiguration = mWifiAdmin?.IsExsits(wifiBean.wifiName)
                mWifiAdmin?.removeWifi(wifiConfiguration?.networkId!!)
                SPUtils.syncPutData("wifi" + wifiBean.wifiName, false)
                ToastUtils.showShort(this, "正在忽略此网络并断开连接")
            }
        }
        wifiListAdapter?.setOnItemClickListener { adapter, view, position ->
            activeConnect = true
            val wifiBean = adapter.data[position] as WifiBean
            this.mWifiBean = wifiBean
            val savePwd = SPUtils.getData("wifi" + wifiBean.wifiName, false) as Boolean
            var wifiConfiguration: WifiConfiguration? = null
            //正在连接的点击不做处理
            if (wifiBean.state == 2)
                return@setOnItemClickListener
            if (savePwd) {
                wifiConfiguration = mWifiAdmin?.IsExsits(wifiBean.wifiName)
            }
//            val index = mWifiAdmin?.getConfigIndex(wifiBean.wifiName)!!
            if (wifiBean.state == 1) {
                //已连接的wifi
            } else if (wifiConfiguration != null && savePwd) {
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
                //未连接的wifi
                showWifiDialog(wifiBean)
            }
        }
    }

    private fun openWifiAndScan() {
        mWifiAdmin?.openWifi(this)
        ToastUtils.showShort(this, "wifi已开启，正在扫描wifi请稍等")
        RxTimerUtil.interval(100) {
            if (wifiManager.scanResults != null && wifiManager.scanResults.size > 0) {
                RxTimerUtil.cancel()
                sortScaResult()
            }
        }
    }

    private fun showWifiDialog(wifiBean: WifiBean) {
        val powerDialog = CustomDialog(this, R.layout.dialog_wifi_connect)
        val tvWifiName = powerDialog.findViewById<TextView>(R.id.tv_wifi_name_dialog)
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
        powerDialog.findViewById<CheckBox>(R.id.cb_record_pwd)
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

    @SuppressLint("MissingPermission")
    private fun IsExsits(SSID: String): WifiConfiguration? {
        var existingConfigs = wifiManager.configuredNetworks
        existingConfigs.forEach {
            if (it.SSID.equals("\"" + SSID + "\"")) {
                return it;
            }
        }
        return null;
    }

    //将搜索到的wifi根据信号从强到弱进行排序
    private fun sortByLevel(list: MutableList<ScanResult>): MutableList<ScanResult> {
        var temp: ScanResult? = null
        for (i in list.indices) for (j in list.indices) {
            if (list[i].level > list[j].level) //level属性即为强度
            {
                temp = list[i]
                list[i] = list[j]
                list[j] = temp
            }
        }

        for (i in list.indices) {
            if (list[i].SSID.isNotEmpty() && list[i].SSID == getWifiSsid()) {
                //把已连接的元素放在首位
                Collections.swap(list, i, 0)
                break
            }
        }
        return list
    }

    /**
     * 获取当前连接的wifi名称
     */
    private fun getWifiSsid(): String {

        var ssid = ""

        var networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo?.isConnected!!) {

            var connectionInfo = wifiManager.connectionInfo;

            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {

                ssid = connectionInfo.ssid;

            }

        }

        return ssid.replace("\"", "")

    }

    /**
     *flag 返回true 并不能代表热点连接成功，但是返回false一定代表连接不成功
     *当密码位数不对时也会直接返回false，因此不能用该参数来判别是否连接成功
     *这也是我在项目中碰到的一个难题
     */

    /**

    连接到WPA2网络

    @param ssid 热点名

    @param password 密码

    @return 配置是否成功

     */


    //添加指定WIFI的配置信息,原列表不存在此SSID
    fun addWifiConfig(wifiList: List<ScanResult>, ssid: String, pwd: String): Int {
        var wifiId = -1
        for (i in wifiList.indices) {
            val wifi = wifiList[i]
            if (wifi.SSID == ssid) {
                Log.i("AddWifiConfig", "equals")
                val wifiCong = WifiConfiguration()
                wifiCong.SSID = "\"" + wifi.SSID + "\"" //\"转义字符，代表"
                wifiCong.preSharedKey = "\"" + pwd + "\"" //WPA-PSK密码
                wifiCong.hiddenSSID = false
                wifiCong.status = WifiConfiguration.Status.ENABLED
                wifiId =
                    wifiManager.addNetwork(wifiCong) //将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
                if (wifiId != -1) {
                    return wifiId
                }
            }
        }
        return wifiId
    }

    //连接指定Id的WIFI
    fun connectWifi(wifiId: Int): Boolean {
        for (i in wifiConfigList!!.indices) {
            val wifi = wifiConfigList!![i]
            if (wifi.networkId == wifiId) {
                while (!wifiManager.enableNetwork(wifiId, true)) { //激活该Id，建立连接
                    //status:0--已经连接，1--不可连接，2--可以连接
                    Log.i("ConnectWifi", wifiConfigList!![wifiId].status.toString())
                }
                return true
            }
        }
        return false
    }

    //判定指定WIFI是否已经配置好,依据WIFI的地址BSSID,返回NetId
    fun isConfiguration(SSID: String): Int {
        Log.i("IsConfiguration", wifiConfigList!!.size.toString())
        for (i in wifiConfigList!!.indices) {
            Log.i(wifiConfigList!![i].SSID, wifiConfigList!![i].networkId.toString())
            if (wifiConfigList!![i].SSID == SSID) { //地址相同
                return wifiConfigList!![i].networkId
            }
        }
        return -1
    }

    /**
     * 移除wifi，因为权限，无法移除的时候，需要手动去翻wifi列表删除
     * 注意：！！！只能移除自己应用创建的wifi。
     * 删除掉app，再安装的，都不算自己应用，具体看removeNetwork源码
     *
     * @param netId wifi的id
     */
    fun removeWifi(netId: Int): Boolean {
        return wifiManager.removeNetwork(netId)
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
            R.id.iv_adb_wifi -> {
                getPresenter().showAdbWifi()
            }
            R.id.iv_setting_wifi -> {
                getPresenter().showWifiSetting(this)
            }
            R.id.ll_back -> {
                finish()
            }
        }
    }

}