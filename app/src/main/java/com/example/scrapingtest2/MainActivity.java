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

public class MainActivity extends AppCompatActivity implements NotificationListener {

    private HashMap<String, String> cookiebag;
    private TextView className;
    private TextView classRoom;
    private ClassDataManager cd;

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

        ManabaScraper.setCookie(cookiebag);

        NotifyManager2.prepareForNotificationWork(this);
        TaskDataManager taskDataManager=new TaskDataManager("TaskData");
        cd=new ClassDataManager("ClassData");
        NotificationReceiver2.setTaskDataManager(taskDataManager);

        NotifyManager2.setNotificationListener(this);

        MyDBHelper myDBHelper = new MyDBHelper(this);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor taskCursor = db.query("TaskData", null, null, null, null, null, "myId");
        Cursor classCursor = db.query("ClassData", null, null, null, null, null, "myId");

        taskDataManager.setDB(db,taskCursor);
        cd.setDB(db,classCursor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Supplier<String> setData = () -> {
                taskDataManager.setTaskData();
                Log.d("aaa","課題セッティング完了！MainActivity 110");
                cd.setClassData();

                return null;
            };

            CompletableFuture.supplyAsync(setData).thenAccept(result2 -> {
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
                        TaskCustomAdapter adapter = new TaskCustomAdapter(MainActivity.this, taskDataManager);//Listviewを表示するためのadapterを設定
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
                                taskDataManager.removeTaskData(swipedPosition);
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
                                AddTaskCustomDialog dialog = new AddTaskCustomDialog(MainActivity.this,adapter,taskDataManager);//追加課題の画面のインスタンスを生成
                                // ダイアログを表示
                                dialog.show();//追加課題の画面を表示
                            }
                        });

                        className = findViewById(R.id.classnameView);
                        classRoom = findViewById(R.id.classroomView);

                        Data now=cd.getClassInfor();

                        className.setText(now.getTitle());
                        classRoom.setText(now.getSubTitle());
                    }
                });
            });

        }
    }
    @Override
    public void onNotificationReceived(int dataId) {
        dataId=(dataId+49)%49;
        className.setText(cd.dataList.get(dataId).getTitle());
        classRoom.setText(cd.dataList.get(dataId).getSubTitle());
    }

}
