package com.example.scrapingtest2;

import android.os.Build;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClassData {
    private int classId;
    private String className;
    private String classRoom;

    //private String professorName;
    private String classURL;
    private ArrayList<TaskData> taskList;

    ClassData(int classId, String className, String classRoom, String classURL){
        this.classId =classId;
        this.className=className;
        this.classRoom=classRoom;
        //this.professorName;
        this.classURL=classURL;
        this.taskList=new ArrayList<TaskData>();
    }
    public int getClassId(){
        return this.classId;
    }
    public String getClassName(){
        return this.className;
    }
    public String getClassRoom(){
        return this.classRoom;
    }
    public String getClassURL(){
        return this.classURL;
    }
    public void setClassName(String className){
        this.className=className;
    }
    public void setClassRoom(String classRoom){
        this.classRoom=classRoom;
    }
    public void setClassURL(String classURL){
        this.classURL=classURL;
    }
    public void addTaskData(TaskData taskData){
        taskList.add(taskData);
    }
    public void resetTaskList() {
        this.taskList.clear();
    }
    public Boolean hasTask() {
        return taskList.size() > 0;
    }
    public ArrayList<TaskData> getTaskList(){
        return this.taskList;
    }
    public void sortTaskList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Comparator<TaskData> longComparator = new Comparator<TaskData>() {
                @Override
                public int compare(TaskData task1, TaskData task2) {
                    // Assuming dueDate is in the format "yyyy-MM-dd"
                    return task1.getDueDate().compareTo(task2.getDueDate());
                }
            };
            Collections.sort(taskList, longComparator);
        }
    }
}
