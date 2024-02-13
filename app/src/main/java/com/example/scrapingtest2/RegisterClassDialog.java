package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

public class RegisterClassDialog extends Dialog {
    private String className;
    private String classRoom;
    private String classDay;
    private int classNum;
    private String professorName;
    private String classURL;
    private HashMap<String, Integer>bag;
    private static ClassDataManager classDataManager;
    private static ClassGridAdapter classGridAdapter;

    public RegisterClassDialog(Context context, String className,String professorName, String classURL) {
        super(context);
        this.className=className;
        this.classRoom="";
        this.professorName=professorName;
        this.classURL="https://ct.ritsumei.ac.jp/ct/"+classURL;
        bag=new HashMap<>();
        bag.put("月",0);
        bag.put("火",1);
        bag.put("水",2);
        bag.put("木",3);
        bag.put("金",4);
        bag.put("土",5);
        bag.put("日",6);
    }
    static void setClassDataManager(ClassDataManager classDataManager){
        RegisterClassDialog.classDataManager=classDataManager;
    }
    static void setClassGridAdapter(ClassGridAdapter classGridAdapter){
        RegisterClassDialog.classGridAdapter=classGridAdapter;
    }
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_class_dialog);

        TextView nameText,professorNameText;
        EditText classRoomEdit,dayEdit,numEdit;
        Button classPageButton,registerButton;

        nameText=findViewById(R.id.selectedClassName);
        classRoomEdit=findViewById(R.id.RoomEdit);
        dayEdit=findViewById(R.id.DayEdit);
        numEdit=findViewById(R.id.NumEdit);
        professorNameText=findViewById(R.id.selectedProfessorName);
        classPageButton=findViewById(R.id.classPageButton);
        registerButton=findViewById(R.id.Register_Button);

        nameText.setText(className);

        professorNameText.setText((professorName));
        Log.d("sss",professorName+"ClassDialog 37");

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(classURL));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定
        classPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                Log.d("ppp","授業ページに飛ぶよ　ClassDataDialog 54");
                getContext().startActivity(chromeIntent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                classRoom=classRoomEdit.getText().toString();
                classDay=dayEdit.getText().toString();
                classNum= Integer.parseInt(numEdit.getText().toString());
                if (!classDay.isEmpty() && (classNum<=7&&0<=classNum)) {
                    classRoom=dayEdit.getText().toString()+numEdit.getText().toString()+":"+classRoom;
                    classDataManager.registerUnRegisteredClass(className, (bag.get(classDay)*7)+classNum-1,classRoom,1);
                }
                classGridAdapter.notifyDataSetChanged();
                dismiss();
            }
        });
    }
}
