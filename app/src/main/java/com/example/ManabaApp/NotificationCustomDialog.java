package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class NotificationCustomDialog extends Dialog {
    private final int position;
    private final Context context;
    private final TaskDataManager taskDataManager;

    public NotificationCustomDialog(Context context, int position, TaskDataManager taskDataManager) {
        super(context);
        this.context = context;
        this.position = position;
        this.taskDataManager = taskDataManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notificationcustom_dialog_layout);

        RecyclerView notificationRecyclerView = findViewById(R.id.notificationRecyclerView);

        Button taskPageButton = findViewById(R.id.taskPageButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button editButton = findViewById(R.id.editButton);
        ImageButton addNotifyButton = findViewById(R.id.addNotifyButton);
        ImageButton trashBoxButton = findViewById(R.id.trashBoxButtonButton);
        TextView selectedTaskNameText = findViewById(R.id.selectedTaskNameText);
        selectedTaskNameText.setText(taskDataManager.getAllTaskDataList().get(position).getTaskName());

        cancelButton.setVisibility(View.GONE);
        trashBoxButton.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        notificationRecyclerView.setLayoutManager(layoutManager);

        ArrayList<LocalDateTime> notificationList = taskDataManager.getAllTaskDataList().get(position).getNotificationTiming();

        NotificationCustomAdapter adapter = new NotificationCustomAdapter(context, notificationList, position);
        notificationRecyclerView.setAdapter(adapter);
        taskDataManager.addAdapter(position, adapter);

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ct.ritsumei.ac.jp/ct/" + taskDataManager.getAllTaskDataList().get(position).getTaskURL()));
        chromeIntent.setPackage("com.android.chrome");

        // Load the trash icon drawable
        Drawable trashIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete); // Ensure the correct drawable resource


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Handle swipe deletion if needed
                int swipedPosition = viewHolder.getAdapterPosition();
                NotificationCustomAdapter adapter = (NotificationCustomAdapter) notificationRecyclerView.getAdapter();
                taskDataManager.deleteTaskNotification(position, swipedPosition);
                adapter.notifyItemRemoved(swipedPosition);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();


                // Draw red background for left swipe
                if (dX < 0) {
                    ColorDrawable background = new ColorDrawable(Color.parseColor("#FF0000"));
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Calculate position for trash icon
                    int iconMargin = (itemHeight - trashIcon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + trashIcon.getIntrinsicHeight();
                    int iconLeft = itemView.getRight() - iconMargin - trashIcon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;

                    trashIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    trashIcon.draw(c);
                }


            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < notificationRecyclerView.getChildCount(); i++) {
                    View childView = notificationRecyclerView.getChildAt(i);
                    CheckBox checkBox = childView.findViewById(R.id.itemCheckBox);
                    if (checkBox != null) {
                        checkBox.setVisibility(View.VISIBLE);
                    }
                }
                addNotifyButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                trashBoxButton.setVisibility(View.VISIBLE);
                Log.d("LongPress", "All items moved to the right and checkboxes displayed.");
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < notificationRecyclerView.getChildCount(); i++) {
                    View childView = notificationRecyclerView.getChildAt(i);
                    childView.setTranslationX(0);
                    CheckBox checkBox = childView.findViewById(R.id.itemCheckBox);
                    if (checkBox != null) {
                        checkBox.setVisibility(View.GONE);
                    }
                }
                cancelButton.setVisibility(View.GONE);
                trashBoxButton.setVisibility(View.GONE);
                editButton.setVisibility(View.VISIBLE);
                addNotifyButton.setVisibility(View.VISIBLE);
                Log.d("CancelButton", "All items moved back to the original position and checkboxes hidden.");
            }
        });

        addNotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNotificationBottomSheetDialog bottomSheetDialog = new AddNotificationBottomSheetDialog(context, 0, position, adapter);
                bottomSheetDialog.show();
            }
        });
        trashBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // RecyclerView内のすべてのアイテムを元に戻す
                ArrayList<Integer>deleteNum=new ArrayList<>();
                for (int i = 0; i < notificationRecyclerView.getChildCount(); i++) {
                    View childView = notificationRecyclerView.getChildAt(i);
                    childView.setTranslationX(0);  // アイテムを元の位置に戻す

                    // チェックボックスを非表示にする
                    CheckBox checkBox = childView.findViewById(R.id.itemCheckBox);
                    if (checkBox != null&&checkBox.isChecked()) {
//                        NotificationCustomAdapter adapter = (NotificationCustomAdapter) notificationRecyclerView.getAdapter();
//                        taskDataManager.deleteTaskNotification(position, i);
//                        adapter.notifyItemRemoved(i);
                        deleteNum.add(i);
                    }
                }
                for(int i=0;i<deleteNum.size();i++){
                    deleteNum.set(i,deleteNum.get(i)-i);
                }
                for(int num:deleteNum){
                    NotificationCustomAdapter adapter = (NotificationCustomAdapter) notificationRecyclerView.getAdapter();
                    taskDataManager.deleteTaskNotification(position, num);
                    adapter.notifyItemRemoved(num);
                }
            }
        });

        taskPageButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                getContext().startActivity(chromeIntent);
            }
        });
    }
}
