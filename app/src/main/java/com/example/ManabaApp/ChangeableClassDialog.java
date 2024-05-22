package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.util.HashMap;

public class ChangeableClassDialog extends Dialog {
    private static ClassDataManager classDataManager;
    private static GridView classGridView;
    private static ClassGridAdapter classGridAdapter;
    private ClassData classData;
    private String classRoom;
    private String classDay; //ここまではclassNumがあれば十分やから削除
    private int classNum;
    private final HashMap<String, Integer> dayBag;

    public ChangeableClassDialog(Context context, ClassData classData) {
        super(context);
        this.classData=classData;
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
        setContentView(R.layout.custom_changeable_class_dialog_layout);

        // ダイアログの背景に角丸を適用する
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        TextView nameText, professorNameText;
        EditText classRoomEdit, dayEdit, numEdit;
        Button classPageButton, registerButton;

        nameText = findViewById(R.id.selectedClassName);
        classRoomEdit = findViewById(R.id.RoomEdit);
        classRoomEdit.setText(classRoom);
        dayEdit = findViewById(R.id.DayEdit);
        dayEdit.setText(String.valueOf(classRoom));
        numEdit = findViewById(R.id.NumEdit);
        numEdit.setText(String.valueOf(classRoom));
        professorNameText = findViewById(R.id.selectedProfessorName);
        classPageButton = findViewById(R.id.classPageButton);
        registerButton = findViewById(R.id.Register_Button);

        nameText.setText(classData.getClassName());

        professorNameText.setText((classData.getProfessorName()));

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ct.ritsumei.ac.jp/ct/" + classData.getClassURL()));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定
        classPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                getContext().startActivity(chromeIntent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                classDataManager.makeClassEmpty(classNum-1);
                classRoom = classRoomEdit.getText().toString();
                classDay = dayEdit.getText().toString();
                classNum = Integer.parseInt(numEdit.getText().toString());
                if (!classDay.isEmpty() && (classNum <= 7 && 0 <= classNum)) {
                    classRoom = dayEdit.getText().toString() + numEdit.getText().toString() + ":" + classRoom;
                    classNum = (dayBag.get(classDay) * 7) + classNum - 1;
                    ClassData newData =new ClassData(classData.getClassId(),classNum,classData.getClassName(),classRoom,classData.getProfessorName(),classData.getClassURL(),1,1);
                    classDataManager.replaceClassDataIntoDB(newData);
                    classDataManager.replaceClassDataIntoClassList(newData);//str[0] 授業番号、str[1] 授業名、str[2] 教室名、str[3] 授業URL ユーザーが登録したデータなのでclassIdChangeableは1
                    classGridView.setNumColumns(ClassDataManager.getMaxColumnNum() + 1);
                    classGridAdapter.customGridSize();
                    classGridAdapter.notifyDataSetChanged();
                }
                dismiss();
            }
        });
    }
}