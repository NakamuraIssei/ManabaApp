package com.example.ManabaApp;

import android.os.Build;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskData {
    public int hasSubmitted;//課題が提出済みかのフラグ 未提出なら0、提出済みなら1
    private final String taskId;
    private final String belongedClassName;
    private final String taskName;//課題名、クラス名
    private final LocalDateTime dueDate;//提出締め切り、教室名
    private final ArrayList<LocalDateTime> notificationTiming;//課題の通知時億
    private final String taskURL;//提出締め切り、教室名

    public TaskData(String taskId, String belongedClassName, String taskName, LocalDateTime dueDate, String taskURL, int hasSubmitted){
        this.taskId = taskId;
        this.belongedClassName = belongedClassName;
        this.taskName = taskName;
        this.dueDate = dueDate;
        this.taskURL=taskURL;
        this.notificationTiming=new ArrayList<LocalDateTime>();
        this.hasSubmitted =hasSubmitted;
    }
    public String getTaskId(){
        return this.taskId;
    }
    public String getBelongedClassName(){
        return this.belongedClassName;
    }
    public String getTaskName(){
        return this.taskName;
    }
    public LocalDateTime getDueDate(){
        return this.dueDate;
    }
    public ArrayList<LocalDateTime> getNotificationTiming(){
        return this.notificationTiming;
    }
    public String getTaskURL(){
        return this.taskURL;
    }
    public int getHasSubmitted(){
        return this.hasSubmitted;
    }
    public void changeSubmitted(int hasSubmitted){
        this.hasSubmitted =hasSubmitted;
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