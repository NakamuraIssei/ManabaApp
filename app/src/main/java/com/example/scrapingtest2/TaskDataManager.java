package com.example.scrapingtest2;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class TaskDataManager extends DataManager{

    private static HashMap<Integer,NotificationCustomAdapter> notificationAdapterBag;
    private static ArrayList<AppCompatImageButton> bellButtonList;

    public static void prepareForTaskWork(String dataName,Context context){
        prepareForWork(dataName,context);
        notificationAdapterBag =new HashMap<>();
        bellButtonList=new ArrayList<>();
    }
    public static void addBellButton(int num,AppCompatImageButton bellButton){
        bellButtonList.add(num, bellButton);
        changeBellButton(num);
    }
    public static void removeBellButton(int num){
        bellButtonList.remove(num);
    }
    public static void changeBellButton(int num){
        if(dataList.get(num).getNotificationTiming().isEmpty())
            bellButtonList.get(num).setImageResource(R.drawable.empty_notification_bell_round);
        else
            bellButtonList.get(num).setImageResource(R.drawable.bell_round);
    }
    public static void addAdapter(int num, NotificationCustomAdapter adapter){
        notificationAdapterBag.put(num,adapter);
    }
    public static void removeAdapter(int num){
        notificationAdapterBag.remove(num);
    }
    public static void loadTaskData() {
        loadData();
        reorderTaskData();
    }
    public static void addTaskData(String title, String deadLine) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                LocalDateTime defaultTiming=LocalDateTime.parse(deadLine, formatter);
                defaultTiming=defaultTiming.plusHours(-1);
                Log.d("aaa",deadLine+"の一時間前は"+defaultTiming+"です。AddTaskCustomDialog 41");
                addData(title, deadLine,defaultTiming);
                Log.d("aaa","デフォルトの通知タイミングを設定できました。AddTaskCustomDialog 43");
            } catch (DateTimeParseException e) {
                addData(title, deadLine);
                Log.d("aaa","デフォルトの通知タイミングを設定できませんでした。AddTaskCustomDialog 46");
            }
            reorderTaskData();
        }
    }
    public static void removeTaskData(int num) {
        DataManager.removeData(num);
        removeAdapter(num);
        removeBellButton(num);
        reorderTaskData();
    }
    public static Boolean isExist(String name){
        for(Data data:dataList)if(Objects.equals(data.getTitle(), name))return false;
        return true;
    }
    public static void reorderTaskData(){
        Comparator<Data> longComparator = new Comparator<Data>() {
            @Override
            public int compare(Data p1, Data p2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return LocalDateTime.parse(p1.getSubTitle(), formatter).compareTo(LocalDateTime.parse(p2.getSubTitle(), formatter));
                }
                return 0;
            }
        };

        Collections.sort(dataList, longComparator);
    }
    public static void deleteTaskNotification(int dataNum,int notificationNum){
        deleteNotification(dataNum,notificationNum);
        changeBellButton(dataNum);
    }
    public static void deleteFinishedTaskNotification(String title,String subTitle){
        int num=deleteFinishedNotification(title,subTitle);
        changeBellButton(num);
        notificationAdapterBag.get(num).notifyDataSetChanged();
    }

}
