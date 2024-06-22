package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

public class UnChangeableClassDialog extends Dialog {
   private final ClassData classData;

    public UnChangeableClassDialog(Context context,ClassData classData) {
        super(context);
        this.classData=classData;
    }

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unchangeable_class_dialog_layout);

        // ダイアログの背景に角丸を適用する
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        TextView nameText, roomText, professorNameText;
        Button classPageButton;
        SwitchCompat notificationSwitch;

        nameText = findViewById(R.id.selectedClassName);
        roomText = findViewById(R.id.selectedClassRoom);
        professorNameText = findViewById(R.id.selectedProfessorName);
        classPageButton = findViewById(R.id.classPageButton);
        notificationSwitch=findViewById(R.id.notificationSwitch);

        nameText.setText(classData.getClassName());
        roomText.setText(classData.getClassRoom());
        professorNameText.setText((classData.getProfessorName()));
        if (classData.getIsNotifying() == 1) {
            notificationSwitch.setChecked(true);
        }else{
            notificationSwitch.setChecked(false);
        }

        String classURL="https://ct.ritsumei.ac.jp/ct/" +classData.getClassURL();
        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(classURL));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定


        classPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                getContext().startActivity(chromeIntent);
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ClassDataManager.changeIsNotifying(classData.getDayAndPeriod(),1);
                } else {
                    ClassDataManager.changeIsNotifying(classData.getDayAndPeriod(),0);
                }
            }
        });
    }
}