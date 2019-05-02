package com.jpgrego.paranoidandroid.utils.notifications;

public interface IRadioNotificationFactory {
    void wifiNewAPNotification(final String ssid, final String bssid);
    void wifiUntrustedAPNotification(final String ssid, final String bssid);
    void wifiSecurityChangedNotification(final String ssid, final String oldSec,
                                         final String newSec);
    void wifiSimilarAPNotification(final String ssid, final int similarCount);
}
