package com.alight.android.aoa_launcher.utils;

import android.os.Environment;
import android.os.StatFs;

import java.text.DecimalFormat;

public class StorageUtil {

    public static String getTotalSize() {

        java.io.File file = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(file.getPath());

        long size = sf.getBlockSize();//SD卡的单位大小

        long total = sf.getBlockCount();//总数量

        long available = sf.getAvailableBlocks();//可使用的数量

        DecimalFormat df = new DecimalFormat();

        df.setGroupingSize(3);//每3位分为一组

        //总容量
        String totalSize = df.format(((size * total) / 1024) / 1024 / 1024) ;

        //未使用量
        String avalilable = (size * available) / 1024 >= 1024 ? df.format(((size * available) / 1024) / 1024) + "MB" : df.format((size * available) / 1024) + "KB";

        //已使用量
        String usedSize = size * (total - available) / 1024 >= 1024 ? df.format(((size * (total - available)) / 1024) / 1024) + "MB" : df.format(size * (total - available) / 1024) + "KB";
        return totalSize;
    }

    public static String getUseSize() {

        java.io.File file = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(file.getPath());

        long size = sf.getBlockSize();//SD卡的单位大小

        long total = sf.getBlockCount();//总数量

        long available = sf.getAvailableBlocks();//可使用的数量

        DecimalFormat df = new DecimalFormat();

        df.setGroupingSize(3);//每3位分为一组

//总容量

        String totalSize = df.format(((size * total) / 1024) / 1024 / 1024) ;

        //未使用量
        String avalilable = (size * available) / 1024 >= 1024 ? df.format(((size * available) / 1024) / 1024) + "MB" : df.format((size * available) / 1024) + "KB";

        //已使用量
        String usedSize = df.format(((size * (total - available)) / 1024) / 1024 / 1024) ;

        return usedSize;
    }

}
