package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ChangeableClassDialog extends Dialog {
    private Context context;
    private static ClassDataManager classDataManager;
    private ArrayList<ClassData> classDataList;
    private ClassData registerationClassData;
    private final HashMap<String, Integer> dayBag;
    private Boolean isVisible;
    private String emptyClassName="次は空きコマです。";
    private static MainClassGridAdapter mainClassGridAdapter;
    private static GridView mainClassGridView;
    private ArrayList<ClassData>selectedClass;

    public ChangeableClassDialog(Context context, ClassData registerationClassData,Boolean isFirst) {
        super(context);
        this.context=context;
        this.registerationClassData = registerationClassData;
        this.classDataList= new ArrayList<>(classDataManager.getClassDataList());
        this.isVisible= isFirst;
        dayBag = new HashMap<>();
        dayBag.put("月", 0);
        dayBag.put("火", 1);
        dayBag.put("水", 2);
        dayBag.put("木", 3);
        dayBag.put("金", 4);
        dayBag.put("土", 5);
        dayBag.put("日", 6);
        selectedClass=new ArrayList<ClassData>();
        for(ClassData cd: this.classDataList){
            if(cd.getClassId()==registerationClassData.getClassId()){
                selectedClass.add(cd);
            }
        }
    }
    static void setClassDataManager(ClassDataManager classDataManager) {
        ChangeableClassDialog.classDataManager = classDataManager;
    }
    static void setMainClassGridAdapter(MainClassGridAdapter mainClassGridAdapter) {
        ChangeableClassDialog.mainClassGridAdapter = mainClassGridAdapter;
    }
    static void setMainClassGridView(GridView mainClassGridView) {
        ChangeableClassDialog.mainClassGridView = mainClassGridView;
    }
    public void sortSelectedClass(){
        Collections.sort(selectedClass, new Comparator<ClassData>() {
            @Override
            public int compare(ClassData classData1, ClassData classData2) {
                return Integer.compare(classData1.getDayAndPeriod(), classData2.getDayAndPeriod());
            }
        });
    }
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changeable_class_dialog_layout);

        // ダイアログの背景に角丸を適用する
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
        setCanceledOnTouchOutside(false);
        TextView nameText,professorNameText,classCommentView;
        RecyclerView roomListView;
        Button classPageButton,cancelButton,saveButton;
        ImageButton timeTableButton;
        CustomChangeableClassGridAdapter customChangeableClassGridAdapter;
        GridView customChangeableClassGridView;

        nameText = findViewById(R.id.selectedClassName);
        roomListView = findViewById(R.id.selectedClassRoomList);
        professorNameText = findViewById(R.id.selectedProfessorName);
        classCommentView=findViewById(R.id.classCommentView);
        classPageButton = findViewById(R.id.classPageButton);
        cancelButton=findViewById(R.id.cancelButton);
        saveButton=findViewById(R.id.saveButton);
        customChangeableClassGridView=findViewById(R.id.mainClassGridView);
        timeTableButton=findViewById(R.id.imageView3);

        customChangeableClassGridAdapter=new CustomChangeableClassGridAdapter(context,classDataList,registerationClassData);


        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        roomListView.setLayoutManager(layoutManager);

        ChangeableClassRoomListAdapter selectedClassAdapter = new ChangeableClassRoomListAdapter(selectedClass);//Listviewを表示するためのadapterを設定
        roomListView.setAdapter(selectedClassAdapter);//listViewにadapterを設定

        nameText.setText(registerationClassData.getClassName());

        professorNameText.setText((registerationClassData.getProfessorName()));

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ct.ritsumei.ac.jp/ct/"+registerationClassData.getClassURL()));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定

        customChangeableClassGridView.setNumColumns(8);
        customChangeableClassGridAdapter.setColumnNum(7);
        customChangeableClassGridAdapter.setRowNum(7);
        customChangeableClassGridView.setAdapter(customChangeableClassGridAdapter);
        if(isVisible) {
            classCommentView.setText("時間割を表示しない");
            timeTableButton.setBackgroundResource(R.drawable.hidden_timetable_button);
            customChangeableClassGridView.setVisibility(View.VISIBLE);
        }else{
            classCommentView.setText("時間割を表示する");
            timeTableButton.setBackgroundResource(R.drawable.indicate_timetable_button);
            customChangeableClassGridView.setVisibility(View.GONE);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                for(int i=0;i<classDataList.size();i++){
                    Log.d("aaa","newClassDataListの"+i+"番目dayAndPeriodは"+classDataList.get(i).getClassRoom()+"ChangeableClassDialog108");
                }
                classDataManager.updateClassDataList(classDataList);
                classDataManager.registerUnRegisteredClass(registerationClassData.getClassName());
                mainClassGridView.setNumColumns(classDataManager.getMaxColumnNum() + 1);
                mainClassGridAdapter.optimizeGridSize();
                mainClassGridAdapter.notifyDataSetChanged();
                dismiss();
            }
        });
        customChangeableClassGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // タップされたセルのPositionをログに表示
                Log.d("aaa", "Tapped Cell Position: " + position);
                // positionの計算
                int row, column;
                row = position % (7 + 1);
                column = position / (7 + 1);
                if (row != 0 && column != 0) {
                    int pushedClassId;
                    int pushedClassNum=(row - 1) * 7 + column - 1;
                    ClassData pushedclassData = classDataList.get(pushedClassNum);
                    pushedClassId = pushedclassData.getClassId();
                    Log.d("aaa","今押した授業は"+column+"時限"+row+"曜日");
                    if(Objects.equals(pushedClassId, registerationClassData.getClassId())){
                        selectedClass.remove(classDataList.get(pushedClassNum));
                        classDataList.set(pushedClassNum,new ClassData(0,pushedClassNum,emptyClassName, "", "", "", 0,1));
                        customChangeableClassGridAdapter.notifyDataSetChanged();
                        selectedClassAdapter.notifyDataSetChanged();
                    }else if(Objects.equals(pushedClassId, 0)){
                        classDataList.set(pushedClassNum,new ClassData(registerationClassData.getClassId(), pushedClassNum, registerationClassData.getClassName(), "", registerationClassData.getProfessorName(), registerationClassData.getClassURL(), registerationClassData.getIsChangeable(), 1));
                        selectedClass.add(classDataList.get(pushedClassNum));
                        sortSelectedClass();
                        customChangeableClassGridAdapter.notifyDataSetChanged();
                        selectedClassAdapter.notifyDataSetChanged();
                    }else{
                        Log.d("aaa","今押した授業は変更不可授業ChangeableClassDialog");
                    }
                }

                // ここで必要な処理を追加
            }
        });
        classPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                Log.d("aaa",registerationClassData.getClassURL());
                getContext().startActivity(chromeIntent);
            }
        });
        timeTableButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                Log.d("aaa","時間割表示ボタン押せてるよー！changableClassDialog");
                if(isVisible){
                    classCommentView.setText("時間割を表示する");
                    timeTableButton.setBackgroundResource(R.drawable.indicate_timetable_button);
                    customChangeableClassGridView.setVisibility(View.GONE);
                }else{
                    classCommentView.setText("時間割を表示しない");
                    timeTableButton.setBackgroundResource(R.drawable.hidden_timetable_button);
                    customChangeableClassGridView.setVisibility(View.VISIBLE);
                }
                isVisible=!isVisible;
            }
        });
    }
}