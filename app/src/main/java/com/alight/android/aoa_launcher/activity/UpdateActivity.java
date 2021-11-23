package com.alight.android.aoa_launcher.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alight.android.aoa_launcher.R;
import com.alight.android.aoa_launcher.application.LauncherApplication;
import com.alight.android.aoa_launcher.common.base.BaseActivity;
import com.alight.android.aoa_launcher.common.bean.UpdateBeanData;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.db.DbHelper;
import com.alight.android.aoa_launcher.net.model.File;
import com.alight.android.aoa_launcher.presenter.PresenterImpl;
import com.alight.android.aoa_launcher.service.UpdateService;
import com.alight.android.aoa_launcher.ui.adapter.UpdateAdapter;
import com.alight.android.aoa_launcher.utils.AppUtils;
import com.alight.android.aoa_launcher.utils.StringUtils;
import com.alight.android.aoa_launcher.utils.ToastUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.alight.android.aoa_launcher.common.constants.AppConstants.AHWCX_PACKAGE_NAME;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.AOA_PACKAGE_NAME;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.AV_PACKAGE_NAME;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.EXTRA_IMAGE_PATH;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.LAUNCHER_PACKAGE_NAME;
import static com.alight.android.aoa_launcher.common.constants.AppConstants.SYSTEM_ZIP_FULL_PATH;

/**
 * 多文件列表下载
 *
 * @author wangzhe
 */
public class UpdateActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MoreDownloadActivity";
    private int REQUEST_CODE_EXTERNAL_STORAGE = 20;
    private RecyclerView recyclerView;
    private UpdateAdapter updateAdapter;
    private ArrayList<File> files = new ArrayList<>();

    private HashMap<String, DownloadReceiver> downloadReceiverMap = new HashMap<>();
    private List<File> list = new ArrayList<>();
    private List<UpdateBeanData> urlList = new ArrayList<>();
    private TextView tvSystemApp;
    private TextView tvOtherApp;
    private View llBackUpdate;
    private TextView tvUpdateAll;
    private Intent serviceIntent;

    @Override
    public void initData() {
        UpdateBeanData systemApp = (UpdateBeanData) getIntent().getSerializableExtra("system");
        systemApp.setType(".zip");
        UpdateBeanData launcherApp = (UpdateBeanData) getIntent().getSerializableExtra("test_apk");
        launcherApp.setPackName(LAUNCHER_PACKAGE_NAME);
        launcherApp.setType(".apk");
        UpdateBeanData aoa = (UpdateBeanData) getIntent().getSerializableExtra("aoa");
        aoa.setPackName(AOA_PACKAGE_NAME);
        aoa.setType(".apk");
        UpdateBeanData ahwc = (UpdateBeanData) getIntent().getSerializableExtra("ahwc");
        ahwc.setPackName(AHWCX_PACKAGE_NAME);
        ahwc.setType(".apk");
        UpdateBeanData av = (UpdateBeanData) getIntent().getSerializableExtra("av");
        av.setPackName(AV_PACKAGE_NAME);
        av.setType(".apk");
        //系统对比VersionName不同则升级
      /*  if (!newSystemVersionName.equals(systemApp.getVersion_name())) {
            systemApp.setApp_name("update");
            urlList.add(systemApp);
        }
        if (localAoaVersionCode < aoa.getVersion_code()) {
            urlList.add(aoa);
        }
        if (localAhwcxVersionCode < ahwc.getVersion_code()) {
            urlList.add(ahwc);
        }
        if (localAvVersionCode < av.getVersion_code()) {
            urlList.add(av);
        }
        if (localLauncherVersionCode < launcherApp.getVersion_code()) {
            urlList.add(launcherApp);
        }
        if (urlList.size() == 0) {
            ToastUtils.showLong(this, "暂无升级");
            finish();
        }*/
        //默认全升级
        urlList.add(ahwc);
//        urlList.add(aoa);
        urlList.add(av);
//        urlList.add(launcherApp);
//        urlList.add(systemApp);

        checkExtrnalStorage();
        getData();

        if (updateAdapter == null) {
            updateAdapter = new UpdateAdapter();
            updateAdapter.addData(list);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(updateAdapter);
        } else {
            updateAdapter.notifyDataSetChanged();
        }
        updateAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.tv_update_item) {
         /*       TextView tvUpdate = (TextView) adapter.getViewByPosition(position, R.id.tv_update_item);
                ProgressBar pbUpdate = (ProgressBar) adapter.getViewByPosition(position, R.id.pb_update_item);
                pbUpdate.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        if (i < 100) {
                            i++;
                            pbUpdate.setProgress(i);
                            tvUpdate.setText(i + "%");
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(runnable);*/
/*
                while (i < 100) {
                    if (i == 100) {
                        tvUpdate.setText("已完成");
                    } else {
                        tvUpdate.setText(i + "%");
                    }
                    pbUpdate.setProgress(i);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
*/
            }
            File file = list.get(position);
            int type = file.getType();
            switch (type) {
                case File.DOWNLOAD_ERROR://出错
                    file.setStatus(File.DOWNLOAD_PROCEED);
                    file.setSpeed("---");
                    updateAdapter.notifyItemChanged(position);
                    if (serviceIntent == null) {
                        serviceIntent = new Intent(UpdateActivity.this, UpdateService.class);
                    }
                    serviceIntent.putExtra("filename", file.getFileName());
                    serviceIntent.putExtra("url", file.getUrl());
                    serviceIntent.putExtra("id", file.getId());
                    serviceIntent.putExtra("seq", file.getSeq());
                    startService(serviceIntent);
                    break;
                default:
                    break;
            }
        });
