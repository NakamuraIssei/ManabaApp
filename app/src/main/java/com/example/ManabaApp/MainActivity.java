package com.example.ManabaApp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MainActivity extends AppCompatActivity implements ClassUpdateListener {

    private HashMap<String, String> cookieBag;
    private TextView classNameTextView;
    private GridView classGridView;
    private StickyListHeadersListView taskListView;
    private ClassDataManager cd;
    private CookieManager cookieManager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.main_activity_layout);

        cookieManager = CookieManager.getInstance();
        cookieBag = new HashMap<>();
        context = this;

        NotifyManager2.prepareForNotificationWork(this);
        TaskDataManager taskDataManager = new TaskDataManager("TaskData");
        AddNotificationBottomSheetDialog.setTaskDataManager(taskDataManager);
        cd = new ClassDataManager("ClassData");

        ChangeableClassDialog.setClassDataManager(cd);

        NotificationReceiver2.setTaskDataManager(taskDataManager);
        NotifyManager2.setClassUpdateListener(this);
        NotificationReceiver2.setClassUpdateListener(this);
        NotifyManager2.setBackScrapingAlarm();

        MyDBHelper myDBHelper = new MyDBHelper(this);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor taskCursor = db.query("TaskData", null, null, null, null, null, "taskId");
        Cursor classCursor = db.query("ClassData", null, null, null, null, null, "dayAndPeriod");

        taskDataManager.setDB(db, taskCursor);
        cd.setDB(db, classCursor);

        cd.loadClassData();
        if (!cd.checkClassData()) cd.resetClassData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("aaa", "授業スクレーピング完了");
                        // Viewの初期化やイベントリスナーの設定などの処理を実装
                        ImageButton imageButton = findViewById(R.id.exclamationButton);
                        imageButton.setOnClickListener(v -> {
                            // ダイアログクラスのインスタンスを作成
                            EncourageRegistringDialog dialog = new EncourageRegistringDialog(context,ClassDataManager.unRegisteredClassDataList);//追加課題の画面のインスタンスを生成
                            // ダイアログを表示
                            dialog.show();//追加課題の画面を表示
                        });

                        taskListView = MainActivity.this.findViewById(R.id.sticky_list);//画面上のListViewの情報を変数listViewに設定

                        //課題の情報をtaskDataから取得
                        TaskCustomAdapter adapter = new TaskCustomAdapter(MainActivity.this, taskDataManager);//Listviewを表示するためのadapterを設定
                        taskListView.setAdapter(adapter);//listViewにadapterを設定

                        classGridView = findViewById(R.id.mainClassGridView);
                        ChangeableClassDialog.setMainClassGridView(classGridView);
                        classGridView.setNumColumns(ClassDataManager.getMaxColumnNum() + 1);
                        classGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // タップされたセルのPositionをログに表示
                                Log.d("aaa", "Tapped Cell Position: " + position);
                                // positionの計算
                                int rowNum, columnNum, row, column;
                                rowNum = 4;
                                columnNum = 5;
                                for (int i = 0; i < cd.getClassDataList().size(); i++) {
                                    if (!Objects.equals(cd.getClassDataList().get(i).getClassName(), "次は空きコマです。")) {
                                        rowNum = Math.max(rowNum, (i % 7) + 1);
                                        columnNum = Math.max(columnNum, (i / 7) + 1);
                                    }
                                }
                                row = position % (columnNum + 1);
                                column = position / (columnNum + 1);
                                if (row != 0 && column != 0) {
                                    String className, classRoom, professorName, classURL;
                                    ClassData classData = cd.getClassDataList().get((row - 1) * 7 + column - 1);
                                    if (!Objects.equals(classData.getClassId(), 0)) {
                                        if (classData.getIsChangeable() == 0) {
                                            UnChangeableClassDialog unChangeableClassDialog = new UnChangeableClassDialog(MainActivity.this, classData);
                                            unChangeableClassDialog.show();
                                        } else {
                                            ChangeableClassDialog changeableClassDialog = new ChangeableClassDialog(MainActivity.this, classData,false);
                                            changeableClassDialog.show();
                                        }
                                    }
                                }

                                // ここで必要な処理を追加
                            }
                        });

                        MainClassGridAdapter mainClassGridAdapter = new MainClassGridAdapter(context, cd.getClassDataList());
                        ChangeableClassDialog.setMainClassGridAdapter(mainClassGridAdapter);
                        mainClassGridAdapter.optimizeGridSize();
                        classGridView.setAdapter(mainClassGridAdapter);

                        AddTaskCustomDialog.setGridAdapter(mainClassGridAdapter);

                        Button addButton = MainActivity.this.findViewById(R.id.AddButton);//課題追加の画面を呼び出すボタンの設定
                        addButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                            @Override
                            public void onClick(View v) {//ボタンが押されたら
                                Log.d("aaa", "課題追加ボタンちゃんと押せてるよー！! MainActivity 217");
                                // ダイアログクラスのインスタンスを作成
                                AddTaskCustomDialog dialog = new AddTaskCustomDialog(MainActivity.this, adapter, taskDataManager);//追加課題の画面のインスタンスを生成
                                // ダイアログを表示
                                for (TaskData taskData : taskDataManager.getAllTaskDataList())
                                    Log.d("aaa", taskData.getTaskName());
                                dialog.show();//追加課題の画面を表示
                            }

                        });
                        Button LogOffButton = MainActivity.this.findViewById(R.id.LogOff);//課題追加の画面を呼び出すボタンの設定
                        LogOffButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                            @Override
                            public void onClick(View v) {//ボタンが押されたら
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    cookieBag.entrySet().forEach(entry -> {
                                    });
                                    cookieManager.removeAllCookie();
                                    db.execSQL("DELETE FROM " + "TaskData");
                                    db.execSQL("DELETE FROM " + "ClassData");
                                }
                            }
                        });

                        classNameTextView = findViewById(R.id.classnameView);

                        //if (checkLogin())Log.d("start","loginがあかんわ");
                        if (!checkLogin()) {
                            LoginDialog dialog = new LoginDialog(context, "https://ct.ritsumei.ac.jp/ct/home_summary_report", cookieBag, cookieManager, taskDataManager, cd, adapter, (ClassUpdateListener) context, mainClassGridAdapter, classGridView,imageButton);//追加課題の画面のインスタンスを生成
                            // ダイアログを表示
                            dialog.show();//追加課題の画面を表示
                        } else {
                            ManabaScraper.setCookie(cookieBag);
                            cd.reflectUnChangeableClassDataFromManaba();
                            cd.getChangeableClassDataFromManaba();
                            cd.eraseNotExistChangeableClass();
                            cd.eraseRegisteredChangeableClass();
                            cd.requestFirstClassNotification();
                            cd.requestSettingAllClassNotification();

                            taskDataManager.loadTaskData();
                            taskDataManager.makeAllTasksSubmitted();
                            taskDataManager.getTaskDataFromManaba();
                            taskDataManager.sortAllTaskDataList();
                            taskDataManager.setTaskDataIntoRegisteredClassData();
                            taskDataManager.setTaskDataIntoUnRegisteredClassData();

                            String className = cd.getClassInfor().getClassName();
                            classNameTextView.setText(className);

                            classGridView.setNumColumns(ClassDataManager.getMaxColumnNum() + 1);
                            mainClassGridAdapter.optimizeGridSize();

                            adapter.notifyDataSetChanged();
                            mainClassGridAdapter.notifyDataSetChanged();
                            if (ClassDataManager.unRegisteredClassDataList.size() > 0) {
                                // 点滅アニメーションをロード
                                Animation blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                                // ImageButtonにアニメーションをセット
                                imageButton.startAnimation(blinkAnimation);
                            }else{
                                imageButton.setVisibility(View.GONE); // ImageViewを非表示
                            }
                        }
                    }

                });
            });

        }
    }

    @Override
    public void onNotificationReceived(String className) {
        classNameTextView.setText(className);
    }
    public boolean checkLogin() {
        try {
            // クッキーを取得するための初期リクエスト
            HttpURLConnection conn = (HttpURLConnection) new URL("https://ct.ritsumei.ac.jp/ct/home_summary_report").openConnection();
            conn.setInstanceFollowRedirects(false); // リダイレクトを自動で追わないように設定

            // クッキーを取得
            String cookies = conn.getHeaderField("Set-Cookie");
            if (cookies != null) {
                cookieBag.clear();
                String[] cookieList = cookies.split(";");
                for (String cookie : cookieList) {
                    String[] str = cookie.split("=");
                    cookieBag.put(str[0].trim(), str[1].trim());
                }

                // ログインチェック
                if (cookieBag.containsKey("sessionid")) {
                    return true;
                }

                // リダイレクトチェック
                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
                    String newUrl = conn.getHeaderField("Location");

                    // リダイレクト先へ再リクエスト
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setInstanceFollowRedirects(false);

                    // 保存したクッキーを再利用
                    conn.setRequestProperty("Cookie", cookies);

                    // 再リクエストのレスポンスを処理
                    cookies = conn.getHeaderField("Set-Cookie");
                    if (cookies != null) {
                        cookieBag.clear();
                        cookieList = cookies.split(";");
                        for (String cookie : cookieList) {
                            String[] str = cookie.split("=");
                            cookieBag.put(str[0].trim(), str[1].trim());
                        }

                        // ログインチェック
                        if (cookieBag.containsKey("sessionid")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}