package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class NotificationReceiver2  extends BroadcastReceiver {
    public NotificationManager notificationManager;
    public SQLiteDatabase db;

    public void onReceive(Context context, Intent intent) {
        CookieManager ck=CookieManager.getInstance();
        String cookies=ck.getCookie("https://ct.ritsumei.ac.jp/ct/home_summary_report");
        HashMap<String, String> cookieBag=new HashMap<>();

        if (cookies != null) {//取ってきたクッキーが空でなければ
            Log.d("aaa",cookies);
            //クッキーバッグになんか残ってたら嫌やから空っぽにしておく
            String[] cookieList = cookies.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
            for (String cookie : cookieList) {//cookieListの中身でループを回す
                Log.d("aaa", cookie.trim());
                String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
            }

        }
        ManabaScraper manabaScraper=new ManabaScraper(cookieBag);
        Log.d("aaa", "今からバックグラウンドでのスクレイピングするよ　NotificationReceiver2 44");
        try {
            Log.d("aaa",manabaScraper.getTaskDataFromManaba().toString()+"NotificationReceiver2 45");
        } catch (ExecutionException e) {
            Log.d("aaa","バックグラウンドでのスクレイピング失敗。NotificationReceiver2 47");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","バックグラウンドでのスクレイピング失敗。NotificationReceiver2 49");
            throw new RuntimeException(e);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, NotificationReceiver2.class);
        newIntent.setAction(String.valueOf(500));
        newIntent.putExtra("DATANAME","");
        newIntent.putExtra("DATAID",1);
        newIntent.putExtra("NOTIFICATIONID",300);
        newIntent.putExtra("TITLE","固有値");
        newIntent.putExtra("SUBTITLE","2023-10-30 10:30");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerTime = System.currentTimeMillis() + (60 * 1000); // 現在の時間から5分後
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }

        String dataName = intent.getStringExtra("DATANAME");
        int dataId = intent.getIntExtra("DATAID",0);
        int notificationId = intent.getIntExtra("NOTIFICATIONID",0);
        String title = intent.getStringExtra("TITLE");
        String subTitle = intent.getStringExtra("SUBTITLE");

        //通知作業
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {        // ・・・(1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(notificationManager ==null)
                    notificationManager = context.getSystemService(NotificationManager.class);
            }
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel                              // ・・・(2)
                    = new NotificationChannel(String.valueOf(notificationId), "サンプルアプリ", importance);

            channel.setDescription("説明・説明 ここに通知の説明を書くことができます");


            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(context, String.valueOf(notificationId))     // ・・・(4)

                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle(title)
                    .setContentText(subTitle)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager
                    = NotificationManagerCompat.from(context);

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag"
            );
            wakeLock.acquire();

            //Log.d("aaa", "notifymap"+this.notificationMap.toString());
            this.notificationManager.notify((int) notificationId, builder.build());
            wakeLock.release();
        }
        //DB処理作業
        MyDBHelper myDBHelper = new MyDBHelper(context);
        db = myDBHelper.getWritableDatabase();

        String[] columns = {"notificationTiming"}; // 取り出したいカラム
        String selection = "myId = ?"; // WHERE句
        String[] selectionArgs = {String.valueOf(dataId)}; // WHERE句の引数
        Cursor cursor = db.query(dataName, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String notificationTime = cursor.getString(cursor.getColumnIndex("notificationTiming"));

            // 最初の '?' の位置を見つける
            int firstQuestionMarkIndex = notificationTime.indexOf('?');

            if (firstQuestionMarkIndex != -1) {
                // 最初の '?' 以降の部分を取得
                String modifiedString = notificationTime.substring(firstQuestionMarkIndex + 1);
                ContentValues values = new ContentValues();
                values.put("notificationTiming", modifiedString);
                db.update(dataName, values, selection, selectionArgs);
            }
            else{
                ContentValues values = new ContentValues();
                values.put("notificationTiming", "");
                db.update(dataName, values, selection, selectionArgs);
            }

            cursor.close();
        }



        if (cursor != null) {
            cursor.close();
        }

        switch(dataName){
            case "TaskData":
                TaskDataManager.deleteFinishedTaskNotification(title,subTitle);
                break;
        }

    }
}

