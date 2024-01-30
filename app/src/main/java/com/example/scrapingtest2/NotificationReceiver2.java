package com.example.scrapingtest2;

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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class NotificationReceiver2  extends BroadcastReceiver {
    public NotificationManager notificationManager;
    public SQLiteDatabase db;
    public Cursor cursor;
    public static TaskDataManager taskDataManager;


    public void onReceive(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String dataName = intent.getStringExtra("DATANAME");
            int dataId = intent.getIntExtra("DATAID", 0);
            int notificationId = intent.getIntExtra("NOTIFICATIONID", 0);
            String title = intent.getStringExtra("TITLE");
            String subTitle = intent.getStringExtra("SUBTITLE");


            //DB処理作業
            MyDBHelper myDBHelper = new MyDBHelper(context);
            db = myDBHelper.getWritableDatabase();


            switch (Objects.requireNonNull(dataName)) {
                case "TaskData":
                    pushNotification(title,subTitle,context,notificationId);
                    taskDataWork(title,subTitle,dataId);
                    break;
                case "ClassData":
                    pushNotification(title,subTitle,context,notificationId);
                    classDataWork(context,dataId);
                    break;
                case "BackScraping":
                    backScraping(context);
                    break;
            }
        }

    }
    static void setTaskDataManager(TaskDataManager taskDataManager){
        NotificationReceiver2.taskDataManager=taskDataManager;
    }
    private void pushNotification(String title,String subTitle,Context context,int notificationId){
        //通知作業
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            //Log.d("aaa", "notifymap"+this.notificationMap.toString());
            this.notificationManager.notify((int) notificationId, builder.build());
            wakeLock.release();
        }
    }
    private void taskDataWork(String title,String subTitle,int dataId){
        String[] tdColumns = {"notificationTiming"}; // 取り出したいカラム
        String tdSelection = "taskId = ?"; // WHERE句
        String[] tdSelectionArgs = {String.valueOf(dataId)}; // WHERE句の引数
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
    private void classDataWork(Context context,int dataId){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // 次の授業の行を取得するクエリ
            String selectQuery = "SELECT * FROM ClassData WHERE classId = " + (dataId + 1) % 49;
            cursor = db.rawQuery(selectQuery, null);

            // カーソルからデータを取得
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int classId = cursor.getInt(cursor.getColumnIndex("classId"));
                @SuppressLint("Range") String nextClass = cursor.getString(cursor.getColumnIndex("className"));
                @SuppressLint("Range") String nextRoom = cursor.getString(cursor.getColumnIndex("classRoom"));

                Log.d("ClassData", "classId: " + classId + ", nextClass: " + nextClass + ", nextRoom: " + nextRoom + "NotificationReceiver2 180");


                // 今日の0時0分を取得
                LocalDateTime nextTiming = null;

                nextTiming = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);


                switch (classId % 7) {
                    case 0:
                        nextTiming = nextTiming.plusDays(1).plusHours(8).plusMinutes(30);
                        break;
                    case 1:
                        nextTiming = nextTiming.plusHours(10).plusMinutes(10);
                        break;
                    case 2:
                        nextTiming = nextTiming.plusHours(12).plusMinutes(30);
                        break;
                    case 3:
                        nextTiming = nextTiming.plusHours(14).plusMinutes(10);
                        break;
                    case 4:
                        nextTiming = nextTiming.plusHours(15).plusMinutes(50);
                        break;
                    case 5:
                        nextTiming = nextTiming.plusHours(17).plusMinutes(30);
                        break;
                    case 6:
                        nextTiming = nextTiming.plusHours(19).plusMinutes(10);
                        break;
                }

                // カーソルを閉じる
                cursor.close();

                NotifyManager2.setContext(context);
                NotifyManager2.setClassNotificationAlarm("ClassData", classId, nextClass, nextRoom, nextTiming);

            }
        }
    }

    private void backScraping(Context context){
        NotifyManager2.prepareForNotificationWork(context);
        NotifyManager2.setBackScrapingAlarm();

        CookieManager ck=CookieManager.getInstance();
        String cookies=ck.getCookie("https://ct.ritsumei.ac.jp/ct/home_summary_report");
        HashMap<String, String> cookieBag=new HashMap<>();

        if (cookies != null) {//取ってきたクッキーが空でなければ
            //クッキーバッグになんか残ってたら嫌やから空っぽにしておく
            String[] cookieList = cookies.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
            for (String cookie : cookieList) {//cookieListの中身でループを回す
                Log.d("aaa", cookie.trim());
                String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
            }
        }
        ManabaScraper.setCookie(cookieBag);
        Log.d("aaa", "今からバックグラウンドでのスクレイピングするよ　NotificationReceiver2 209");
            TaskDataManager taskDataManager=new TaskDataManager("TaskData");
            ClassDataManager classDataManager=new ClassDataManager("ClassData");
            cursor = db.query("TaskData", null, null, null, null, null, "taskId");
            taskDataManager.setDB(db,cursor);
            cursor = db.query("ClassData", null, null, null, null, null, "classId");
            classDataManager.setDB(db,cursor);
            classDataManager.loadClassData();

            taskDataManager.loadTaskData();
            taskDataManager.setTaskDataIntoClassData();
            taskDataManager.sortAllTaskDataList();
            taskDataManager.makeAllTasksSubmitted();
            taskDataManager.getTaskDataFromManaba();
    }
}

