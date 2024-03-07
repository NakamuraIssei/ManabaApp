package com.example.manabaApp;

public interface ClassUpdateListener {
    void onNotificationReceived(int dataId);
    void updateClassTextView(ClassData classData);
    void showRegisterClassDialog();
}