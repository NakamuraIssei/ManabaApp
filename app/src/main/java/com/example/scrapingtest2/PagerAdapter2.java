package com.example.scrapingtest2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter2 extends FragmentStateAdapter {
    private TaskDataManager taskDataManager;
    private static final int NUM_TABS = 4; // タブの数
    public PagerAdapter2(FragmentActivity fragmentActivity,TaskDataManager taskDataManager) {
        super(fragmentActivity);
        this.taskDataManager=taskDataManager;
    }
    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TaskFragment(taskDataManager); // 1つ目のフラグメント
            case 1:
                return new TaskFragment(taskDataManager); // 2つ目のフラグメント
            case 2:
                return new TaskFragment(taskDataManager); // 3つ目のフラグメント
            case 3:
                return new TaskFragment(taskDataManager); // 4つ目のフラグメント
            default:
                return null;
        }
    }
}
