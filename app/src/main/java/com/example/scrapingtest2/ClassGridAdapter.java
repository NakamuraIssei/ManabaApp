package com.example.scrapingtest2;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
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
    private ArrayList<String>Day;
    private static class ViewHolder {
        public TextView dateText;
    }

    public ClassGridAdapter(Context context, ArrayList<ClassData> classDataList) {
        this.context = context;
        this.classDataList= classDataList;
        this.Day=new ArrayList<String>();
        this.Day.add("");
        this.Day.add("月");
        this.Day.add("火");
        this.Day.add("水");
        this.Day.add("木");
        this.Day.add("金");
        this.Day.add("土");
        this.Day.add("日");
    }
    @Override
    public int getCount() {
        return 8*8; // 7x7のセル数
    }
    @Override
    public Object getItem(int position) {
        // セルのデータを取得
        int row = position / 8;
        int col = position % 8;
        return classDataList.get(8*row+col);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // positionの計算
        int row = position / 8;
        int col = position % 8;
        position = col * 8 + row;
            // レイアウトをインフレートして新しいセルを作成
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.class_table_cell, null);
            holder = new ViewHolder();
            holder.dateText = convertView.findViewById(R.id.dateText);
            convertView.setBackgroundResource(R.drawable.empty_class_grid);

            // セルのテキスト設定および余白の設定
            if(row == 0 && col == 0) {
                holder.dateText.setText(""); // セル(0, 0)のテキストは空
            } else if (row == 0) {
                holder.dateText.setText(this.Day.get(col));
            } else if (col == 0) {
                holder.dateText.setText(String.valueOf(row));
            } else {
                // セルにデータを表示
                Log.d("aaa", (position - 8) - (position / 8) + "番目を時間割にひゅおうじします。GridAdapter 68");
                ClassData classData = classDataList.get((position - 8) - (position / 8));

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
//            AbsListView.LayoutParams params = new AbsListView.LayoutParams(calculateCellWidth(), calculateCellHeight());
//            convertView.setLayoutParams(params);
            convertView.setTag(holder);

        return convertView;
    }
    private int calculateCellWidth() {
        // ここでセルの大きさを計算するロジックを実装
        // 例: 画面の幅を7で割った値をセルの大きさとする
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        return screenWidth * (23/3);
    }
    private int calculateCellHeight() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        return screenWidth /8;
    }

}

