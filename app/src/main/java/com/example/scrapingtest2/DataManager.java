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
    public ArrayList<String> requestScraping() throws IOException, ExecutionException, InterruptedException {
        return ManabaScraper.receiveRequest(dataName);
    }
}
