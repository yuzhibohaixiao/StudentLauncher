
package com.alight.android.aoa_launcher.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.alight.android.aoa_launcher.application.LauncherApplication;

/**
 * 九学王应用工具类
 */
public class UserDBUtil {
    /**
     * 保存信息的时候
     * 当年级或者其他信息更改是调用一下方法
     * UserDBUtil.keepLastRecord("小学","四年级",-1,-1,"",null);
     * <p>
     * 获取信息的时候
     * <p>
     * if (UserDBUtil.hasLastRecocrd()){
     * UserInfoBean userInfoRecocrd = UserDBUtil.getUserInfoRecocrd();
     * if (userInfoRecocrd!=null && !TextUtils.isEmpty(userInfoRecocrd.getGrade())){
     * //获取到年级信息
     * String grader=userInfoRecocrd.getGrade();
     * //TODO 根据年级信息去刷新数据
     * <p>
     * }
     * }
     * <p>
     * _id id
     * _section 学段
     * _grade 年级
     * _image 用户icon 头像
     * _sex 性别
     * _age 年龄
     * _nickname 昵称
     */
    public static final String ID = "_id";
    public static final String SECTION = "_section";
    public static final String GRADE = "_grade";
    public static final String IMAGE = "_image";
    public static final String SEX = "_sex";
    public static final String AGE = "_age";
    public static final String NICKNAME = "_nickname";
    public static String LAUNCHER_GRADE = "一年级上      ▼";


    public static final Uri LAST_RECORDR_URI = Uri.parse("content://com.jxw.mskt.video.userInfoProvider/userinfo");


    /**
     * 一般使用这个方法   主要需要传年级和学段
     *
     * @param section  小学 中学
     * @param grade    一年级  二年级   ...
     * @param sex      没有的时候传-1  男0 女1
     * @param age      没有的时候传-1   0-100
     * @param nickname 昵称
     * @param jpeg     用户头像
     */
    public static void keepLastRecord(String section, String grade, int sex, int age, String nickname, byte[] jpeg) {
        if (hasLastRecocrd()) {
            updateLastRecord(section, grade, sex, age, nickname, jpeg);
        } else {
            insertLastRecord(section, grade, sex, age, nickname, jpeg);
        }
    }

    /**
     * 添加记录
     */
    // 获取我的数据存放路径
    public static void insertLastRecord(String section, String grade, int sex, int age, String nickname, byte[] jpeg) {
        Log.e("lyx", "---5261656546-7-" + section + "-" + grade + "-" + jpeg);
        ContentValues contentValues = new ContentValues();
        if (section != null) {
            contentValues.put(SECTION, section);
        }

        if (grade != null) {
            contentValues.put(GRADE, grade);
        }

        contentValues.put(SEX, sex);

        contentValues.put(AGE, age);


        if (nickname != null) {
            contentValues.put(NICKNAME, nickname);
        }

        if (jpeg != null) {
            contentValues.put(IMAGE, jpeg);
        }


        try {
            Log.e("lyx", "---5261656546-8-" + contentValues.toString());
            LauncherApplication.Companion.getContext().getContentResolver().insert(LAST_RECORDR_URI, contentValues);
        } catch (Exception e) {

        }

    }

    /**
     * 更新指定类型记录
     *
     * @param jpeg 图片字节数组
     */
    public static void updateLastRecord(String section, String grade, int sex, int age, String nickname, byte[] jpeg) {
        ContentValues contentValues = new ContentValues();
        if (section != null) {
            contentValues.put(SECTION, section);
        }

        if (grade != null) {
            contentValues.put(GRADE, grade);
        }

        contentValues.put(SEX, sex);

        contentValues.put(AGE, age);


        if (nickname != null) {
            contentValues.put(NICKNAME, nickname);
        }

        if (jpeg != null) {
            contentValues.put(IMAGE, jpeg);
        }


        try {
            Log.e("lyx", "---5261656546-8-" + contentValues.toString());

            int update = LauncherApplication.Companion.getContext().getContentResolver().update(LAST_RECORDR_URI, contentValues, null, null);
        } catch (Exception e) {

        }

    }

    /**
     * 判断指定类型下是否有记录
     *
     * @param
     * @return
     */
    public static boolean hasLastRecocrd() {
        boolean hasLastReocrd = false;
        try {
            Cursor cursor = LauncherApplication.Companion.getContext().getContentResolver().query(LAST_RECORDR_URI, null, null, null, null, null);
            if (cursor == null || cursor.getCount() < 1) {
                hasLastReocrd = false;
            } else {
                hasLastReocrd = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {

        }

        return hasLastReocrd;
    }

    /**
     * 获取指定类型下的记录
     *
     * @param
     * @return
     */
    public static UserInfoBean getUserInfoRecocrd() {

        UserInfoBean lastOpenInfo = null;
        try {
            Cursor cursor = LauncherApplication.Companion.getContext().getContentResolver().query(LAST_RECORDR_URI, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                lastOpenInfo = new UserInfoBean();
                while (cursor.moveToNext()) {
                    lastOpenInfo.setSection(cursor.getString(cursor.getColumnIndex(SECTION)));
                    lastOpenInfo.setUserIamge(cursor.getBlob(cursor.getColumnIndex(IMAGE)));
                    lastOpenInfo.setGrade(cursor.getString(cursor.getColumnIndex(GRADE)));
                    lastOpenInfo.setSex(cursor.getInt(cursor.getColumnIndex(SEX)));
                    lastOpenInfo.setAge(cursor.getInt(cursor.getColumnIndex(AGE)));
                    lastOpenInfo.setNickname(cursor.getString(cursor.getColumnIndex(NICKNAME)));
                }
            }


            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {

        }

        return lastOpenInfo;
    }


}
