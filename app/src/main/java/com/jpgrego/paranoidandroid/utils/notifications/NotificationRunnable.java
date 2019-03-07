package com.jpgrego.paranoidandroid.utils.notifications;

import android.app.Notification;
import android.app.NotificationManager;

final class NotificationRunnable implements Runnable {

    private final int notificationID;
    private final NotificationManager notificationManager;
    private final Notification notification;

    NotificationRunnable(final int notificationId, final NotificationManager notificationManager,
                         final Notification notification) {
        this.notificationID = notificationId;
        this.notificationManager = notificationManager;
        this.notification = notification;
    }

    @Override
    public void run() {
        notificationManager.notify(notificationID, notification);
    }
}