//        startDownload();
    }

    private void startDownload() {
        for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            file.setSizeStr("请稍等");
            file.setSpeed("");
            if (serviceIntent == null) {
                serviceIntent = new Intent(UpdateActivity.this, UpdateService.class);
            }
            serviceIntent.putExtra("filename", file.getFileName());
            serviceIntent.putExtra("url", file.getUrl());
            serviceIntent.putExtra("id", file.getId());
            serviceIntent.putExtra("seq", file.getSeq());
            startService(serviceIntent);
            file.setStatus(File.DOWNLOAD_PROCEED);
        }
    }

    private void getData() {
        List<String> downloadedFileIds = getDownloadedFileIds();
        Log.d(TAG, "getData: " + downloadedFileIds);
        List<File> fileList = selectDownloadedFiles();
        for (int i = 0; i < urlList.size(); i++) {
            File file = new File();
            file.setId("" + i);
            file.setSeq(i);
            file.setFileName(urlList.get(i).getApp_name() + urlList.get(i).getType());
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
            file.setFileType(".apk");
            file.setUrl(urlList.get(i).getApp_url());
            if (!StringUtils.isEmpty(urlList.get(i).getPackName())) {
                file.setPackName(urlList.get(i).getPackName());
            }
            list.add(file);
            IntentFilter filter = new IntentFilter();
            DownloadReceiver receiver = new DownloadReceiver();
            filter.addAction(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId());
            registerReceiver(receiver, filter);
            downloadReceiverMap.put(file.getId(), receiver);
        }
    }

    private void installSystem(Context context) {
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
        if (list != null) {
            list.clear();
        }
        if (urlList != null) {
            urlList.clear();
        }
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
    }

    @Override
    public void setListener() {
        tvSystemApp.setOnClickListener(this);
        tvOtherApp.setOnClickListener(this);
        llBackUpdate.setOnClickListener(this);
        tvUpdateAll.setOnClickListener(this);
    }

    @Override
    public void initView() {
        tvSystemApp = findViewById(R.id.tv_system_app);
        tvOtherApp = findViewById(R.id.tv_other_app);
        llBackUpdate = findViewById(R.id.ll_back_update);
        tvSystemApp.setSelected(true);
        recyclerView = findViewById(R.id.rv_app_update);
        tvUpdateAll = findViewById(R.id.tv_update_all);
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
            case R.id.tv_system_app:
                tvSystemApp.setSelected(true);
                tvOtherApp.setSelected(false);
                tvUpdateAll.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_other_app:
                tvOtherApp.setSelected(true);
                tvSystemApp.setSelected(false);
                tvUpdateAll.setVisibility(View.GONE);
                break;
            //开始下载
            case R.id.tv_update_all:
                tvUpdateAll.setTextColor(Color.parseColor("#50ffffff"));
                tvUpdateAll.setEnabled(false);
                tvUpdateAll.setClickable(false);
                startDownload();
                break;
            case R.id.ll_back_update:
                finish();
                break;
            default:
        }
    }

    class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            for (int i = 0; i < list.size(); i++) {
                File file = list.get(i);
                if (intent.getAction().equals(AppConstants.LAUNCHER_PACKAGE_NAME + file.getId())) {
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
                        updateAdapter.notifyItemChanged(i);
                    }
                    if (status == File.DOWNLOAD_COMPLETE) {//完成
                        file.setStatus(File.DOWNLOAD_COMPLETE);
                        file.setProgress(pencent);
                        file.setSizeStr(totalSize);
                        file.setPath(filePath);
                        LauncherApplication.Companion.getDownloadTaskHashMap().remove(file.getId());
                        files.add(file);
                        Log.i(TAG, "已下载数量: " + files.size());
                        if (list.size() == files.size()) {
                     /*       for (int j = 0; j < files.size(); j++) {
                                if (files.get(j).getFileName().equals("update.zip")) {
                                    installSystem(context);
                                    continue;
                                }
                                String apkPath = Environment.getExternalStorageDirectory().getPath() + "/" + files.get(j).getFileName();
                                ApkController.slienceInstallWithSysSign(LauncherApplication.Companion.getContext(), apkPath);
//                                ApkController.install(Environment.getExternalStorageDirectory().getPath() + "/" + files.get(j).getFileName(), MoreDownloadActivity.this);
//                                installAPK(MoreDownloadActivity.this, new java.io.File(Environment.getExternalStorageDirectory().getPath() + "/" + files.get(j).getFileName()), false);
                            }*/
                            //todo
                            tvUpdateAll.setTextColor(Color.WHITE);
                            tvUpdateAll.setEnabled(true);
                            tvUpdateAll.setClickable(true);
                            ToastUtils.showLong(UpdateActivity.this, "都下完啦！");
                            updateAdapter.setInstallNotify();
//                            finish();
                        }
                    }
                    if (status == File.DOWNLOAD_ERROR) {
                        LauncherApplication.Companion.getDownloadTaskHashMap().get(file.getId()).cancel();
                        file.setStatus(File.DOWNLOAD_ERROR);
                    }
                }
            }

        }

    }

    public void installAPK(Context context, java.io.File apkFile, boolean closeApp) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(this, "com.alight.android.aoa_launcher.fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            Log.e("TAG", "installAPK: ................uri=" + apkUri);
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
        if (closeApp) {
            android.os.Process.killProcess(android.os.Process.myPid());// 如果不加上这句的话在apk安装完成之后点击单开会崩溃
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;//系统层不做处理 就可以了
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

}
