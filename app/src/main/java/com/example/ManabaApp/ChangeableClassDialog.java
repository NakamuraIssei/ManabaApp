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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import java.util.HashMap;
import java.util.Objects;

public class ChangeableClassDialog extends Dialog {
    private static ClassDataManager classDataManager;
    private static GridView classGridView;
    private static ClassGridAdapter classGridAdapter;
    private final String className;
    private String classRoom;
    private final String professorName;
    private final String classURL;
    private int classNum;
    private final HashMap<String, Integer> dayBag;
    private Boolean isVisible;

    public ChangeableClassDialog(Context context, int classNum, String className, String classRoom, String professorName, String classURL) {
        super(context);
        this.classNum = classNum;
        this.className = className;
        this.classRoom = classRoom;
        this.professorName = professorName;
        this.classURL = classURL;
        this.isVisible=false;
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

    static void setClassGridAdapter(ClassGridAdapter classGridAdapter) {
        ChangeableClassDialog.classGridAdapter = classGridAdapter;
    }

    static void setClassGridView(GridView classsGridView) {
        ChangeableClassDialog.classGridView = classsGridView;
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
        Button classPageButton;
        ImageButton timeTableButton;
        GridView editClassGridView;

        nameText = findViewById(R.id.selectedClassName);
        roomText = findViewById(R.id.selectedClassRoom);
        professorNameText = findViewById(R.id.selectedProfessorName);
        classCommentView=findViewById(R.id.classCommentView);
        classPageButton = findViewById(R.id.classPageButton);
        editClassGridView=findViewById(R.id.classTableGrid);
        timeTableButton=findViewById(R.id.imageView3);

        nameText.setText(className);
        roomText.setText(classRoom);
        professorNameText.setText((professorName));

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(classURL));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定

        classCommentView.setText("時間割を表示する");
        timeTableButton.setBackgroundResource(R.drawable.indicate_timetable_button);
        editClassGridView.setNumColumns(8);
        classGridAdapter.setColumnNum(7);
        classGridAdapter.setRowNum(7);
        editClassGridView.setAdapter(classGridAdapter);
        editClassGridView.setVisibility(View.GONE);
        editClassGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // タップされたセルのPositionをログに表示
                Log.d("aaa", "Tapped Cell Position: " + position);
                // positionの計算
                int rowNum, columnNum, row, column;
                row = position % (7 + 1);
                column = position / (7 + 1);
                if (row != 0 && column != 0) {
                    String pushedClassName;
                    int pushedClassNum;
                    ClassData classData = classDataManager.getClassDataList().get((row - 1) * 7 + column - 1);
                    pushedClassName = classData.getClassName();
                    pushedClassNum= row*7+column;
                    Log.d("aaa","今押した授業は"+column+"時限"+row+"曜日");
                    if(Objects.equals(pushedClassName, className)){
                        Log.d("aaa","今押した授業は既に登録済みの授業ChangeableClassDialog");
//                        classDataManager.replaceClassDataIntoDB(pushedClassNum, "次は空きコマです。", "", "", 0);
//                        classDataManager.replaceClassDataIntoClassList(pushedClassNum, "次は空きコマです。", "", "", "", 0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
//                        editClassGridView.setNumColumns(ClassDataManager.getMaxColumnNum() + 1);
//                        classGridAdapter.customGridSize();
//                        classGridAdapter.notifyDataSetChanged();
                    }else if(Objects.equals(pushedClassName, "次は空きコマです。")){
                        Log.d("aaa","今押した授業は新たに登録しようとしている授業ChangeableClassDialog");
//                        classDataManager.replaceClassDataIntoDB(pushedClassNum, className, classRoom, classURL, 0);
//                        classDataManager.replaceClassDataIntoClassList(pushedClassNum, className, classRoom, professorName, classURL, 0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
//                        editClassGridView.setNumColumns(ClassDataManager.getMaxColumnNum() + 1);
//                        classGridAdapter.customGridSize();
//                        classGridAdapter.notifyDataSetChanged();
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
                    editClassGridView.setVisibility(View.GONE);
                }else{
                    classCommentView.setText("時間割を表示しない");
                    timeTableButton.setBackgroundResource(R.drawable.hidden_timetable_button);
                    editClassGridView.setVisibility(View.VISIBLE);
                }
                isVisible=!isVisible;
            }
        });



//        TextView nameText, professorNameText;
//        EditText classRoomEdit, dayEdit, numEdit;
//        Button classPageButton, registerButton;
//
//        nameText = findViewById(R.id.selectedClassName);
//        classRoomEdit = findViewById(R.id.RoomEdit);
//        classRoomEdit.setText(classRoom.substring(3));
//        dayEdit = findViewById(R.id.DayEdit);
//        dayEdit.setText(String.valueOf(classRoom.charAt(0)));
//        numEdit = findViewById(R.id.NumEdit);
//        numEdit.setText(String.valueOf(classRoom.charAt(1)));
//        professorNameText = findViewById(R.id.selectedProfessorName);
//        classPageButton = findViewById(R.id.classPageButton);
//        registerButton = findViewById(R.id.Register_Button);
//
//        nameText.setText(className);
//
//        professorNameText.setText((professorName));
//
//        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ct.ritsumei.ac.jp/ct/" + classURL));
//        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定
//        classPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
//            @SuppressLint("QueryPermissionsNeeded")
//            @Override
//            public void onClick(View v) {//ボタンが押されたら
//                getContext().startActivity(chromeIntent);
//            }
//        });
//        registerButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
//            @SuppressLint("QueryPermissionsNeeded")
//            @Override
//            public void onClick(View v) {//ボタンが押されたら
//                classDataManager.replaceClassDataIntoDB(classNum - 1, "次は空きコマです。", "", "", 0);
//                classDataManager.replaceClassDataIntoClassList(classNum - 1, "次は空きコマです。", "", "", "", 0);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL
//                classRoom = classRoomEdit.getText().toString();
//                classDay = dayEdit.getText().toString();
//                classNum = Integer.parseInt(numEdit.getText().toString());
//                if (!classDay.isEmpty() && (classNum <= 7 && 0 <= classNum)) {
//                    classRoom = dayEdit.getText().toString() + numEdit.getText().toString() + ":" + classRoom;
//                    classNum = (dayBag.get(classDay) * 7) + classNum - 1;
//                    classDataManager.replaceClassDataIntoDB(classNum, className, classRoom, classURL, 1);
//                    classDataManager.replaceClassDataIntoClassList(classNum, className, classRoom, professorName, classURL, 1);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL ユーザーが登録したデータなのでclassIdChangeableは1
//                    classGridView.setNumColumns(ClassDataManager.getMaxColumnNum() + 1);
//                    classGridAdapter.customGridSize();
//                    classGridAdapter.notifyDataSetChanged();
//                }
//                dismiss();
//            }
//        });
    }
}