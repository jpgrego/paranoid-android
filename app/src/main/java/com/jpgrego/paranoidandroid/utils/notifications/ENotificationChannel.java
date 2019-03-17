package com.jpgrego.paranoidandroid.utils.notifications;

enum ENotificationChannel {
    WIFI("WiFi"),
    USB("USB"),
    DEBUG("Debug");

    private final String name;

    ENotificationChannel(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
