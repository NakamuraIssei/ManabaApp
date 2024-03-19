package com.example.ManabaApp;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

public class NotifyManager2 {
    private static int dataCount;
    private static HashMap<NotificationData, Integer> dataBag;
    private static HashMap<Integer, PendingIntent> pendingIntentBag;
    private static AlarmManager notificationAlarmManager;
    private static ClassUpdateListener classUpdateListener;
    private static Context context;

    static void prepareForNotificationWork(Context context) {
        NotifyManager2.context = context;
        dataCount = 1;
        dataBag = new HashMap<NotificationData, Integer>();
        pendingIntentBag = new HashMap<Integer, PendingIntent>();
        notificationAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    static void setContext(Context context) {
        NotifyManager2.context = context;
        if (notificationAlarmManager == null)
            notificationAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    static void setNotificationListener(ClassUpdateListener listener) {
        classUpdateListener = listener;
    }

    static void setTaskNotificationAlarm(String dataName, int dataId, String title, String subTitle, LocalDateTime notificationTiming) {
        dataBag.put(new NotificationData(dataName, title, subTitle, notificationTiming), dataCount);
        Intent notificationIntent = new Intent(context, NotificationReceiver2.class);
        notificationIntent.setAction(String.valueOf(dataCount));
        notificationIntent.putExtra("DATANAME", dataName);
        notificationIntent.putExtra("DATAID", dataId);
        notificationIntent.putExtra("NOTIFICATIONID", dataCount);
        notificationIntent.putExtra("TITLE", title);
        notificationIntent.putExtra("SUBTITLE", subTitle);
        PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(context, dataCount, notificationIntent, PendingIntent.FLAG_MUTABLE);
        pendingIntentBag.put(dataCount, notificationPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZoneId japanZone = ZoneId.of("Asia/Tokyo");// notificationTiming を日本時間に変換
            Instant japanInstant = notificationTiming.atZone(japanZone).toInstant();// 日本時間のエポックミリ秒を取得
            long japanEpochMilli = japanInstant.toEpochMilli();
            notificationAlarmManager.set(AlarmManager.RTC_WAKEUP, japanEpochMilli, notificationPendingIntent);
        }
        dataCount = (dataCount + 1) % 99999999;
    }

    static void cancelTaskNotificationAlarm(String dataName, String title, String subTitle, LocalDateTime notificationTiming) {
        NotificationData nt = new NotificationData(dataName, title, subTitle, notificationTiming);
        Integer notificationId = dataBag.get(nt);
        Log.d("aaa", dataBag.toString() + "キャンセルする通知番号は" + notificationId + "です。NotifyManager2 64");
        PendingIntent pendingIntent = pendingIntentBag.get(notificationId);
        if (pendingIntent != null) {
            notificationAlarmManager.cancel(pendingIntent); // アラームをキャンセル
        } else
            Log.d("aaa", title + "の通知設定" + notificationTiming.toString() + "をキャンセルできませんでした。NotifyManager2 71");

        dataBag.remove(nt);
        pendingIntentBag.remove(notificationId);
    }

    static void setClassNotificationAlarm(String dataName, int dataId, String title, String subTitle, LocalDateTime notificationTiming) {
        Intent notificationIntent = new Intent(context, NotificationReceiver2.class);
        notificationIntent.setAction(String.valueOf(dataCount));
        notificationIntent.putExtra("DATANAME", dataName);
        notificationIntent.putExtra("DATAID", dataId);
        notificationIntent.putExtra("NOTIFICATIONID", -1);
        notificationIntent.putExtra("TITLE", title);
        notificationIntent.putExtra("SUBTITLE", subTitle);
        PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(context, dataCount, notificationIntent, PendingIntent.FLAG_MUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZoneId japanZone = ZoneId.of("Asia/Tokyo");// notificationTiming を日本時間に変換
            Instant japanInstant = notificationTiming.atZone(japanZone).toInstant();// 日本時間のエポックミリ秒を取得
            long japanEpochMilli = japanInstant.toEpochMilli();
            notificationAlarmManager.set(AlarmManager.RTC_WAKEUP, japanEpochMilli, notificationPendingIntent);
        }
        if (classUpdateListener != null) {
            classUpdateListener.onNotificationReceived(dataId - 1);
        }
        dataCount = (dataCount + 1) % 99999999;
    }

    static void setBackScrapingAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Intent notificationIntent = new Intent(context, NotificationReceiver2.class);
            notificationIntent.setAction(String.valueOf(dataCount));
            notificationIntent.putExtra("DATANAME", "BackScraping");
            PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(context, dataCount, notificationIntent, PendingIntent.FLAG_MUTABLE);


            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextTiming = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

            int currentHour = now.getHour();
            int targetHour = 12;

            if (currentHour < targetHour) {
                // 現在時刻が12時より前の場合、今日の12時に設定
                nextTiming = nextTiming.plusHours(12);
            } else {
                // 現在時刻が12時以降の場合、翌日の0時に設定
                nextTiming = nextTiming.plusDays(1);
            }

            ZoneId japanZone = ZoneId.of("Asia/Tokyo");// notificationTiming を日本時間に変換
            Instant japanInstant = nextTiming.atZone(japanZone).toInstant();// 日本時間のエポックミリ秒を取得
            long japanEpochMilli = japanInstant.toEpochMilli();

            notificationAlarmManager.set(AlarmManager.RTC_WAKEUP, japanEpochMilli, notificationPendingIntent);
        }
        dataCount = (dataCount + 1) % 99999999;
    }

    static void setClassRegistrationAlarm() {
        Intent notificationIntent = new Intent(context, NotificationReceiver2.class);
        notificationIntent.setAction(String.valueOf(dataCount));
        notificationIntent.putExtra("DATANAME", "ClassRegistration");
        PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(context, dataCount, notificationIntent, PendingIntent.FLAG_MUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZoneId japanZone = ZoneId.of("Asia/Tokyo");// notificationTiming を日本時間に変換
            Instant japanInstant = LocalDateTime.now().atZone(japanZone).toInstant();// 日本時間のエポックミリ秒を取得
            long japanEpochMilli = japanInstant.toEpochMilli();
            notificationAlarmManager.set(AlarmManager.RTC_WAKEUP, japanEpochMilli, notificationPendingIntent);
        }
        dataCount = (dataCount + 1) % 99999999;
    }
}
