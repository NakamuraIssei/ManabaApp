package com.example.scrapingtest2;

import android.os.Build;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageButton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class TaskDataManager extends DataManager{

    private HashMap<Integer,NotificationCustomAdapter> notificationAdapterBag;
    private ArrayList<AppCompatImageButton> bellButtonList;

    TaskDataManager(String dataName,int firstNum){
        prepareForWork(dataName,firstNum);
        notificationAdapterBag =new HashMap<>();
        bellButtonList=new ArrayList<>();
    }
    public void addBellButton(int num,AppCompatImageButton bellButton){
        bellButtonList.add(num, bellButton);
        changeBellButton(num);
    }
    public void removeBellButton(int num){
        bellButtonList.remove(num);
    }
    public void changeBellButton(int num){
        if(dataList.get(num).getNotificationTiming().isEmpty())
            bellButtonList.get(num).setImageResource(R.drawable.empty_notification_bell_round);
        else
            bellButtonList.get(num).setImageResource(R.drawable.bell_round);
    }
    public void addAdapter(int num, NotificationCustomAdapter adapter){
        notificationAdapterBag.put(num,adapter);
    }
    public void removeAdapter(int num){
        notificationAdapterBag.remove(num);
    }
    public void setTaskData() {
        loadData();
        //getTaskDataFromManaba();
        reorderTaskData();
    }
    public void addTaskData(String title, String deadLine) {
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
    public void removeTaskData(int num) {
        removeData(num);
        removeAdapter(num);
        removeBellButton(num);
        reorderTaskData();
    }
    public Boolean isExist(String name){
        for(Data data:dataList)if(Objects.equals(data.getTitle(), name))return false;
        return true;
    }
    public void reorderTaskData(){
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
    public void deleteTaskNotification(int dataNum,int notificationNum){
        deleteNotification(dataNum,notificationNum);
        changeBellButton(dataNum);
    }
    public void deleteFinishedTaskNotification(String title,String subTitle){
        int num=deleteFinishedNotification(title,subTitle);
        changeBellButton(num);
        notificationAdapterBag.get(num).notifyDataSetChanged();
    }
    public void getTaskDataFromManaba(){
        try {
            ArrayList<String> taskList;
            taskList=requestScraping();
            Log.d("aaa","課題スクレーピング完了！　TaskDataManager 104");
            for(String k:taskList){
                Log.d("aaa",k+"TaskDataManager　106");
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける

                if(isExist(str[0])){
                    Log.d("aaa",k+"持ってないから追加するよー！TaskDataManager　110");
                    addTaskData(str[0],str[1]);//str[0]は課題名、str[1]は締め切り
                    Log.d("aaa",k+"追加したよー！TaskDataManager　112");
                }
            }
        } catch (ExecutionException e) {
            Log.d("aaa","課題スクレーピングみすった！　TaskDataManager　116");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","課題スクレーピングみすった！　TaskDataManager　119");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
