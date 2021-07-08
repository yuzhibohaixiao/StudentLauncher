package com.alight.android.aoa_launcher.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alight.android.aoa_launcher.apiservice.Apiservice;
import com.alight.android.aoa_launcher.listener.DownloadListener;
import com.alight.android.aoa_launcher.urls.Urls;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author wangzhe
 * 提供下载进度的工具类
 */
public class DownloadUtil {

    public static void download(String url, final String path, final DownloadListener downloadListener) {
       /* if (TextUtils.isEmpty(url))
            return;
        String[] urls = url.split("//");
        for (int i = 0; i < urls.length; i++) {
            if (TextUtils.isEmpty(urls[i])) {
                if (TextUtils.isEmpty(url))
                    return;
            }
        }*/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://alight-apk.oss-cn-beijing.aliyuncs.com/")
                //通过线程池获取一个线程，指定callback在子线程中运行。
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .build();

        Apiservice service = retrofit.create(Apiservice.class);

        Call<ResponseBody> call = service.download("update.zip");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                //将Response写入到从磁盘中，详见下面分析
                //注意，这个方法是运行在子线程中的
                if (response != null) {
                    writeResponseToDisk(path, response, downloadListener);
                } else {
                    Log.i("DownloadUtil", "onResponse: 错误的返回下载格式");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                downloadListener.onFail("网络错误～");
            }
        });
    }

    private static void writeResponseToDisk(String path, Response<ResponseBody> response, DownloadListener downloadListener) {
        //从response获取输入流以及总大小
        writeFileFromIS(new File(path), response.body().byteStream(), response.body().contentLength(), downloadListener);
    }

    private static int sBufferSize = 8192;

    //将输入流写入文件
    private static void writeFileFromIS(File file, InputStream is, long totalLength, DownloadListener downloadListener) {
        //开始下载
        downloadListener.onStart();

        //创建文件
        if (!file.exists()) {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                downloadListener.onFail("createNewFile IOException");
            }
        }

        OutputStream os = null;
        long currentLength = 0;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte data[] = new byte[sBufferSize];
            int len;
            while ((len = is.read(data, 0, sBufferSize)) != -1) {
                os.write(data, 0, len);
                currentLength += len;
                //计算当前下载进度
                downloadListener.onProgress((int) (100 * currentLength / totalLength));
            }
            //下载完成，并返回保存的文件路径
            downloadListener.onFinish(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            downloadListener.onFail("IOException");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}