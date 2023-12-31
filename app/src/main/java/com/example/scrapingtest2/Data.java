package com.example.scrapingtest2;

import android.os.Build;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Data {
    private int id;//課題の識別子
    private String title;//課題名、クラス名
    private String subtitle;//提出締め切り、教室名
    private ArrayList<LocalDateTime> notificationTiming;//課題の通知時億
    public Boolean done;//課題が提出済みかのフラグ

    public Data(int id,String title,String subtitle){
        this.id =id;
        this.title=title;
        this.subtitle=subtitle;
        this.notificationTiming=new ArrayList<LocalDateTime>();
        this.done=false;
    }
    public int getId(){
        return this.id;
    }
    public String getTitle(){
        return this.title;
    }
    public String getSubTitle(){
        return this.subtitle;
    }
    public ArrayList<LocalDateTime> getNotificationTiming(){
        return this.notificationTiming;
    }
    public Boolean getDone(){
        return this.done;
    }
    public void replaceTitle(String title){
        this.title=title;
    }
    public void replaceSubtitle(String subTitle){
        this.subtitle=subTitle;
    }
    public void replaceDone(boolean done){
        this.done=done;
    }
    public void addNotificationTiming(LocalDateTime newTiming){
        this.notificationTiming.add(newTiming);
        reorderNotificationTiming();
    }
    public void deleteNotificationTiming(int notificationNum){
        this.notificationTiming.remove(notificationNum);
        reorderNotificationTiming();
    }
    public void deleteFinishedNotification(){
        notificationTiming.remove(0);
        reorderNotificationTiming();
    }
    public void reorderNotificationTiming(){
        Comparator<LocalDateTime> longComparator = new Comparator<LocalDateTime>() {
            @Override
            public int compare(LocalDateTime p1, LocalDateTime p2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return p1.compareTo(p2);
                }
                return 0;
            }
        };
        Collections.sort(notificationTiming, longComparator);
    }
}
