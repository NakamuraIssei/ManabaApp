package com.example.scrapingtest2;

import android.content.Context;
import android.content.Intent;
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

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MainActivity extends AppCompatActivity implements ClassUpdateListener {

    private HashMap<String, String> cookieBag;
    private TextView className;
    private GridView classGridView;
    private StickyListHeadersListView taskRecyclerView;
    private ClassDataManager cd;
    private CookieManager cookieManager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_taskwork);

        //tabName.add("課題");
        //tabName.add("時間割");
        //tabName.add("自由時間");
        //tabName.add("就活");

        cookieManager=CookieManager.getInstance();
        cookieBag=new HashMap<>();
        context=this;

        NotifyManager2.prepareForNotificationWork(this);
        TaskDataManager taskDataManager=new TaskDataManager("TaskData");
        AddNotificationDialog.setTaskdataManager(taskDataManager);
        cd=new ClassDataManager("ClassData");

        RegisterClassDialog.setClassDataManager(cd);
        ChangeableClassDialog.setClassDataManager(cd);

        NotificationReceiver2.setTaskDataManager(taskDataManager);
        NotifyManager2.setNotificationListener(this);
        NotifyManager2.setBackScrapingAlarm();
        NotificationReceiver2.setNotificationListener(this);

        MyDBHelper myDBHelper = new MyDBHelper(this);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor taskCursor = db.query("TaskData", null, null, null, null, null, "taskId");
        Cursor classCursor = db.query("ClassData", null, null, null, null, null, "classId");

        taskDataManager.setDB(db,taskCursor);
        cd.setDB(db,classCursor);

        cd.loadClassData();
        if(!cd.checkClassData())cd.resetClassData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Supplier<String> setData = () -> {
//                return null;
//            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("aaa","授業スクレーピング完了");
                        // Viewの初期化やイベントリスナーの設定などの処理を実装
                        taskRecyclerView = MainActivity.this.findViewById(R.id.sticky_list);//画面上のListViewの情報を変数listViewに設定

                        // LinearLayoutManagerを設定する
//                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
//                        taskRecyclerView.setLayoutManager(layoutManager);

                        //課題の情報をtaskDataから取得
                        TaskCustomAdapter adapter = new TaskCustomAdapter(MainActivity.this, taskDataManager);//Listviewを表示するためのadapterを設定
                        taskRecyclerView.setAdapter(adapter);//listViewにadapterを設定

                        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, 0) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                return false;
                            }
                            @Override
                            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            }
                            @Override
                            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                    Log.d("aaa","スワイプできてるやん");
                                    View itemView = viewHolder.itemView;
                                    ColorDrawable background = new ColorDrawable();
                                    background .setColor(Color.parseColor("#FF0000"));
                                    background.setBounds(itemView.getLeft() , itemView.getTop(), (int)dX, itemView.getBottom());
                                    background.draw(c);
                                }
                            }

                        };
