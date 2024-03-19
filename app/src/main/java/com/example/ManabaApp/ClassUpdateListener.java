package com.example.ManabaApp;

public interface ClassUpdateListener {
    void onNotificationReceived(int dataId);

    void updateClassTextView(ClassData classData);

    void showRegisterClassDialog();
}