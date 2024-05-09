package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

public class EncourageRegistrationDialog extends Dialog {
    private final String className;

    public EncourageRegistrationDialog(Context context, String className) {
        super(context);
        this.className = className;
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

        classNameText.setText(className);

    }
}
