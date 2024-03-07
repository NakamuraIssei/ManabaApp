package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
public class UnChangeableClassDialog extends Dialog{
    private String className;
    private String classRoom;
    private String professorName;
    private String classURL;

    public UnChangeableClassDialog(Context context, String className, String classRoom, String professorName, String classURL) {
        super(context);
        this.className=className;
        this.classRoom=classRoom;
        this.professorName=professorName;
        this.classURL="https://ct.ritsumei.ac.jp/ct/"+classURL;
    }
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unchangeable_class_dialog_layout);

        TextView nameText,roomText,professorNameText;
        Button classPageButton;

        nameText=findViewById(R.id.selectedClassName);
        roomText=findViewById(R.id.selectedClassRoom);
        professorNameText=findViewById(R.id.selectedProfessorName);
        classPageButton=findViewById(R.id.classPageButton);

        nameText.setText(className);
        roomText.setText(classRoom);
        professorNameText.setText((professorName));

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(classURL));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定
        classPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                getContext().startActivity(chromeIntent);
            }
        });
    }
}
