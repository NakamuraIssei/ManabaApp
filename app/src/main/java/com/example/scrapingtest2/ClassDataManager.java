package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ClassDataManager extends DataManager{

    ClassDataManager(String dataName){
        prepareForWork(dataName);
    }

    public void setClassData() {
        loadData();
        Log.d("aaa","テーブルからのClassData読み込み完了！。ClassDataManager 24");
        if(dataCount!=49){
            Log.d("aaa","ClassDataの数が"+dataCount+"しかなかったのでスクレーピングします。ClassDataManager 23");
            dataList.clear();
            db.execSQL("DELETE FROM " + dataName);
            dataCount=0;
            for(int i=0;i<49;i++){
                addData("次は空きコマです","");
            }
            getClassDataFromManaba();
        }
    }

    public Data getClassInfor(){
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
            }else if (minute <1250) {
                line = 7;
            }else line = 8;

            //Log.d("aaa","今見たのは"+row+"曜日"+line+"時間目");
            if(line==0||line==8)return new Data(0,"時間外です。","行く当てなし");
            else return dataList.get(7*row+line);
        }
        return new Data(0,"時間外です。","行く当てなし");
    }
    private void getClassDataFromManaba(){
        try {
            ArrayList<String> classList;
            classList=requestScraping();
            Log.d("aaa","課題スクレーピング完了！　ClassDataManager 35");
            for(String k:classList){
                Log.d("aaa",k+"ClassDataManager　37");
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける
                replaceClassData(Integer.parseInt(str[0]),str[1],str[2]);
            }
        } catch (ExecutionException e) {
            Log.d("aaa","課題スクレーピングみすった！　ClassDataManager　42");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","課題スクレーピングみすった！　ClassDataManager　45");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceClassData(int num,String title,String subTitle){
        dataList.get(num).replaceTitle(title);
        dataList.get(num).replaceSubtitle(subTitle);

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("subTitle", subTitle);
        String selection = "myId = ?";
        String[] selectionArgs = {String.valueOf(num)};

        int affectedRows = db.update(dataName, values, selection, selectionArgs);

        if (affectedRows > 0) {
            Log.d("aaa", dataName + "の" + num+"時間目を"+title+"に更新しました。ClassDataManager 65");
        } else {
            Log.d("aaa", dataName + "の" + num+"時間目を"+title+"に更新できませんでした。ClassDataManager 67");
        }
    }
}
