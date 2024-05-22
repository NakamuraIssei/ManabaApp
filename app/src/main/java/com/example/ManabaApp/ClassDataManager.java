package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.util.Log;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ClassDataManager extends DataManager {
    static String emptyClassName="次は空きコマです。";
    ClassDataManager(String dataName) {
        unRegisteredClassDataList = new ArrayList<ClassData>();
        prepareForWork(dataName);
    }
    public ArrayList<ClassData> getClassDataList() {
        return classDataList;
    }
    public void loadClassData() {//データベースからデータを読み込んで、dataListに追加
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String dataId = cursor.getString(cursor.getColumnIndex("classId"));
            @SuppressLint("Range") int dayAndPeriod = cursor.getInt(cursor.getColumnIndex("dayAndPeriod"));
            @SuppressLint("Range") String className = cursor.getString(cursor.getColumnIndex("className"));
            @SuppressLint("Range") String classRoom = cursor.getString(cursor.getColumnIndex("classRoom"));
            @SuppressLint("Range") String professorName = cursor.getString(cursor.getColumnIndex("professorName"));
            @SuppressLint("Range") String classURL = cursor.getString(cursor.getColumnIndex("classURL"));
            @SuppressLint("Range") int classIdChangeable = cursor.getInt(cursor.getColumnIndex("isChangeable"));
            @SuppressLint("Range") int isNotifying = cursor.getInt(cursor.getColumnIndex("isNotifying"));
            ClassData classData = new ClassData(dataId,dayAndPeriod, className, classRoom, professorName, classURL, classIdChangeable,isNotifying);
            //this.dataCount = (dataCount + 1) % 99999999;
            classDataList.add(classData);
        }
    }
    public Boolean checkClassData() {
        return classDataList.size() == 49;
    }
    public void resetClassData() {
        classDataList.clear();
        db.execSQL("DELETE FROM " + dataName);
        for (int i = 0; i < 49; i++) {
            makeClassEmpty(i);
        }
        dataCount = 49;
    }
    public void makeClassEmpty(int num){
        //指定された時限を空きコマにする
        ClassData classData = new ClassData("000000",num,emptyClassName, "", "", "", 0,1);//classIdは000000、授業時間変更不可(classIdChangeableを0)で登録。
        classDataList.add(classData);
        replaceClassDataIntoDB(classData);//ここでデータベースの中身を書く
    }
    public ClassData getClassInfor() {
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
            if (minute < 510) {
                line = 0;
            } else if (minute < 610) {
                line = 1;
            } else if (minute < 750) {
                line = 2;
            } else if (minute < 850) {
                line = 3;
            } else if (minute < 950) {
                line = 4;
            } else if (minute < 1050) {
                line = 5;
            } else if (minute < 1150) {
                line = 6;
            } else
                line = 7;

            if (classDataList.size() != 49)
                return new ClassData("000000",0,"授業情報を取得できませんでした。", "", "", "", 0,1);
            if (line == 0 || line == 7) {
                Log.d("className", "時間外アクセス　ClassDataManager");
                NotifyManager2.setClassNotificationAlarm(dataName, 7 * row + 6, "次は空きコマです", "", now);
                return new ClassData("000000",0,"次は空きコマです。", "", "", "", 0,1);
            } else {
                NotifyManager2.setClassNotificationAlarm(dataName, 7 * row + line - 1, classDataList.get(7 * row + line - 1).getClassName(), classDataList.get(7 * row + line - 1).getClassRoom(), now);
                return classDataList.get(7 * row + line - 1);
            }
        }
        return new ClassData("000000",0,"次は空きコマです。", "", "", "", 0,1);
    }
    public void getChangeableClassDataFromManaba() {//ここで時間割表、その他の曜日欄の授業情報処理
        try {
            ArrayList<String> classList;
            classList = ManabaScraper.scrapeChangableClassDataFromManaba();
            for (String k : classList) {
                String[] str = k.split("\\?\\?\\?");// classId,className,professorName,classURL順に切り分ける
                unRegisteredClassDataList.add(new ClassData(str[0], -1, str[1],"",str[2], str[2],1,1));
            }
        } catch (ExecutionException e) {
            Log.d("aaa", "授業スクレーピングみすった！　ClassDataManager　154");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa", "授業スクレーピングみすった！　ClassDataManager　156");
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.d("aaa", "授業スクレーピングみすった！　ClassDataManager　143" + e);
            throw new RuntimeException(e);
        }
    }
    public void getUnChangeableClassDataFromManaba() {//ここで時間割表、その他の曜日欄の授業情報処理
        try {
            HashMap<Integer, String> classList;
            classList = ManabaScraper.scrapeUnChangableClassDataFromManaba();
            // HashMapのエントリセットを取得し、それを使って反復処理する
            for (Map.Entry<Integer, String> entry : classList.entrySet()) {
                int dayAndPeriod = entry.getKey(); // エントリのキー（授業番号）を取得
                String value = entry.getValue(); // エントリの値（文字列データ）を取得
                String[] str = value.split("\\?\\?\\?"); // 値を分割
                ClassData newData=new ClassData(str[0],dayAndPeriod,str[1], str[2], str[3],str[4],0,1);
                replaceClassDataIntoDB(newData);//str[0] classId、str[1] 授業名、str[2] 教室名、str[3] 教授名,str[4]　URL
                replaceClassDataIntoClassList(newData);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL 時間割表から取ってきたデータなのでclassIdChangeableは0
            }
        } catch (ExecutionException e) {
            Log.d("aaa", "授業スクレーピングみすった！　ClassDataManager　132");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa", "授業スクレーピングみすった！　ClassDataManager　135");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void eraseUnchangeableClass() {
        for (int i = 0; i < classDataList.size(); i++) {
            if (classDataList.get(i).getIsChangeable() == 0) {
                ClassData newData = new ClassData("000000", classDataList.get(i).getDayAndPeriod(), emptyClassName, "", "","",0,1);
                Log.d("aaa", classDataList.get(i).getClassName() + "は登録済み授業なので一回削除します。");
                replaceClassDataIntoDB(newData);
                replaceClassDataIntoClassList(newData);
            }
        }
    }
    public void eraseNotExistChangeableClass() {
        for (int i = 0; i < classDataList.size(); i++) {
            if (!Objects.equals(classDataList.get(i).getClassName(), emptyClassName) && !isExistInUnRegisteredClassDataList(classDataList.get(i).getClassName())) {
                ClassData newData = new ClassData("000000", classDataList.get(i).getDayAndPeriod(), emptyClassName, "", "","",0,1);
                Log.d("aaa", classDataList.get(i).getClassName() + "はmanaba上にない可動授業なので一回削除します。");
                replaceClassDataIntoDB(newData);
                replaceClassDataIntoClassList(newData);
            }
        }
    }
    public void eraseRegisteredChangeableClass() {
        ArrayList<String> eraseClassName = new ArrayList<String>();
        for (int i = 0; i < unRegisteredClassDataList.size(); i++) {
            if (isExistInClassDataList(unRegisteredClassDataList.get(i).getClassName())) {
                eraseClassName.add(unRegisteredClassDataList.get(i).getClassName());
            }
        }
        for (String className : eraseClassName) {
            eraseClassFromUnRegisteredClassDataList(className);
        }
    }
    public Boolean isExistInClassDataList(String className) {
        for (int i = 0; i < classDataList.size(); i++) {
            if (Objects.equals(classDataList.get(i).getClassName(), className)) return true;
        }
        return false;
    }
    public Boolean isExistInUnRegisteredClassDataList(String className) {
        for (int i = 0; i < unRegisteredClassDataList.size(); i++) {
            if (Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className))
                return true;
        }
        return false;
    }
    public void eraseClassFromUnRegisteredClassDataList(String className) {
        for (int i = 0; i < unRegisteredClassDataList.size(); i++) {
            if (Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className)) {
                unRegisteredClassDataList.remove(i);
                break;
            }
        }
    }
    public static int getMaxColumnNum() {
        int column = 5;
        for (int i = 0; i < classDataList.size(); i++) {
            if (!Objects.equals(classDataList.get(i).getClassName(), emptyClassName)) {
                //Log.d("aaa",classDataList.get(i).getClassName()+" "+i+"ClassDataManager getMaxColumnNUm");
                column = Math.max(column, (i / 7) + 1);
            }
        }
        //Log.d("aaa",column+"ClassDataManager getMaxColumnNUm");
        return column;
    }
    public void registerUnRegisteredClass(String className, int num, String classRoom) {
        for (int i = 0; i < unRegisteredClassDataList.size(); i++) {
            if (Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className)) {
                ClassData classData = unRegisteredClassDataList.get(i);
                ClassData newData = new ClassData(classData.getClassId(),num,classData.getClassName(),classRoom,classData.getProfessorName(),classData.getClassURL(),1,1);
                replaceClassDataIntoDB(newData);
                replaceClassDataIntoClassList(newData);
            }
            break;
        }
    }
    public void replaceClassDataIntoClassList(ClassData classData) {
        classDataList.set(classData.getDayAndPeriod(), classData);
    }
    public void replaceClassDataIntoDB(ClassData classData) {
        ContentValues values = new ContentValues();
        values.put("classId", classData.getClassId());
        values.put("className", classData.getClassName());
        values.put("classRoom", classData.getClassRoom());
        values.put("professorName", classData.getProfessorName());
        values.put("classURL", classData.getClassURL());
        values.put("isChangeable", classData.getIsChangeable());
        values.put("isNotifying", classData.getIsNotifying());
        String selection = "dayAndPeriod = ?";
        String[] selectionArgs = {String.valueOf(classData.getDayAndPeriod())};

        int affectedRows = db.update(dataName, values, selection, selectionArgs);

        if (affectedRows > 0) {
            Log.d("aaa", dataName + "の" + classData.getDayAndPeriod() + "時間目を" + classData.getClassName() + "に更新しました。ClassDataManager 65");
        } else {
            Log.d("aaa", dataName + "の" + classData.getDayAndPeriod() + "時間目を" + classData.getClassName() + "に更新できませんでした。ClassDataManager 67");
        }
    }
    public void insertClassDataIntoDB(ClassData classData) {
        if (db == null) {
            Log.d("aaa", "db空っぽです。(ClassDataManager 152)");
        }
        ContentValues values = new ContentValues();
        values.put("classId", classData.getClassId());
        values.put("dayAndPeriod", classData.getDayAndPeriod());
        values.put("className", classData.getClassName());
        values.put("classRoom", classData.getClassRoom());
        values.put("classURL", classData.getClassURL());
        Log.d("aaa", values + " ClassDataManager 160");
        Log.d("aaa", db + " ClassDataManager 161");
        Log.d("aaa", dataName + " ClassDataManager 162");
        long newRowId = db.insert(dataName, null, values);
        if (newRowId != -1) {
            Log.d("aaa", dataName + "に" + classData.getClassName() + "追加しました。ClassDataManager 164");
        } else {
            Log.d("aaa", dataName + "に追加失敗。ClassDataManager 166");
        }
    }
}