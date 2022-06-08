package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alight.android.aoa_launcher.R
import com.alight.android.aoa_launcher.application.LauncherApplication
import com.alight.android.aoa_launcher.common.base.BaseActivity
import com.alight.android.aoa_launcher.presenter.PresenterImpl
import com.alight.android.aoa_launcher.ui.adapter.WifiListAdapter
import kotlinx.android.synthetic.main.activity_wifi.*


class WifiActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "WifiActivity"
    private var wifiListAdapter: WifiListAdapter? = null

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
        wifiListAdapter?.setNewInstance(filterScanResult)
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
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

}