package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.WifiListAdapter
import com.alight.android.aoa_launcher.ui.view.CustomDialog
import com.alight.android.aoa_launcher.utils.WifiUtil
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.android.synthetic.main.dialog_wifi_connect.*
import java.util.*


class WifiActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "WifiActivity"
    private var wifiListAdapter: WifiListAdapter? = null
    private var wifiConfigList: List<WifiConfiguration>? = null

    val wifiManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val connectivityManager =
        LauncherApplication.getContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    //得到Wifi配置好的信息
    @SuppressLint("MissingPermission")
    fun getConfiguration() {
        wifiConfigList = wifiManager.configuredNetworks //得到配置好的网络信息
        if (wifiConfigList == null || wifiConfigList?.size == 0) {
            return
        }
        for (i in 0 until wifiConfigList?.size!!) {
            Log.i(TAG, wifiConfigList?.get(i)?.SSID!!)
            Log.i(TAG, java.lang.String.valueOf(wifiConfigList?.get(i)?.networkId))
        }
    }

    /**
     * 过滤重复项
     */
    private fun filterScanResult(list: MutableList<ScanResult>): MutableList<ScanResult> {
        val linkedMap: LinkedHashMap<String, ScanResult> = LinkedHashMap(list.size)
        for (rst in list) {
            if (linkedMap.containsKey(rst.SSID)) {
                if (rst.level > linkedMap[rst.SSID]!!.level) {
                    linkedMap[rst.SSID] = rst
                }
                continue
            }
            linkedMap[rst.SSID] = rst
        }
        list.clear()
        list.addAll(linkedMap.values)
        return list
    }

    /**
     * WiFi未打开，开启wifi
     */
    private fun enableWifi() {
        if (wifiManager != null && !wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }

    private fun scanSuccess() {
        val results = wifiManager.scanResults
        val filterScanResult = filterScanResult(results)
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
        val sortScanResult = sortByLevel(filterScanResult)
        wifiListAdapter?.setNewInstance(sortScanResult)
//        ... use new scan results ...
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
//        ... potentially use older scan results ...
    }

    override fun initData() {
        val PERMS_INITIAL = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(PERMS_INITIAL, 127)

        val startWifi = intent.getBooleanExtra("startWifi", false)
        if (startWifi) {
            WifiUtil.openWifi(wifiManager)
        }

        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        Log.i(TAG, "当前设备可以感知wifi: $hasSystemFeature")

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        Log.i(TAG, "开始扫描: $success")
        if (!success) {
            // scan failure handling
            scanFailure()
        }
        getConfiguration()
    }

    override fun initView() {
        if (wifiListAdapter == null) {
            rv_wifi_list.layoutManager = LinearLayoutManager(this)
            wifiListAdapter = WifiListAdapter()
            wifiListAdapter?.setEmptyView(View.inflate(this, R.layout.item_wifi_empty, null))
            rv_wifi_list.adapter = wifiListAdapter
        }
        switch_wifi.isChecked = wifiManager.isWifiEnabled
    }

    override fun setListener() {
        iv_setting_wifi.setOnClickListener(this)
        iv_adb_wifi.setOnClickListener(this)
        ll_back.setOnClickListener(this)
        switch_wifi.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //wifi开启
                wifiManager.isWifiEnabled = true
                //  Toast.makeText(WifiActivity.this, "打开", Toast.LENGTH_SHORT).show();
                //第二次点击的时候，清除之前的list
//                isRefresh = true;
//                presenter.subscribe(isRefresh);
            } else {
                //wifi关闭
                wifiManager.isWifiEnabled = false
                wifiListAdapter?.data?.clear()
                wifiListAdapter?.notifyDataSetChanged()
//                Toast.makeText(WifiActivity.this, "关闭", Toast.LENGTH_SHORT).show();
//                listView.setVisibility(View.GONE);

            }
        }
        wifiListAdapter?.setOnItemClickListener { adapter, view, position ->
            val scanResult = adapter.data[position] as ScanResult
            if (scanResult.SSID.isNotEmpty() && scanResult.SSID == getWifiSsid()) {
                //已连接的wifi
            } else {
                //未连接的wifi
                val powerDialog = CustomDialog(this, R.layout.dialog_wifi_connect)
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
                    }
                powerDialog.show()
            }
            //连接方式
//            var wifiConfiguration = CreateWifiInfo(scanResult.SSID, "Password", Type)
//            var flag = addNetwork(wifiConfiguration) //连接网络
        }
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


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

}