package com.alight.android.aoa_launcher.common.db;

import static org.xutils.x.getDb;

import android.util.Log;

import org.xutils.DbManager;
import org.xutils.db.table.TableEntity;


public class DbHelper {
    private static final String TAG= DbHelper.class.getSimpleName();
    /**
     * 数据库名称
     */
    private static final String DB_NAME="download.db";
    public static DbManager getDbManager() {
        try {
            DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                    //设置数据库名，默认xutils.db
                    .setDbName(DB_NAME)
                    // 不设置dbDir时, 默认存储在app的私有目录.
//                    .setDbDir(new File("/sdcard")) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
                    .setDbVersion(3)//数据库版本

                    //设置是否允许事务，默认true
                    //.setAllowTransaction(true)

                    //设置表创建的监听
                    .setTableCreateListener(new DbManager.TableCreateListener() {

                        @Override
                        public void onTableCreated(DbManager db, TableEntity<?> table) {
                            Log.i(TAG, "onTableCreated：" + table.getName());
                        }
                    })

                    //设置数据库更新的监听
                    .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                        @Override
                        public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                        }
                    })
                    //设置数据库打开的监听
                    .setDbOpenListener(new DbManager.DbOpenListener() {
                        @Override
                        public void onDbOpened(DbManager db) {
                            //开启数据库支持多线程操作，提升性能
                            db.getDatabase().enableWriteAheadLogging();
                        }
                    });
            DbManager db = getDb(daoConfig);
            return db;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
