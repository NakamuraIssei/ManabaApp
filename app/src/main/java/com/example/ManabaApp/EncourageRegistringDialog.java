package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class EncourageRegistringDialog extends Dialog {
    private ArrayList<ClassData> unRegisteredClassDataList;
    private Context context;

    public EncourageRegistringDialog(Context context, ArrayList<ClassData> unRegisteredClassDataList) {
        super(context);
        this.unRegisteredClassDataList=unRegisteredClassDataList;
        this.context=context;
    }

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encourage_registeration_dialog);

        // ダイアログの背景に角丸を適用する
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        RecyclerView unRegisteredClassRecyclerView= findViewById(R.id.unRegisteredClassRecyclerView);

        // LinearLayoutManagerを設定する
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        unRegisteredClassRecyclerView.setLayoutManager(layoutManager);
        UnRegisteredClassAdapter adapter = new UnRegisteredClassAdapter(context,unRegisteredClassDataList,this);//Listviewを表示するためのadapterを設定
        unRegisteredClassRecyclerView.setAdapter(adapter);//listViewにadapterを設定

    }
}
