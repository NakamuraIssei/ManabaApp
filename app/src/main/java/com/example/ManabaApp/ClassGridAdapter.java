package com.example.ManabaApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class ClassGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ClassData> classDataList;  // 表示するデータに合わせて変更
    private ArrayList<String> Day;
    private int rowNum;
    private int columnNum;

    //private int columns
    public ClassGridAdapter(Context context, ArrayList<ClassData> classDataList) {
        this.context = context;
        this.classDataList = classDataList;
        this.Day = new ArrayList<String>();
        this.Day.add("");
        this.Day.add("月");
        this.Day.add("火");
        this.Day.add("水");
        this.Day.add("木");
        this.Day.add("金");
        this.Day.add("土");
        this.Day.add("日");
    }

    public void customGridSize() {
        rowNum = 4;
        columnNum = 5;
        for (int i = 0; i < classDataList.size(); i++) {
            if (!Objects.equals(classDataList.get(i).getClassName(), "次は空きコマです。")) {
                rowNum = Math.max(rowNum, (i % 7) + 1);
                columnNum = Math.max(columnNum, (i / 7) + 1);
            }
        }
    }

    @Override
    public int getCount() {
        return (rowNum + 1) * (columnNum + 1); // 全体のマス数
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // positionの計算
        int row = position % (columnNum + 1);
        int col = position / (columnNum + 1);
        // レイアウトをインフレートして新しいセルを作成
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.class_table_cell_layout, null);
        holder = new ViewHolder();
        holder.dateText = convertView.findViewById(R.id.dateText);
        convertView.setBackgroundResource(R.drawable.empty_class_grid);

        // セルのテキスト設定および余白の設定
        if (row == 0 && col == 0) {
            holder.dateText.setText("　"); // セル(0, 0)のテキストは空
        } else if (row == 0) {
            holder.dateText.setText(String.valueOf(col));
        } else if (col == 0) {
            holder.dateText.setText(this.Day.get(row));
        } else {
            // セルにデータを表示
            ClassData classData = classDataList.get((row - 1) * 7 + (col - 1));

            // 特定の条件を満たす場合に背景色を設定
            if (Objects.equals(classData.getClassRoom(), "")) {
                convertView.setBackgroundResource(R.drawable.empty_class_grid);
            } else {
                if (classData.hasTask()) {
                    convertView.setBackgroundResource(R.drawable.hastask_class_grid);
                } else {
                    convertView.setBackgroundResource(R.drawable.normal_class_grid);
                }
            }
        }
        convertView.setTag(holder);
        return convertView;
    }

    private static class ViewHolder {
        public TextView dateText;
    }

}

