package com.jpgrego.paranoidandroid.utils.notifications;

enum NotificationEnum {
    WIFI_NEW_AP(1001),
    WIFI_SIMILAR_AP(1002),
    WIFI_SEC_CHANGE(1003),
    USB_TRUSTED_DEVICE_CONNECTED(1004),
    USB_UNKNOWN_DEVICE_CONNECTED(1005);

    private final int id;

    NotificationEnum(final int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}
