package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class AddNotificationBottomSheetDialog extends BottomSheetDialog {
    private TextView selectedTimeView,selectedDateView;
    private int taskId,editNotificationNum,operationMode;//新規追加0、編集1
    private NotificationCustomAdapter adapter;
    private static TaskDataManager taskDataManager;


    public AddNotificationBottomSheetDialog(@NonNull Context context,int operationMode,int taskId,NotificationCustomAdapter adapter) {
        super(context);
        this.operationMode=operationMode;
        this.taskId=taskId;
        this.adapter=adapter;

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                // ここで高さを設定します。ピクセル単位で指定します。
                behavior.setPeekHeight(0); // 例えば、1000ピクセルに設定します。
            }
        });
    }
    public void setEditNotificationNum(int editNotificationNum){this.editNotificationNum=editNotificationNum;}
    public static void setTaskDataManager(TaskDataManager taskDataManager){
        AddNotificationBottomSheetDialog.taskDataManager=taskDataManager;
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_notification_dialog_layout);

        Button cancelButton = findViewById(R.id.cancelButton);
        Button saveButton = findViewById(R.id.saveButton);
        CalendarView calendarView = findViewById(R.id.calendarView);
        TimePicker timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        selectedTimeView =findViewById(R.id.selectedTimeView);
        selectedDateView=findViewById(R.id.selectedDateView);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ダイアログを閉じる
                dismiss();
            }
        });
        // CalendarViewの日付変更リスナーを設定
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDateView.setText("");
                selectedDateView.setText(year+"年"+(month+1)+"月"+dayOfMonth+"日");
            }
        });

        // TimePickerの時刻変更リスナーを設定
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                selectedTimeView.setText(hourOfDay+"時"+minute+"分");
            }
        });

        // 保存ボタンのクリックリスナーを設定
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // 選択された日付と時刻を取得するためのテキストを取得
                    String selectedDateText = selectedDateView.getText().toString();
                    String selectedTimeText = selectedTimeView.getText().toString();

                    // 選択された日付と時刻を解析して、年月日時分を取得
                    String[] dateParts = selectedDateText.split("年|月|日");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int dayOfMonth = Integer.parseInt(dateParts[2]);

                    String[] timeParts = selectedTimeText.split("時|分");
                    int hourOfDay = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);

                    // 選択された日付と時刻を日本のLocalDateTime型に変換
                    LocalDateTime selectedDateTime = LocalDateTime.of(year, month, dayOfMonth, hourOfDay, minute);
                    if(operationMode==1){//新規通知追加の処理
                        taskDataManager.deleteTaskNotification(taskId,editNotificationNum);
                    }
                    taskDataManager.addNotificationTiming(taskId,selectedDateTime);

                    adapter.notifyDataSetChanged();
                }
                dismiss();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 現在の日時を日本時間で取得
            ZonedDateTime japanTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));

            // 選択された日付を表示
            selectedDateView.setText(japanTime.getYear() + "年" +japanTime.getMonthValue() + "月" + japanTime.getDayOfMonth() + "日");

            // 選択された時刻を表示
            selectedTimeView.setText(japanTime.getHour() + "時" + japanTime.getMinute() + "分");
        }

    }
}
