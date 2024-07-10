package com.example.ManabaApp;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;

public class ChangeableClassRoomListAdapter extends RecyclerView.Adapter<ChangeableClassRoomListAdapter.ViewHolder> {
    private ArrayList<ClassData> changeableClassList;
    private final ArrayList<String> days;

    public ChangeableClassRoomListAdapter(ArrayList<ClassData> changeableClassList) {
        this.changeableClassList = changeableClassList;
        this.days = new ArrayList<>(Arrays.asList("月", "火", "水", "木", "金", "土", "日"));
    }

    @NonNull
    @Override
    public ChangeableClassRoomListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changeable_class_dialog_room_layout, parent, false);
        return new ChangeableClassRoomListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangeableClassRoomListAdapter.ViewHolder holder, int position) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String dayAndPeriod = days.get(changeableClassList.get(position).getDayAndPeriod() / 7);
            dayAndPeriod += String.valueOf((changeableClassList.get(position).getDayAndPeriod() % 7) + 1) + ":";
            holder.dayAndPeriodText.setText(dayAndPeriod);
            holder.classroomText.setText(changeableClassList.get(position).getClassRoom());

            // Temporarily disable the listener
            holder.notificationSwitch.setOnCheckedChangeListener(null);

            // Set the switch checked state
            holder.notificationSwitch.setChecked(changeableClassList.get(position).getIsNotifying() == 1);

            // Re-enable the listener
            holder.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                changeableClassList.get(holder.getAdapterPosition()).setIsNotifying(isChecked ? 1 : 0);
            });
        } else {
            holder.classroomText.setText("アンドロイドのバージョンが小さいです");
        }

        holder.classroomText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the class room in the list
                changeableClassList.get(holder.getAdapterPosition()).setClassRoom(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    @Override
    public int getItemCount() {
        return changeableClassList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SwitchCompat notificationSwitch;
        TextView dayAndPeriodText;
        EditText classroomText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationSwitch = itemView.findViewById(R.id.notificationSwitch);
            dayAndPeriodText = itemView.findViewById(R.id.dayAndPeriodTextView);
            classroomText = itemView.findViewById(R.id.classRoomEditText);
        }
    }
}
