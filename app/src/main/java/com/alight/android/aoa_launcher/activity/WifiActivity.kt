package com.alight.android.aoa_launcher.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
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

    private fun scanSuccess() {
        val results = wifiManager.scanResults
//        wifiListAdapter?.data?.clear()
        wifiListAdapter?.setNewInstance(results)
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
        } else {

        }

//        val scanResults = wifiManager.scanResults
//        wifiListAdapter?.setNewInstance(scanResults)
    }

    override fun initView() {
        if (wifiListAdapter == null) {
            rv_wifi_list.layoutManager = LinearLayoutManager(this)
            wifiListAdapter = WifiListAdapter()
            rv_wifi_list.adapter = wifiListAdapter
        }
    }

    override fun setListener() {
        iv_setting_wifi.setOnClickListener(this)
        iv_adb_wifi.setOnClickListener(this)
        switch_wifi.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//            } else {
//            }
//            }
        }
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
}