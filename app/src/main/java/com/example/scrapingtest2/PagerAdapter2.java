package com.example.scrapingtest2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter2 extends FragmentStateAdapter {
    private static final int NUM_TABS = 4; // タブの数
    public PagerAdapter2(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
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
                return new TaskFragment(); // 1つ目のフラグメント
            case 1:
                return new TaskFragment(); // 2つ目のフラグメント
            case 2:
                return new TaskFragment(); // 3つ目のフラグメント
            case 3:
                return new TaskFragment(); // 4つ目のフラグメント
            default:
                return null;
        }
    }
}
