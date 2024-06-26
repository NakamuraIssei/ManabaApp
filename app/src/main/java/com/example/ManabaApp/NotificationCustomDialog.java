package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class NotificationCustomDialog extends Dialog {
    private final int position;
    private final Context context;
    private final TaskDataManager taskDataManager;

    public NotificationCustomDialog(Context context, int position,TaskDataManager taskDataManager) {
        super(context);
        this.context=context;
        this.position=position;
        this.taskDataManager=taskDataManager;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notificationcustom_dialog_layout);

        // ダイアログの各要素や操作を設定

        // 例: ダイアログ内のボタンをクリックしたらダイアログを閉じる
        RecyclerView notificationRecyclerView= findViewById(R.id.notificationRecyclerView);

        Button taskPageButton=findViewById(R.id.taskPageButton);
        ImageButton addNotifyButton = findViewById(R.id.addNotifyButton);
        TextView selectedTaskNameText= findViewById(R.id.selectedTaskNameText);
        selectedTaskNameText.setText(taskDataManager.getAllTaskDataList().get(position).getTaskName());

        // LinearLayoutManagerを設定する
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        notificationRecyclerView.setLayoutManager(layoutManager);

        ArrayList<LocalDateTime> notificationList = taskDataManager.getAllTaskDataList().get(position).getNotificationTiming();//タップされた行の課題の通知情報を取得

        NotificationCustomAdapter adapter = new NotificationCustomAdapter(context, notificationList,position);//Listviewを表示するためのadapterを設定
        notificationRecyclerView.setAdapter(adapter);//listViewにadapterを設定
        taskDataManager.addAdapter(position,adapter);

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ct.ritsumei.ac.jp/ct/"+taskDataManager.getAllTaskDataList().get(position).getTaskURL()));
        chromeIntent.setPackage("com.android.chrome");  // Chromeのパッケージ名を指定

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped (@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                NotificationCustomAdapter adapter = (NotificationCustomAdapter) notificationRecyclerView.getAdapter();
                // 登録とかするんだったらなにかのリストから削除をする処理はここ
                taskDataManager.deleteTaskNotification(position,swipedPosition);
                // 削除されたことを知らせて反映させる。
                adapter.notifyItemRemoved(swipedPosition);
            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    //Drawable background;
                    if (dX < 0) {
                        ColorDrawable background = new ColorDrawable();
                        background .setColor(Color.parseColor("#FF0000"));
                        background.setBounds(itemView.getRight(), itemView.getTop(), itemView.getRight() + (int)dX, itemView.getBottom());
                        background.draw(c);
                    }
                }
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView);

        addNotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNotificationBottomSheetDialog bottomSheetDialog = new AddNotificationBottomSheetDialog(context,0,position,adapter);//新規追加なのでoperationModeは0
                bottomSheetDialog.show();
            }
        });
        taskPageButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {//ボタンが押されたら
                getContext().startActivity(chromeIntent);
            }
        });
    }
}