package com.example.scrapingtest2;

import android.os.Build;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskData {
    private int taskId;
    private int belongedClassId;
    private String taskName;//課題名、クラス名
    private LocalDateTime dueDate;//提出締め切り、教室名
    private ArrayList<LocalDateTime> notificationTiming;//課題の通知時億
    private String taskURL;//提出締め切り、教室名
    public int hasSubmitted;//課題が提出済みかのフラグ 未提出なら0、提出済みなら1

    public TaskData(int taskId, int belongedClassId,String taskName, LocalDateTime dueDate, String taskURL,int hasSubmitted){
        this.taskId = taskId;
        this.belongedClassId=belongedClassId;
        this.taskName = taskName;
        this.dueDate = dueDate;
        this.taskURL=taskURL;
        this.notificationTiming=new ArrayList<LocalDateTime>();
        this.hasSubmitted =hasSubmitted;
    }
    public int getTaskId(){
        return this.taskId;
    }
    public int getBelongedClassId(){
        return this.belongedClassId;
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
    public void replaceTaskName(String taskName){
        this.taskName =taskName;
    }
    public void replaceDueDate(LocalDateTime dueDate){
        this.dueDate =dueDate;
    }
}
