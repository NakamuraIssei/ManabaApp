package com.example.scrapingtest2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MainActivity extends AppCompatActivity implements ClassUpdateListener {

    private HashMap<String, String> cookieBag;
    private TextView className;
    private GridView classGridView;
    private RecyclerView taskRecyclerView;
    private ClassDataManager cd;
    private CookieManager cookieManager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_taskwork);
        //setContentView(R.layout.aaa);

        //tabName.add("課題");
        //tabName.add("時間割");
        //tabName.add("自由時間");
        //tabName.add("就活");

        /*cookiebag=new HashMap<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("BUNDLE");
        cookiebag = (HashMap<String, String>)bundle.getSerializable("cookiebag");

        ManabaScraper.setCookie(cookiebag);*/
        cookieManager=CookieManager.getInstance();
        cookieBag=new HashMap<>();
        context=this;

        NotifyManager2.prepareForNotificationWork(this);
        TaskDataManager taskDataManager=new TaskDataManager("TaskData");
        AddNotificationDialog.setTaskdataManager(taskDataManager);
        cd=new ClassDataManager("ClassData");

        NotificationReceiver2.setTaskDataManager(taskDataManager);
        NotifyManager2.setNotificationListener(this);
        NotifyManager2.setBackScrapingAlarm();

        MyDBHelper myDBHelper = new MyDBHelper(this);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor taskCursor = db.query("TaskData", null, null, null, null, null, "taskId");
        Cursor classCursor = db.query("ClassData", null, null, null, null, null, "classId");

        taskDataManager.setDB(db,taskCursor);
        cd.setDB(db,classCursor);

        cd.loadClassData();
        if(!cd.checkClassData())cd.resetClassData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Supplier<String> setData = () -> {
                taskDataManager.loadTaskData();
                Log.d("aaa","TaskDataロード完了！ MainActivity 83");
                taskDataManager.setTaskDataIntoClassData();
                taskDataManager.sortAllTaskDataList();
                return null;
            };

            CompletableFuture.supplyAsync(setData).thenAccept(result2 -> {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("aaa","授業スクレーピング完了");
                        // Viewの初期化やイベントリスナーの設定などの処理を実装
                        taskRecyclerView = MainActivity.this.findViewById(R.id.RecycleView);//画面上のListViewの情報を変数listViewに設定

                        // LinearLayoutManagerを設定する
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        taskRecyclerView.setLayoutManager(layoutManager);

                        //課題の情報をtaskDataから取得
                        TaskCustomAdapter adapter = new TaskCustomAdapter(MainActivity.this, taskDataManager);//Listviewを表示するためのadapterを設定
                        taskRecyclerView.setAdapter(adapter);//listViewにadapterを設定

                        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, 0) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                return false;
                            }
                            @Override
                            public void onSwiped (@NonNull RecyclerView.ViewHolder viewHolder, int i) {
//                                int swipedPosition = viewHolder.getAdapterPosition();
//                                TaskCustomAdapter adapter = (TaskCustomAdapter) recyclerView.getAdapter();
//                                // 登録とかするんだったらなにかのリストから削除をする処理はここ
//                                taskDataManager.removeTaskData(swipedPosition);
//                                // 削除されたことを知らせて反映させる。
//                                adapter.notifyItemRemoved(swipedPosition);
                            }
                            @Override
                            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                    Log.d("aaa","スワイプできてるやん");
                                    View itemView = viewHolder.itemView;
                    /*Drawable background;
                    if (dX <= 0) {
                        background = new ColorDrawable(Color.RED); // 右にスワイプしたときの背景
                    } else {
                        background = new ColorDrawable(Color.GREEN); // 左にスワイプしたときの背景
                    }*/
                                    ColorDrawable background = new ColorDrawable();
                                    background .setColor(Color.parseColor("#FF0000"));
                                    background.setBounds(itemView.getLeft() , itemView.getTop(), (int)dX, itemView.getBottom());
                                    background.draw(c);
                                    //background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                                    //background.draw(c);
                                }
                            }

                        };
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                        itemTouchHelper.attachToRecyclerView(taskRecyclerView);

                        classGridView=findViewById(R.id.classTableGrid);
                        classGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // タップされたセルのPositionをログに表示
                                Log.d("aaa", "Tapped Cell Position: " + position);

                                // ここで必要な処理を追加
                            }
                        });
                        GridAdapter gridAdapter=new GridAdapter(context,cd.getClassDataList());
                        classGridView.setAdapter(gridAdapter);
                        AddTaskCustomDialog.setGridAdapter(gridAdapter);

                        Button addButton = MainActivity.this.findViewById(R.id.AddButton);//課題追加の画面を呼び出すボタンの設定
                        addButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                            @Override
                            public void onClick(View v) {//ボタンが押されたら
                                Log.d("aaa","課題追加ボタンちゃんと押せてるよー！! MainActivity 217");
                                // ダイアログクラスのインスタンスを作成
                                AddTaskCustomDialog dialog = new AddTaskCustomDialog(MainActivity.this,adapter,taskDataManager);//追加課題の画面のインスタンスを生成
                                // ダイアログを表示
                                dialog.show();//追加課題の画面を表示
                            }
                        });
                        Button LogOffButton = MainActivity.this.findViewById(R.id.LogOff);//課題追加の画面を呼び出すボタンの設定
                        LogOffButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                            @Override
                            public void onClick(View v) {//ボタンが押されたら
                                cookieManager.removeAllCookie();
                            }
                        });

                        className = findViewById(R.id.classnameView);

                        if(!checkLogin()){
                            LoginDialog dialog = new LoginDialog(context,"https://ct.ritsumei.ac.jp/ct/home_summary_report",cookieBag,cookieManager,taskDataManager,cd,adapter, (ClassUpdateListener) context,gridAdapter);//追加課題の画面のインスタンスを生成
                            // ダイアログを表示
                            dialog.show();//追加課題の画面を表示
                        }else{
                            ManabaScraper.setCookie(cookieBag);
                            cd.getClassDataFromManaba();
                            taskDataManager.makeAllTasksSubmitted();
                            taskDataManager.getTaskDataFromManaba();
                            taskDataManager.sortAllTaskDataList();

                            ClassData now=cd.getClassInfor();
                            className.setText(now.getClassName());

                            adapter.notifyDataSetChanged();
                            gridAdapter.notifyDataSetChanged();
                        }
                    }

                });
            });

        }
    }
    @Override
    public void onNotificationReceived(int dataId) {
        dataId=(dataId+49)%49;
        className.setText(cd.classDataList.get(dataId).getClassName());
    }
    @Override
    public void updateClassTextView(ClassData classData) {
        className.setText(classData.getClassName());
    }
    public boolean checkLogin(){
        String cookies = cookieManager.getCookie("https://ct.ritsumei.ac.jp/ct/home_summary_report");//クッキーマネージャに指定したurl(引数として受け取ったやつ)のページから一回クッキーを取ってきてもらう
        if (cookies != null) {//取ってきたクッキーが空でなければ
            Log.d("aaa",cookies);
            cookieBag.clear();//クッキーバッグになんか残ってたら嫌やから空っぽにしておく
            String[] cookieList = cookies.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
            for (String cookie : cookieList) {//cookieListの中身でループを回す
                Log.d("aaa", cookie.trim());
                String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
            }
            //flag=false;
            for(String cookie : cookieBag.keySet()){
                //if(flag)break;
                if(Objects.equals(cookie, " sessionid")) {//;で切り分けたクッキーが4種類以上なら（ログインできてたら）
                    Log.d("aaa", "ログインできてたわー MainActivity 209");
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
