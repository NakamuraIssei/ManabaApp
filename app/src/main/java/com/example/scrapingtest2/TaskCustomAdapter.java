package com.example.scrapingtest2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

public class TaskCustomAdapter extends RecyclerView.Adapter<TaskCustomAdapter.ViewHolder>{
    private Context context;
    private TaskDataManager taskDataManager;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageButton pushButton;
        TextView text1;
        TextView text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pushButton = itemView.findViewById(R.id.button2);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
    public TaskCustomAdapter(Context context,TaskDataManager taskDataManager) {
        this.context = context;
        this.taskDataManager=taskDataManager;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homeworkadapter_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String before=String.valueOf(taskDataManager.getAllTaskDataList().get(position).getDueDate());
        char[] charArray = before.toCharArray();
        charArray[10] = ' ';
        String deadline = new String(charArray);
        //ここまで期限の文字列の整理操作
        holder.text1.setText(taskDataManager.getAllTaskDataList().get(position).getTaskName());
        holder.text2.setText(deadline);
        if(!taskDataManager.getAllTaskDataList().get(position).getNotificationTiming().isEmpty())holder.pushButton.setImageResource(R.drawable.bell_round);

        holder.pushButton.setOnClickListener(v -> {
            Log.d("aaa","ベルボタン押せてるよー！TaskCustomAdapter 65");
            // ダイアログクラスのインスタンスを作成
            NotificationCustomDialog dialog = new NotificationCustomDialog(context,position,taskDataManager);//追加課題の画面のインスタンスを生成
            // ダイアログを表示
            dialog.show();//追加課題の画面を表示

        });
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return taskDataManager.getAllTaskDataList().size();
    }

}

