package com.alight.android.aoa_launcher.activity;

import static com.alight.android.aoa_launcher.common.constants.AppConstants.EXTRA_IMAGE_PATH;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.LAUNCHER_PACKAGE_NAME;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.SYSTEM_ZIP_FULL_PATH;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.alight.android.aoa_launcher.service.UpdateService;
import com.alight.android.aoa_launcher.ui.adapter.UpdateAdapter;
import com.alight.android.aoa_launcher.ui.view.CustomDialog;
import com.alight.android.aoa_launcher.utils.ApkController;
import com.alight.android.aoa_launcher.utils.AppUtils;
import com.alight.android.aoa_launcher.utils.RxTimerUtil;
import com.alight.android.aoa_launcher.utils.SPUtils;
import com.alight.android.aoa_launcher.utils.StringUtils;
import com.alight.android.aoa_launcher.utils.ToastUtils;
import com.liulishuo.okdownload.DownloadTask;
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
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ????????????????????????????????????
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
    private TextView tvUpdateAllSystem;
    private TextView tvUpdateAllOther;
    private Intent serviceIntent;
    private ArrayList<UpdateBeanData> systemAppList;
    private ArrayList<UpdateBeanData> otherAppList;
    //ota???????????????
    private UpdateBeanData otaUpdateBean;
    //1??????????????? 2???????????????
    private int appType = 1;
    //???????????????????????????????????????
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
    //true???????????????????????????????????????
    private boolean newSplash;
    private String source;
    private boolean isShowDialog = false;
    private UpgradeApkReceiver upgradeApkReceiver;
    private int updatePostion;
    //true?????????????????????
    private boolean isCheckUpdate = false;
    private boolean isStartOtaUpdate = false;
    private int installApp = 0;//????????????????????????????????????
    private boolean forceUpdateUnzipFlag = false;

    private String launcherApkPath;
    //??????????????????????????????
    private int installedSystemAppNumber;

    public static int selectPage;

    private Thread downAllOtherAppThread;
    private final static Object lockObjectA = new Object();

    @Override

    public void initData() {
        EventBus.getDefault().register(this);
        systemAppList = (ArrayList<UpdateBeanData>) getIntent().getSerializableExtra("systemApp");
        otherAppList = (ArrayList<UpdateBeanData>) getIntent().getSerializableExtra("otherApp");

        for (int i = 0; i < systemAppList.size(); i++) {
            if (systemAppList.get(i).getFormat() == 3) {
                //??????OTA???
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
        //?????????????????????TextView?????????????????????
        otherAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.tv_update_item && (otherAdapter.getData().get(position).getFormat() == 1 || otherAdapter.getData().get(position).getFormat() == 2)) {
                View tvUpdate = otherAdapter.getViewByPosition(position, R.id.tv_update_item);
                tvUpdate.setEnabled(false);
                startSingleDownload(position);
            }
        });
        source = getIntent().getStringExtra("source");
        newSplash = getIntent().getBooleanExtra("new_splash", true);
        //????????????????????????
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
            selectPage = 1;
        } else {
            isShowDialog = true;
        }
        boolean isHaveOtaUpdate = initOtaUpdate();

        //????????????????????????????????????????????????
        if (systemAdapter.getData().size() > 0) {
            for (int i = 0; i < systemAdapter.getData().size(); i++) {
                if (systemAdapter.getData().get(i).getFormat() != 3 && systemAdapter.getData().get(i).getFormat() != 4) {
                    //????????????????????????
                    break;
                } else if (i == systemAdapter.getData().size() - 1) {
                    tvUpdateAllSystem.setBackgroundResource(R.drawable.update_oval);
                    tvUpdateAllSystem.setTextColor(getColor(R.color.person_center_text_alpha_green));
                    tvUpdateAllSystem.setEnabled(false);
                    tvUpdateAllSystem.setClickable(false);
                    tvUpdateAllSystem.setText("????????????");
                }
            }
        }
        if (otherAdapter.getData().size() > 0) {
            for (int i = 0; i < otherAdapter.getData().size(); i++) {
                if (otherAdapter.getData().get(i).getFormat() != 3 && otherAdapter.getData().get(i).getFormat() != 4) {
                    //????????????????????????
                    break;
                } else if (i == otherAdapter.getData().size() - 1) {
                    tvUpdateAllOther.setBackgroundResource(R.drawable.update_oval);
                    tvUpdateAllOther.setTextColor(getColor(R.color.person_center_text_alpha_green));
                    tvUpdateAllOther.setEnabled(false);
                    tvUpdateAllOther.setClickable(false);
                    tvUpdateAllOther.setText("????????????");
                }
            }
        }
        MMKV mmkv = LauncherApplication.Companion.getMMKV();
        isStartOtaUpdate = mmkv.getBoolean("isStartOtaUpdate", false);
        //??????????????????
        if (isStartOtaUpdate && !isHaveOtaUpdate && !StringUtils.isEmpty(source)) {
            startForceUpdate();
        }
        if (selectPage == 1) {
            setSelectRefreshUI(tvSystemApp);
        } else if (selectPage == 2) {
            setSelectRefreshUI(tvOtherApp);
            if (otherList.size() == 0) {
                getData(2);
                otherAdapter.setNewInstance(otherList);
                otherAdapter.notifyDataSetChanged();
            }
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
        tvCheckStep.setText("??????2???????????????????????????2??????");
        startSystemAppDownload();
    }

    private boolean initOtaUpdate() {
        tvLocalOtaApp.setText("?????????" + Build.DISPLAY);
        if (otaUpdateBean != null && !otaUpdateBean.getVersion_name().equals(Build.DISPLAY)) {
            //?????????
            tvNewOtaApp.setText("???????????????:" + otaUpdateBean.getVersion_name());
            IntentFilter filter = new IntentFilter();
            DownloadReceiver receiver = new DownloadReceiver();
            filter.addAction(AppConstants.LAUNCHER_PACKAGE_NAME + otaUpdateBean.getId() + 3);
            registerReceiver(receiver, filter);
            downloadReceiverMap.put(String.valueOf(otaUpdateBean.getId()), receiver);
            return true;
        } else {
            //?????????
            tvNewOtaApp.setText("??????????????????");
            tvOtaAppUpdate.setVisibility(View.GONE);
            ivOtaLogo.setPadding(0, 80, 0, 0);
            return false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetUpdateEvent(UpdateEvent updateEvent) {
        String packageName = updateEvent.packageName;
        refreshInstallState(packageName);
       /* if (packageName.equals(AHWCX_PACKAGE_NAME) && selectPage == 1) {
            setSelectRefreshUI(tvSystemApp);
        }*/
    }

    private void refreshInstallState(String packageName) {
        List<File> systemAdapterData = systemAdapter.getData();
        for (int i = 0; i < systemAdapterData.size(); i++) {
            if (packageName.equals(systemAdapterData.get(i).getPackName())) {
                TextView tvUpdate = (TextView) systemAdapter.getViewByPosition(i, R.id.tv_update_item);
                if (tvUpdate != null)
                    tvUpdate.setText("?????????");
                return;
            }
        }
        List<File> otherAdapterData = otherAdapter.getData();
        for (int i = 0; i < otherAdapterData.size(); i++) {
            if (packageName.equals(otherAdapterData.get(i).getPackName())) {
                TextView tvUpdate = (TextView) otherAdapter.getViewByPosition(i, R.id.tv_update_item);
                if (tvUpdate != null)
                    tvUpdate.setText("?????????");
                return;
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @throws Exception
     */

// ???????????????????????????????????????????????????????????????????????????
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean upZipFile(String zipFile, String folderPath) {
        ZipFile zfile = null;

        try {
// ?????????GBK?????????????????????

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

// ??????????????????????????????????????????????????????????????????

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

// ze.getName()????????? script/start.script????????????????????????????????????File

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

// ??????????????????????????????

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
     * ?????????????????????????????????????????????????????????????????????.
     *
     * @param baseDir     ???????????????
     * @param absFileName ???????????????????????????ZipEntry??????name
     * @return java.io.File ???????????????
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
                Log.d("print", "????????????????????????");
                // ??????????????????
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
                    String launcherApkPath = "";
                    for (int i = 0; i < systemAdapter.getData().size(); i++) {
                        File file = systemAdapter.getData().get(i);
                        if (file.getFormat() == 1) {
                            zipPosition = i;
                        }
                        if (file.getPackName() != null && file.getPackName().equals(LAUNCHER_PACKAGE_NAME)) {
                            launcherApkPath = Environment.getExternalStorageDirectory().getPath() + "/" + file.getFileName();
                        }
                    }
                    if (isStartOtaUpdate) {
                        forceUpdateUnzipFlag = true;
                    }
                    runOnUiThread(() -> {
                        ToastUtils.showShort(this, "???????????????");
                        TextView tvUpdate = (TextView) systemAdapter.getViewByPosition(zipPosition, R.id.tv_update_item);
                        if (tvUpdate != null)
                            tvUpdate.setText("?????????");
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
                        }
                    }
                }
            } else {
                Log.d("print", "??????????????????");
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ??????????????????
     *
     * @param filePath ???????????????????????????
     * @return ????????????????????????true???????????????false
     */
    public boolean deleteSingleFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param filePath ??????????????????????????????
     * @return ????????????????????????true???????????????false
     */
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //??????filePath?????????????????????????????????????????????????????????
        if (!filePath.endsWith(java.io.File.separator)) {
            filePath = filePath + java.io.File.separator;
        }
        java.io.File dirFile = new java.io.File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        java.io.File[] files = dirFile.listFiles();
        //???????????????????????????????????????(???????????????)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //???????????????
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                //???????????????
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        //?????????????????????
        return dirFile.delete();
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param filePath ???????????????????????????
     * @return ?????????????????? true??????????????? false???
     */
    public boolean deleteFolder(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // ????????????????????????????????????
                return deleteFile(filePath);
            } else {
                // ????????????????????????????????????
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * ota??????
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
                //????????????????????????
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
     * ????????????????????????
     */
    private void startSystemAppDownload() {
        needUpdateSystemList.clear();
        for (int i = 0; i < systemList.size(); i++) {
            File file = systemList.get(i);
            if (file.getFormat() == 4) {
                continue;
            }
            needUpdateSystemList.add(systemList.get(i));
            file.setSizeStr("?????????");
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
            file.setSizeStr("?????????");
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
                file.setTopFlag(systemUpdateBean.getApp_info().getTop_flag());
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
                    //????????????????????????
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
                    //??????ota??????
                    continue;
                } else if ((systemUpdateBean.getFormat() == 1 && (int) SPUtils.getData("configVersion", 1) >= systemUpdateBean.getVersion_code())
                        || (file.getPackName() != null && AppUtils.getVersionCode(this, file.getPackName()) >= systemUpdateBean.getVersion_code())) {
                    if (systemUpdateBean.getFormat() == 1) {
                        //???????????????????????????
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
            complexSorting(systemList);
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
                file.setTopFlag(otherUpdateBean.getApp_info().getTop_flag());
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
                    //????????????????????????
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
                    //??????ota??????
                    continue;
                } else if ((otherUpdateBean.getFormat() == 1 && (int) SPUtils.getData("configVersion", 1) >= otherUpdateBean.getVersion_code())
                        || (file.getPackName() != null && AppUtils.getVersionCode(this, file.getPackName()) >= otherUpdateBean.getVersion_code())) {
                    file.setFormat(4);
                    otherList.add(file);
                    continue;
                } else {
                    otherList.add(file);
                }
            }
            Collections.sort(otherList);
            complexSorting(otherList);
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

    private void complexSorting(List<File> list) {
        ArrayList<File> needUpdateTopFlagList = new ArrayList<>();
        ArrayList<File> otherTopFlagList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            if (file.getTopFlag() == 1) {
                if (file.getFormat() == 4) {
                    //????????????
                    otherTopFlagList.add(file);
                } else {
                    //????????????
                    needUpdateTopFlagList.add(file);
                }
            }
        }
        list.removeAll(needUpdateTopFlagList);
        list.removeAll(otherTopFlagList);
        list.addAll(0, needUpdateTopFlagList);
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getFormat() == 4) {
                index = i;
                break;
            }
        }
        list.addAll(index, otherTopFlagList);
    }

    private void installSystem(Context context) {
        //????????????????????????????????????????????????????????????????????????
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
        tvUpdateAllSystem.setOnClickListener(this);
        tvUpdateAllOther.setOnClickListener(this);
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
        tvUpdateAllSystem = findViewById(R.id.tv_update_all_system);
        tvUpdateAllOther = findViewById(R.id.tv_update_all_other);
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

/*
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        switch (selectPage) {
            case 0: {
                setSelectRefreshUI(tvOtaApp);
                break;
            }
            case 1: {
                setSelectRefreshUI(tvSystemApp);
                break;
            }
//            case 2: {
//                setSelectRefreshUI(tvOtherApp);
//                break;
//            }
        }
    }
*/

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
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ota_app:
                setSelectRefreshUI(tvOtaApp);
                selectPage = 0;
                break;
            case R.id.tv_system_app:
                setSelectRefreshUI(tvSystemApp);
                selectPage = 1;
                break;
            case R.id.tv_other_app:
                setSelectRefreshUI(tvOtherApp);
                if (otherList.size() == 0) {
                    getData(2);
                    otherAdapter.setNewInstance(otherList);
                    otherAdapter.notifyDataSetChanged();
                }
                selectPage = 2;
                break;
            //????????????
            case R.id.tv_update_all_system:
                //????????????????????????
                ToastUtils.showShort(this, "??????????????????");
                tvUpdateAllSystem.setBackgroundResource(R.drawable.update_oval);
                tvUpdateAllSystem.setTextColor(Color.parseColor("#80215558"));
                tvUpdateAllSystem.setEnabled(false);
                tvUpdateAllSystem.setClickable(false);
                //??????????????????
                startSystemAppDownload();
                break;
            case R.id.tv_update_all_other:
                //????????????????????????
                ToastUtils.showShort(this, "??????????????????");
                tvUpdateAllOther.setBackgroundResource(R.drawable.update_oval);
                tvUpdateAllOther.setTextColor(Color.parseColor("#80215558"));
                tvUpdateAllOther.setEnabled(false);
                tvUpdateAllOther.setClickable(false);
                for (int i = 0; i < otherAdapter.getData().size(); i++) {
                    File file = otherAdapter.getData().get(i);
                    if (file.getFormat() == 1 || file.getFormat() == 2) {
                        file.setToBeUpdated(true);
                         /*   TextView tvUpdate = (TextView) otherAdapter.getViewByPosition(i, R.id.tv_update_item);
                            if (tvUpdate != null) {
                                tvUpdate.setEnabled(false);
                                tvUpdate.setBackgroundResource(R.drawable.update_oval_trans20);
                                tvUpdate.setText("?????????");
                            }*/
                    }
                }
                otherAdapter.notifyDataSetChanged();
                if (downAllOtherAppThread == null) {
                    Runnable runnable = () -> {
                        for (int i = 0; i < otherAdapter.getData().size(); i++) {
                            synchronized (lockObjectA) {
                                File file = otherAdapter.getData().get(i);
                                if (file.getFormat() == 1 || file.getFormat() == 2) {
                                    startSingleDownload(i);
                                    try {
                                        lockObjectA.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    };
                    downAllOtherAppThread = new Thread(runnable);
                    downAllOtherAppThread.start();
                }
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
        tvOtaVersion.setText("???????????????????????????");
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
     * ????????????????????????
     */
    private void startOtaUpdate() {
        getPresenter().sendMenuEnableBroadcast(this, false);
        ToastUtils.showShort(this, "??????OTA????????????");
        llOtaUpdateBtn.setVisibility(View.GONE);
        llOtaUpdateProgress.setVisibility(View.VISIBLE);
        if (isCheckUpdate) {
            tvCheckTitle.setVisibility(View.VISIBLE);
            tvCheckContent.setVisibility(View.VISIBLE);
            tvCheckStep.setVisibility(View.VISIBLE);
            ivOtaLogo.setVisibility(View.GONE);
            tvUpdating.setVisibility(View.GONE);
            if (!isExistSystemUpdate()) {
                tvCheckStep.setText("??????1???????????????????????????1??????");
            }
        }
        setBanOnBack(true);
        //??????ota??????
        startOtaDownload();
    }

    /**
     * true ????????????????????????
     */
    private boolean isExistSystemUpdate() {
        Integer configVersion = (Integer) SPUtils.getData(
                "configVersion",
                1
        );
        for (int i = 0; i < systemAppList.size(); i++) {
            UpdateBeanData updateBeanData = systemAppList.get(i);
            //?????????
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
     * ?????????????????????
     *
     * @param b true?????????????????? false????????????
     */
    private void setBanOnBack(boolean b) {
        if (b) {
            //????????????
            llBackUpdate.setVisibility(View.GONE);
            tvOtherApp.setVisibility(View.GONE);
            tvSystemApp.setVisibility(View.GONE);
        } else {
            //????????????
            llBackUpdate.setVisibility(View.VISIBLE);
            tvSystemApp.setVisibility(View.VISIBLE);
            tvOtherApp.setVisibility(View.VISIBLE);
        }
    }

  /*  @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //do something.
            return true;//????????????????????? ????????????
        } else {
            return super.dispatchKeyEvent(event);
        }
    }*/

    /**
     * ??????????????????UI??????
     *
     * @param view ?????????????????????
     */
    private void setSelectRefreshUI(View view) {
        tvOtaApp.setSelected(view == tvOtaApp);
        tvSystemApp.setSelected(view == tvSystemApp);
        tvOtherApp.setSelected(view == tvOtherApp);

        systemRecyclerView.setVisibility(view == tvSystemApp ? View.VISIBLE : View.GONE);
        otherRecyclerView.setVisibility(view == tvOtherApp ? View.VISIBLE : View.GONE);
        tvUpdateAllSystem.setVisibility(view == tvSystemApp ? View.VISIBLE : View.GONE);
        tvUpdateAllOther.setVisibility(view == tvOtherApp ? View.VISIBLE : View.GONE);
        llOtaUpdate.setVisibility(view == tvOtaApp ? View.VISIBLE : View.GONE);
    }

    class DownloadReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", 1);
            if (type == 1) {
                //????????????
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
                            if (status == File.DOWNLOAD_PROCEED) {//???????????????
                                file.setSpeed(speedS);
                                file.setProgress(pencent);
                                file.setSizeStr(sizeS);
                                systemAdapter.notifyItemChanged(i);
                            }
                            if (status == File.DOWNLOAD_COMPLETE) {//??????
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
                                    if (file.getPackName() != null && file.getPackName().equals(AppConstants.LAUNCHER_PACKAGE_NAME)) {
                                        launcherApkPath = Environment.getExternalStorageDirectory().getPath() + "/" + file.getFileName();
                                    } else if (ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), apkPath)) {
                                        if (!isStartOtaUpdate) {
                                            file.setInstalled(true);
                                            systemAdapter.notifyItemChanged(i);
//                                        sendUpdateBroadcast(file.getPackName());
                                        }
                                        //??????????????????????????????
                                        installedSystemAppNumber++;
                                    }
                                    //?????????????????????????????????????????????????????????
                                    if (installedSystemAppNumber == needUpdateSystemList.size() - 1 && !StringUtils.isEmpty(launcherApkPath)) {
                                        Observable.timer(5, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                                System.out.println("onSubscribe");
                                            }

                                            @Override
                                            public void onNext(Long value) {
                                                if (ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), launcherApkPath)) {
                                                    for (int j = 0; j < systemAdapter.getData().size(); j++) {
                                                        if (LAUNCHER_PACKAGE_NAME.equals(systemAdapter.getData().get(j).getPackName())) {
                                                            updatePostion = j;
                                                            systemList.get(updatePostion).setInstalled(true);
                                                            installedSystemAppNumber++;
                                                            runOnUiThread(() -> systemAdapter.notifyItemChanged(updatePostion));
                                                        }
                                                    }
                                                }
                                                System.out.println("?????????:" + value + "???.");
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                System.out.println("onError");
                                            }

                                            @Override
                                            public void onComplete() {
                                                System.out.println("onComplete");
                                            }
                                        });
                                    }

                                } else if (file.getFormat() == 1) {
                                    if (!StringUtils.isEmpty(apkPath)) {
                                        ToastUtils.showLong(UpdateActivity.this, "??????????????????????????????????????????..???????????????????????????");
                                        new Thread(() -> loadZip(apkPath, file.getVersionCode())).start();
                                    }
                                }
                            }
                            if (status == File.DOWNLOAD_ERROR) {
                                //?????????????????????
                                file.setStatus(File.DOWNLOAD_PROCEED);
                                file.setSpeed("---");
                                intent = new Intent(LauncherApplication.Companion.getContext(), UpdateService.class);
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
            } else if (type == 2 || type == 3) {
                //????????????
                for (int i = 0; i < otherList.size(); i++) {
                    File file = otherList.get(i);
                    if (intent.getAction().equals(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId() + type)) {
                        String speedS = intent.getStringExtra("speed");
                        String sizeS = intent.getStringExtra("size");
                        String totalSize = intent.getStringExtra("totalSize");
                        int pencent = intent.getIntExtra("percent", 0);
                        int status = intent.getIntExtra("status", 0);
                        String filePath = intent.getStringExtra("filePath");
                        if (status == File.DOWNLOAD_PROCEED) {//???????????????
                            file.setSpeed(speedS);
                            file.setProgress(pencent);
                            file.setSizeStr(sizeS);
                            otherAdapter.notifyItemChanged(i);
                        }
                        if (status == File.DOWNLOAD_COMPLETE) {//??????
                            file.setStatus(File.DOWNLOAD_COMPLETE);
                            file.setProgress(pencent);
                            file.setSizeStr(totalSize);
                            file.setPath(filePath);
                            //??????????????????????????????????????????????????????
                            if (downAllOtherAppThread != null) {
                                synchronized (lockObjectA) {
                                    lockObjectA.notify();
                                }
                            }
                            otherAdapter.notifyItemChanged(i);
                            LauncherApplication.Companion.getDownloadTaskHashMap().remove(file.getId());
                            String apkPath = Environment.getExternalStorageDirectory().getPath() + "/" + file.getFileName();
                            if (file.getFormat() == 2) {

                                //??????????????????100M???????????????
//                                if (getAvailableSize() > 100) {
                                if (ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), apkPath)) {
                                    file.setInstalled(true);
                                    systemAdapter.notifyItemChanged(i);
//                                        sendUpdateBroadcast(file.getPackName());
                                }
//                                } else {
//                                    ToastUtils.showLong(context, "????????????????????????????????????????????????");
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
                    if (status == File.DOWNLOAD_PROCEED) {//???????????????
                        file.setSpeed(speedS);
                        file.setProgress(pencent);
                        file.setSizeStr(sizeS);
                        tvOtaUpdateText.setText("???????????????" + file.getProgress() + "%");
                        pbUpdateOta.setProgress(file.getProgress());
                    }
                    if (status == File.DOWNLOAD_COMPLETE) {//??????
                        file.setStatus(File.DOWNLOAD_COMPLETE);
                        file.setProgress(pencent);
                        file.setSizeStr(totalSize);
                        file.setPath(filePath);
                        tvOtaUpdateText.setText("???????????????100%");
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
                        //??????????????????????????????
                        llBackUpdate.setVisibility(View.VISIBLE);
                        tvOtaAppUpdate.setText("??????");
                        otaInstall = true;
                    }
                    if (status == File.DOWNLOAD_ERROR) {
                        DownloadTask downloadTask = LauncherApplication.Companion.getDownloadTaskHashMap().get(file.getId());
                        if (downloadTask != null) {
                            downloadTask.cancel();
                        }
                        file.setStatus(File.DOWNLOAD_ERROR);
                    }
                }
            }
        }

    }

    private void syncDownloadProgress() {
        int configVersion = (int) SPUtils.getData(
                "configVersion",
                1
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
                    //apk??????
                    a++;
                    Log.e("UpdateActivity", "???" + needUpdateSystemList.size() + "????????????,?????????" + a + "????????????");
                } else if (file.getFormat() == 1 && forceUpdateUnzipFlag) {
                    //???????????????
                    a++;
                    Log.e("UpdateActivity", "???" + needUpdateSystemList.size() + "????????????,?????????" + a + "????????????");
                }
            }
            if (a != 0 && needUpdateSystemList.size() > 0 && a > installApp) {
                installApp = a;
                int progress = installApp * 100 / needUpdateSystemList.size();
                pbUpdateOta.setProgress(progress);
                tvOtaUpdateText.setText("???????????????" + progress + "%");
                Log.i(TAG, "syncDownloadProgress: ???????????????" + progress + "%");
                if (needUpdateSystemList.size() == installApp) {
                    ToastUtils.showShort(this, "????????????????????????");
                    llBackUpdate.setVisibility(View.VISIBLE);
                    //??????????????????
                    MMKV mmkv = LauncherApplication.Companion.getMMKV();
                    mmkv.encode("isStartOtaUpdate", false);
                }
            }
        });
    }

    /**
     * ??????Sdcard???????????????
     *
     * @return MB
     */
    private long getAvailableSize() {
        String sdcard = Environment.getExternalStorageState();
        //????????????sdcard???????????????
        String state = Environment.MEDIA_MOUNTED;
        //??????Sdcard?????????
        //????????????
        java.io.File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        if (sdcard.equals(state)) {
            //??????Sdcard?????????block???size
            long blockSize = statFs.getBlockSize();
            //???????????????????????????Block??????
            long blockavailable = statFs.getAvailableBlocks();
            //???????????????????????????1024???????????????1000?????????
            long blockavailableTotal = blockSize * blockavailable / 1000 / 1000;
            return blockavailableTotal;
        } else {
            return -1;
        }
    }

    /**
     * ????????????scard????????????
     */
    private void checkExtrnalStorage() {
        //???????????????????????????????????? ??????????????????6.0???????????????????????????
        //??????????????????6.0???????????????????????????
        if (Build.VERSION.SDK_INT > 23) {
            //?????????android6.0?????????
            //????????????????????????
            //1.????????????????????????????????????????????????
            String[] permissons = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
            //2.????????????????????????????????????????????????????????????????????????????????????????????????????????????
            ArrayList<String> deniedPermissions = new ArrayList<String>();
            for (String permission : permissons) {
                int i = ContextCompat.checkSelfPermission(this, permission);
                if (PackageManager.PERMISSION_DENIED == i) {
                    //???????????????????????????
                    deniedPermissions.add(permission);
                }
            }
            if (deniedPermissions.size() == 0) {
                //?????????android6.0?????????
                return;
            }
            //?????????????????????????????????????????????????????????????????????????????????????????????toArray??????????????????????????????????????????????????????????????????
            //?????????????????????????????????
            String[] strings = deniedPermissions.toArray(new String[permissons.length]);
            //3.???????????????
            ActivityCompat.requestPermissions(this, strings, REQUEST_CODE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * ????????????????????????
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
