package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.Calendar;

public class ClassDataManager extends DataManager {
    protected static SQLiteDatabase db;
    protected static Cursor cursor;
    protected static String dataName="ClassData";//継承先クラスのコンストラクタで設定！
    protected static ArrayList<ClassData> classDataList;
    protected static ArrayList<ClassData> unRegisteredClassDataList;
    static String emptyClassName="次は空きコマです。";
    ClassDataManager(String dataName) {
        classDataList = new ArrayList<ClassData>();
        unRegisteredClassDataList = new ArrayList<ClassData>();
        prepareForWork(dataName);
    }
    public ArrayList<ClassData> getClassDataList() {
        return classDataList;
    }
    public void loadClassData() {//データベースからデータを読み込んで、dataListに追加
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int dataId = cursor.getInt(cursor.getColumnIndex("classId"));
            @SuppressLint("Range") int dayAndPeriod = cursor.getInt(cursor.getColumnIndex("dayAndPeriod"));
            @SuppressLint("Range") String className = cursor.getString(cursor.getColumnIndex("className"));
            @SuppressLint("Range") String classRoom = cursor.getString(cursor.getColumnIndex("classRoom"));
            @SuppressLint("Range") String professorName = cursor.getString(cursor.getColumnIndex("professorName"));
            @SuppressLint("Range") String classURL = cursor.getString(cursor.getColumnIndex("classURL"));
            @SuppressLint("Range") int classIdChangeable = cursor.getInt(cursor.getColumnIndex("isChangeable"));
            @SuppressLint("Range") int isNotifying = cursor.getInt(cursor.getColumnIndex("isNotifying"));
            ClassData classData = new ClassData(dataId,dayAndPeriod, className, classRoom, professorName, classURL, classIdChangeable,isNotifying);
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
        ClassData classData = new ClassData(0,num,emptyClassName, "", "", "", 0,1);//classIdは000000、授業時間変更不可(classIdChangeableを0)で登録。
        classDataList.add(classData);
        insertClassDataIntoDB(classData);//ここでデータベースの中身を書く
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
                return new ClassData(0,0,"授業情報を取得できませんでした。", "", "", "", 0,1);
            if (line == 0 || line == 7) {
                return new ClassData(0,0,"次は空きコマです。", "", "", "", 0,1);
            } else {
                return classDataList.get(7 * row + line - 1);
            }
        }
        return new ClassData(0,0,"次は空きコマです。", "", "", "", 0,1);
    }
    public void requestSettingAllClassNotification(){
        //通知onの授業の通知設定を行う。
        Calendar calendar = Calendar.getInstance();
        for(ClassData classData:classDataList) {
            if(classData.getIsNotifying()==1) {
                switch(classData.getDayAndPeriod()/7){
                    case 0: // 月曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        break;
                    case 1: // 火曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                        break;
                    case 2: // 水曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        break;
                    case 3: // 木曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        break;
                    case 4: // 金曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        break;
                    case 5: // 土曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                        break;
                    case 6: // 日曜日
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        break;
                }
                switch(classData.getDayAndPeriod()%7){
                    case 0: // 1時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 8);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 30);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                    case 1: // 2時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 10);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 10);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                    case 2: // 3時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 12);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 30);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                    case 3: // 4時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 14);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 10);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                    case 4: // 5時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 15);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 50);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                    case 5: // 6時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 17);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 30);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                    case 6: // 7時限目
                        calendar.set(Calendar.HOUR_OF_DAY, 19);  // 時間を指定
                        calendar.set(Calendar.MINUTE, 10);  // 分を指定
                        calendar.set(Calendar.SECOND, 0);  // 秒を指定
                        break;
                }
                NotifyManager2.setClassNotificationAlarm(classData.getClassName(),classData.getClassRoom(),classData.getDayAndPeriod(),calendar);
            }
        }

    }
    public void requestFirstClassNotification(){
        //アプリを立ち上げたときの一番最初の次の授業を通知する作業
        ClassData cd=getClassInfor();
        NotifyManager2.setFirstClassNotificationAlarm(cd.getClassName(),cd.getClassRoom());
    }
    public void getChangeableClassDataFromManaba() {//ここで時間割表、その他の曜日欄の授業情報処理
        try {
            ArrayList<String> classList;
            classList = ManabaScraper.scrapeChangableClassDataFromManaba();
            for (String k : classList) {
                String[] str = k.split("\\?\\?\\?");// classId,className,professorName,classURL順に切り分ける
                unRegisteredClassDataList.add(new ClassData(Integer.parseInt(str[0]), -1, str[1],"",str[2], str[3],1,1));
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
    public void reflectUnChangeableClassDataFromManaba() {//ここで時間割表、その他の曜日欄の授業情報処理
        try {
            HashMap<Integer, String> classList;
            classList = ManabaScraper.scrapeUnChangableClassDataFromManaba();
            ArrayList<ClassData>newClassList = new ArrayList<ClassData>();
            for(int i=0;i<49;i++){
                //classIdは000000、授業時間変更不可(classIdChangeableを0)で登録。)
                newClassList.add(new ClassData(0,i,emptyClassName, "", "", "", 0,1));
            }
            // HashMapのエントリセットを取得し、それを使って反復処理する
            for (Map.Entry<Integer, String> entry : classList.entrySet()) {
                int dayAndPeriod = entry.getKey(); // エントリのキー（授業番号）を取得
                String value = entry.getValue(); // エントリの値（文字列データ）を取得
                String[] str = value.split("\\?\\?\\?"); // 値を分割
                ClassData newData=new ClassData(Integer.parseInt(str[0]),dayAndPeriod,str[1], str[2], str[3],str[4],0,1);
                newClassList.set(newData.getDayAndPeriod(),newData);
            }
            for(int i=0;i<49;i++){
                if((classDataList.get(i).getIsChangeable()==1||classDataList.get(i).getClassId()==0)&&newClassList.get(i).getClassId()!=0){
                    replaceClassDataIntoDB(newClassList.get(i));//str[0] classId、str[1] 授業名、str[2] 教室名、str[3] 教授名,str[4]　URL
                    replaceClassDataIntoClassList(newClassList.get(i));//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL 時間割表から取ってきたデータなのでclassIdChangeableは0
                    continue;
                }
                if(classDataList.get(i).getIsChangeable()==0&&classDataList.get(i).getClassId()!=0&&newClassList.get(i).getClassId()==0){
                    replaceClassDataIntoDB(newClassList.get(i));//str[0] classId、str[1] 授業名、str[2] 教室名、str[3] 教授名,str[4]　URL
                    replaceClassDataIntoClassList(newClassList.get(i));//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL 時間割表から取ってきたデータなのでclassIdChangeableは0
                }
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
                ClassData newData = new ClassData(0, classDataList.get(i).getDayAndPeriod(), emptyClassName, "", "","",0,1);
                Log.d("aaa", classDataList.get(i).getClassName() + "は登録済み授業なので一回空きコマにします。");
                replaceClassDataIntoDB(newData);
                replaceClassDataIntoClassList(newData);
            }
        }
    }
    public void eraseNotExistChangeableClass() {
        for (int i = 0; i < classDataList.size(); i++) {
            if (!Objects.equals(classDataList.get(i).getClassName(), emptyClassName) && !isExistInUnRegisteredClassDataList(classDataList.get(i).getClassName())&&classDataList.get(i).getIsChangeable()==1) {
                ClassData newData = new ClassData(0, classDataList.get(i).getDayAndPeriod(), emptyClassName, "", "","",0,1);
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
    public void updateClassDataList(ArrayList<ClassData> newClassDataList){
        for(int i=0;i<newClassDataList.size();i++){
            if(!Objects.equals(newClassDataList.get(i).getClassName(), classDataList.get(i).getClassName())){
                Log.d("aaa",newClassDataList.get(i).getClassName()+"ClassDataManger updateClassDataList");
                replaceClassDataIntoClassList(newClassDataList.get(i));
                replaceClassDataIntoDB(newClassDataList.get(i));
            }
        }
    }
    public static int getMaxColumnNum() {
        int column = 5;
        for (int i = 0; i < classDataList.size(); i++) {
            if (!Objects.equals(classDataList.get(i).getClassName(), emptyClassName)) {
                column = Math.max(column, (i / 7) + 1);
            }
        }
        return column;
    }
    public void registerUnRegisteredClass(String className) {
        for (int i = 0; i < unRegisteredClassDataList.size(); i++) {
            if (Objects.equals(unRegisteredClassDataList.get(i).getClassName(), className)) {
                unRegisteredClassDataList.remove(i);
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
        values.put("dayAndPeriod", classData.getDayAndPeriod());
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
            Log.d("aaa", String.valueOf(affectedRows));
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

    public static void changeIsNotifying(int dayAndPeriod,int isNotifying){
        classDataList.get(dayAndPeriod).changeIsNotifying(isNotifying);
        ContentValues values = new ContentValues();
        values.put("dayAndPeriod", dayAndPeriod);
        values.put("isNotifying", isNotifying);
        String selection = "dayAndPeriod = ?";
        String[] selectionArgs = {String.valueOf(dayAndPeriod)};

        int affectedRows = db.update(dataName, values, selection, selectionArgs);

        if (affectedRows <= 0) {
            Log.d("aaa", "isNotifyingの更新に失敗しました。");
        }
    }
    public static void setDB(SQLiteDatabase DB, Cursor Cursor) {// データベースを渡す
        db = DB;
        cursor = Cursor;
    }
}