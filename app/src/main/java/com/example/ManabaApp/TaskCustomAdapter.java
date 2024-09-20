package com.example.ManabaApp;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class TaskCustomAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private final Context context;
    private final TaskDataManager taskDataManager;
    private final LayoutInflater inflater;
    private final ArrayList<String> sectionTitle = new ArrayList<>(Arrays.asList(
            "提出した課題",
            "今日",
            "明日",
            "明後日以降"
    ));

    public TaskCustomAdapter(Context context,TaskDataManager taskDataManager) {
        this.context = context;
        this.taskDataManager=taskDataManager;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        HeaderViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.task_headder_layout, null);
            holder = new HeaderViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) view.getTag();
        }
        holder.sectionText.setText(sectionTitle.get(taskDataManager.getTaskGroupId(i)));
        holder.sectionText.setTextColor(ContextCompat.getColor(context, R.color.black));
        return view;
    }
    @Override
    public long getHeaderId(int i) {
        return getHeaderItem(i);
    }
    public int getHeaderItem(int position) {
        return taskDataManager.getTaskGroupId(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder holder;
        TaskData taskData=taskDataManager.getAllTaskDataList().get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_item_layout, parent, false);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
            if (holder.countDownTimer != null) {
                holder.countDownTimer.cancel();
            }
        }
        String before=String.valueOf(taskData.getDueDate());
        char[] charArray = before.toCharArray();
        charArray[10] = ' ';
        String deadline = new String(charArray);
        //ここまで期限の文字列の整理操作
        holder.taskNameText.setText(taskData.getTaskName());
        holder.taskDeadlineText.setText(deadline);

        if(taskDataManager.getTaskGroupId(position)==1&&android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime dueDateTime = taskData.getDueDate();

            // タイムアウトしていない場合
            if (dueDateTime.isAfter(currentTime)) {
                Duration duration = Duration.between(currentTime, dueDateTime);
                long millisecondsLeft = duration.toMillis();

                holder.countDownTimer=new CountDownTimer(millisecondsLeft, 1000) {
                    public void onTick(long millisUntilFinished) {
                        long hoursLeft = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                        long minutesLeft = (millisUntilFinished / (1000 * 60)) % 60;
                        long secondsLeft = (millisUntilFinished / 1000) % 60;

                        String countdownText = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                                hoursLeft, minutesLeft, secondsLeft);

                        holder.remainingTimeText.setText(countdownText);
                    }

                    public void onFinish() {
                        holder.remainingTimeText.setText("期限切れ");
                    }
                }.start();

                holder.remainingTimeText.setVisibility(View.VISIBLE);
            } else {
                // タイムアウトした場合の処理
                holder.remainingTimeText.setText("");
                holder.remainingTimeText.setVisibility(View.VISIBLE);
            }
        }else holder.remainingTimeText.setText("");

        // convertViewのクリックリスナーを設定
        convertView.setOnClickListener(v -> {
            Log.d("aaa", "アイテム全体が押せてるよー！TaskCustomAdapter 65");
            NotificationCustomDialog dialog = new NotificationCustomDialog(context, position, taskDataManager);
            dialog.show();
        });

        if(taskDataManager.getTaskGroupId(position)==0)convertView.setBackgroundColor(ContextCompat.getColor(context, androidx.cardview.R.color.cardview_dark_background));
        else convertView.setBackgroundColor(ContextCompat.getColor(context, androidx.cardview.R.color.cardview_light_background));

        holder.remainingTimeText.setText("");
        return convertView;
    }
    @Override
    public int getCount() {
        return taskDataManager.getAllTaskDataList().size();
    }
    @Override
    public Object getItem(int position) {
        return null;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public static class HeaderViewHolder {
        TextView sectionText;
        public HeaderViewHolder(@NonNull View itemView) {
            sectionText = itemView.findViewById(android.R.id.text1);
        }
    }
    public static class ItemViewHolder {
        ImageButton pushButton;
        TextView taskNameText;
        TextView taskDeadlineText;
        TextView remainingTimeText;
        CountDownTimer countDownTimer;


        public ItemViewHolder(@NonNull View itemView) {
            pushButton = itemView.findViewById(R.id.button2);
            taskNameText = itemView.findViewById(android.R.id.text1);
            taskDeadlineText = itemView.findViewById(android.R.id.text2);
            remainingTimeText= itemView.findViewById(R.id.remainingTime);
        }
    }
}
