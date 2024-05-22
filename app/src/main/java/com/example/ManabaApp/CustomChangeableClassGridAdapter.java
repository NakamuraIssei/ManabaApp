package com.example.ManabaApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class CustomChangeableClassGridAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<ClassData> classDataList;  // 表示するデータに合わせて変更
    private final ArrayList<String> Day;
    private int rowNum;
    private int columnNum;
    static String emptyClassName="次は空きコマです。";
    private ClassData registerationClassData;

    public CustomChangeableClassGridAdapter(Context context, ArrayList<ClassData> classDataList, ClassData registerationClassData ) {
        this.context = context;
        this.classDataList = classDataList;
        this.registerationClassData=registerationClassData;
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

    public void setRowNum(int rowNum){
        this.rowNum=rowNum;
    }
    public void setColumnNum(int columnNum){
        this.columnNum=columnNum;
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
            if (Objects.equals(classData.getClassName(), emptyClassName)) {
                convertView.setBackgroundResource(R.drawable.empty_class_grid);
            } else if(Objects.equals(classData.getClassName(), registerationClassData.getClassName())){
                convertView.setBackgroundResource(R.drawable.normal_class_grid);
            } else{
                    convertView.setBackgroundResource(R.drawable.unselectable_class_grid);
            }
        }
        convertView.setTag(holder);
        return convertView;
    }

    private static class ViewHolder {
        public TextView dateText;
    }

}