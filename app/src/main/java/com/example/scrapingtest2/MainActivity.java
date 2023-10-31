package com.example.scrapingtest2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, String> cookiebag;
    private final ArrayList<String> urlList = new ArrayList<>();
    private final ArrayList<String> tabName = new ArrayList<>();

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private TextView className;
    private TextView classRoom;
    private BroadcastReceiver classInforReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            className.setText(ClassData.getInfor().className);
            classRoom.setText(ClassData.getInfor().classRoom);
            NotifyManager.notifyClassInfor(getApplicationContext(),ClassData.getInfor().className,ClassData.getInfor().classRoom);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDateTime currentDateTime = LocalDateTime.now();
                long nextHour = (long) ClassData.getInfor().nextTiming.getHour() - (long) currentDateTime.getHour();
                long nextMinute = (long) ClassData.getInfor().nextTiming.getMinute() - (long) currentDateTime.getMinute();
                long currentTimeMillis = System.currentTimeMillis() + nextHour * 3600000L + nextMinute * 60000L;

                Log.d("aaa", String.valueOf((currentTimeMillis - System.currentTimeMillis()) / 60000) + "分後に教室の情報を更新します");
                Log.d("aaa", String.valueOf(currentTimeMillis) + "これが教室更新のエポックタイム");

                intent = new Intent();
                intent.setAction("CLASSACTION");
                // PendingIntent pending = PendingIntent.getBroadcast( getApplicationContext(), 0, intent, 0 );
                // アラームをセットする
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.set(AlarmManager.RTC_WAKEUP, currentTimeMillis, pendingIntent);
            }
        }
    };

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
        cookiebag=new HashMap<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("BUNDLE");
        cookiebag = (HashMap<String, String>)bundle.getSerializable("cookiebag");

        NotifyManager2.prepareForNotificationWork(this);
        TaskDataManager.prepareForTaskWork("TaskData",this);

        MyDBHelper myDBHelper = new MyDBHelper(this);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor cursor = db.query("TaskData", null, null, null, null, null, "myId");

        TaskDataManager.setDB(db,cursor);
        TaskDataManager.loadTaskData();
        Log.d("aaa","課題ロード完了！MainActivity 112");

        ManabaScraper manabaScraper=new ManabaScraper(cookiebag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /*Supplier<String> loadData = () -> {
                getDataFromDatabase();
                return null;
            };*/

            Supplier<String> getTask = () -> {
                try {
                    ArrayList<String> taskList;
                    taskList=manabaScraper.getTaskDataFromManaba();
                    Log.d("aaa","課題スクレーピング完了！　MainActivity　126");
                    for(String k:taskList){
                        Log.d("aaa",k+"MainActivity　128");
                        String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける

                        //TaskData context;
                        //context = new TaskData(str[0],1,"hoegohoge",str[1]);//締め切り日時と課題名をペアにする。
                        if(TaskDataManager.isExist(str[0])){
                            Log.d("aaa",k+"持ってないから追加するよー！MainActivity　134");
                            TaskDataManager.addTaskData(str[0],str[1]);//str[0]は課題名、str[1]は締め切り
                            Log.d("aaa",k+"追加したよー！MainActivity　136");
                        }
                    }
                } catch (ExecutionException e) {
                    Log.d("aaa","課題スクレーピングみすった！　MainActivity　135");
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    Log.d("aaa","課題スクレーピングみすった！　MainActivity　138");
                    throw new RuntimeException(e);
                }
                return null;
            };

            Supplier<String> getClassData = () -> {
                try {
                    Log.d("aaa","授業データ、スクレーピングするよー！　MainActivity　144");
                    manabaScraper.getClassInforFromManaba();
                } catch (ExecutionException | InterruptedException | IOException e) {
                    Log.d("aaa","授業データ、スクレーピング失敗！　MainActivity　147");
                    throw new RuntimeException(e);
                }
                return null;
            };

            CompletableFuture.supplyAsync(getTask).thenCompose(result1 -> {
                Log.d("aaa","課題スクレーピング完了！");
                return CompletableFuture.supplyAsync(getClassData);
            }).thenAccept(result2 -> {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("aaa","授業スクレーピング完了");
                        // Viewの初期化やイベントリスナーの設定などの処理を実装
                        RecyclerView recyclerView = MainActivity.this.findViewById(R.id.RecycleView);//画面上のListViewの情報を変数listViewに設定

                        // LinearLayoutManagerを設定する
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        recyclerView.setLayoutManager(layoutManager);

                        //ArrayList<TaskData> taskList = TaskData.getTask();//課題の情報をtaskDataから取得
                        TaskCustomAdapter adapter = new TaskCustomAdapter(MainActivity.this, TaskDataManager.getDataList());//Listviewを表示するためのadapterを設定
                        recyclerView.setAdapter(adapter);//listViewにadapterを設定

                        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                return false;
                            }
                            @Override
                            public void onSwiped (@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                int swipedPosition = viewHolder.getAdapterPosition();
                                TaskCustomAdapter adapter = (TaskCustomAdapter) recyclerView.getAdapter();
                                // 登録とかするんだったらなにかのリストから削除をする処理はここ
                                TaskDataManager.removeTaskData(swipedPosition);
                                // 削除されたことを知らせて反映させる。
                                adapter.notifyItemRemoved(swipedPosition);
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
                        itemTouchHelper.attachToRecyclerView(recyclerView);

                        Button addButton = MainActivity.this.findViewById(R.id.AddButton);//課題追加の画面を呼び出すボタンの設定
                        addButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
                            @Override
                            public void onClick(View v) {//ボタンが押されたら
                                Log.d("aaa","課題追加ボタンちゃんと押せてるよー！! MainActivity 217");
                                // ダイアログクラスのインスタンスを作成
                                AddTaskCustomDialog dialog = new AddTaskCustomDialog(MainActivity.this,adapter);//追加課題の画面のインスタンスを生成
                                // ダイアログを表示
                                dialog.show();//追加課題の画面を表示
                            }
                        });

                        className = findViewById(R.id.classnameView);
                        classRoom = findViewById(R.id.classroomView);

                        className.setText(ClassData.getInfor().className);
                        classRoom.setText(ClassData.getInfor().classRoom);

                        NotifyManager.notifyClassInfor(MainActivity.this,ClassData.getInfor().className,ClassData.getInfor().classRoom);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            long nextHour=(long)ClassData.getInfor().nextTiming.getHour()-(long)currentDateTime.getHour();
                            long nextMinute=(long)ClassData.getInfor().nextTiming.getMinute()-(long)currentDateTime.getMinute();
                            Log.d("aaa","nextTiming is "+ClassData.getInfor().nextTiming);
                            long currentTimeMillis = System.currentTimeMillis()+nextHour* 3600000L +nextMinute* 60000L;
                            if(ClassData.getInfor().judge)currentTimeMillis+=86400000L;

                            Log.d("aaa", String.valueOf((currentTimeMillis-System.currentTimeMillis())/60000)+"分後に教室の情報を更新します");
                            Log.d("aaa", String.valueOf(currentTimeMillis)+"これが教室更新のエポックタイム");

                            Intent intent = new Intent();
                            intent.setAction("CLASSACTION");

                            AlarmManager am = (AlarmManager) getSystemService( ALARM_SERVICE );
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            am.set(AlarmManager.RTC_WAKEUP, currentTimeMillis, pendingIntent);

                            Log.d("aaa", "教室情報更新アラーム完了");

                        }
                    }
                });
            });

        }

        //getDataFromDatabase();
        //getDataFromManaba();
    }




    public void getDataFromManaba(){
        urlList.add("https://ct.ritsumei.ac.jp/ct/home_summary_query");
        urlList.add("https://ct.ritsumei.ac.jp/ct/home_summary_survey");
        urlList.add("https://ct.ritsumei.ac.jp/ct/home_summary_report");
        CompletableFuture<String> future1 = null;//非同期処理をするために、CompletableFuture型のデータを使う。

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない

            future1.thenApply(res -> {//上で設定した非同期処理を行う。
                return res; //
            }).thenAccept(result-> {
                Log.d("aaa","スクレーピング完了！");
                //非同期処理を行った後にする処理（課題追加の部分やからまだいいかも）
                runOnUiThread(() -> {//UIスレッドで処理を行う（画面描写に関する処理のため）
                    // UI スレッドでテキストビューを更新する

                    tabLayout = findViewById(R.id.tab_layout);
                    viewPager = findViewById(R.id.view_pager);

                    className = findViewById(R.id.classnameView);
                    classRoom = findViewById(R.id.classroomView);

                    className.setText(ClassData.getInfor().className);
                    classRoom.setText(ClassData.getInfor().classRoom);
                    NotifyManager.notifyClassInfor(this,ClassData.getInfor().className,ClassData.getInfor().classRoom);

                    PagerAdapter2 adapter = new PagerAdapter2(this);
                    viewPager.setAdapter(adapter);

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("CLASSACTION");
                    registerReceiver(classInforReceiver, intentFilter );


                    // TabLayoutとViewPager2を連携
                    new TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> tab.setText(tabName.get(position))
                    ).attach();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            long nextHour=(long)ClassData.getInfor().nextTiming.getHour()-(long)currentDateTime.getHour();
                            long nextMinute=(long)ClassData.getInfor().nextTiming.getMinute()-(long)currentDateTime.getMinute();
                            Log.d("aaa","nextTiming is "+ClassData.getInfor().nextTiming);
                            long currentTimeMillis = System.currentTimeMillis()+nextHour* 3600000L +nextMinute* 60000L;
                            if(ClassData.getInfor().judge)currentTimeMillis+=86400000L;

                            Log.d("aaa", String.valueOf((currentTimeMillis-System.currentTimeMillis())/60000)+"分後に教室の情報を更新します");
                            Log.d("aaa", String.valueOf(currentTimeMillis)+"これが教室更新のエポックタイム");

                        Intent intent = new Intent();
                        intent.setAction("CLASSACTION");

                        AlarmManager am = (AlarmManager) getSystemService( ALARM_SERVICE );
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        am.set(AlarmManager.RTC_WAKEUP, currentTimeMillis, pendingIntent);

                        Log.d("aaa", "教室情報更新アラーム完了");

                    }
                });
            });
        }
    }


}
