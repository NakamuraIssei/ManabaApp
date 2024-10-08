package com.example.ManabaApp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class NotificationCustomAdapter extends RecyclerView.Adapter<NotificationCustomAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<LocalDateTime> notificationTiming;
    private final int taskDataId;

    public NotificationCustomAdapter(Context context, ArrayList<LocalDateTime> notificationTiming, int taskDataId) {
        this.context = context;
        this.notificationTiming = notificationTiming;
        this.taskDataId = taskDataId;
    }

    @NonNull
    @Override
    public NotificationCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_custom_adapter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationCustomAdapter.ViewHolder holder, int position) {
        DateTimeFormatter formatter = null;
        // チェックボックスの初期化: 毎回非表示とチェックなしにリセットする
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM/dd　HH:mm");
            String time = notificationTiming.get(position).format(formatter);
            time = time.substring(5);
            holder.text1.setText(time);
        } else holder.text1.setText("アンドロイドのバージョンが小さいです");
        holder.editButton.setOnClickListener(v -> {
            Log.d("aaa", "通知編集ボタン押せてるよー");
            AddNotificationBottomSheetDialog dialog = new AddNotificationBottomSheetDialog(context, 1, taskDataId, this);
            dialog.setEditNotificationNum(position);
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        //リサイクルビューに表示されるデータの数
        return notificationTiming.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton editButton;
        TextView text1;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editButton = itemView.findViewById(R.id.button2);
            text1 = itemView.findViewById(android.R.id.text1);
            checkBox=itemView.findViewById(R.id.notificationSwitch);
        }
    }
}