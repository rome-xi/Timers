package com.xyb.timers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StopwatchVibrationService extends Service {
    private static final long[] PATTERN = {0, 1000, 500};  // 震动模式：0ms 静止，1000ms 震动，500ms 静止

    private Vibrator vibrator;
    private boolean isVibrating;

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 将服务设置为 Foreground Service，以便在后台显示通知
        Notification notification = createNotification();
        startForeground(1, notification);

        // 启动震动
        if (vibrator != null && !isVibrating) {
            vibrator.vibrate(PATTERN, 0);
            isVibrating = true;
        }

        // 返回 START_STICKY 以便系统在服务被杀死后能够自动重启
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止震动
        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        // 创建通知
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("一个人的针扎好啦！")
                .setContentText("Stopwatch is vibrating")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channel.getId());
        }

        return builder.build();
    }
}
