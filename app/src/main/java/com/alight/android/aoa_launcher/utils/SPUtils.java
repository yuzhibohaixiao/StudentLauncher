package com.alight.android.aoa_launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.alight.android.aoa_launcher.application.LauncherApplication;

/**
 * Created on 2018/8/14
 *
 * @author wangzhe
 */
public class SPUtils {

    //存储的sharedpreferences文件名
    private static final String SP_NAME = "SP_NAME";

    /**
     * 异步保存数据到文件
     *
     * @param key
     * @param data
     */
    public static void asyncPutData(String key, Object data) {

        String type = data.getClass().getSimpleName();
        SharedPreferences sharedPreferences = LauncherApplication.Companion.getContext()
                .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) data);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) data);
        } else if ("String".equals(type)) {
            editor.putString(key, (String) data);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) data);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) data);
        }

        editor.apply();
    }

    /**
     * 同步保存数据到文件
     *
     * @param key
     * @param data
     * @return
     */
    public static boolean syncPutData(String key, Object data) {

        String type = data.getClass().getSimpleName();
        SharedPreferences sharedPreferences = LauncherApplication.Companion.getContext()
                .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) data);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) data);
        } else if ("String".equals(type)) {
            editor.putString(key, (String) data);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) data);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) data);
        }

        return editor.commit();
    }

    /**
     * 从文件中读取数据
     * 根据默认值来确认转换类型
     *
     * @param key
     * @param defValue
     * @return
     */
    public static Object getData(String key, Object defValue) {

        String type = defValue.getClass().getSimpleName();
        SharedPreferences sharedPreferences = LauncherApplication.Companion.getContext().getSharedPreferences
                (SP_NAME, Context.MODE_PRIVATE);

        //defValue为为默认值，如果当前获取不到数据就返回它
        if ("Integer".equals(type)) {
            return sharedPreferences.getInt(key, (Integer) defValue);
        } else if ("Boolean".equals(type)) {
            return sharedPreferences.getBoolean(key, (Boolean) defValue);
        } else if ("String".equals(type)) {
            return sharedPreferences.getString(key, (String) defValue);
        } else if ("Float".equals(type)) {
            return sharedPreferences.getFloat(key, (Float) defValue);
        } else if ("Long".equals(type)) {
            return sharedPreferences.getLong(key, (Long) defValue);
        }

        return null;
    }

    public static void clearData(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (editor != null) {
            editor.clear();
            editor.commit();
        }
    }

}

