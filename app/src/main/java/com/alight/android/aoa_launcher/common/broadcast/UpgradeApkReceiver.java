package com.alight.android.aoa_launcher.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alight.android.aoa_launcher.activity.LauncherActivity;
import com.alight.android.aoa_launcher.common.bean.BaseBean;
import com.alight.android.aoa_launcher.common.bean.UploadImage;
import com.alight.android.aoa_launcher.common.constants.AppConstants;
import com.alight.android.aoa_launcher.common.event.UpdateEvent;
import com.alight.android.aoa_launcher.net.urls.Urls;
import com.alight.android.aoa_launcher.utils.AccountUtil;
import com.alight.android.aoa_launcher.utils.BitmapUtil;
import com.alight.android.aoa_launcher.utils.NetUtils;
import com.alight.android.aoa_launcher.utils.SPUtils;
import com.alight.android.aoa_launcher.utils.StringUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 接收用户安装的广播
 */
public class UpgradeApkReceiver extends BroadcastReceiver {

    // context 上下文对象 intent 接收的意图对象
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        Log.i("UpgradeApkReceiver", "安装完成: packageName = " + packageName);
        EventBus.getDefault().post(UpdateEvent.getInstance(packageName));
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            Drawable drawable = applicationInfo.loadIcon(context.getPackageManager());
            String appName = applicationInfo.loadLabel(context.getPackageManager()).toString();
            uploadIcon(drawableToBitmap(drawable), appName, packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            if (packageName.equals(AppConstants.AHWCX_PACKAGE_NAME)) {
                Intent myIntent = new Intent();
                ComponentName componentName =
                        new ComponentName(AppConstants.AHWCX_PACKAGE_NAME, AppConstants.AHWCX_SERVICE_NAME);
                myIntent.setComponent(componentName);
                context.startService(myIntent);
            } else if (packageName.equals(AppConstants.LAUNCHER_PACKAGE_NAME)) {
                try {
                    Intent myIntent = new Intent(context, LauncherActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(myIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return bitmapDrawable.getBitmap();
    }

    private final OkHttpClient client = new OkHttpClient();

    public void uploadImagePost(String bitmap2StrByBase64, String appName, String packName) {
        String token = (String) SPUtils.getData(
                AppConstants.AOA_LAUNCHER_USER_INFO_TOKEN,
                ""
        );
        String url = "https://test.api.alight-sys.com/oss_sts/v1/upload_icon";
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("icon_base64", bitmap2StrByBase64);
        formBodyBuilder.add("icon_name", appName);
        if (StringUtils.isEmpty(token)) return;
        Request request = new Request.Builder().addHeader("ACToken", token).url(url).post(formBodyBuilder.build()).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println(result);
                if (result != null && result.length() > 0) {
                    UploadImage uploadImage = new Gson().fromJson(result, UploadImage.class);
                    postInstallApk(uploadImage.getData().getUrl(), appName, packName);
                }
            }
        });
    }

    private void uploadIcon(Bitmap bitmap, String appName, String packageName) {
        String bitmap2StrByBase64 = BitmapUtil.Bitmap2StrByBase64(bitmap);
        uploadImagePost(bitmap2StrByBase64, appName, packageName);
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("icon_base64", bitmap2StrByBase64);
//        hashMap.put("icon_name", appName);
//        RequestBody requestBody = RequestBody.create(MultipartBody.FORM, new Gson().toJson(hashMap));
    /*    MultipartBody requestBody2 = new MultipartBody.Builder()
                .setType(MultipartBody.FORM).
                addFormDataPart("icon_base64", bitmap2StrByBase64).
                addFormDataPart("icon_name", appName).build();

        NetUtils.Companion.getIntance().postInfo(
                Urls.UPLOAD_ICON,
                requestBody2, UploadImage.class, new NetUtils.NetCallback() {
                    @Override
                    public void onSuccess(@NonNull Object any) {
                        if (any != null && any instanceof UploadImage) {
                            UploadImage uploadImage = (UploadImage) any;
                            postInstallApk(uploadImage.getData().getUrl(), appName, packageName);
                        }
                    }

                    @Override
                    public void onError(@NonNull String error) {

                    }
                });
*/

/*
        NetUtils.Companion.getIntance().uploadIcon(
                Urls.DEVICE_INSTALL, imgPath, UploadImage.class,
                new NetUtils.NetCallback() {
                    @Override
                    public void onSuccess(@NonNull Object any) {
                        if (any != null && any instanceof UploadImage) {
                            UploadImage uploadImage = (UploadImage) any;
                            postInstallApk(uploadImage.getData().getUrl(), appName, packageName);
                        }
                    }

                    @Override
                    public void onError(@NonNull String error) {

                    }
                });
*/
    }


    private void postInstallApk(String imgUrl, String appName, String packageName) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("package_name", packageName);
        hashMap.put("app_name", appName);
        hashMap.put("logo", imgUrl);
        hashMap.put("dsn", AccountUtil.INSTANCE.getDSN());
        NetUtils.Companion.getIntance().postInfo(
                Urls.DEVICE_INSTALL,
                RequestBody.create(null, new Gson().toJson(hashMap)), BaseBean.class, new NetUtils.NetCallback() {
                    @Override
                    public void onSuccess(@NonNull Object any) {
                        Log.i("UpgradeApkReceiver", "onSuccess: " + any);
                    }

                    @Override
                    public void onError(@NonNull String error) {
                        Log.i("UpgradeApkReceiver", "onError: " + error);
                    }
                });
    }
}