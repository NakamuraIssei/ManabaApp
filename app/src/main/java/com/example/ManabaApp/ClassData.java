package com.example.ManabaApp;

import java.util.ArrayList;

public class ClassData {
    private final int classId;//空きコマは000000
    private int dayAndPeriod;
    private String className;
    private String classRoom;
    private String professorName;
    private String classURL;
    private int isChangeable;//0の時、unChangeable,1の時、changeable　空きコマは0
    private int isNotifying;//0の時、通知しない、1の時、通知する　空きコマは1
    private final ArrayList<TaskData> taskList;

    ClassData(int classId, int dayAndPeriod, String className, String classRoom, String professorName, String classURL, int isChangeable, int isNotifying) {
        this.classId = classId;
        this.dayAndPeriod=dayAndPeriod;
        this.className = className;
        this.classRoom = classRoom;
        this.professorName = professorName;
        this.classURL = classURL;
        this.isChangeable = isChangeable;
        this.isNotifying=isNotifying;
        this.taskList = new ArrayList<TaskData>();
    }

    public int getClassId() {
        return this.classId;
    }
    public int getDayAndPeriod() {
        return this.dayAndPeriod;
    }
    public String getClassName() {
        return this.className;
    }
    public String getClassRoom() {
        return this.classRoom;
    }
    public String getProfessorName() {
        return this.professorName;
    }
    public String getClassURL() {
        return this.classURL;
    }
    public int getIsChangeable() {
        return this.isChangeable;
    }
    public int getIsNotifying() {
        return this.isNotifying;
    }


    public void addTaskData(TaskData taskData) {
        taskList.add(taskData);
    }
    public Boolean hasTask() {
        return taskList.size() > 0;
    }
}