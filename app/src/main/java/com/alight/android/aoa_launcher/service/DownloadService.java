package com.alight.android.aoa_launcher.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alight.android.aoa_launcher.R;
import com.alight.android.aoa_launcher.activity.MoreDownloadActivity;
import com.alight.android.aoa_launcher.application.LauncherApplication;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.db.DbHelper;
import com.alight.android.aoa_launcher.utils.NotifyUtils;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;


import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.alight.android.aoa_launcher.common.constants.AppConstants.SYSTEM_ZIP_PATH;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private DownloadTask task;
    private SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 下载开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = intent.getStringExtra("url");
                String filename = intent.getStringExtra("filename");
                String id = intent.getStringExtra("id");
                int seq = intent.getIntExtra("seq", 0);
                Log.d(TAG, "onStartCommand: " + url);
                Log.d(TAG, "onStartCommand: " + filename);
                //开启下载进度
                sharedPreferences = getSharedPreferences("download", MODE_PRIVATE);

                download(filename, url, id, seq);
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void download(final String filename, final String url, final String id, final int seq) {
        final boolean openDownloadNotify = sharedPreferences.getBoolean("open_download_notify", false);
        final File parentFile = Environment.getExternalStorageDirectory();
        final com.alight.android.aoa_launcher.net.model.File file = new com.alight.android.aoa_launcher.net.model.File();
        file.setId(id);
        file.setCreateTime(new Date());
        file.setUrl(url);
        file.setFileName(filename);
        file.setPath(SYSTEM_ZIP_PATH + filename);
        System.out.println(file.getPath());
        String fileType = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        file.setFileType(fileType);
        Log.d(TAG, "download: 下载" + filename);
        final Intent intent = new Intent(AppConstants.LAUNCHER_PACKAGE_NAME + id);
        intent.setPackage(getPackageName());

        Log.d(TAG, "parentFile: " + parentFile.getPath());
//        File parentFile = getExternalCacheDir();
        task = new DownloadTask.Builder(url, parentFile)
                .setFilename(filename)
                .setMinIntervalMillisCallbackProcess(30) // 下载进度回调的间隔时间（毫秒）
                .setPassIfAlreadyCompleted(false)// 任务过去已完成是否要重新下载
                .setPriority(10)

                .build();
        task.enqueue(new DownloadListener4WithSpeed() {
            @Override
            public void taskStart(@NonNull DownloadTask task) {
                Log.d(TAG, "taskStart: " + task.getId());
            }

            @Override
            public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {
                Log.d(TAG, "connectStart: ");
            }

            @Override
            public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {
                Log.d(TAG, "connectEnd: ");
            }

            @Override
            public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
                Log.d(TAG, "infoReady: ");
            }

            @Override
            public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
                Log.d(TAG, "progressBlock: ");
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
                Log.d(TAG, "progress: " + taskSpeed.speed());
                String speed = taskSpeed.speed();
                int percent = (int) (((float) task.getInfo().getTotalOffset() / task.getInfo().getTotalLength()) * 100);
                String totalSize = Util.humanReadableBytes(task.getInfo().getTotalLength(), true).toString();
                String size = totalSize + "(" + (int) percent + "%)";
                intent.putExtra("percent", percent);
                intent.putExtra("size", size);
                intent.putExtra("speed", speed);
                intent.putExtra("status", com.alight.android.aoa_launcher.net.model.File.DOWNLOAD_PROCEED);
                intent.putExtra("totalSize", totalSize);
                sendBroadcast(intent);
                if (openDownloadNotify) {
                    createNotification(percent, filename, speed, size, seq);
                }
            }

            @Override
            public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {
                Log.d(TAG, "blockEnd: ");
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
                if (task.getInfo() == null) return;
                int percent = (int) (((float) task.getInfo().getTotalOffset() / task.getInfo().getTotalLength()) * 100);
                String totalSize = Util.humanReadableBytes(task.getInfo().getTotalLength(), true).toString();
                String size = totalSize + "(" + (int) percent + "%)";
                file.setSize(task.getInfo().getTotalLength());
                System.out.println(cause == EndCause.COMPLETED);
                if (cause == EndCause.COMPLETED) {
                    intent.putExtra("percent", percent);
                    intent.putExtra("size", size);
                    intent.putExtra("speed", "");
                    intent.putExtra("status", com.alight.android.aoa_launcher.net.model.File.DOWNLOAD_COMPLETE);
                    intent.putExtra("totalSize", totalSize);
                    intent.putExtra("filePath", file.getPath());
                    file.setProgress(percent);
                    file.setSizeStr(totalSize);
                    file.setStatus(com.alight.android.aoa_launcher.net.model.File.DOWNLOAD_COMPLETE);
                    if (openDownloadNotify) {
                        createDownloadCompleteNotification(filename, totalSize, seq);
                    }
                }
                Log.d(TAG, "cause: " + cause.toString());
                if (realCause != null) {
                    intent.putExtra("status", com.alight.android.aoa_launcher.net.model.File.DOWNLOAD_ERROR);
                    file.setProgress(percent);
                    file.setSizeStr(size);
                    file.setStatus(com.alight.android.aoa_launcher.net.model.File.DOWNLOAD_PAUSE);
                    createDownloadErrorNotification(filename, seq);
                }
                Log.d(TAG, "taskEnd: ");
                Log.d(TAG, "taskEnd: " + task.getFile().getPath());
                Log.d(TAG, "taskEnd: " + task.getFilename());
                Log.d(TAG, "taskEnd: " + task.getFilenameHolder().get());
                Log.d(TAG, "taskEnd: " + task.getParentFile().getPath());
                insertOrUpdate(file);
                sendBroadcast(intent);
                stopSelf();
            }
        });
        LauncherApplication.Companion.getDownloadTaskHashMap().put(id, task);
    }

    /**
     * 更新或保存下载文件
     */
    private void insertOrUpdate(com.alight.android.aoa_launcher.net.model.File file) {
        Log.d(TAG, "insertOrUpdate: " + file);
        DbManager db = DbHelper.getDbManager();
        try {
            db.saveOrUpdate(file);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送通知
     */
    private void createNotification(int percent, String fileName, String speed, String size, int seq) {
        System.out.println("percent=" + percent);
        System.out.println(task.getId());
        Log.d(TAG, "createNotification: ");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_notice);
        remoteViews.setProgressBar(R.id.progressBar, 100, percent, false);
        remoteViews.setTextViewText(R.id.speed, speed);
        remoteViews.setTextViewText(R.id.file_name, fileName);
        remoteViews.setTextViewText(R.id.size, size);
        NotifyUtils notifyUtils = new NotifyUtils(this, "chanel1", "low");
        notifyUtils.notifyCustomView(remoteViews, null,
                R.drawable.file, 0, "文件下载", seq, false, false, false);
    }

    /**
     * 发送下载完成
     *
     * @param fileName
     * @param size
     * @param seq
     */
    private void createDownloadCompleteNotification(String fileName, String size, int seq) {
        Log.d(TAG, "createDownloadCompleteNotification: ");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_complete_notice);
        remoteViews.setTextViewText(R.id.file_name, fileName);
        remoteViews.setTextViewText(R.id.size_str, size);
        NotifyUtils notifyUtils = new NotifyUtils(this, "chanel1", "low1");
        notifyUtils.removeId(seq);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MoreDownloadActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        notifyUtils.notifyCustomView(remoteViews, contentIntent,
                R.drawable.file, 0, "文件下载", seq + 10000, false, false, false);
    }

    /**
     * 下载出错
     *
     * @param fileName
     * @param seq
     */
    private void createDownloadErrorNotification(String fileName, int seq) {
        Log.d(TAG, "createDownloadErrorNotification: ");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_error_notice);
        remoteViews.setTextViewText(R.id.file_name, fileName);
        remoteViews.setTextViewText(R.id.tip, "下载出错");
        NotifyUtils notifyUtils = new NotifyUtils(this, "chanel1", "low1");
        notifyUtils.clear();
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MoreDownloadActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        notifyUtils.notifyCustomView(remoteViews, contentIntent,
                R.drawable.file, 0, "文件下载", seq + 10001, false, false, false);
    }


}
