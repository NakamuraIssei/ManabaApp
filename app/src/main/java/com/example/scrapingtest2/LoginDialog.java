package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridView;

import java.util.HashMap;
import java.util.Objects;

public class LoginDialog extends Dialog {

    public WebView myWebView;
    public String url;
    public HashMap<String, String> cookieBag;
    public CookieManager cookieManager;
    public TaskDataManager taskDataManager;
    public ClassDataManager classDataManager;
    public TaskCustomAdapter adapter;
    public ClassGridAdapter classGridAdapter;
    public GridView classGridView;
    public static ClassUpdateListener classUpdateListener;
    public LoginDialog(Context context, String url, HashMap<String,String>cookieBag, CookieManager cookieManager, TaskDataManager td, ClassDataManager cd, TaskCustomAdapter adapter, ClassUpdateListener listener, ClassGridAdapter classGridAdapter,GridView classGridView) {
        super(context);
        this.url=url;
        this.cookieBag=cookieBag;
        this.cookieManager = cookieManager;
        taskDataManager=td;
        classDataManager=cd;
        this.adapter=adapter;
        this.classGridAdapter = classGridAdapter;
        this.classGridView=classGridView;
        classUpdateListener=listener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myWebView=findViewById(R.id.webView);
        cookieManager.setAcceptCookie(true);//クッキーマネージャにアクセスできるようにしておく
        final int[] flag = {0};//多分、swiftなら無くても大丈夫！
        myWebView.setVisibility(View.VISIBLE);//webViewを見えるようにする
        myWebView.setWebViewClient(new WebViewClient() {//webViewの挙動の設定
            // URLの読み込み設定
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }//webView内で画面遷移？（他のページに飛ぶとか）できるようにする
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPageFinished(WebView view, String url) {//webViewがページを読み込み終えたら
                super.onPageFinished(view, url);//おまじない
                String cookies1 = cookieManager.getCookie(url);//クッキーマネージャに指定したurl(引数として受け取ったやつ)のページから一回クッキーを取ってきてもらう
                if (cookies1 != null) {//取ってきたクッキーが空でなければ
                    cookieBag.clear();//クッキーバッグになんか残ってたら嫌やから空っぽにしておく
                    String[] cookieList = cookies1.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
                    for (String cookie : cookieList) {//cookieListの中身でループを回す
                        String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                        cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
                    }
                    for(String cookie : cookieBag.keySet()){
                        if(Objects.equals(cookie, " sessionid")){//;で切り分けたクッキーが4種類以上なら（ログインできてたら）
                            //ダイアログを閉じる
                            ManabaScraper.setCookie(cookieBag);
                            classDataManager.eraseUnchangeableClass();
                            classDataManager.getChangeableClassDataFromManaba();
                            classDataManager.eraseNotExistChangeableClass();
                            classDataManager.eraseRegisteredChangeableClass();
                            classDataManager.getUnChangeableClassDataFromManaba();
                            classDataManager.getProfessorNameFromManaba();

                            taskDataManager.loadTaskData();
                            taskDataManager.makeAllTasksSubmitted();
                            taskDataManager.getTaskDataFromManaba();
                            taskDataManager.sortAllTaskDataList();
                            taskDataManager.setTaskDataIntoRegisteredClassData();
                            taskDataManager.setTaskDataIntoUnRegisteredClassData();

                            classGridView.setNumColumns(ClassDataManager.getMaxColumnNum()+1);
                            classGridAdapter.customGridSize();
                            adapter.notifyDataSetChanged();
                            classGridAdapter.notifyDataSetChanged();
                            classUpdateListener.updateClassTextView(classDataManager.getClassInfor());
                            if(DataManager.unRegisteredClassDataList.size()>0)NotifyManager2.setClassRegistrationAlarm();
                            dismiss();
                        }
                    }

                }
            }
        });

        // WebViewのJavaScriptの許可
        myWebView.getSettings().setJavaScriptEnabled(true);//WebViewで入力ができるようにJavaScriptを設定する
        myWebView.loadUrl(url);//上で挙動設定したwebViewにurlを読み込ませて動かす
    }
}
