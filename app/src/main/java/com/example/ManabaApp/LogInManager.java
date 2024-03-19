package com.example.ManabaApp;


import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Objects;

public class LogInManager extends AppCompatActivity {

    private AppCompatActivity loginActivity;
    private WebView myWebView;
    private static CookieManager cookieManager;
    static HashMap<String, String> cookieBag;
    private Listener listener;
    private boolean flag = false;

    public LogInManager(AppCompatActivity activity) {
        cookieBag = new HashMap<>();
        this.loginActivity = activity;
        cookieManager = CookieManager.getInstance();
    }

    public void checkLogin(String url) {

        myWebView = loginActivity.findViewById(R.id.webView);//画面上のwebViewの情報を変数myWebViewに設定
        myWebView.setVisibility(View.GONE);//チェックするだけなので画面上のwebViewは見えなくしておく
        cookieManager.setAcceptCookie(true);//クッキーマネージャにアクセスできるようにしておく

        myWebView.setWebViewClient(new WebViewClient() {//webViewの挙動の設定
            @Override
            public void onPageFinished(WebView view, String url) {//webViewがページを読み込み終えたら
                super.onPageFinished(view, url);//おまじない
                String cookies = cookieManager.getCookie(url);//クッキーマネージャに指定したurl(引数として受け取ったやつ)のページから一回クッキーを取ってきてもらう
                if (cookies != null) {//取ってきたクッキーが空でなければ
                    cookieBag.clear();//クッキーバッグになんか残ってたら嫌やから空っぽにしておく
                    String[] cookieList = cookies.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
                    for (String cookie : cookieList) {//cookieListの中身でループを回す
                        String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                        cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
                    }
                    myWebView.setWebViewClient(null);//webViewの挙動の設定をデフォルトに戻す
                    flag = false;
                    for (String cookie : cookieBag.keySet()) {
                        if (flag) break;
                        if (Objects.equals(cookie, " sessionid")) {//;で切り分けたクッキーが4種類以上なら（ログインできてたら）
                            flag = true;
                            listener.onSuccess();//次の画面に遷移する
                        }
                    }
                    if (!flag) {
                        doLogin(url);//doLoginを呼び出す
                    }
                }
            }
        });
        myWebView.loadUrl(url);//上で挙動設定したwebViewにurlを読み込ませて動かす
    }

    public void doLogin(String url) {
        final int[] flag = {0};//多分、swiftなら無くても大丈夫！
        myWebView.setVisibility(View.VISIBLE);//webViewを見えるようにする
        myWebView.setWebViewClient(new WebViewClient() {//webViewの挙動の設定
            // URLの読み込み設定
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }//webView内で画面遷移？（他のページに飛ぶとか）できるようにする

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
                    for (String cookie : cookieBag.keySet()) {
                        if (Objects.equals(cookie, " sessionid")) {//;で切り分けたクッキーが4種類以上なら（ログインできてたら）
                            myWebView.setVisibility(View.GONE);//webViewを見えなくする
                            myWebView.getSettings().setJavaScriptEnabled(false);//JavaScriptの設定を外す
                            myWebView.destroy();//webViewを消す
                            listener.onSuccess();//次の画面に遷移する
                        }
                    }

                }
            }
        });
        // WebViewのJavaScriptの許可
        myWebView.getSettings().setJavaScriptEnabled(true);//WebViewで入力ができるようにJavaScriptを設定する
        myWebView.loadUrl(url);//上で挙動設定したwebViewにurlを読み込ませて動かす
    }

    public void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cookieBag.entrySet().forEach(entry -> {
            });
        }
        myWebView.clearCache(false);
        cookieManager.removeAllCookie();
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    // 画面更新処理を呼び出すためのインタフェース
    interface Listener {
        void onSuccess();
    }
}
