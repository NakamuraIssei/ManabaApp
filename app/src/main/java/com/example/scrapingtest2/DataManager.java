package com.example.scrapingtest2;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class DataManager {

    protected String dataName;//継承先クラスのコンストラクタで設定！
    protected int dataCount;
    protected static ArrayList<ClassData> classDataList;
    protected SQLiteDatabase db;
    protected Cursor cursor;
    protected DateTimeFormatter formatter;

    // 0~48 授業用のid
    // 49~ 課題用のid

    public void prepareForWork(String DataName){//インスタンスを生成した時に使う初期化用のメゾッド
        dataName=DataName;
        dataCount =0;
        classDataList =new ArrayList<ClassData>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN);
        }
    };
    public void setDB(SQLiteDatabase DB, Cursor Cursor){// データベースを渡す
        db=DB;
        cursor=Cursor;
    }
//    public void loadData(){//データベースからデータを読み込んで、dataListに追加
//        Log.d("aaa","今からデータをロードします。DataManager 41");
//        while (cursor.moveToNext()) {
//            @SuppressLint("Range")int datacount = cursor.getInt(cursor.getColumnIndex("myId"));
//            @SuppressLint("Range")String title = cursor.getString(cursor.getColumnIndex("title"));
//            @SuppressLint("Range")String subTitle = cursor.getString(cursor.getColumnIndex("subTitle"));
//            @SuppressLint("Range")String notificationTiming = cursor.getString(cursor.getColumnIndex("notificationTiming"));
//            TaskData taskData =new TaskData(datacount,title,subTitle);
//            dataCount=(datacount+1)%99999999;
//            Log.d("aaa",datacount+"番目の"+ taskData.getTaskName()+"をロードしました。DataManager 50");
//            if(!Objects.equals(notificationTiming, "")){
//                String[] parts = notificationTiming.split("\\?"); // ? をエスケープして使用
//                for (String time : parts) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        Log.d("aaa",notificationTiming+"を設定します。DataManager 55");
//                        taskData.addNotificationTiming(LocalDateTime.parse(time, formatter));
//                    }
//                }
//            }
//            for(LocalDateTime nt: taskData.getNotificationTiming()){
//                requestSettingNotification(dataName, taskData.getTaskId(), taskData.getTaskName(), taskData.getDueDate(),nt);
//            }
//            taskDataList.add(taskData);
//        }
//        Log.d("aaa","データロード完了!。DataManager 65");
//    }
//    public void addData(String title,String subTitle){
//        TaskData taskData =new TaskData(dataCount,title,subTitle);
//        dataCount =(dataCount +1)%99999999;
//        classDataList.add(taskData);
//        //insertDataIntoDB(taskData);
//    }
//  public void addData(String title,String subTitle,LocalDateTime defaultTiming){
//        Log.d("aaa","データを追加します。DataManager 77");
//        TaskData taskData =new TaskData(dataCount,title,subTitle);
//        Log.d("aaa","データを作成しました。DataManager 79");
//        dataCount =(dataCount +1)%99999999;
//        taskData.addNotificationTiming(defaultTiming);
//        Log.d("aaa", taskData.getNotificationTiming().toString()+"デフォルトの通知を依頼します。DataManager 84");
//        requestSettingNotification(dataName, taskData.getTaskId(),title,subTitle,defaultTiming);
//        Log.d("aaa", taskData.getNotificationTiming().toString()+"デフォルトの通知を依頼しました。DataManager 86");
//        classDataList.add(taskData);
//        insertDataIntoDB(taskData);
//    }
//    public void removeData(int num){
//        for(LocalDateTime notificationTime: classDataList.get(num).getNotificationTiming()){
//            requestCancelNotification(dataName, classDataList.get(num).getTaskName(), classDataList.get(num).getDueDate(),notificationTime);
//        }
//        deleteDataFromDB(classDataList.get(num));
//        classDataList.remove(num);
//    }
//    public void deleteDataFromDB(TaskData taskData){
//        String[] whereArgs = { String.valueOf(taskData.getTaskId()) };
//        db.delete(dataName, "myId = ?", whereArgs);
//    }
//    public void addNotificationTiming(int num,LocalDateTime notificationTiming){
//        classDataList.get(num).addNotificationTiming(notificationTiming);
//        requestSettingNotification(dataName, classDataList.get(num).getTaskId(), classDataList.get(num).getTaskName(), classDataList.get(num).getDueDate(),notificationTiming);
//        updateNotificationTimingFromDB(classDataList.get(num));
//    }
//    public void updateNotificationTimingFromDB(TaskData taskData){
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            StringBuilder sb = new StringBuilder();
//            for (LocalDateTime nt : taskData.getNotificationTiming()) {
//                String dateTimeStr = nt.format(formatter);
//                // StringBuilderを使って文字列に追加
//                sb.append(dateTimeStr);
//                // エントリ間に '?' を追加
//                sb.append('?');
//            }
//            if (sb.length() > 0) {
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            // 最終的な文字列を取得
//            String finalString = sb.toString();
//
//            ContentValues values = new ContentValues();
//            values.put("notificationTiming", finalString);
//
//            // WHERE 句を設定（どのレコードを更新するか）
//            String whereClause = "myId=?";
//            String[] whereArgs = { String.valueOf(taskData.getTaskId()) };
//
//            // レコードを更新
//            Log.d("aaa",finalString+"を書き込みます(DataManager.updateNotificationDataFromDB)");
//            db.update(dataName, values, whereClause, whereArgs);
//            Cursor cursor = db.query(dataName, new String[]{"notificationTiming"}, whereClause, whereArgs, null, null, null);
//            if(cursor==null)Log.d("aaa","myId="+String.valueOf(taskData.getTaskId())+"のやつが見つかりませんでした(DataManager.updateNotificationDataFromDB)");
//            String result = null;
//            assert cursor != null;
//            if (cursor.moveToFirst()) { // カーソルを最初の位置に移動
//                // カラムのインデックスを取得
//                int columnIndex = cursor.getColumnIndex("notificationTiming");
//                // 値を取得
//                result = cursor.getString(columnIndex);
//            }
//            Log.d("aaa", "通知情報を更新しました(DataManager.updateNotificationDataFromDB)" + result);
//        }
//    }
    public ArrayList<String> requestScraping() throws IOException, ExecutionException, InterruptedException {
        return ManabaScraper.receiveRequest(dataName);
    }
}
