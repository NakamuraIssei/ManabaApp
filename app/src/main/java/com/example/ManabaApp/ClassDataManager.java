package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.util.Log;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ClassDataManager extends DataManager{
    ClassDataManager(String dataName){
        unRegisteredClassDataList =new ArrayList<ClassData>();
        prepareForWork(dataName);
    }
    public ArrayList<ClassData> getClassDataList(){
        return classDataList;
    }
    public void loadClassData(){//データベースからデータを読み込んで、dataListに追加
        while (cursor.moveToNext()) {
            @SuppressLint("Range")int dataCount = cursor.getInt(cursor.getColumnIndex("classId"));
            @SuppressLint("Range")String className = cursor.getString(cursor.getColumnIndex("className"));
            @SuppressLint("Range")String classRoom = cursor.getString(cursor.getColumnIndex("classRoom"));
            @SuppressLint("Range")String professorName = cursor.getString(cursor.getColumnIndex("professorName"));
            @SuppressLint("Range")String classURL = cursor.getString(cursor.getColumnIndex("classURL"));
            @SuppressLint("Range")int classIdChangeable = cursor.getInt(cursor.getColumnIndex("classIdChangeable"));
            ClassData classData =new ClassData(dataCount,className,classRoom,professorName,classURL,classIdChangeable);
            this.dataCount =(dataCount+1)%99999999;
            classDataList.add(classData);
        }
    }
    public Boolean checkClassData(){
        return classDataList.size() == 49;
    }
    public void resetClassData() {
        classDataList.clear();
        db.execSQL("DELETE FROM " + dataName);
        for(int i=0;i<49;i++){
            ClassData classData=new ClassData(i,"次は空きコマです。","","","",0);//授業時間変更不可(classIdChangeableを0)で登録。
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

            if(classDataList.size()!=49)return new ClassData(0,"授業情報を取得できませんでした","","","",0);
            if(line==0||line==7){
                Log.d("className","時間外アクセス　ClassDataManager");
                NotifyManager2.setClassNotificationAlarm(dataName,7*row+6,"次は空きコマです","",now);
                return new ClassData(0,"次は空きコマです","","","",0);
            }
            else {
                NotifyManager2.setClassNotificationAlarm(dataName,7*row+line-1, classDataList.get(7*row+line-1).getClassName(), classDataList.get(7*row+line-1).getClassRoom(),now);
                return classDataList.get(7*row+line-1);
            }
        }
        return new ClassData(0,"次は空きコマです。","","","",0);
    }
    public void getUnChangeableClassDataFromManaba(){//ここで時間割表、その他の曜日欄の授業情報処理
        try {
            ArrayList<String> classList;
            classList=ManabaScraper.getRegisteredClassDataFromManaba();
            for(String k:classList){
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける
                replaceClassDataIntoDB(Integer.parseInt(str[0]),str[1],str[2],str[3],0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
                replaceClassDataIntoClassList(Integer.parseInt(str[0]),str[1],str[2],"",str[3],0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL 時間割表から取ってきたデータなのでclassIdChangeableは0
            }
        } catch (ExecutionException e) {
            Log.d("aaa","授業スクレーピングみすった！　ClassDataManager　132");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","授業スクレーピングみすった！　ClassDataManager　135");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void getChangeableClassDataFromManaba(){//ここで時間割表、その他の曜日欄の授業情報処理
        try {
            ArrayList<String> classList;
            classList=ManabaScraper.getUnRegisteredClassDataFromManaba();
            for(String k:classList){
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける
                unRegisteredClassDataList.add(new ClassData(-1,str[0],"",str[1],str[2],1));
            }
        } catch (ExecutionException e) {
            Log.d("aaa","授業スクレーピングみすった！　ClassDataManager　154");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","授業スクレーピングみすった！　ClassDataManager　156");
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.d("aaa","授業スクレーピングみすった！　ClassDataManager　143"+e.toString());
            throw new RuntimeException(e);
        }
    }
    public void getProfessorNameFromManaba(){
        ArrayList<String>professorInfor;
        try {
            professorInfor=ManabaScraper.getProfessorNameFromManaba();
            for(String k:professorInfor){
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける
                for(int i=0;i<classDataList.size();i++){
                    if(Objects.equals(classDataList.get(i).getClassName(), str[0])){//str[0] 授業名
                        classDataList.get(i).setProfessorName(str[1]);//str[1] 教授名
                    }
                }
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            Log.d("aaa","教授名スクレーピングみすった！　ClassDataManager　148");
            throw new RuntimeException(e);
        }
    }
    public void eraseUnchangeableClass(){
        for(int i=0;i<classDataList.size();i++){
            if(classDataList.get(i).getClassIdChangeable()==0){
                Log.d("aaa",classDataList.get(i).getClassName()+"は登録済み授業なので一回削除します。");
                replaceClassDataIntoDB(i,"次は空きコマです。","","",0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
                replaceClassDataIntoClassList(i,"次は空きコマです。","","","",0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL 時間割表から取ってきたデータなのでclassIdChangeableは0
            }
        }
    }
    public void eraseNotExistChangeableClass(){
        for(int i=0;i<classDataList.size();i++){
            if(!Objects.equals(classDataList.get(i).getClassName(), "次は空きコマです。")&&!isExistInUnRegisteredClassDataList(classDataList.get(i).getClassName())){
                replaceClassDataIntoDB(i,"次は空きコマです。","","",0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
                replaceClassDataIntoClassList(i,"次は空きコマです。","","","",0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL 時間割表から取ってきたデータなのでclassIdChangeableは0
            }
        }
    }
    public void eraseRegisteredChangeableClass(){
        ArrayList<String>eraseClassName=new ArrayList<String>();
        for(int i = 0; i< unRegisteredClassDataList.size(); i++){
            if(isExistInClassDataList(unRegisteredClassDataList.get(i).getClassName())){
                eraseClassName.add(unRegisteredClassDataList.get(i).getClassName());
            }
        }
        for(String className:eraseClassName){
            eraseClassFromUnRegisteredClassDataList(className);
        }
    }
    public Boolean isExistInClassDataList(String className){
        for(int i=0;i<classDataList.size();i++){
            if(Objects.equals(classDataList.get(i).getClassName(), className))return true;
        }
        return false;
    }
    public Boolean isExistInUnRegisteredClassDataList(String className){
        for(int i = 0; i< unRegisteredClassDataList.size(); i++){
            if(Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className))return true;
        }
        return false;
    }
    public void eraseClassFromUnRegisteredClassDataList(String className){
        for(int i = 0; i< unRegisteredClassDataList.size(); i++){
            if(Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className)){
                unRegisteredClassDataList.remove(i);
                break;
            }
        }
    }
    public void registerUnRegisteredClass(String className,int num,String classRoom,int classIdChangeable){
        for(int i=0;i<unRegisteredClassDataList.size();i++){
            if(Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className)){
                ClassData classData=unRegisteredClassDataList.get(i);
                classData.setClassRoom(classRoom);
                replaceClassDataIntoDB(num,className,classRoom,classData.getClassURL(),classIdChangeable);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
                replaceClassDataIntoClassList(num,className,classRoom,classData.getProfessorName(),classData.getClassURL(),classIdChangeable);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL ユーザーが登録したデータなのでclassIdChangeableは1
            }
            break;
        }
    }
    public static int getMaxColumnNum(){
        int column=5;
        for(int  i=0;i<classDataList.size();i++){
            if(!Objects.equals(classDataList.get(i).getClassName(), "次は空きコマです。")){
                column =Math.max(column,(i/7)+1);
            }
        }
        return column;
    }
    public void replaceClassDataIntoClassList(int classId, String className,String classRoom,String professorName,String classURL,int classIdChangeable){
        ClassData classData =new ClassData(classId,className,classRoom,professorName,classURL,classIdChangeable);
        classDataList.set(classId,classData);
    }
    public void replaceClassDataIntoDB(int classId, String className , String classRoom, String classURL,int classIdChangeable){
        classDataList.get(classId).setClassName(className);
        classDataList.get(classId).setClassRoom(classRoom);
        classDataList.get(classId).setClassURL(classURL);
        classDataList.get(classId).setClassIdChangeable(classIdChangeable);

        ContentValues values = new ContentValues();
        values.put("className", className);
        values.put("classRoom", classRoom);
        values.put("classURL", classURL);
        values.put("classIdChangeable", classIdChangeable);
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
}
