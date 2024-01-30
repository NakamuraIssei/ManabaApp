package com.example.scrapingtest2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.time.ZoneId;


public class AddNotificationDialog extends Dialog {
    private Calendar selectedTime;
    private static TaskDataManager taskDataManager;
    private Context context;

    public AddNotificationDialog(Context context) {
        super(context);
        this.context = context;
    }

    public static void setTaskdataManager(TaskDataManager taskDataManager){
        AddNotificationDialog.taskDataManager=taskDataManager;
    }

    public void setNotificationN(int position, NotificationCustomAdapter adapter ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedTime = Calendar.getInstance();
            int year = selectedTime.get(Calendar.YEAR);
            int month = selectedTime.get(Calendar.MONTH);
            int day = selectedTime.get(Calendar.DAY_OF_MONTH);
            int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
            int minute = selectedTime.get(Calendar.MINUTE);
            int second = selectedTime.get(Calendar.MILLISECOND);
            Log.d("aaa", String.valueOf(selectedTime.getTimeInMillis()));

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int selectedYear, int monthOfYear, int dayOfMonth) {
                            selectedTime.set(Calendar.YEAR, selectedYear);
                            selectedTime.set(Calendar.MONTH, monthOfYear);
                            selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    context,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                                            selectedTime.set(Calendar.MINUTE, selectedMinute);
                                            selectedTime.set(Calendar.SECOND, 0);

                                            Log.d("aaa", selectedYear + "年");
                                            Log.d("aaa", (monthOfYear + 1) + "月"); // 月は0から始まるため+1する
                                            Log.d("aaa", dayOfMonth + "日");
                                            Log.d("aaa", selectedHour + "時");
                                            Log.d("aaa", selectedMinute + "分");
                                            Log.d("aaa", String.valueOf(selectedTime.getTimeInMillis()));

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                taskDataManager.addNotificationTiming(position,selectedTime.getTime().toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime());
                                                //taskDataManager.changeBellButton(position);

                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    },
                                    hour,
                                    minute,
                                    true
                            );
                            timePickerDialog.show();
                        }
                    },
                    year,
                    month,
                    day
            );
            datePickerDialog.show();
        }
    }
    public void editNotificationN(int taskDataId, NotificationCustomAdapter adapter, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedTime = Calendar.getInstance();
            int year = selectedTime.get(Calendar.YEAR);
            int month = selectedTime.get(Calendar.MONTH);
            int day = selectedTime.get(Calendar.DAY_OF_MONTH);
            int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
            int minute = selectedTime.get(Calendar.MINUTE);
            int second = selectedTime.get(Calendar.MILLISECOND);
            Log.d("aaa", String.valueOf(selectedTime.getTimeInMillis()));

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int selectedYear, int monthOfYear, int dayOfMonth) {
                            selectedTime.set(Calendar.YEAR, selectedYear);
                            selectedTime.set(Calendar.MONTH, monthOfYear);
                            selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    context,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                                            selectedTime.set(Calendar.MINUTE, selectedMinute);
                                            selectedTime.set(Calendar.SECOND, 0);

                                            Log.d("aaa", selectedYear + "年");
                                            Log.d("aaa", (monthOfYear + 1) + "月"); // 月は0から始まるため+1する
                                            Log.d("aaa", dayOfMonth + "日");
                                            Log.d("aaa", selectedHour + "時");
                                            Log.d("aaa", selectedMinute + "分");
                                            Log.d("aaa", String.valueOf(selectedTime.getTimeInMillis()));

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                //まず編集する通知を削除する
                                                taskDataManager.deleteTaskNotification(taskDataId,position);
                                                //taskData.cancelNotification(position);
                                                //再設定した通知を追加
                                                taskDataManager.addNotificationTiming(taskDataId,selectedTime.getTime().toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime());
                                                //変更を反映
                                                adapter.notifyDataSetChanged();
                                                //taskDataManager.changeBellButton(taskDataId);

                                            }
                                        }
                                    },
                                    hour,
                                    minute,
                                    true
                            );
                            timePickerDialog.show();
                        }
                    },
                    year,
                    month,
                    day
            );
            datePickerDialog.show();
        }
    }

}
