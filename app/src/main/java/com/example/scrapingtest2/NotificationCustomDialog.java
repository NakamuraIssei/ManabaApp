package com.example.scrapingtest2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class NotificationCustomDialog extends Dialog {
    private int position;
    private Context context;

    public NotificationCustomDialog(Context context, int position, TaskCustomAdapter homeworkCustomAdapter) {
        super(context);
        this.context=context;
        this.position=position;
        //this.homeworkCustomAdapter=homeworkCustomAdapter;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_notification_dialog_layout);

        // ダイアログの各要素や操作を設定

        // 例: ダイアログ内のボタンをクリックしたらダイアログを閉じる
        RecyclerView recyclerView= findViewById(R.id.notifyRecyclerView);

        Button addNotifyButton = findViewById(R.id.addNotifyButton);

        // LinearLayoutManagerを設定する
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<LocalDateTime> notificationList = TaskDataManager.getDataList().get(position).getNotificationTiming();//タップされた行の課題の通知情報を取得

        NotificationCustomAdapter adapter = new NotificationCustomAdapter(context, notificationList,position);//Listviewを表示するためのadapterを設定
        recyclerView.setAdapter(adapter);//listViewにadapterを設定
        TaskDataManager.addAdapter(position,adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped (@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                NotificationCustomAdapter adapter = (NotificationCustomAdapter) recyclerView.getAdapter();
                // 登録とかするんだったらなにかのリストから削除をする処理はここ
                TaskDataManager.deleteTaskNotification(position,swipedPosition);
                //TaskData.taskData.get(position).cancelNotification(swipedPosition);
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
                    //Drawable background;
                    //if (dX <= 0) {
                        //background = new ColorDrawable(Color.RED); // 右にスワイプしたときの背景
                    //} else {
                        //background = new ColorDrawable(Color.GREEN); // 左にスワイプしたときの背景
                    //}
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

        addNotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNotificationDialog.setNotificationN(context,position,adapter);
            }
        });
    }
}
