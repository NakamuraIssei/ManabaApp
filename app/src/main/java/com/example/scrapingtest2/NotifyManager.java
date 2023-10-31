package com.example.scrapingtest2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.HashMap;

public class NotifyManager{
    public static NotificationManager n1;
    public NotificationManager n2;
    private static Context context;

    public NotifyManager(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            n2 = context.getSystemService(NotificationManager.class);
        }
        NotifyManager.context = context;
    }
    public static void notifyClassInfor(Context context, String classname, String classroom){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {        // ・・・(1)
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel                              // ・・・(2)
                    = new NotificationChannel(String.valueOf(2147483647), "サンプルアプリ", importance);

            channel.setDescription("説明・説明 ここに通知の説明を書くことができます");

            n1 = context.getSystemService(NotificationManager.class);
            n1.createNotificationChannel(channel);

            NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(context, String.valueOf(2147483647))     // ・・・(4)

                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle(classname)
                    .setContentText(classroom)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true); // 通知が自動的にキャンセルされる

            NotificationManagerCompat notificationManager
                    = NotificationManagerCompat.from(context);

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag"
            );
            wakeLock.acquire();
            Log.d("aaa", "notifyId"+String.valueOf(2147483647));
            //Log.d("aaa", "notifymap"+this.notificationMap.toString());
            n1.notify(2147483647, builder.build());
            wakeLock.release();
        }
    }
}
