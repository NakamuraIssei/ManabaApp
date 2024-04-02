package com.example.ManabaApp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class DataManager {

    protected String dataName;//継承先クラスのコンストラクタで設定！
    protected int dataCount;
    protected static ArrayList<ClassData> classDataList;
    protected static ArrayList<ClassData> unRegisteredClassDataList;
    protected SQLiteDatabase db;
    protected Cursor cursor;
    protected DateTimeFormatter formatter;

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
}