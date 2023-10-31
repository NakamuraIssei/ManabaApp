package com.example.scrapingtest2;

import android.os.Build;
import android.util.Log;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Queue;

public class ClassData {
    public static ArrayList<ArrayList<ClassData>> classData = new ArrayList<>();
    public String className;
    public String classRoom;
    public LocalTime nextTiming;
    boolean judge;

    public ClassData(){
        this.className="次は空きコマです。";
        this.classRoom="";
        this.nextTiming=null;
        this.judge=false;
    }
    public static void setClassData(ClassData classData,int num){
        ClassData.classData.get(num).add(classData);
    }
    public static ClassData getInfor(){
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

            Log.d("aaa","今見たのは"+row+"曜日"+line+"時間目");
            if(row==-1)return classData.get(0).get(0);
            else return classData.get(row).get(line);
        }
        return ClassData.classData.get(0).get(0);
    }

}
