package com.alight.android.aoa_launcher.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.alight.android.aoa_launcher.common.bean.StorageBean;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class StorageUtil {

    private static final String TAG = "StorageUtil";

    public static String getTotalSize() {

        java.io.File file = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(file.getPath());

        long size = sf.getBlockSize();//SD卡的单位大小

        long total = sf.getBlockCount();//总数量

        long available = sf.getAvailableBlocks();//可使用的数量

        DecimalFormat df = new DecimalFormat();

        df.setGroupingSize(3);//每3位分为一组

        //总容量
        String totalSize = df.format(((size * total) / 1024) / 1024 / 1024);

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

        String totalSize = df.format(((size * total) / 1024) / 1024 / 1024);

        //未使用量
        String avalilable = (size * available) / 1024 >= 1024 ? df.format(((size * available) / 1024) / 1024) + "MB" : df.format((size * available) / 1024) + "KB";

        //已使用量
        String usedSize = df.format(((size * (total - available)) / 1024) / 1024 / 1024);

        return usedSize;
    }

    private static String[] units = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 单位转换
     */
    private static String getUnit(float size, float unit) {
        int index = 0;
        while (size > unit && index < 4) {
            size = size / unit;
            index++;
        }
        //  不带单位
        return String.valueOf(size);
        //  带有单位
//        return String.format(Locale.getDefault(), " %.2f %s", size, units[index]);
    }

    public static StorageBean queryWithStorageManager(Context context) {
        //5.0 查外置存储
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        float unit = 1024;
        int version = Build.VERSION.SDK_INT;
        if (version < Build.VERSION_CODES.M) {//小于6.0
            try {
                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
                StorageVolume[] volumeList = (StorageVolume[]) getVolumeList.invoke(storageManager);
                long totalSize = 0, availableSize = 0;
                if (volumeList != null) {
                    Method getPathFile = null;
                    for (StorageVolume volume : volumeList) {
                        if (getPathFile == null) {
                            getPathFile = volume.getClass().getDeclaredMethod("getPathFile");
                        }
                        File file = (File) getPathFile.invoke(volume);
                        totalSize += file.getTotalSpace();
                        availableSize += file.getUsableSpace();
                    }
                }
                Log.d(TAG, "totalSize = " + getUnit(totalSize, unit) + " ,availableSize = " + getUnit(availableSize, unit));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {

            try {
                Method getVolumes = StorageManager.class.getDeclaredMethod("getVolumes");//6.0
                List<Object> getVolumeInfo = (List<Object>) getVolumes.invoke(storageManager);
                long total = 0L, used = 0L;
                for (Object obj : getVolumeInfo) {

                    Field getType = obj.getClass().getField("type");
                    int type = getType.getInt(obj);

                    Log.d(TAG, "type: " + type);
                    if (type == 1) {//TYPE_PRIVATE

                        long totalSize = 0L;

                        //获取内置内存总大小
                        if (version >= Build.VERSION_CODES.O) {//8.0
                            unit = 1000;
                            Method getFsUuid = obj.getClass().getDeclaredMethod("getFsUuid");
                            String fsUuid = (String) getFsUuid.invoke(obj);
//                            totalSize = getTotalSize(context, fsUuid);//8.0 以后使用
                        } else if (version >= Build.VERSION_CODES.N_MR1) {//7.1.1
                            Method getPrimaryStorageSize = StorageManager.class.getMethod("getPrimaryStorageSize");//5.0 6.0 7.0没有
                            totalSize = (long) getPrimaryStorageSize.invoke(storageManager);
                        }
                        long systemSize = 0L;

                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                        boolean readable = (boolean) isMountedReadable.invoke(obj);
                        if (readable) {
                            Method file = obj.getClass().getDeclaredMethod("getPath");
                            File f = (File) file.invoke(obj);

                            if (totalSize == 0) {
                                totalSize = f.getTotalSpace();
                            }
                            systemSize = totalSize - f.getTotalSpace();
                            used += totalSize - f.getFreeSpace();
                            total += totalSize;
                        }
                        Log.d(TAG, "设备内存大小：" + getUnit(totalSize, unit) + "\n系统大小：" + getUnit(systemSize, unit));
                        Log.d(TAG, "totalSize = " + getUnit(totalSize, unit) + " ,used(with system) = " + getUnit(used, unit) + " ,free = " + getUnit(totalSize - used, unit));

                    } else if (type == 0) {//TYPE_PUBLIC
                        //外置存储
                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                        boolean readable = (boolean) isMountedReadable.invoke(obj);
                        if (readable) {
                            Method file = obj.getClass().getDeclaredMethod("getPath");
                            File f = (File) file.invoke(obj);
                            used += f.getTotalSpace() - f.getFreeSpace();
                            total += f.getTotalSpace();
                        }
                    } else if (type == 2) {//TYPE_EMULATED

                    }
                }
                Log.d(TAG, "总内存 total = " + getUnit(total, unit) + "\n已用 used(with system) = " + getUnit(used, 1000) + "\n可用 available = " + getUnit(total - used, unit));
                return new StorageBean(getUnit(total, unit), getUnit(used, unit));
            } catch (SecurityException e) {
                Log.e(TAG, "缺少权限：permission.PACKAGE_USAGE_STATS");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
