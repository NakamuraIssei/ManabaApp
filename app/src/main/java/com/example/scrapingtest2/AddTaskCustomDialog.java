package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddTaskCustomDialog extends Dialog {

    private TaskCustomAdapter adapter;
    private TaskDataManager taskDataManager;
    static ClassGridAdapter classGridAdapter;

    public AddTaskCustomDialog(Context context, TaskCustomAdapter adapter,TaskDataManager taskDataManager) {
        super(context);
        this.adapter=adapter;
        this.taskDataManager=taskDataManager;
    }
    static void setGridAdapter(ClassGridAdapter classGridAdapter){
        AddTaskCustomDialog.classGridAdapter = classGridAdapter;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task_dialog_layout);

        // ダイアログの各要素や操作を設定

        // 例: ダイアログ内のボタンをクリックしたらダイアログを閉じる
        Button addButton = findViewById(R.id.addbutton);
        EditText title2 = findViewById(R.id.title2);
        EditText year = findViewById(R.id.year);
        EditText month = findViewById(R.id.month);
        EditText day = findViewById(R.id.day);
        EditText hour = findViewById(R.id.hour);
        EditText minute = findViewById(R.id.minute);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText URL = findViewById(R.id.classURL);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText name = findViewById(R.id.className);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yearvalue=year.getText().toString();
                String monthvalue=month.getText().toString();
                String dayvalue=day.getText().toString();
                String hourvalue=hour.getText().toString();
                String minutevalue=minute.getText().toString();
                String className=name.getText().toString();
                String taskURL=URL.getText().toString();

                if(monthvalue.length()<2)monthvalue='0'+monthvalue;
                if(dayvalue.length()<2)dayvalue='0'+dayvalue;
                if(hourvalue.length()<2)hourvalue='0'+hourvalue;
                if(minutevalue.length()<2)minutevalue='0'+minutevalue;

                final String deadline =yearvalue+'-'+monthvalue+'-'+dayvalue+' '+hourvalue+':'+minutevalue;
                Log.d("aaa", deadline+"AddTaskCustomDialog 52");
                //TaskData context=new TaskData(title2.getText().toString(),1,"hoeghoge",deadline);
                Log.d("aaa", title2.getText().toString()+"を追加します。AddTaskCustomDialog 54");
                taskDataManager.addTaskData(title2.getText().toString(),deadline,className,taskURL);
                taskDataManager.sortAllTaskDataList();
                //TaskData.addTask(context,1);//第二引数はdbに書き込むから1。書き込まないなら0
                adapter.notifyDataSetChanged();
                classGridAdapter.notifyDataSetChanged();
                Log.d("aaa", String.valueOf(Thread.currentThread())+"AddTaskCustomDialog 56");
                dismiss();  // ダイアログを閉じる
            }
        });
    }
}
