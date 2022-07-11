package com.alight.android.aoa_launcher.utils;

import android.content.Context;
import android.provider.Settings;//导入包
import android.view.inputmethod.InputMethodManager;
// compile 'com.jakewharton.timber:timber:2.7.1'

public class InputMethodUtil {
    /**
     * 若触宝输入法已安装，则设其为系统默认输入法
     * (写入Android系统数据库)
     */
    public static void setDefaultInputMethod(Context context) {
        //获取系统已安装的输入法ID
        String[] methods = getInputMethodIdList(context);
        if (methods == null || methods.length == 0) {
//            Timber.w(String.format("found no input method."));
            return;
        }

        //检查是否安装触宝输入法
        //触宝输入法ID "com.cootek.smartinputv5/com.cootek.smartinput5.TouchPalIME";
//        var inputTypeId =
//                "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME"
        String targetKeyword = "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME";
        String value = "";
        for (String m : methods) {
//            Timber.d(String.format("find : %s", m));
            if (m.toLowerCase().contains(targetKeyword.toLowerCase())) {
                value = m;//找到触宝输入法
            }
        }
        if (value == "") {
//            Timber.w(String.format("didn't find " + targetKeyword));
            return;
        }

        //设置默认输入法
        String key = Settings.Secure.DEFAULT_INPUT_METHOD;
        boolean success = Settings.Secure.putString(context.getContentResolver(), key, value);
//        Timber.d(String.format("writeDbDefaultInputMethod(%s),result: %s", value,success));

        //读取默认输入法
        String current = Settings.Secure.getString(context.getContentResolver(), key);
//        Timber.d(String.format("current default: %s",current));
    }

    /**
     * 获取系统已安装的输入法ID
     *
     * @param context
     * @return
     */
    public static String[] getInputMethodIdList(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.getInputMethodList() != null) {
            String[] methodIds = new String[imm.getInputMethodList().size()];
            for (int i = 0; i < imm.getInputMethodList().size(); i++) {
                methodIds[i] = imm.getInputMethodList().get(i).getId();
            }
            return methodIds;
        }
        return new String[]{};
    }
}