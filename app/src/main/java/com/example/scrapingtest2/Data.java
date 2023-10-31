package com.example.scrapingtest2;

import android.os.Build;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Data {
    private int id;
    private String title;
    private String subtitle;
    private ArrayList<LocalDateTime> notificationTiming;

    public Data(int id,String title,String subtitle){
        this.id =id;
        this.title=title;
        this.subtitle=subtitle;
        this.notificationTiming=new ArrayList<LocalDateTime>();
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
