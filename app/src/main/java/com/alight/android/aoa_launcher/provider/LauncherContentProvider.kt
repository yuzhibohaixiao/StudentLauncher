package com.alight.android.aoa_launcher.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.alight.android.aoa_launcher.constants.AppConstants
import com.alight.android.aoa_launcher.db.DbOpenHelper
import com.alight.android.aoa_launcher.utils.AccountUtil
import com.alight.android.aoa_launcher.utils.AccountUtil.getAllToken
import com.alight.android.aoa_launcher.utils.SPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class LauncherContentProvider : ContentProvider() {
    private var mContext: Context? = null
    private var sqLiteDatabase: SQLiteDatabase? = null
    private val TAG = "LauncherContentProvider"

    companion object {
        var URI =
            Uri.parse("content://com.alight.android.aoa_launcher.provider.LauncherContentProvider/child")
        const val AUTHORITY =
            "com.alight.android.aoa_launcher.provider.LauncherContentProvider"
        const val CHILD_URI_CODE = 0
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(
                AUTHORITY,
                DbOpenHelper.BOY_TABLE_NAME,
                CHILD_URI_CODE
            )
        }
    }

    /**
     * 获取表名
     *
     * @param uri
     * @return
     */
    private fun getTableName(uri: Uri): String? {
        var tableName: String? = null
        when (uriMatcher.match(
            uri
        )) {
            CHILD_URI_CODE -> tableName =
                DbOpenHelper.BOY_TABLE_NAME
        }
        return tableName
    }

    override fun onCreate(): Boolean {
        mContext = getContext()
        initProviderData()
        return false
    }

    //初始化原始数据
    private fun initProviderData() {
        sqLiteDatabase = DbOpenHelper(mContext).writableDatabase
        //        sqLiteDatabase.beginTransaction();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name", "男孩1");
//        sqLiteDatabase.insert(DbOpenHelper.BOY_TABLE_NAME, null, contentValues);
//        contentValues.put("name", "男孩2");
//        sqLiteDatabase.insert(DbOpenHelper.BOY_TABLE_NAME, null, contentValues);
//        contentValues.put("name", "男孩3");
//        sqLiteDatabase.insert(DbOpenHelper.BOY_TABLE_NAME, null, contentValues);
//        contentValues.clear();
//
//        sqLiteDatabase.setTransactionSuccessful();
//        sqLiteDatabase.endTransaction();
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val tableName = getTableName(uri)
        val uri2 = runBlocking {
            delete(URI, null, null)
            GlobalScope.async(Dispatchers.IO) {
                val tokenPair = AccountUtil.getToken()
                val contentValues = ContentValues()
                contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN, tokenPair.token)
                contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_AVATAR, tokenPair.avatar)
                contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_NAME, tokenPair.name)
                contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_USER_ID, tokenPair.userId)
                contentValues.put(AppConstants.AOA_LAUNCHER_USER_INFO_GENDER, tokenPair.gender)
                contentValues.put(
                    AppConstants.AOA_LAUNCHER_USER_INFO_EXPIRE_TIME,
                    tokenPair.expireTime
                )
                //将登陆的用户数据插入保存
                insert(URI, contentValues)
            }.await()
        }
        require(!TextUtils.isEmpty(tableName)) { "Unsupported URI:$uri" }
        //todo 也可通过handler去刷新token
        mContext!!.contentResolver.notifyChange(uri, null)
        return sqLiteDatabase!!.query(
            tableName,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        //todo 不再需要操作数据库 需要把msg数据直接发广播
        val tableName = getTableName(uri)
        require(!TextUtils.isEmpty(tableName)) { "Unsupported URI:$uri" }
        sqLiteDatabase!!.insert(tableName, null, values)
        mContext!!.contentResolver.notifyChange(uri, null)
        return uri
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val tableName = getTableName(uri)
        require(!TextUtils.isEmpty(tableName)) { "Unsupported URI:$uri" }
        val count = sqLiteDatabase!!.delete(tableName, selection, selectionArgs)
        if (count > 0) {
            mContext!!.contentResolver.notifyChange(uri, null)
        }
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val tableName = getTableName(uri)
        require(!TextUtils.isEmpty(tableName)) { "Unsupported URI:$uri" }
        val row = sqLiteDatabase!!.update(tableName, values, selection, selectionArgs)
        if (row > 0) {
            mContext!!.contentResolver.notifyChange(uri, null)
        }
        return row
    }
}