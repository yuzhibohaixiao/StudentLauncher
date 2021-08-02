package com.alight.android.aoa_launcher.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alight.android.aoa_launcher.common.constants.AppConstants;

public class DbOpenHelper extends SQLiteOpenHelper {

    //数据库名称
    private static final String DATA_BASE_NAME = "people.db";

    //数据库版本号
    private static final int DATE_BASE_VERSION = 1;

    //表名-孩子
    public static final String BOY_TABLE_NAME = "child";

    //创建表-孩子（两列：主键自增长、姓名）
    private final String CREATE_BOY_TABLE = "create table " + BOY_TABLE_NAME + "(_id integer primary key autoincrement, " + AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN + " TEXT,"
            + AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR + " TEXT,"
            + AppConstants.AOA_LAUNCHER_USER_INFO_NAME + " TEXT,"
            + AppConstants.AOA_LAUNCHER_USER_INFO_GENDER + " INTEGER,"
            + AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID + " INTEGER,"
            + AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME + " REAL,"
            + AppConstants.AOA_LAUNCHER_USER_INFO_GRADE_TYPE + " INTEGER)"
            ;

    public DbOpenHelper(Context context) {
        super(context, DATA_BASE_NAME, null, DATE_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOY_TABLE);//创建孩子表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //一些数据库升级操作
    }
}