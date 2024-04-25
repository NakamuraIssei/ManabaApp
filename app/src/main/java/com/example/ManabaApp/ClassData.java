package com.example.ManabaApp;

import java.util.ArrayList;

public class ClassData {
    private final int classId;
    private String className;
    private String classRoom;
    private String professorName;
    private String classURL;
    private int classIdChangeable;
    private final ArrayList<TaskData> taskList;

    ClassData(int classId, String className, String classRoom, String professorName, String classURL, int classIdChangeable) {
        this.classId = classId;
        this.className = className;
        this.classRoom = classRoom;
        this.professorName = professorName;
        this.classURL = classURL;
        this.classIdChangeable = classIdChangeable;
        this.taskList = new ArrayList<TaskData>();
    }

    public int getClassId() {
        return this.classId;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassRoom() {
        return this.classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getProfessorName() {
        return this.professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getClassURL() {
        return this.classURL;
    }

    public void setClassURL(String classURL) {
        this.classURL = classURL;
    }

    public int getClassIdChangeable() {
        return this.classIdChangeable;
    }

    public void setClassIdChangeable(int classIdChangeable) {
        this.classIdChangeable = classIdChangeable;
    }

    public void addTaskData(TaskData taskData) {
        taskList.add(taskData);
    }

    public Boolean hasTask() {
        return taskList.size() > 0;
    }
}