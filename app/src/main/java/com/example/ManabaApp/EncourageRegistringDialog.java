package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EncourageRegistringDialog extends Dialog {
    private ClassData classData;

    public EncourageRegistringDialog(Context context, ClassData classData) {
        super(context);
        this.classData=classData;
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

        TextView classNameText;
        Button button;

        button=findViewById(R.id.button);

        classNameText = findViewById(R.id.textView2);

        classNameText.setText(classData.getClassName());


        button.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                    @Override
                    public void onClick(View v) {//ボタンが押されたら
                        ChangeableClassDialog changeableClassDialog = new ChangeableClassDialog(getContext(), classData);
                        changeableClassDialog.show();
                    }
                }
        );
    }
}
