package com.example.ManabaApp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class DataManager {
    protected int dataCount;
    protected DateTimeFormatter formatter;

    public void prepareForWork(String DataName) {//インスタンスを生成した時に使う初期化用のメゾッド
        dataCount = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN);
        }
    }
}