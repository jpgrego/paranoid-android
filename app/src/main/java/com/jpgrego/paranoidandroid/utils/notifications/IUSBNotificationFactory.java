package com.jpgrego.paranoidandroid.utils.notifications;

public interface IUSBNotificationFactory {
    void usbTrustedDeviceConnectedNotification(final String vendorID, final String productID);
    void usbTrustedDeviceConnectedNotification(final String serial);
    void usbUnknownDeviceConnected(final String vendorID, final String productID);
    void usbUnknownDeviceConnected(final String serial);
}
