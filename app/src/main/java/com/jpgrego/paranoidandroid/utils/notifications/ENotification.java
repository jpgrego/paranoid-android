package com.jpgrego.paranoidandroid.utils.notifications;

enum ENotification {
    WIFI_NEW_AP(1001),
    WIFI_SIMILAR_AP(1002),
    WIFI_SEC_CHANGE(1003),
    WIFI_UNTRUSTED_AP(1004),
    USB_TRUSTED_DEVICE_CONNECTED(1004),
    USB_UNKNOWN_DEVICE_CONNECTED(1005),
    DEBUG(1006);

    private final int id;

    ENotification(final int id) {
        this.id = id;
    }

    public int notificationId() {
        return id;
    }
}
