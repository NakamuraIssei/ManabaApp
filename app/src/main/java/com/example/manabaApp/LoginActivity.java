package com.example.manabaApp;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    // 変数の初期化
    private String loginUrl = "https://ct.ritsumei.ac.jp/ct/home_summary_report";
    private LogInManager logInManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog_layout);

        logInManager = new LogInManager(this);
        logInManager.setListener(new LogInManager.Listener() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("cookiebag",logInManager.cookieBag);
                intent.putExtra("BUNDLE", bundle);
                startActivity(intent);
            }
        });

        logInManager.checkLogin(loginUrl);
    }
}


