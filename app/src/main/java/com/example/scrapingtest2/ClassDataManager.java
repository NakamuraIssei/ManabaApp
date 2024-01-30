package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.util.Log;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ClassDataManager extends DataManager{

    ClassDataManager(String dataName){
        prepareForWork(dataName);
    }

    public void loadClassData(){//データベースからデータを読み込んで、dataListに追加
        Log.d("aaa","今からクラスデータをロードします。ClassDataManager 22");
        while (cursor.moveToNext()) {
            @SuppressLint("Range")int datacount = cursor.getInt(cursor.getColumnIndex("classId"));
            @SuppressLint("Range")String className = cursor.getString(cursor.getColumnIndex("className"));
            @SuppressLint("Range")String classRoom = cursor.getString(cursor.getColumnIndex("classRoom"));
            @SuppressLint("Range")String classURL = cursor.getString(cursor.getColumnIndex("classURL"));
            ClassData classData =new ClassData(datacount,className,classRoom,classURL);
            dataCount=(datacount+1)%99999999;
            Log.d("aaa",datacount+"番目の"+ classData.getClassName()+"をロードしました。ClassDataManager 30");

            classDataList.add(classData);
        }
        Log.d("aaa","クラスデータロード完了!。ClassDataManager 34");
    }
    public Boolean checkClassData(){
        return classDataList.size() == 49;
    }
    public void resetClassData() {
        Log.d("aaa","ClassDataの数が"+dataCount+"しかなかったので初期化します。ClassDataManager 38");
        classDataList.clear();
        db.execSQL("DELETE FROM " + dataName);
        for(int i=0;i<49;i++){
            ClassData classData=new ClassData(i,"次は空きコマです。","","");
            classDataList.add(classData);
            insertClassDataIntoDB(classData);//ここでデータベースの中身を書く
        }
        dataCount=49;
    }
    public ClassData getClassInfor(){
        LocalDateTime now = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
            DayOfWeek dayOfWeek = now.getDayOfWeek(); // 曜日
            int minute = now.getHour() * 60 + now.getMinute(); // 分

            int line;
            int row;

            switch (dayOfWeek.getValue()) {
                case 1: // 月曜日
                    row = 0;
                    break;
                case 2: // 火曜日
                    row = 1;
                    break;
                case 3: // 水曜日
                    row = 2;
                    break;
                case 4: // 木曜日
                    row = 3;
                    break;
                case 5: // 金曜日
                    row = 4;
                    break;
                case 6: // 土曜日
                    row = 5;
                    break;
                default: // 土曜日 (7) と日曜日 (1) の場合
                    row = 6; // 例外的な値
            }
            if(minute<510){
                line=0;
            } else if (minute < 610) {
                line = 1;
            } else if (minute <750) {
                line = 2;
            } else if (minute <850) {
                line = 3;
            } else if (minute <950) {
                line = 4;
            }  else if (minute <1050) {
                line = 5;
            } else if (minute <1150) {
                line = 6;
            }else
                line = 7;

            Log.d("aaa","今見たのは"+row+"曜日"+line+"時間目");
            if(classDataList.size()!=49)return new ClassData(0,"授業情報を取得できませんでした","","");
            if(line==7){
                NotifyManager2.setClassNotificationAlarm(dataName,7*row+6,"次は空きコマです","",now);
                return new ClassData(0,"次は空きコマです","","");
            }
            else{
                NotifyManager2.setClassNotificationAlarm(dataName,7*row+line-1, classDataList.get(7*row+line-1).getClassName(), classDataList.get(7*row+line-1).getClassRoom(),now);
                return classDataList.get(7*row+line-1);
            }
        }
        return new ClassData(0,"時間外です。","行く当てなし","");
    }
    public void getClassDataFromManaba(){
        try {
            ArrayList<String> classList;
            classList=requestScraping();
            Log.d("aaa","課題スクレーピング完了！　ClassDataManager 113");
            for(String k:classList){
                Log.d("aaa",k+"ClassDataManager　115");
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける
                replaceClassDataIntoDB(Integer.parseInt(str[0]),str[1],str[2],str[3]);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
                replaceClassDataIntoClassList(Integer.parseInt(str[0]),str[1],str[2],str[3]);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
            }
        } catch (ExecutionException e) {
            Log.d("aaa","課題スクレーピングみすった！　ClassDataManager　120");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","課題スクレーピングみすった！　ClassDataManager　123");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void replaceClassDataIntoClassList(int classId, String className , String classRoom, String classURL){
        ClassData classData =new ClassData(classId,className,classRoom,classURL);
        classDataList.set(classId,classData);
    }
    public void replaceClassDataIntoDB(int classId, String className , String classRoom, String classURL){
        classDataList.get(classId).setClassName(className);
        classDataList.get(classId).setClassRoom(classRoom);
        classDataList.get(classId).setClassURL(classURL);

        ContentValues values = new ContentValues();
        values.put("className", className);
        values.put("classRoom", classRoom);
        values.put("classURL", classURL);
        String selection = "classId = ?";
        String[] selectionArgs = {String.valueOf(classId)};

        int affectedRows = db.update(dataName, values, selection, selectionArgs);

        if (affectedRows > 0) {
            Log.d("aaa", dataName + "の" + classId+"時間目を"+className+"に更新しました。ClassDataManager 65");
        } else {
            Log.d("aaa", dataName + "の" + classId+"時間目を"+classRoom+"に更新できませんでした。ClassDataManager 67");
        }
    }
    public void insertClassDataIntoDB(ClassData classData){
        if (db == null) {
            Log.d("aaa", "db空っぽです。(ClassDataManager 152)");
        }
        ContentValues values = new ContentValues();
        values.put("classId", classData.getClassId());
        values.put("className", classData.getClassName());
        values.put("classRoom", classData.getClassRoom());
        values.put("classURL", classData.getClassURL());

        Log.d("aaa", values+" ClassDataManager 160");
        Log.d("aaa", db+" ClassDataManager 161");
        long newRowId = db.insert(dataName, null, values);
        if (newRowId != -1) {
            Log.d("aaa", dataName+"に"+ classData.getClassName()+"追加しました。ClassDataManager 164");
        } else {
            Log.d("aaa", dataName+"に追加失敗。ClassDataManager 166");
        }
    }
    public void resetAlltaskList(){
        for(int i=0;i<classDataList.size();i++){
            classDataList.get(i).resetTaskList();
        }
    }
}
