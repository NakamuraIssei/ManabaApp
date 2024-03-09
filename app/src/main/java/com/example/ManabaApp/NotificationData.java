package com.example.ManabaApp;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationData {
    private final String dataName;
    private final String title;
    private final String subTitle;
    private final LocalDateTime notificationTiming;

    NotificationData(String dataName,String title,String subTitle,LocalDateTime notificationTiming){
        this.dataName=dataName;
        this.title=title;
        this.subTitle=subTitle;
        this.notificationTiming=notificationTiming;
    }
    @Override
    public int hashCode() {
        return Objects.hash(dataName, title, subTitle, notificationTiming);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NotificationData other = (NotificationData) obj;
        return Objects.equals(dataName, other.dataName) &&
                Objects.equals(title, other.title) &&
                Objects.equals(subTitle, other.subTitle) &&
                Objects.equals(notificationTiming, other.notificationTiming);
    }
}