//                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
//                        itemTouchHelper.attachToRecyclerView(taskRecyclerView);

                        classGridView=findViewById(R.id.classTableGrid);
                        classGridView.setNumColumns(ClassDataManager.getMaxColumnNum()+1);
                        classGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // タップされたセルのPositionをログに表示
                                Log.d("aaa", "Tapped Cell Position: " + position);
                                // positionの計算
                                int rowNum,columnNum,row,column;
                                rowNum =4;
                                columnNum =5;
                                for(int  i=0;i<cd.getClassDataList().size();i++){
                                    if(!Objects.equals(cd.getClassDataList().get(i).getClassName(), "次は空きコマです。")){
                                        rowNum =Math.max(rowNum,(i%7)+1);
                                        columnNum =Math.max(columnNum,(i/7)+1);
                                    }
                                }
                                row=position%(columnNum+1);
                                column=position/(columnNum+1);
                                if(row!=0&&column!=0){
                                    String className,classRoom,professorName,classURL;
                                    ClassData classData =cd.getClassDataList().get((row-1)*7+column-1);
                                    className=classData.getClassName();
                                    classRoom=classData.getClassRoom();
                                    professorName=classData.getProfessorName();
                                    classURL=classData.getClassURL();
                                    Log.d("aaa", "今押した授業情報は"+className+"   "+professorName+"MainActivity 166");
                                    if(!Objects.equals(className, "次は空きコマです。")){
                                        if(classData.getClassIdChangeable()==0){
                                            UnChangeableClassDialog unChangeableClassDialog =new UnChangeableClassDialog(MainActivity.this,className,classRoom,professorName,classURL);
                                            unChangeableClassDialog.show();
                                        }else{
                                            ChangeableClassDialog changeableClassDialog =new ChangeableClassDialog(MainActivity.this,(row-1)*7+column,className,classRoom,professorName,classURL);
                                            changeableClassDialog.show();
                                        }
                                    }
                                }

                                // ここで必要な処理を追加
                            }
                        });

                        ClassGridAdapter classGridAdapter =new ClassGridAdapter(context,cd.getClassDataList());
                        classGridAdapter.customGridSize();
                        classGridView.setAdapter(classGridAdapter);

                        AddTaskCustomDialog.setGridAdapter(classGridAdapter);
                        RegisterClassDialog.setClassGridAdapter(classGridAdapter);
                        ChangeableClassDialog.setClassGridAdapter(classGridAdapter);
                        ChangeableClassDialog.setClassGridView(classGridView);

                        Button addButton = MainActivity.this.findViewById(R.id.AddButton);//課題追加の画面を呼び出すボタンの設定
                        addButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                            @Override
                            public void onClick(View v) {//ボタンが押されたら
                                Log.d("aaa","課題追加ボタンちゃんと押せてるよー！! MainActivity 217");
                                // ダイアログクラスのインスタンスを作成
                                AddTaskCustomDialog dialog = new AddTaskCustomDialog(MainActivity.this,adapter,taskDataManager);//追加課題の画面のインスタンスを生成
                                // ダイアログを表示
                                for(TaskData taskData:taskDataManager.getAllTaskDataList())Log.d("aaa",taskData.getTaskName());
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
                            LoginDialog dialog = new LoginDialog(context,"https://ct.ritsumei.ac.jp/ct/home_summary_report",cookieBag,cookieManager,taskDataManager,cd,adapter, (ClassUpdateListener) context, classGridAdapter,classGridView);//追加課題の画面のインスタンスを生成
                            // ダイアログを表示
                            dialog.show();//追加課題の画面を表示
                        }else{
                            ManabaScraper.setCookie(cookieBag);
                            cd.eraseUnchangeableClass();
                            cd.getChangeableClassDataFromManaba();
                            cd.eraseNotExistChangeableClass();
                            cd.eraseRegisteredChangeableClass();
                            cd.getUnChangeableClassDataFromManaba();
                            cd.getProfessorNameFromManaba();

                            taskDataManager.loadTaskData();
                            taskDataManager.makeAllTasksSubmitted();
                            taskDataManager.getTaskDataFromManaba();
                            taskDataManager.sortAllTaskDataList();
                            taskDataManager.setTaskDataIntoRegisteredClassData();
                            taskDataManager.setTaskDataIntoUnRegisteredClassData();

                            ClassData now=cd.getClassInfor();
                            className.setText(now.getClassName());

                            adapter.notifyDataSetChanged();
                            classGridAdapter.notifyDataSetChanged();
                            if(DataManager.unRegisteredClassDataList.size()>0)NotifyManager2.setClassRegistrationAlarm();
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
    @Override
    public  void showRegisterClassDialog(){
        for(ClassData classData:DataManager.unRegisteredClassDataList){
            RegisterClassDialog registerClassDialog=new RegisterClassDialog(this,classData.getClassName(),classData.getProfessorName(),classData.getClassURL(),classGridView);
            registerClassDialog.show();
        }
    }
    public boolean checkLogin(){
        String cookies = cookieManager.getCookie("https://ct.ritsumei.ac.jp/ct/home_summary_report");//クッキーマネージャに指定したurl(引数として受け取ったやつ)のページから一回クッキーを取ってきてもらう
        if (cookies != null) {//取ってきたクッキーが空でなければ
            cookieBag.clear();//クッキーバッグになんか残ってたら嫌やから空っぽにしておく
            String[] cookieList = cookies.split(";");//1つの長い文字列として受け取ったクッキーを;で切り分ける
            for (String cookie : cookieList) {//cookieListの中身でループを回す
                String[] str = cookie.split("=");//切り分けたクッキーをさらに=で切り分ける
                cookieBag.put(str[0], str[1]);//切り分けたクッキーをcookiebagに詰める
            }
            for(String cookie : cookieBag.keySet()){
                //if(flag)break;
                if(Objects.equals(cookie, " sessionid")) {//;で切り分けたクッキーが4種類以上なら（ログインできてたら）
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
