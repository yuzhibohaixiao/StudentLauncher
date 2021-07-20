package com.alight.android.aoa_launcher.utils;

import android.content.UriMatcher;


public class UriMatcherUtil {
    /**
     * 标识码
     */
    public static final int CODE_ID_1 = 1;
    public static final int CODE_ID_2 = 2;
    public static final String HOST = "com.alight.android.aoa_launcher.common.provider.LauncherContentProvider";
    /**
     * 路径
     */
    public static final String PATH = "students";
    /**
     * 初始化UriMatcher工具类
     */
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // # 为通配符
        uriMatcher.addURI(HOST, PATH, CODE_ID_1);
        uriMatcher.addURI(HOST, PATH + "/#", CODE_ID_2);
    }
}
