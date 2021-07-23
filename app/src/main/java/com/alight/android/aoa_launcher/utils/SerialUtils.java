package com.alight.android.aoa_launcher.utils;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取设备信息和DSN
 */
public class SerialUtils {

  /*  public static Map<String,String> getDviceInfo(){
        Map<String,String> mp =new HashMap<>(3);
        mp.put("dsn",getCPUSerial());
        mp.put("name", SystemProperties.get("ro.product.name","Unkown"));
        mp.put("type_name",Build.MODEL);
        mp.put("os_version",Build.VERSION.RELEASE);
        mp.put("device_type",Build.DEVICE);
        mp.put("remark",Build.FINGERPRINT);
        return mp;

    }*/


    /**
     * 获取DSN
     * @return
     */
    public static String getCPUSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Build.getSerial();
        }
        //读取CPU信息
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String cpu = null;
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((cpu = bufferedReader.readLine()) != null) {
                if (cpu.contains("Serial")) {
                    cpu = cpu.substring(cpu.indexOf(":") + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cpu != null ? cpu.toUpperCase() : "deadbeef";
    }
}
