package com.alight.android.aoa_launcher.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

/**
 * @author : Lss kiwilss
 * @FileName: NotifyUtils
 * @e-mail : kiwilss@163.com
 * @time : 2018/11/21
 * @desc : ${DESCRIPTION}
 */
public class NotifyUtils {

    private Context mContext;
    private NotificationManager mNm;
    private Notification.Builder mBuilder;
    private Notification notification;

    public NotifyUtils(Context context, String channelName, String channelId) {
        mContext = context;
        // 获取系统服务来初始化对象
        mNm = (NotificationManager) mContext
                .getSystemService(Activity.NOTIFICATION_SERVICE);
        //判断是否是8.0,8.0创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//默认聊天高级通知
            mBuilder = new Notification.Builder(mContext,channelId);
            NotificationChannel notificationChannel = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(null, null);
            mNm.createNotificationChannel(notificationChannel);
        }else {
            mBuilder = new Notification.Builder(mContext);
        }

    }


    /**
     * 设置在顶部通知栏中的各种信息
     *
     * @param pendingIntent
     * @param smallIcon
     * @param ticker
     */
    private void setCompatBuilder(PendingIntent pendingIntent, int smallIcon, int largeIcon,
                                  String ticker,
                                  String title, String content, boolean sound, boolean vibrate, boolean lights) {


        mBuilder.setContentIntent(pendingIntent);// 该通知要启动的Intent
        mBuilder.setSmallIcon(smallIcon);// 设置顶部状态栏的小图标
        mBuilder.setTicker(ticker);// 在顶部状态栏中的提示信息

        mBuilder.setContentTitle(title);// 设置通知中心的标题
        mBuilder.setContentText(content);// 设置通知中心中的内容
        mBuilder.setSound(null);
        mBuilder.setWhen(System.currentTimeMillis());
        if (largeIcon != 0){//默认0是没有大图
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), largeIcon));
        }
        /*
         * 将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失,
         * 不设置的话点击消息后也不清除，但可以滑动删除
         */
        mBuilder.setAutoCancel(true);
        // 将Ongoing设为true 那么notification将不能滑动删除
        // notifyBuilder.setOngoing(true);
        /*
         * 从Android4.1开始，可以通过以下方法，设置notification的优先级，
         * 优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
         */
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        /*
         * Notification.DEFAULT_ALL：铃声、闪光、震动均系统默认。
         * Notification.DEFAULT_SOUND：系统默认铃声。
         * Notification.DEFAULT_VIBRATE：系统默认震动。
         * Notification.DEFAULT_LIGHTS：系统默认闪光。
         * notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
         */
        int defaults = 0;

        if (sound) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (vibrate) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (lights) {
            defaults |= Notification.DEFAULT_LIGHTS;
        }

        mBuilder.setDefaults(defaults);
    }

    /**普通的通知
     * @param pendingIntent
     * @param smallIcon
     * @param ticker
     * @param title
     * @param content
     * @param id
     * @param sound
     * @param vibrate
     * @param lights
     */
    public void notifyNormal(PendingIntent pendingIntent, int smallIcon, int largeIcon,
                             String ticker, String title, String content, int id,
                             boolean sound, boolean vibrate, boolean lights) {
        setCompatBuilder(pendingIntent, smallIcon, largeIcon, ticker, title, content, sound, vibrate, lights);
        sent(id);
    }

    /**
     * 自定义视图的通知(默认64dp)
     *
     * @param remoteViews
     * @param pendingIntent
     * @param smallIcon
     * @param ticker
     */
    public void notifyCustomView(RemoteViews remoteViews, PendingIntent pendingIntent,
                                 int smallIcon, int largeIcon, String ticker, int id,
                                 boolean sound, boolean vibrate, boolean lights) {

        setCompatBuilder(pendingIntent, smallIcon, largeIcon, ticker, null, null, sound, vibrate, lights);

        notification = mBuilder.build();
        notification.contentView = remoteViews;
        // 发送该通知
        mNm.notify(id, notification);
    }

    /**自定义视图的通知
     * @param remoteViews
     * @param pendingIntent
     * @param smallIcon
     * @param ticker
     * @param id
     * @param sound
     * @param vibrate
     * @param lights
     */
    public void notifyCustomView2(RemoteViews remoteViews, PendingIntent pendingIntent,
                                  int smallIcon, int largeIcon, String ticker, int id,
                                  boolean sound, boolean vibrate, boolean lights){

        setCompatBuilder(pendingIntent, smallIcon, largeIcon, ticker, null, null, sound, vibrate, lights);

        Notification notification = mBuilder.build();
        notification.bigContentView=remoteViews;
        mNm.notify(id, notification);
    }

    /**
     * 容纳大图片的通知
     *
     * @param pendingIntent
     * @param smallIcon
     * @param ticker
     * @param title
     * @param bigPic
     */
    public void notifyBigPic(PendingIntent pendingIntent, int smallIcon, int largeIcon, String ticker,
                             String title, String content, int bigPic, int id, boolean sound, boolean vibrate, boolean lights) {

        setCompatBuilder(pendingIntent, smallIcon, largeIcon, ticker, title, null, sound, vibrate, lights);
        Notification.BigPictureStyle picStyle = new Notification.BigPictureStyle();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                bigPic, options);
        picStyle.bigPicture(bitmap);
        picStyle.bigLargeIcon(bitmap);
        mBuilder.setContentText(content);
        mBuilder.setStyle(picStyle);
        sent(id);
    }

    /**
     * 可以容纳多行提示文本的通知信息 (因为在高版本的系统中才支持，所以要进行判断)
     *
     * @param pendingIntent
     * @param smallIcon
     * @param ticker
     * @param title
     * @param content
     */
    public void notifyNormailMoreline(PendingIntent pendingIntent, int smallIcon, int largeIcon,
                                      String ticker,
                                      String title, String content, int id, boolean sound, boolean vibrate, boolean lights) {

        final int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            notifyNormal(pendingIntent, smallIcon, largeIcon,ticker, title, content, id, sound, vibrate, lights);
        } else {
            setCompatBuilder(pendingIntent, smallIcon, largeIcon,ticker, title,content,true, true, false);

            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle()
                    .bigText(content);
            mBuilder.setStyle(bigTextStyle);
            sent(id);
        }
    }

    /**
     * 里面有两个按钮的通知
     *
     * @param smallIcon
     * @param leftbtnicon
     * @param lefttext
     * @param leftPendIntent
     * @param rightbtnicon
     * @param righttext
     * @param rightPendIntent
     * @param ticker
     * @param title
     * @param content
     */
    public void notifyButton(int smallIcon, int largeIcon, int leftbtnicon, String lefttext, PendingIntent leftPendIntent,
                             int rightbtnicon, String righttext, PendingIntent rightPendIntent, String ticker,
                             String title, String content, int id, boolean sound, boolean vibrate, boolean lights) {


        setCompatBuilder(rightPendIntent, smallIcon, largeIcon, ticker, title, content, sound, vibrate, lights);
        mBuilder.addAction(leftbtnicon,
                lefttext, leftPendIntent);
        mBuilder.addAction(rightbtnicon,
                righttext, rightPendIntent);
        sent(id);
    }

    public void notifyHeadUp(PendingIntent pendingIntent, int smallIcon, int largeIcon,
                             String ticker, String title, String content, int leftbtnicon, String lefttext,
                             PendingIntent leftPendingIntent, int rightbtnicon, String righttext,
                             PendingIntent rightPendingIntent, int id,
                             boolean sound, boolean vibrate, boolean lights) {

        setCompatBuilder(pendingIntent, smallIcon, largeIcon,ticker, title, content, sound, vibrate, lights);
        //mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), largeIcon));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.addAction(leftbtnicon,
                    lefttext, leftPendingIntent);
            mBuilder.addAction(rightbtnicon,
                    righttext, rightPendingIntent);
        } else {
            Toast.makeText(mContext, "版本低于Andriod5.0，无法体验HeadUp样式通知", Toast.LENGTH_SHORT).show();
        }
        sent(id);
    }

    /**
     * 发送通知
     */
    private void sent(int id) {
        notification = mBuilder.build();
        // 发送该通知
        mNm.notify(id, notification);
    }

    public void clear() {
        // 取消通知
        mNm.cancelAll();
    }
    public void removeId(int id){
        //id不存在会崩溃
        mNm.cancel(id);
    }
}
