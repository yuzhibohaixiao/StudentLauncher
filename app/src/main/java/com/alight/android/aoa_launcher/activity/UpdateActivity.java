package com.alight.android.aoa_launcher.activity;

import static com.alight.android.aoa_launcher.common.constants.AppConstants.EXTRA_IMAGE_PATH;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.LAUNCHER_PACKAGE_NAME;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.SYSTEM_ZIP_FULL_PATH;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alight.android.aoa_launcher.R;
import com.alight.android.aoa_launcher.application.LauncherApplication;
import com.alight.android.aoa_launcher.common.base.BaseActivity;
import com.alight.android.aoa_launcher.common.bean.UpdateBeanData;
import com.alight.android.aoa_launcher.common.broadcast.UpgradeApkReceiver;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.db.DbHelper;
import com.alight.android.aoa_launcher.common.event.UpdateEvent;
import com.alight.android.aoa_launcher.net.model.File;
import com.alight.android.aoa_launcher.presenter.PresenterImpl;
import com.alight.android.aoa_launcher.service.DownloadService;
import com.alight.android.aoa_launcher.service.UpdateService;
import com.alight.android.aoa_launcher.ui.adapter.UpdateAdapter;
import com.alight.android.aoa_launcher.ui.view.CustomDialog;
import com.alight.android.aoa_launcher.utils.ApkController;
import com.alight.android.aoa_launcher.utils.AppUtils;
import com.alight.android.aoa_launcher.utils.RxTimerUtil;
import com.alight.android.aoa_launcher.utils.SPUtils;
import com.alight.android.aoa_launcher.utils.StringUtils;
import com.alight.android.aoa_launcher.utils.ToastUtils;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 多文件列表下载
 *
 * @author wangzhe
 */
