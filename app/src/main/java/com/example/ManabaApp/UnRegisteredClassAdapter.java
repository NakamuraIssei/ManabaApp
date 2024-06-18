package com.example.ManabaApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class UnRegisteredClassAdapter extends RecyclerView.Adapter<UnRegisteredClassAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<ClassData> unRegisteredClassList;
    private final EncourageRegistringDialog dialog;

    public UnRegisteredClassAdapter(Context context, ArrayList<ClassData> unRegisteredClassList,EncourageRegistringDialog dialog) {
        this.context = context;
        this.unRegisteredClassList = unRegisteredClassList;
        this.dialog=dialog;
    }
    @NonNull
    @Override
    public UnRegisteredClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unregistered_class_custom_layout, parent, false);
        return new UnRegisteredClassAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull UnRegisteredClassAdapter.ViewHolder holder, int position) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String className="・"+unRegisteredClassList.get(position).getClassName();
            holder.classNameText.setText(className);
        } else holder.classNameText.setText("アンドロイドのバージョンが小さいです");
        holder.showClassDialogButton.setOnClickListener(v -> {
            // ダイアログクラスのインスタンスを作成
            ChangeableClassDialog dialog = new ChangeableClassDialog(context,unRegisteredClassList.get(position),true);//追加課題の画面のインスタンスを生成
            this.dialog.dismiss();
            // ダイアログを表示
            dialog.show();//追加課題の画面を表示
        });
    }

    @Override
    public int getItemCount() {
        //リサイクルビューに表示されるデータの数
        return unRegisteredClassList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton showClassDialogButton;
        TextView classNameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showClassDialogButton = itemView.findViewById(R.id.showClassDialogButton);
            classNameText = itemView.findViewById(R.id.classNameText);
        }
    }
}
