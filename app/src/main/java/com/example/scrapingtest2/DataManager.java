package com.example.scrapingtest2;

import android.annotation.SuppressLint;
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
    protected ArrayList<Data> dataList;
    protected SQLiteDatabase db;
    protected Cursor cursor;
    protected DateTimeFormatter formatter;

    // 0~48 授業用のid
    // 49~ 課題用のid

    public void prepareForWork(String DataName, int firstNum){//インスタンスを生成した時に使う初期化用のメゾッド
        dataName=DataName;
        dataCount =firstNum;
        dataList=new ArrayList<Data>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN);
        }
    };
    public void setDB(SQLiteDatabase DB, Cursor Cursor){// データベースを渡す
        db=DB;
        cursor=Cursor;
    }
    public void loadData(){//データベースからデータを読み込んで、dataListに追加
        Log.d("aaa","今からデータをロードします。DataManager 41");
        while (cursor.moveToNext()) {
            @SuppressLint("Range")int datacount = cursor.getInt(cursor.getColumnIndex("myId"));
            @SuppressLint("Range")String title = cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range")String subTitle = cursor.getString(cursor.getColumnIndex("subTitle"));
            @SuppressLint("Range")String notificationTiming = cursor.getString(cursor.getColumnIndex("notificationTiming"));
            Data data=new Data(datacount,title,subTitle);
            dataCount=(datacount+1)%99999999;
            Log.d("aaa",datacount+"番目の"+data.getTitle()+"をロードしました。DataManager 50");
            if(!Objects.equals(notificationTiming, "")){
                String[] parts = notificationTiming.split("\\?"); // ? をエスケープして使用
                for (String time : parts) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.d("aaa",notificationTiming+"を設定します。DataManager 55");
                        data.addNotificationTiming(LocalDateTime.parse(time, formatter));
                    }
                }
            }
            for(LocalDateTime nt:data.getNotificationTiming()){
                requestSettingNotification(dataName,data.getId(),data.getTitle(),data.getSubTitle(),nt);
            }
            dataList.add(data);
        }
        Log.d("aaa","データロード完了!。DataManager 65");
    }
    public ArrayList<Data> getDataList(){
        return dataList;
    }
    public void addData(String title,String subTitle){
        Data data=new Data(dataCount,title,subTitle);
        dataCount =(dataCount +1)%99999999;
        dataList.add(data);
        insertDataIntoDB(data);
    }
    public void addData(String title,String subTitle,LocalDateTime defaultTiming){
        Log.d("aaa","データを追加します。DataManager 77");
        Data data=new Data(dataCount,title,subTitle);
        Log.d("aaa","データを作成しました。DataManager 79");
        dataCount =(dataCount +1)%99999999;
        data.addNotificationTiming(defaultTiming);
        Log.d("aaa",data.getNotificationTiming().toString()+"デフォルトの通知を依頼します。DataManager 84");
        requestSettingNotification(dataName,data.getId(),title,subTitle,defaultTiming);
        Log.d("aaa",data.getNotificationTiming().toString()+"デフォルトの通知を依頼しました。DataManager 86");
        dataList.add(data);
        insertDataIntoDB(data);
    }
    public void removeData(int num){
        for(LocalDateTime notificationTime:dataList.get(num).getNotificationTiming()){
            requestCancelNotification(dataName,dataList.get(num).getTitle(),dataList.get(num).getSubTitle(),notificationTime);
        }
        deleteDataFromDB(dataList.get(num));
        dataList.remove(num);
    }
    public void insertDataIntoDB(Data data){
        if (db == null) {
            Log.d("aaa", "db空っぽです。(DataManager.insertDB())");
        }
        ContentValues values = new ContentValues();
        values.put("myId", data.getId());
        values.put("title", data.getTitle());
        values.put("subTitle", data.getSubTitle());
        String notificationtime="";
        if (data.getNotificationTiming().size() > 0) {
            String before = String.valueOf(data.getNotificationTiming().get(0));
            char[] charArray2 = before.toCharArray();
            charArray2[10] = ' ';
            notificationtime = new String(charArray2);
        } else notificationtime = "";
        values.put("notificationTiming", notificationtime);
        Log.d("aaa", values+" dataManager 120");
        Log.d("aaa", db+" dataManager 121");
        long newRowId = db.insert(dataName, null, values);
        if (newRowId != -1) {
            Log.d("aaa", dataName+"に"+data.getTitle()+"追加しました。DataManager 124");
        } else {
            Log.d("aaa", dataName+"に追加失敗。DataManager 126");
        }
    }
    public void deleteDataFromDB(Data data){
        String[] whereArgs = { String.valueOf(data.getId()) };
        db.delete(dataName, "myId = ?", whereArgs);
    }
    public void addNotificationTiming(int num,LocalDateTime notificationTiming){
        dataList.get(num).addNotificationTiming(notificationTiming);
        requestSettingNotification(dataName,dataList.get(num).getId(),dataList.get(num).getTitle(),dataList.get(num).getSubTitle(),notificationTiming);
        updateNotificationTimingFromDB(dataList.get(num));
    }
    public void updateNotificationTimingFromDB(Data data){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            StringBuilder sb = new StringBuilder();
            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (LocalDateTime nt : data.getNotificationTiming()) {
                String dateTimeStr = nt.format(formatter);
                // StringBuilderを使って文字列に追加
                sb.append(dateTimeStr);
                // エントリ間に '?' を追加
                sb.append('?');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            // 最終的な文字列を取得
            String finalString = sb.toString();

            ContentValues values = new ContentValues();
            values.put("notificationTiming", finalString);

            // WHERE 句を設定（どのレコードを更新するか）
            String whereClause = "myId=?";
            String[] whereArgs = { String.valueOf(data.getId()) };

            // レコードを更新
            Log.d("aaa",finalString+"を書き込みます(DataManager.updateNotificationDataFromDB)");
            db.update(dataName, values, whereClause, whereArgs);
            Cursor cursor = db.query(dataName, new String[]{"notificationTiming"}, whereClause, whereArgs, null, null, null);
            if(cursor==null)Log.d("aaa","myId="+String.valueOf(data.getId())+"のやつが見つかりませんでした(DataManager.updateNotificationDataFromDB)");
            String result = null;
            assert cursor != null;
            if (cursor.moveToFirst()) { // カーソルを最初の位置に移動
                // カラムのインデックスを取得
                int columnIndex = cursor.getColumnIndex("notificationTiming");
                // 値を取得
                result = cursor.getString(columnIndex);
            }
            Log.d("aaa", "通知情報を更新しました(DataManager.updateNotificationDataFromDB)" + result);
        }
    }
    public void requestSettingNotification(String dataName, int dataId, String title, String subTitle, LocalDateTime notificationTiming){
        NotifyManager2.setTaskNotificationAlarm(dataName,dataId,title,subTitle,notificationTiming);
    }
    public void requestCancelNotification(String dataName, String title, String subTitle, LocalDateTime notificationTiming){
        NotifyManager2.cancelTaskNotificationAlarm(dataName,title,subTitle,notificationTiming);
    }
    public int deleteFinishedNotification(String title,String subTitle){
        //dbの更新は呼び出し元で行うので、ここでは行わない。ここではメモリ上の通知情報のみ更新。
        for(Data data: dataList){
            if(Objects.equals(data.getTitle(), title) && Objects.equals(data.getSubTitle(), subTitle)){
                data.deleteFinishedNotification();
                return dataList.indexOf(data);
            }
        }
        return 0;
    }
    public void deleteNotification(int dataNum,int notificationNum){
        //dbの更新もここで行う。
        requestCancelNotification(dataName,dataList.get(dataNum).getTitle(),dataList.get(dataNum).getSubTitle(),dataList.get(dataNum).getNotificationTiming().get(notificationNum));
        dataList.get(dataNum).deleteNotificationTiming(notificationNum);
        updateNotificationTimingFromDB(dataList.get(dataNum));
    }
    public ArrayList<String> requestScraping() throws IOException, ExecutionException, InterruptedException {
        return ManabaScraper.receiveRequest(dataName);
    }
}