public class UpdateActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MoreDownloadActivity";
    private int REQUEST_CODE_EXTERNAL_STORAGE = 20;
    private RecyclerView systemRecyclerView;
    private RecyclerView otherRecyclerView;
    private UpdateAdapter systemAdapter;
    private UpdateAdapter otherAdapter;
    private int zipPosition = 0;
    private HashMap<String, DownloadReceiver> downloadReceiverMap = new HashMap<>();
    private List<File> systemList = new ArrayList<>();
    private List<File> otherList = new ArrayList<>();
    private List<File> needUpdateSystemList = new ArrayList<>();
    private File otaFile = new File();
    private TextView tvOtaApp;
    private TextView tvSystemApp;
    private TextView tvOtherApp;
    private View llBackUpdate;
    private TextView tvUpdateAll;
    private Intent serviceIntent;
    private ArrayList<UpdateBeanData> systemAppList;
    private ArrayList<UpdateBeanData> otherAppList;
    //ota包数据封装
    private UpdateBeanData otaUpdateBean;
    //1为系统应用 2是预置应用
    private int appType = 1;
    //是否包含需要解压的配置文件
    private boolean containsConfigFile = false;
    private TextView tvLocalOtaApp;
    private TextView tvNewOtaApp;
    private TextView tvOtaAppUpdate;
    private View llOtaUpdate;
    private View llOtaUpdateBtn;
    private View llOtaUpdateProgress;
    private ProgressBar pbUpdateOta;
    private TextView tvOtaUpdateText;
    private TextView tvCheckTitle;
    private TextView tvCheckContent;
    private TextView tvCheckStep;
    private TextView tvUpdating;
    private FrameLayout flEyeshieldMode;
    private FrameLayout flClearMemory;
    private boolean otaInstall = false;
    private View ivOtaLogo;
    //true表示是否第一次进行引导更新
    private boolean newSplash;
    private String source;
    private boolean isShowDialog = false;
    private UpgradeApkReceiver upgradeApkReceiver;
    private int updatePostion;
    //true是开机检测流程
    private boolean isCheckUpdate = false;
    private boolean isStartOtaUpdate = false;
    private int installApp = 0;//强更过程已安装的应用个数
    private boolean forceUpdateUnzipFlag = false;

    @Override
    public void initData() {
        EventBus.getDefault().register(this);
        systemAppList = (ArrayList<UpdateBeanData>) getIntent().getSerializableExtra("systemApp");
        otherAppList = (ArrayList<UpdateBeanData>) getIntent().getSerializableExtra("otherApp");

        for (int i = 0; i < systemAppList.size(); i++) {
            if (systemAppList.get(i).getFormat() == 3) {
                //筛选OTA包
                otaUpdateBean = systemAppList.get(i);
                systemAppList.remove(i);
            }
        }
        checkExtrnalStorage();
        getData(appType);

        if (systemAdapter == null) {
            systemAdapter = new UpdateAdapter();
            systemAdapter.setAppType(appType);
            systemAdapter.addData(systemList);
            systemRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            ((SimpleItemAnimator) systemRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            systemRecyclerView.setAdapter(systemAdapter);
        } else {
            systemAdapter.notifyDataSetChanged();
        }
        if (otherAdapter == null) {
            otherAdapter = new UpdateAdapter();
            otherAdapter.setAppType(2);
            otherRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            ((SimpleItemAnimator) otherRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            otherRecyclerView.setAdapter(otherAdapter);
        } else {
            otherAdapter.notifyDataSetChanged();
        }
        otherAdapter.setNewInstance(otherList);
        //只给预装应用的TextView设置了点击监听
        otherAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.tv_update_item && (otherAdapter.getData().get(position).getFormat() == 1 || otherAdapter.getData().get(position).getFormat() == 2)) {
                View tvUpdate = otherAdapter.getViewByPosition(position, R.id.tv_update_item);
                tvUpdate.setEnabled(false);
                startSingleDownload(position);
            }
        });
        source = getIntent().getStringExtra("source");
        newSplash = getIntent().getBooleanExtra("new_splash", true);
        //开机固件升级检测
        if (!StringUtils.isEmpty(source) && source.equals("splash")) {
            isCheckUpdate = true;
            tvSystemApp.setVisibility(View.GONE);
            tvOtherApp.setVisibility(View.GONE);
            if (newSplash) {
                llBackUpdate.setVisibility(View.GONE);
            } else {
                isShowDialog = true;
            }
        } else if (!StringUtils.isEmpty(source) && source.equals("onlySystemUpdate")) {
            tvOtaApp.setVisibility(View.GONE);
            tvOtherApp.setVisibility(View.GONE);
            setSelectRefreshUI(tvSystemApp);
        } else {
            isShowDialog = true;
        }
        boolean isHaveOtaUpdate = initOtaUpdate();

        //系统应用没有应用时不提示一键更新
        if (systemAdapter.getData().size() > 0) {
            for (int i = 0; i < systemAdapter.getData().size(); i++) {
                if (systemAdapter.getData().get(i).getFormat() != 3 && systemAdapter.getData().get(i).getFormat() != 4) {
                    //表示有需要更新的
                    break;
                } else if (i == systemAdapter.getData().size() - 1) {
                    tvUpdateAll.setBackgroundResource(R.drawable.update_oval);
                    tvUpdateAll.setTextColor(Color.parseColor("#50ffffff"));
                    tvUpdateAll.setEnabled(false);
                    tvUpdateAll.setClickable(false);
                    tvUpdateAll.setText("无需更新");
                }
            }
        }
        MMKV mmkv = LauncherApplication.Companion.getMMKV();
        isStartOtaUpdate = mmkv.getBoolean("isStartOtaUpdate", false);
        //开始强更流程
        if (isStartOtaUpdate && !isHaveOtaUpdate && !StringUtils.isEmpty(source)) {
            startForceUpdate();
        }
    }

    private void startForceUpdate() {
        llOtaUpdateBtn.setVisibility(View.GONE);
        llOtaUpdateProgress.setVisibility(View.VISIBLE);
        tvCheckTitle.setVisibility(View.VISIBLE);
        tvCheckContent.setVisibility(View.VISIBLE);
        tvCheckStep.setVisibility(View.VISIBLE);
        ivOtaLogo.setVisibility(View.GONE);
        tvUpdating.setVisibility(View.GONE);
        llBackUpdate.setVisibility(View.GONE);
        tvCheckStep.setText("共计2个更新项，当前为第2项目");
        startSystemAppDownload();
    }

    private boolean initOtaUpdate() {
        tvLocalOtaApp.setText("版本：" + Build.DISPLAY);
        if (otaUpdateBean != null && !otaUpdateBean.getVersion_name().equals(Build.DISPLAY)) {
            //有新版
            tvNewOtaApp.setText("发现新版本:" + otaUpdateBean.getVersion_name());
            IntentFilter filter = new IntentFilter();
            DownloadReceiver receiver = new DownloadReceiver();
            filter.addAction(AppConstants.LAUNCHER_PACKAGE_NAME + otaUpdateBean.getId() + 3);
            registerReceiver(receiver, filter);
            downloadReceiverMap.put(String.valueOf(otaUpdateBean.getId()), receiver);
            return true;
        } else {
            //无新版
            tvNewOtaApp.setText("已是最新版本");
            tvOtaAppUpdate.setVisibility(View.GONE);
            ivOtaLogo.setPadding(0, 80, 0, 0);
            return false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetUpdateEvent(UpdateEvent updateEvent) {
        String packageName = updateEvent.packageName;
        refreshInstallState(packageName);
    }

    private void refreshInstallState(String packageName) {
        List<File> systemAdapterData = systemAdapter.getData();
        for (int i = 0; i < systemAdapterData.size(); i++) {
            if (packageName.equals(systemAdapterData.get(i).getPackName())) {
                TextView tvUpdate = (TextView) systemAdapter.getViewByPosition(i, R.id.tv_update_item);
                if (tvUpdate != null)
                    tvUpdate.setText("已完成");
                return;
            }
        }
        List<File> otherAdapterData = otherAdapter.getData();
        for (int i = 0; i < otherAdapterData.size(); i++) {
            if (packageName.equals(otherAdapterData.get(i).getPackName())) {
                TextView tvUpdate = (TextView) otherAdapter.getViewByPosition(i, R.id.tv_update_item);
                if (tvUpdate != null)
                    tvUpdate.setText("已完成");
                return;
            }
        }
    }

    /**
     * 含子目录的文件压缩
     *
     * @throws Exception
     */

// 第一个参数就是需要解压的文件，第二个就是解压的目录
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean upZipFile(String zipFile, String folderPath) {
        ZipFile zfile = null;

        try {
// 转码为GBK格式，支持中文

            zfile = new ZipFile(zipFile, Charset.forName("gbk"));

        } catch (IOException e) {
            e.printStackTrace();

            return false;

        }

        Enumeration zList = zfile.entries();

        ZipEntry ze = null;

        byte[] buf = new byte[1024];

        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();

// 列举的压缩文件里面的各个文件，判断是否为目录

            if (ze.isDirectory()) {
                String dirstr = folderPath + ze.getName();

                Log.i(TAG, "dirstr=" + dirstr);

                dirstr.trim();

                java.io.File f = new java.io.File(dirstr);

                f.mkdir();

                continue;

            }

            OutputStream os = null;

            FileOutputStream fos = null;

// ze.getName()会返回 script/start.script这样的，是为了返回实体的File

            java.io.File realFile = getRealFileName(folderPath, ze.getName());

            try {
                fos = new FileOutputStream(realFile);

            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());

                return false;

            }

            os = new BufferedOutputStream(fos);

            InputStream is = null;

            try {
                is = new BufferedInputStream(zfile.getInputStream(ze));

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }

            int readLen = 0;

// 进行一些内容复制操作

            try {
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    os.write(buf, 0, readLen);

                }

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());

                return false;

            }

            try {
                is.close();

                os.close();

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;

            }

        }

        try {
            zfile.close();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());

            return false;

        }

        return true;

    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */

    public static java.io.File getRealFileName(String baseDir, String absFileName) {
        Log.i(TAG, "baseDir=" + baseDir + "------absFileName="

                + absFileName);

        absFileName = absFileName.replace("\\", "/");

        Log.i(TAG, "absFileName=" + absFileName);

        String[] dirs = absFileName.split("/");

        Log.i(TAG, "dirs=" + dirs);

        java.io.File ret = new java.io.File(baseDir);

        String substr = null;

        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];

                ret = new java.io.File(ret, substr);

            }

            if (!ret.exists()) {
                ret.mkdirs();
            }

            substr = dirs[dirs.length - 1];

            ret = new java.io.File(ret, substr);

            return ret;

        } else {
            ret = new java.io.File(ret, absFileName);

        }

        return ret;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadZip(String zipPath, int versionCode) {
        try {
            String string = new String();
            ZipFile zip = new ZipFile(new java.io.File(zipPath));
            ZipEntry ze = zip.getEntry("path.txt");
            if (ze != null) {
                Log.d("print", "已经查找到该文件");
                // 读取目标对象
                Scanner scanner = new Scanner(zip.getInputStream(ze));
                if (scanner.hasNextLine()) {
                    string = scanner.nextLine();
                }
                scanner.close();
                zip.close();
//                deleteFolder(string);
                if (upZipFile(zipPath, string)) {
                    containsConfigFile = false;
                    SPUtils.syncPutData("configVersion", versionCode);
                    String launcherApkPath = Environment.getExternalStorageDirectory().getPath() + "/launcher.apk";

                    for (int i = 0; i < systemAdapter.getData().size(); i++) {
                        if (systemAdapter.getData().get(i).getFormat() == 1) {
                            zipPosition = i;
                        }
                    }
                    if (isStartOtaUpdate) {
                        forceUpdateUnzipFlag = true;
                    }
                    runOnUiThread(() -> {
                        ToastUtils.showShort(this, "解压完成！");
                        TextView tvUpdate = (TextView) systemAdapter.getViewByPosition(zipPosition, R.id.tv_update_item);
                        if (tvUpdate != null)
                            tvUpdate.setText("已完成");
                    });
                    if (!StringUtils.isEmpty(launcherApkPath)) {
                        if (ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), launcherApkPath)) {
                            for (int i = 0; i < systemAdapter.getData().size(); i++) {
                                if (LAUNCHER_PACKAGE_NAME.equals(systemAdapter.getData().get(i).getPackName())) {
                                    updatePostion = i;
                                    systemList.get(updatePostion).setInstalled(true);
                                    runOnUiThread(() -> systemAdapter.notifyItemChanged(updatePostion));
                                }
                            }
//                            sendUpdateBroadcast(LAUNCHER_PACKAGE_NAME);
                        } else {

                        }
                    }
                }
            } else {
                Log.d("print", "该文件不存在");
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteSingleFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(java.io.File.separator)) {
            filePath = filePath + java.io.File.separator;
        }
        java.io.File dirFile = new java.io.File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        java.io.File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public boolean deleteFolder(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * ota下载
     */
    private void startOtaDownload() {
        if (otaUpdateBean != null) {
            List<String> downloadedFileIds = getDownloadedFileIds();
            List<File> fileList = selectDownloadedFiles();
            File file = new File();
            file.setId("0");
            file.setSeq(0);
            file.setFormat(otaUpdateBean.getFormat());
            file.setFileName("update.zip");
            file.setVersionCode(otaUpdateBean.getVersion_code());
            if (downloadedFileIds.contains(file.getId())) {
                File file1 = fileList.get(downloadedFileIds.indexOf(file.getId()));
                System.out.println(file1);
                Log.d(TAG, "getData: " + file1.getStatus());
//                file.setStatus(file1.getStatus());
                //状态始终保持初始
                file.setStatus(File.DOWNLOAD_REDYA);
                file.setSizeStr(file1.getSizeStr());
                file.setCreateTime(file1.getCreateTime());
            } else {
                file.setStatus(File.DOWNLOAD_REDYA);
                file.setCreateTime(new Date());
            }
            file.setUrl(otaUpdateBean.getApp_url());
            if (!StringUtils.isEmpty(otaUpdateBean.getApp_info().getPackage_name())) {
                file.setPackName(otaUpdateBean.getApp_info().getPackage_name());
            }
            file.setFormat(otaUpdateBean.getFormat());
            file.setSpeed("");
            serviceIntent = new Intent(UpdateActivity.this, UpdateService.class);
            serviceIntent.putExtra("filename", file.getFileName());
            serviceIntent.putExtra("url", file.getUrl());
            serviceIntent.putExtra("id", otaUpdateBean.getId() + "");
            serviceIntent.putExtra("seq", file.getSeq());
            serviceIntent.putExtra("type", 3);
            startService(serviceIntent);
            file.setStatus(File.DOWNLOAD_PROCEED);
            otaFile = file;
        }
    }


    /**
     * 系统应用一键更新
     */
    private void startSystemAppDownload() {
        needUpdateSystemList.clear();
        for (int i = 0; i < systemList.size(); i++) {
            File file = systemList.get(i);
            if (file.getFormat() == 4) {
                continue;
            }
            needUpdateSystemList.add(systemList.get(i));
            file.setSizeStr("请稍等");
            file.setSpeed("");
            serviceIntent = new Intent(UpdateActivity.this, UpdateService.class);
            serviceIntent.putExtra("filename", file.getFileName());
            serviceIntent.putExtra("url", file.getUrl());
            serviceIntent.putExtra("id", file.getId());
            serviceIntent.putExtra("seq", file.getSeq());
            serviceIntent.putExtra("type", 1);
            startService(serviceIntent);
            file.setStatus(File.DOWNLOAD_PROCEED);
        }
    }

    private void startSingleDownload(int position) {
        if (otherList != null && otherList.size() > 0) {
            File file = otherList.get(position);
            if (file.getFormat() == 3 || file.getFormat() == 4) {
                return;
            }
            file.setSizeStr("请稍等");
            file.setSpeed("");
            serviceIntent = new Intent(UpdateActivity.this, UpdateService.class);
            serviceIntent.putExtra("filename", file.getFileName());
            serviceIntent.putExtra("url", file.getUrl());
            serviceIntent.putExtra("id", file.getId());
            serviceIntent.putExtra("seq", file.getSeq());
            serviceIntent.putExtra("type", 2);
            startService(serviceIntent);
            file.setStatus(File.DOWNLOAD_PROCEED);
        }
    }


    private void getData(int appType) {
        if (appType == 1) {
            List<String> downloadedFileIds = getDownloadedFileIds();
            Log.d(TAG, "getData: " + downloadedFileIds);
            List<File> fileList = selectDownloadedFiles();
            for (int i = 0; i < systemAppList.size(); i++) {
                UpdateBeanData systemUpdateBean = systemAppList.get(i);
                File file = new File();
                file.setId("" + i);
                file.setSeq(i);
                if (systemUpdateBean.getFormat() == 2) {
                    file.setFormat(systemUpdateBean.getFormat());
                    file.setFileName(systemUpdateBean.getApp_name() + ".apk");
                } else if (systemUpdateBean.getFormat() == 1 || systemUpdateBean.getFormat() == 3) {
                    file.setFormat(systemUpdateBean.getFormat());
                    file.setFileName(systemUpdateBean.getApp_name() + ".zip");
                    if (systemUpdateBean.getFormat() == 1) {
                        file.setVersionCode(systemUpdateBean.getVersion_code());
//                        file.setFileName("ansystem.zip");
                        containsConfigFile = true;
                    }
                } else {
                    file.setFileName(systemUpdateBean.getApp_name());
                }
                if (downloadedFileIds.contains(file.getId())) {
                    File file1 = fileList.get(downloadedFileIds.indexOf(file.getId()));
                    System.out.println(file1);
                    Log.d(TAG, "getData: " + file1.getStatus());
//                file.setStatus(file1.getStatus());
                    //状态始终保持初始
                    file.setStatus(File.DOWNLOAD_REDYA);
                    file.setSizeStr(file1.getSizeStr());
                    file.setCreateTime(file1.getCreateTime());
                } else {
                    file.setStatus(File.DOWNLOAD_REDYA);
                    file.setCreateTime(new Date());
                }
                file.setUrl(systemUpdateBean.getApp_url());
                if (!StringUtils.isEmpty(systemUpdateBean.getApp_info().getPackage_name())) {
                    file.setPackName(systemUpdateBean.getApp_info().getPackage_name());
                }
                file.setVersionCode(systemUpdateBean.getVersion_code());
                if (systemUpdateBean.getFormat() == 3) {
                    file.setFormat(systemUpdateBean.getFormat());
                    systemList.add(file);
                    //跳过ota应用
                    continue;
                } else if ((systemUpdateBean.getFormat() == 1 && (int) SPUtils.getData("configVersion", 0) >= systemUpdateBean.getVersion_code())
                        || (file.getPackName() != null && AppUtils.getVersionCode(this, file.getPackName()) >= systemUpdateBean.getVersion_code())) {
                    if (systemUpdateBean.getFormat() == 1) {
                        //配置文件已经是最新
                        containsConfigFile = false;
                    }
                    file.setFormat(4);
                    systemList.add(file);
                    continue;
                } else {
                    systemList.add(file);
                }
            }
            Collections.sort(systemList);
            for (int i = 0; i < systemList.size(); i++) {
                File file = systemList.get(i);
                if (file.getFormat() == 3 || file.getFormat() == 4) {
                    continue;
                }
                IntentFilter filter = new IntentFilter();
                DownloadReceiver receiver = new DownloadReceiver();
                filter.addAction(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId() + appType);
                registerReceiver(receiver, filter);
                downloadReceiverMap.put(file.getId(), receiver);
            }
        } else {
            List<String> downloadedFileIds = getDownloadedFileIds();
            Log.d(TAG, "getData: " + downloadedFileIds);
            List<File> fileList = selectDownloadedFiles();
            for (int i = 0; i < otherAppList.size(); i++) {
                UpdateBeanData otherUpdateBean = otherAppList.get(i);
                File file = new File();
                file.setId("" + i);
                file.setSeq(i);
                if (otherUpdateBean.getFormat() == 2) {
                    file.setFormat(otherUpdateBean.getFormat());
                    file.setFileName(otherUpdateBean.getApp_name() + ".apk");
                } else if (otherUpdateBean.getFormat() == 1 || otherUpdateBean.getFormat() == 3) {
                    file.setFormat(otherUpdateBean.getApp_info().getType());
                    file.setFileName(otherUpdateBean.getApp_name() + ".zip");
                    if (otherUpdateBean.getFormat() == 1) {
                        file.setVersionCode(otherUpdateBean.getVersion_code());
                    }
                } else {
                    file.setFileName(otherUpdateBean.getApp_name());
                }
                if (downloadedFileIds.contains(file.getId())) {
                    File file1 = fileList.get(downloadedFileIds.indexOf(file.getId()));
                    System.out.println(file1);
                    Log.d(TAG, "getData: " + file1.getStatus());
//                file.setStatus(file1.getStatus());
                    //状态始终保持初始
                    file.setStatus(File.DOWNLOAD_REDYA);
                    file.setSizeStr(file1.getSizeStr());
                    file.setCreateTime(file1.getCreateTime());
                } else {
                    file.setStatus(File.DOWNLOAD_REDYA);
                    file.setCreateTime(new Date());
                }
                file.setUrl(otherUpdateBean.getApp_url());
                if (!StringUtils.isEmpty(otherUpdateBean.getApp_info().getPackage_name())) {
                    file.setPackName(otherUpdateBean.getApp_info().getPackage_name());
                }
                if (otherUpdateBean.getFormat() == 3) {
                    file.setFormat(otherUpdateBean.getFormat());
                    otherList.add(file);
                    //跳过ota应用
                    continue;
                } else if ((otherUpdateBean.getFormat() == 1 && (int) SPUtils.getData("configVersion", 0) >= otherUpdateBean.getVersion_code())
                        || (file.getPackName() != null && AppUtils.getVersionCode(this, file.getPackName()) >= otherUpdateBean.getVersion_code())) {
                    file.setFormat(4);
                    otherList.add(file);
                    continue;
                } else {
                    otherList.add(file);
                }
            }
            Collections.sort(otherList);
            for (int i = 0; i < otherList.size(); i++) {
                File file = otherList.get(i);
                if (file.getFormat() == 3 || file.getFormat() == 4) {
                    continue;
                }
                IntentFilter filter = new IntentFilter();
                DownloadReceiver receiver = new DownloadReceiver();
                filter.addAction(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId() + appType);
                registerReceiver(receiver, filter);
                downloadReceiverMap.put(file.getId(), receiver);
            }
        }
    }

    private void installSystem(Context context) {
        //包含系统应用更新，且为开机检测更新则进入强更流程
        if (isExistSystemUpdate() && !StringUtils.isEmpty(source) && source.equals("splash")) {
            MMKV mmkv = LauncherApplication.Companion.getMMKV();
            mmkv.encode("isStartOtaUpdate", true);
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "android.rockchip.update.service",
                    "android.rockchip.update.service.FirmwareUpdatingActivity"
            ));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(
                    EXTRA_IMAGE_PATH,
                    SYSTEM_ZIP_FULL_PATH
            );
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadReceiverMap.size() > 0) {
            for (String key : downloadReceiverMap.keySet()) {
                unregisterReceiver(downloadReceiverMap.get(key));
            }
            downloadReceiverMap.clear();
        }
        if (systemList != null) {
            systemList.clear();
        }
        if (systemList != null) {
            systemList.clear();
        }
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
        RxTimerUtil.cancel();
    }

    @Override
    public void setListener() {
        tvOtaApp.setOnClickListener(this);
        tvSystemApp.setOnClickListener(this);
        tvOtherApp.setOnClickListener(this);
        llBackUpdate.setOnClickListener(this);
        tvUpdateAll.setOnClickListener(this);
        tvOtaAppUpdate.setOnClickListener(this);
    }

    @Override
    public void initView() {
        tvOtaApp = findViewById(R.id.tv_ota_app);
        tvSystemApp = findViewById(R.id.tv_system_app);
        tvOtherApp = findViewById(R.id.tv_other_app);
        llBackUpdate = findViewById(R.id.ll_back_update);
        tvOtaApp.setSelected(true);
        systemRecyclerView = findViewById(R.id.rv_system_app_update);
        otherRecyclerView = findViewById(R.id.rv_other_app_update);
        tvUpdateAll = findViewById(R.id.tv_update_all);
        tvLocalOtaApp = findViewById(R.id.tv_local_ota_app);
        tvNewOtaApp = findViewById(R.id.tv_new_ota_app);
        tvOtaAppUpdate = findViewById(R.id.tv_ota_app_update);
        llOtaUpdate = findViewById(R.id.ll_ota_update);
        llOtaUpdateBtn = findViewById(R.id.ll_ota_update_btn);
        llOtaUpdateProgress = findViewById(R.id.ll_ota_update_progress);
        pbUpdateOta = findViewById(R.id.pb_update_ota);
        tvOtaUpdateText = findViewById(R.id.tv_ota_update_text);
        ivOtaLogo = findViewById(R.id.iv_ota_logo);

        tvCheckTitle = findViewById(R.id.tv_check_update_title);
        tvCheckContent = findViewById(R.id.tv_check_update_content);
        tvCheckStep = findViewById(R.id.tv_check_update_step);
        tvUpdating = findViewById(R.id.tv_updating);
    }

    @Nullable
    @Override
    public PresenterImpl initPresenter() {
        return new PresenterImpl();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_update;
    }

    @Override
    public void onSuccess(@NotNull Object any) {

    }

    @Override
    public void onError(@NotNull String error) {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ota_app:
                setSelectRefreshUI(tvOtaApp);
                break;
            case R.id.tv_system_app:
                setSelectRefreshUI(tvSystemApp);
                break;
            case R.id.tv_other_app:
                setSelectRefreshUI(tvOtherApp);
                if (otherList.size() == 0) {
                    getData(2);
                    otherAdapter.setNewInstance(otherList);
                    otherAdapter.notifyDataSetChanged();
                }
                break;
            //开始下载
            case R.id.tv_update_all:
                ToastUtils.showShort(this, "开始一键更新");
                tvUpdateAll.setBackgroundResource(R.drawable.update_oval);
                tvUpdateAll.setTextColor(Color.parseColor("#50ffffff"));
                tvUpdateAll.setEnabled(false);
                tvUpdateAll.setClickable(false);
                startSystemAppDownload();
                break;
            case R.id.ll_back_update:
                finish();
                break;
            case R.id.tv_ota_app_update:
                if (otaInstall) {
                    installSystem(this);
                } else {
                    if (!isShowDialog) {
                        startOtaUpdate();
                    } else {
                        showUpdateOtaDialog();
                    }
                }
                break;
            default:
        }
    }

    private void showUpdateOtaDialog() {
        CustomDialog customDialog = new CustomDialog(this, R.layout.dialog_confirm_new);
        TextView tvOtaVersion = customDialog.findViewById(R.id.tv_content_dialog);
        tvOtaVersion.setText("确定要更新版本吗？");
        customDialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            customDialog.dismiss();
        });
        customDialog.findViewById(R.id.confirm).setOnClickListener(v -> {
            customDialog.dismiss();
            startOtaUpdate();
        });
        customDialog.show();
    }

    /**
     * 开始下载固件更新
     */
    private void startOtaUpdate() {
        getPresenter().sendMenuEnableBroadcast(this, false);
        ToastUtils.showShort(this, "开始OTA固件更新");
        llOtaUpdateBtn.setVisibility(View.GONE);
        llOtaUpdateProgress.setVisibility(View.VISIBLE);
        if (isCheckUpdate) {
            tvCheckTitle.setVisibility(View.VISIBLE);
            tvCheckContent.setVisibility(View.VISIBLE);
            tvCheckStep.setVisibility(View.VISIBLE);
            ivOtaLogo.setVisibility(View.GONE);
            tvUpdating.setVisibility(View.GONE);
            if (!isExistSystemUpdate()) {
                tvCheckStep.setText("共计1个更新项，当前为第1项目");
            }
        }
        setBanOnBack(true);
        //开启ota升级
        startOtaDownload();
    }

    /**
     * true 表示存在系统更新
     */
    private boolean isExistSystemUpdate() {
        Integer configVersion = (Integer) SPUtils.getData(
                "configVersion",
                0
        );
        for (int i = 0; i < systemAppList.size(); i++) {
            UpdateBeanData updateBeanData = systemAppList.get(i);
            //资源包
            if (updateBeanData.getFormat() == 1 && configVersion < updateBeanData.getVersion_code()) {
                return true;
            } else if (AppUtils.getVersionCode(
                    this,
                    updateBeanData.getPackName()
            ) < updateBeanData.getVersion_code()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 禁止返回的方法
     *
     * @param b true表示禁止返回 false可以返回
     */
    private void setBanOnBack(boolean b) {
        if (b) {
            //禁止返回
            llBackUpdate.setVisibility(View.GONE);
            tvOtherApp.setVisibility(View.GONE);
            tvSystemApp.setVisibility(View.GONE);
        } else {
            //可以返回
            llBackUpdate.setVisibility(View.VISIBLE);
            tvSystemApp.setVisibility(View.VISIBLE);
            tvOtherApp.setVisibility(View.VISIBLE);
        }
    }

  /*  @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //do something.
            return true;//系统层不做处理 就可以了
        } else {
            return super.dispatchKeyEvent(event);
        }
    }*/

    /**
     * 设置点击后的UI刷新
     *
     * @param view 点击触发的控件
     */
    private void setSelectRefreshUI(View view) {
        tvOtaApp.setSelected(view == tvOtaApp);
        tvSystemApp.setSelected(view == tvSystemApp);
        tvOtherApp.setSelected(view == tvOtherApp);

        systemRecyclerView.setVisibility(view == tvSystemApp ? View.VISIBLE : View.GONE);
        otherRecyclerView.setVisibility(view == tvOtherApp ? View.VISIBLE : View.GONE);
        tvUpdateAll.setVisibility(view == tvSystemApp ? View.VISIBLE : View.GONE);
        llOtaUpdate.setVisibility(view == tvOtaApp ? View.VISIBLE : View.GONE);
    }

    class DownloadReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", 1);
            if (type == 1) {
                //系统应用
                for (int i = 0; i < systemList.size(); i++) {
                    try {
                        File file = systemList.get(i);
                        if (intent.getAction().equals(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId() + type)) {
                            String speedS = intent.getStringExtra("speed");
                            String sizeS = intent.getStringExtra("size");
                            String totalSize = intent.getStringExtra("totalSize");
                            int pencent = intent.getIntExtra("percent", 0);
                            int status = intent.getIntExtra("status", 0);
                            String filePath = intent.getStringExtra("filePath");
                            if (status == File.DOWNLOAD_PROCEED) {//下载进行中
                                file.setSpeed(speedS);
                                file.setProgress(pencent);
                                file.setSizeStr(sizeS);
                                systemAdapter.notifyItemChanged(i);
                            }
                            if (status == File.DOWNLOAD_COMPLETE) {//完成
                                file.setStatus(File.DOWNLOAD_COMPLETE);
                                file.setProgress(pencent);
                                file.setSizeStr(totalSize);
                                file.setPath(filePath);
                                systemAdapter.notifyItemChanged(i);
                                LauncherApplication.Companion.getDownloadTaskHashMap().remove(file.getId());
                                String apkPath = Environment.getExternalStorageDirectory().getPath() + "/" + file.getFileName();
                                if (isStartOtaUpdate) {
                                    syncDownloadProgress();
                                }
                                if (file.getPackName() != null && file.getPackName().equals(AppConstants.LAUNCHER_PACKAGE_NAME) && containsConfigFile) {
                                } else if (file.getFormat() == 2) {
                                    if (ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), apkPath)) {
                                        if (!isStartOtaUpdate) {
                                            file.setInstalled(true);
                                            systemAdapter.notifyItemChanged(i);
//                                        sendUpdateBroadcast(file.getPackName());
                                        }
                                    }

                                } else if (file.getFormat() == 1) {
                                    if (!StringUtils.isEmpty(apkPath)) {
                                        ToastUtils.showLong(UpdateActivity.this, "当前正在解压配置文件，请稍等..请不要退出当前页面");
                                        new Thread(() -> loadZip(apkPath, file.getVersionCode())).start();
                                    }
                                }
                            }
                            if (status == File.DOWNLOAD_ERROR) {
                                //出错则继续下载
                                file.setStatus(File.DOWNLOAD_PROCEED);
                                file.setSpeed("---");
                                intent = new Intent(LauncherApplication.Companion.getContext(), DownloadService.class);
                                intent.putExtra("filename", file.getFileName());
                                intent.putExtra("url", file.getUrl());
                                intent.putExtra("id", file.getId());
                                intent.putExtra("seq", file.getSeq());
                                startService(intent);
//                            LauncherApplication.Companion.getDownloadTaskHashMap().get(file.getId()).cancel();
//                            file.setStatus(File.DOWNLOAD_ERROR);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (type == 2) {
                //预装应用
                for (int i = 0; i < otherList.size(); i++) {
                    File file = otherList.get(i);
                    if (intent.getAction().equals(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId() + type)) {
                        String speedS = intent.getStringExtra("speed");
                        String sizeS = intent.getStringExtra("size");
                        String totalSize = intent.getStringExtra("totalSize");
                        int pencent = intent.getIntExtra("percent", 0);
                        int status = intent.getIntExtra("status", 0);
                        String filePath = intent.getStringExtra("filePath");
                        if (status == File.DOWNLOAD_PROCEED) {//下载进行中
                            file.setSpeed(speedS);
                            file.setProgress(pencent);
                            file.setSizeStr(sizeS);
                            otherAdapter.notifyItemChanged(i);
                        }
                        if (status == File.DOWNLOAD_COMPLETE) {//完成
                            file.setStatus(File.DOWNLOAD_COMPLETE);
                            file.setProgress(pencent);
                            file.setSizeStr(totalSize);
                            file.setPath(filePath);
                            otherAdapter.notifyItemChanged(i);
                            LauncherApplication.Companion.getDownloadTaskHashMap().remove(file.getId());
                            String apkPath = Environment.getExternalStorageDirectory().getPath() + "/" + file.getFileName();
                            if (file.getFormat() == 2) {

                                //剩余容量大于100M时执行安装
//                                if (getAvailableSize() > 100) {
                                if (ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), apkPath)) {
                                    file.setInstalled(true);
                                    systemAdapter.notifyItemChanged(i);
//                                        sendUpdateBroadcast(file.getPackName());
                                }
//                                } else {
//                                    ToastUtils.showLong(context, "安装失败，请查看存储空间是否充足");
//                                }
                            } else if (file.getFormat() == 1) {
                                if (!StringUtils.isEmpty(apkPath)) {
                                    loadZip(apkPath, file.getVersionCode());
                                }
                            }
                        }
                        if (status == File.DOWNLOAD_ERROR) {
                            LauncherApplication.Companion.getDownloadTaskHashMap().get(file.getId()).cancel();
                            file.setStatus(File.DOWNLOAD_ERROR);
                        }
                    }
                }

            } else {
                File file = otaFile;
                if (intent.getAction().equals(AppConstants.LAUNCHER_PACKAGE_NAME + otaUpdateBean.getId() + type)) {
                    String speedS = intent.getStringExtra("speed");
                    String sizeS = intent.getStringExtra("size");
                    String totalSize = intent.getStringExtra("totalSize");
                    int pencent = intent.getIntExtra("percent", 0);
                    int status = intent.getIntExtra("status", 0);
                    String filePath = intent.getStringExtra("filePath");
                    if (status == File.DOWNLOAD_PROCEED) {//下载进行中
                        file.setSpeed(speedS);
                        file.setProgress(pencent);
                        file.setSizeStr(sizeS);
                        tvOtaUpdateText.setText("更新进度：" + file.getProgress() + "%");
                        pbUpdateOta.setProgress(file.getProgress());
                    }
                    if (status == File.DOWNLOAD_COMPLETE) {//完成
                        file.setStatus(File.DOWNLOAD_COMPLETE);
                        file.setProgress(pencent);
                        file.setSizeStr(totalSize);
                        file.setPath(filePath);
                        tvOtaUpdateText.setText("更新进度：100%");
                        pbUpdateOta.setProgress(file.getProgress());
                        LauncherApplication.Companion.getDownloadTaskHashMap().remove(file.getId());
                        String apkPath = Environment.getExternalStorageDirectory().getPath() + "/" + file.getFileName();
                        installSystem(context);
                        if (StringUtils.isEmpty(source)) {
                            setBanOnBack(false);
                            getPresenter().sendMenuEnableBroadcast(UpdateActivity.this, true);
                        } else if (source.equals("splash") && !newSplash) {
                            getPresenter().sendMenuEnableBroadcast(UpdateActivity.this, true);
                        }
                        if (isCheckUpdate) {
                            ivOtaLogo.setVisibility(View.VISIBLE);
                        }
                        llOtaUpdateProgress.setVisibility(View.GONE);
                        llOtaUpdateBtn.setVisibility(View.VISIBLE);
                        //固件安装之前可以返回
                        llBackUpdate.setVisibility(View.VISIBLE);
                        tvOtaAppUpdate.setText("安装");
                        otaInstall = true;
                    }
                    if (status == File.DOWNLOAD_ERROR) {
                        LauncherApplication.Companion.getDownloadTaskHashMap().get(file.getId()).cancel();
                        file.setStatus(File.DOWNLOAD_ERROR);
                    }
                }
            }
        }
    }

    private void syncDownloadProgress() {
        int configVersion = (int) SPUtils.getData(
                "configVersion",
                0
        );
        RxTimerUtil.interval(1000, number -> {
            int a = 0;
            for (int i = 0; i < needUpdateSystemList.size(); i++) {
                File file = needUpdateSystemList.get(i);
                int localVersionCode = 0;
                if (file.getFormat() == 2) {
                    localVersionCode = AppUtils.getVersionCode(UpdateActivity.this, file.getPackName());
                }
                if (file.getFormat() == 2 && localVersionCode != 0 && localVersionCode >= file.getVersionCode()
                ) {
                    //apk更新
                    a++;
                    Log.e("UpdateActivity", "共" + needUpdateSystemList.size() + "个安装包,已更新" + a + "个安装包");
                } else if (file.getFormat() == 1 && forceUpdateUnzipFlag) {
                    //资源包更新
                    a++;
                    Log.e("UpdateActivity", "共" + needUpdateSystemList.size() + "个安装包,已更新" + a + "个安装包");
                }
            }
            if (a != 0 && needUpdateSystemList.size() > 0 && a > installApp) {
                installApp = a;
                int progress = installApp * 100 / needUpdateSystemList.size();
                pbUpdateOta.setProgress(progress);
                tvOtaUpdateText.setText("更新进度：" + progress + "%");
                Log.i(TAG, "syncDownloadProgress: 更新进度：" + progress + "%");
                if (needUpdateSystemList.size() == installApp) {
                    ToastUtils.showShort(this, "恭喜，更新完成！");
                    llBackUpdate.setVisibility(View.VISIBLE);
                    //解除强更状态
                    MMKV mmkv = LauncherApplication.Companion.getMMKV();
                    mmkv.encode("isStartOtaUpdate", false);
                }
            }
        });
    }

    /**
     * 发送自定义刷新安装状态的广播
     */
    private void sendUpdateBroadcast(String packageName) {
        Log.i(TAG, "sendUpdateBroadcast: packageName = " + packageName);
        Intent intent = new Intent();
        intent.setAction("com.alight.android.update");
        intent.putExtra("packageName", packageName);// 设置广播的消息
        sendBroadcast(intent);
    }

    /**
     * 计算Sdcard的剩余大小
     *
     * @return MB
     */
    private long getAvailableSize() {
        String sdcard = Environment.getExternalStorageState();
        //外部储存sdcard存在的情况
        String state = Environment.MEDIA_MOUNTED;
        //获取Sdcard的路径
        //获得路径
        java.io.File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        if (sdcard.equals(state)) {
            //获得Sdcard上每个block的size
            long blockSize = statFs.getBlockSize();
            //获取可供程序使用的Block数量
            long blockavailable = statFs.getAvailableBlocks();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockavailableTotal = blockSize * blockavailable / 1000 / 1000;
            return blockavailableTotal;
        } else {
            return -1;
        }
    }

    /**
     * 检查外部scard读写权限
     */
    private void checkExtrnalStorage() {
        //首先判断用户手机的版本号 如果版本大于6.0就需要动态申请权限
        //如果版本小于6.0就直接去扫描二维码
        if (Build.VERSION.SDK_INT > 23) {
            //说明是android6.0之前的
            //添加动态权限申请
            //1.定义一个数组，用来装载申请的权限
            String[] permissons = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
            //2.判断这些权限有没有申请，没有申请的话，就把没有申请的权限放到一个数组里面
            ArrayList<String> deniedPermissions = new ArrayList<String>();
            for (String permission : permissons) {
                int i = ContextCompat.checkSelfPermission(this, permission);
                if (PackageManager.PERMISSION_DENIED == i) {
                    //说明权限没有被申请
                    deniedPermissions.add(permission);
                }
            }
            if (deniedPermissions.size() == 0) {
                //说明是android6.0之前的
                return;
            }
            //当你不知道数组多大的时候，就可以先创建一个集合，然后调用集合的toArray方法需要传递一个数组参数，这个数组参数的长度
            //设置成跟集合一样的长度
            String[] strings = deniedPermissions.toArray(new String[permissons.length]);
            //3.去申请权限
            ActivityCompat.requestPermissions(this, strings, REQUEST_CODE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 查询已下载的文件
     *
     * @return
     */
    private List<File> selectDownloadedFiles() {
        List<File> fileList = new ArrayList<>();
        DbManager db = DbHelper.getDbManager();
        try {
            fileList = db.selector(File.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private List<String> getDownloadedFileIds() {
        List<String> ids = new ArrayList<>();
        List<File> fileList = selectDownloadedFiles();
        if (fileList != null) {
            for (File file : fileList) {
                ids.add(file.getId());
            }
        }
        return ids;
    }
}
