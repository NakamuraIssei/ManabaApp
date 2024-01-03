package com.example.scrapingtest2;

public interface ClassUpdateListener {
    void onNotificationReceived(int dataId);
    void updateClassTextView(Data data);
}