package com.example.scrapingtest2;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class NotificationCustomAdapter extends RecyclerView.Adapter<NotificationCustomAdapter.ViewHolder>{

    private Context context;
    private ArrayList<LocalDateTime> notificationTiming;
    private int taskDataId;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageButton editButton;
        TextView text1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editButton = itemView.findViewById(R.id.button2);
            text1 = itemView.findViewById(android.R.id.text1);
        }
    }
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String time = notificationTiming.get(position).format(formatter);
            holder.text1.setText(String.valueOf(time));
        }
        else holder.text1.setText("アンドロイドのバージョンが小さいです");
        holder.editButton.setOnClickListener(v -> {
            Log.d("aaa","通知編集ボタン押せてるよー");
            AddNotificationDialog dialog=new AddNotificationDialog(context);
            dialog.editNotificationN(taskDataId,this,position);
        });
    }
    @Override
    public int getItemCount() {
        //リサイクルビューに表示されるデータの数
        return notificationTiming.size();
    }
}
