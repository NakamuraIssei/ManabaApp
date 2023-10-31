package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

public class ClassDataManager extends DataManager{

    private static TextView className;
    private String name;
    @SuppressLint("StaticFieldLeak")
    private static TextView classRoom;

    public static void prepareForClassWork(String dataName, Context context){
        prepareForWork(dataName,context);
    }

    public static void setTextView(TextView ClassName, TextView ClassRoom){
        className=null;
        classRoom=null;
        className=ClassName;
        classRoom=ClassRoom;

    }

}
