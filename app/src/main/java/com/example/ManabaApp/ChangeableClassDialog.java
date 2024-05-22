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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changeable_class_dialog_layout);

        // ダイアログの背景に角丸を適用する
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        TextView nameText, roomText, professorNameText,classCommentView;
        Button classPageButton,cancelButton,saveButton;
        ImageButton timeTableButton;
        CustomChangeableClassGridAdapter customChangeableClassGridAdapter;
        GridView customChangeableClassGridView;

        nameText = findViewById(R.id.selectedClassName);
        roomText = findViewById(R.id.selectedClassRoom);
        professorNameText = findViewById(R.id.selectedProfessorName);
        classCommentView=findViewById(R.id.classCommentView);
        classPageButton = findViewById(R.id.classPageButton);
        cancelButton=findViewById(R.id.cancelButton);
        saveButton=findViewById(R.id.saveButton);
        customChangeableClassGridView=findViewById(R.id.mainClassGridView);
        timeTableButton=findViewById(R.id.imageView3);
        customChangeableClassGridAdapter=new CustomChangeableClassGridAdapter(context,classDataList,registerationClassData);

        nameText.setText(registerationClassData.getClassName());
        roomText.setText(registerationClassData.getClassRoom());
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
                    Log.d("aaa","newClassDataListの"+i+"番目dayAndPeriodは"+classDataList.get(i).getDayAndPeriod()+"ChangeableClassDialog108");
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
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // タップされたセルのPositionをログに表示
                Log.d("aaa", "Tapped Cell Position: " + position);
                // positionの計算
                int row, column;
                row = position % (7 + 1);
                column = position / (7 + 1);
                if (row != 0 && column != 0) {
                    String pushedClassName;
                    int pushedClassNum=(row - 1) * 7 + column - 1;
                    ClassData pushedclassData = classDataList.get(pushedClassNum);
                    pushedClassName = pushedclassData.getClassName();
                    Log.d("aaa","今押した授業は"+column+"時限"+row+"曜日");
                    if(Objects.equals(pushedClassName, registerationClassData.getClassName())){
                        Log.d("aaa","今押した授業は既に登録済みの授業"+pushedClassName+ registerationClassData.getClassName()+"ChangeableClassDialog");
                        classDataList.set(pushedClassNum,new ClassData("000000",pushedClassNum,emptyClassName, "", "", "", 0,1));
                        customChangeableClassGridAdapter.notifyDataSetChanged();
                    }else if(Objects.equals(pushedClassName, "次は空きコマです。")){
                        Log.d("aaa","今押した授業は新たに登録しようとしている授業ChangeableClassDialog");
                        classDataList.set(pushedClassNum,new ClassData(registerationClassData.getClassId(), pushedClassNum, registerationClassData.getClassName(), registerationClassData.getClassRoom(), registerationClassData.getProfessorName(), registerationClassData.getClassURL(), registerationClassData.getIsChangeable(), registerationClassData.getIsNotifying()));
                        customChangeableClassGridAdapter.notifyDataSetChanged();
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