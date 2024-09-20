package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {
    public static TaskDataManager taskDataManager;
    private static ClassUpdateListener classUpdateListener;
    public NotificationManager notificationManager;
    public SQLiteDatabase db;
    public Cursor cursor;

    public static void setClassUpdateListener(ClassUpdateListener listener) {
        classUpdateListener = listener;
    }
    public static void setTaskDataManager(TaskDataManager taskDataManager) {
        NotificationReceiver.taskDataManager = taskDataManager;
    }
    public void onReceive(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String dataName = intent.getStringExtra("DATANAME");
            int notificationId = intent.getIntExtra("NOTIFICATIONID", 0);
            String title = intent.getStringExtra("TITLE");
            String subTitle = intent.getStringExtra("SUBTITLE");
            Log.d("className", title + "///NotificationReceiver2 ");
            //DB処理作業
            MyDBHelper myDBHelper = new MyDBHelper(context);
            db = myDBHelper.getWritableDatabase();

            switch (Objects.requireNonNull(dataName)) {
                case "TaskData":
                    pushNotification(title, subTitle, context, notificationId);
                    String dataId = intent.getStringExtra("DATAID");
                    taskDataWork(title, subTitle, dataId);
                    break;
                case "ClassData":
                    int dayAndPeriod=intent.getIntExtra("DAYANDPERIOD",0);
                    if(checkNotify(dayAndPeriod))
                    pushNotification(title, subTitle, context, notificationId);
                    if (classUpdateListener != null) {
                        classUpdateListener.onNotificationReceived(title);
                    }
                    break;
                case "BackScraping":
                    backScraping(context);
                    break;
            }
        }

    }
    public void pushNotification(String title, String subTitle, Context context, int notificationId) {
        //通知作業
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("classname", title + "NotificationReceiver2 pushNotification");
            if (notificationManager == null)
                notificationManager = context.getSystemService(NotificationManager.class);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel                              // ・・・(2)
                    = null;

            channel = new NotificationChannel(String.valueOf(notificationId), "サンプルアプリ", importance);

            channel.setDescription("説明・説明 ここに通知の説明を書くことができます");


            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(context, String.valueOf(notificationId))     // ・・・(4)

                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle(String.valueOf(title))
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

            this.notificationManager.notify((int) notificationId, builder.build());
            wakeLock.release();
        }
    }
    public void taskDataWork(String title, String subTitle, String dataId) {
        String[] tdColumns = {"notificationTiming"}; // 取り出したいカラム
        String tdSelection = "taskId = ?"; // WHERE句
        String[] tdSelectionArgs = {dataId}; // WHERE句の引数
        cursor = db.query("TaskData", tdColumns, tdSelection, tdSelectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String notificationTime = cursor.getString(cursor.getColumnIndex("notificationTiming"));

            // 最初の '?' の位置を見つける
            int firstQuestionMarkIndex = notificationTime.indexOf('?');

            if (firstQuestionMarkIndex != -1) {
                // 最初の '?' 以降の部分を取得
                String modifiedString = notificationTime.substring(firstQuestionMarkIndex + 1);
                ContentValues values = new ContentValues();
                values.put("notificationTiming", modifiedString);
                db.update("TaskData", values, tdSelection, tdSelectionArgs);
            } else {
                ContentValues values = new ContentValues();
                values.put("notificationTiming", "");
                db.update("TaskData", values, tdSelection, tdSelectionArgs);
            }

            cursor.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        if (taskDataManager != null)
            taskDataManager.deleteFinishedTaskNotification(title, subTitle);
    }
    public void backScraping(Context context) {
        NotifyManager.prepareForNotificationWork(context);
        NotifyManager.setBackScrapingAlarm();

        CookieManager ck = CookieManager.getInstance();
        String cookies = ck.getCookie("https://ct.ritsumei.ac.jp/ct/home_summary_report");
        HashMap<String, String> cookieBag = new HashMap<>();

        if (cookies != null) {//取ってきたクッキーが空でなければ
            //クッキーバッグになんか残ってたら嫌やから空っぽにしておく
            String[] cookieList = cookies.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
            for (String cookie : cookieList) {//cookieListの中身でループを回す
                String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
            }
        }
        ManabaScraper.setCookie(cookieBag);
        Log.d("aaa", "今からバックグラウンドでのスクレイピングします。　NotificationReceiver2 209");
        TaskDataManager taskDataManager = new TaskDataManager();
        ClassDataManager classDataManager = new ClassDataManager();
        cursor = db.query("TaskData", null, null, null, null, null, "taskId");
        taskDataManager.setDB(db, cursor);
        cursor = db.query("ClassData", null, null, null, null, null, "classId");
        classDataManager.setDB(db, cursor);
        classDataManager.loadClassData();

        taskDataManager.loadTaskData();
        taskDataManager.setTaskDataIntoRegisteredClassData();
        taskDataManager.sortAllTaskDataList();
        taskDataManager.makeAllTasksSubmitted();
        taskDataManager.getTaskDataFromManaba();
    }
    public Boolean checkNotify(int dayAndPeriod) {
        // DB内のdayAndPeriodの授業の通知設定(isNotifyingを確認)
        String[] tdColumns = {"isNotifying"}; // 取り出したいカラム
        String tdSelection = "dayAndPeriod = ?"; // WHERE句
        String[] tdSelectionArgs = {String.valueOf(dayAndPeriod)}; // WHERE句の引数
        cursor = db.query("ClassData", tdColumns, tdSelection, tdSelectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int isNotifying = cursor.getInt(cursor.getColumnIndex("isNotifying"));
            cursor.close();
            Log.d("notify","うまく取り出せました"+dayAndPeriod+"時間目"+isNotifying);
            return isNotifying == 1;
        } else {
            if (cursor != null) {
                cursor.close();
            }
            Log.d("notify","あかんかったわ");
            return false;
        }
    }

}
