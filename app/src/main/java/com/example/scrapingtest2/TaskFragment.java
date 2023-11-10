package com.example.scrapingtest2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskFragment extends Fragment {
    private TaskDataManager taskDataManager;

    TaskFragment(TaskDataManager taskDataManager){
        this.taskDataManager=taskDataManager;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // レイアウトファイルをインフレートしてViewを作成
        return inflater.inflate(R.layout.activity_taskwork, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Viewの初期化やイベントリスナーの設定などの処理を実装
        RecyclerView recyclerView = view.findViewById(R.id.RecycleView);//画面上のListViewの情報を変数listViewに設定

        // LinearLayoutManagerを設定する
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        TaskCustomAdapter adapter = new TaskCustomAdapter(getContext(), taskDataManager);//Listviewを表示するためのadapterを設定
        recyclerView.setAdapter(adapter);//listViewにadapterを設定

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped (@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                TaskCustomAdapter adapter = (TaskCustomAdapter) recyclerView.getAdapter();
                // 登録とかするんだったらなにかのリストから削除をする処理はここ
                //TaskDataManager.deleteTask(swipedPosition);
                // 削除されたことを知らせて反映させる。
                adapter.notifyItemRemoved(swipedPosition);
            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    Log.d("aaa","スワイプできてるやん");
                    View itemView = viewHolder.itemView;
                    /*Drawable background;
                    if (dX <= 0) {
                        background = new ColorDrawable(Color.RED); // 右にスワイプしたときの背景
                    } else {
                        background = new ColorDrawable(Color.GREEN); // 左にスワイプしたときの背景
                    }*/
                    ColorDrawable background = new ColorDrawable();
                    background .setColor(Color.parseColor("#FF0000"));
                    background.setBounds(itemView.getLeft() , itemView.getTop(), (int)dX, itemView.getBottom());
                    background.draw(c);
                    //background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    //background.draw(c);
                }
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        Button addButton = view.findViewById(R.id.AddButton);//課題追加の画面を呼び出すボタンの設定
        addButton.setOnClickListener(new View.OnClickListener() {//ボタンが押されたら
            @Override
            public void onClick(View v) {//ボタンが押されたら
                Log.d("bbb","課題追加ボタンちゃんと押せてるよー！!");
                // ダイアログクラスのインスタンスを作成
                AddTaskCustomDialog dialog = new AddTaskCustomDialog(getContext(),adapter,taskDataManager);//追加課題の画面のインスタンスを生成
                // ダイアログを表示
                dialog.show();//追加課題の画面を表示
            }
        });
    }
}

