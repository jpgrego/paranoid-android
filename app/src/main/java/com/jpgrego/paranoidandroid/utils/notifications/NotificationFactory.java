package com.jpgrego.paranoidandroid.utils.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.support.v4.app.NotificationCompat;

import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.services.DataService;
import com.jpgrego.paranoidandroid.services.NotificationActionHandler;

import static com.jpgrego.paranoidandroid.utils.notifications.NotificationEnum.*;

public final class NotificationFactory implements IRadioNotificationFactory,
        IUSBNotificationFactory, IDebugNotificationFactory {

    private final String appName;
    private final NotificationManager notificationManager;
    private final Context context;
    //private final Intent resultIntent;
    private final PendingIntent notificationPendingIntent;

    public NotificationFactory(final Context context) {
        this.context = context;

        // TODO: "monitoring" notification obtained name too. how?
        this.appName = getApplicationName();
        this.notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // this is the intent responsible to trigger the consequence of clicking in the notification
        // in this case, open the first activity. revisit this to be more flexible later
        //resultIntent = new Intent(context, WifiCellsBTActivity.class);
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        final Intent serviceIntent = new Intent(context, NotificationActionHandler.class);
        notificationPendingIntent =
                PendingIntent.getService(context, 0, serviceIntent, 0);
    }

    @Override
    public void wifiNewAPNotification(final String ssid, final String bssid) {
        final String notificationTicker = context.getString(R.string.new_ap_associated_title);
        final String notificationDesc =
                context.getString(R.string.new_ap_associated_desc, ssid, bssid);

        final NotificationCompat.Action trustAction =
                new NotificationCompat.Action(R.drawable.direction_arrow, "Trust this network",
                        notificationPendingIntent);
        final Notification notification = generateNotification(WIFI_NEW_AP.id(),
                notificationTicker, notificationDesc, trustAction);

        final NotificationRunnable runnable =
                new NotificationRunnable(WIFI_NEW_AP.id(), notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void wifiSecurityChangedNotification(final String oldSec, final String newSec) {
        final String notificationTicker = context.getString(R.string.ap_changed_sec_title);
        final String notificationDesc = context.getString(R.string.ap_changed_sec_desc, oldSec,
                newSec);
        final Notification notification = generateNotification(WIFI_SEC_CHANGE.id(),
                notificationTicker, notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(WIFI_SEC_CHANGE.id(), notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void wifiSimilarAPNotification(final String ssid, final int similarCount) {
        final String notificationTicker = context.getString(R.string.ap_similar_title);
        final String notificationDesc;
        if(similarCount > 0) {
            notificationDesc = context.getString(R.string.ap_similar_desc_many, similarCount, ssid);
        } else notificationDesc = context.getString(R.string.ap_similar_desc_one, ssid);

        final Notification notification =
                generateNotification(WIFI_SIMILAR_AP.id(), notificationTicker, notificationDesc);

        final NotificationRunnable runnable =
                new NotificationRunnable(WIFI_SIMILAR_AP.id(), notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbTrustedDeviceConnectedNotification(final String vendorID,
                                                      final String productID) {
        final String notificationTicker = context.getString(R.string.device_connected_title);
        final String notificationDesc =
                context.getString(R.string.trusted_usb_device, vendorID, productID);
        final Notification notification =
                generateNotification(USB_TRUSTED_DEVICE_CONNECTED.id(), notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_TRUSTED_DEVICE_CONNECTED.id(), notificationManager,
                        notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbTrustedDeviceConnectedNotification(final String serial) {
        final String notificationTicker = context.getString(R.string.device_connected_title);
        final String notificationDesc =
                context.getString(R.string.trusted_usb_accessory, serial);
        final Notification notification =
                generateNotification(USB_TRUSTED_DEVICE_CONNECTED.id(), notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_TRUSTED_DEVICE_CONNECTED.id(), notificationManager,
                        notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbUnknownDeviceConnected(final String vendorID, final String productID) {
        final String notificationTicker = context.getString(R.string.device_connected_title);
        final String notificationDesc =
                context.getString(R.string.untrusted_usb_device, vendorID, productID);
        final Notification notification =
                generateNotification(USB_UNKNOWN_DEVICE_CONNECTED.id(), notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_UNKNOWN_DEVICE_CONNECTED.id(), notificationManager,
                        notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbUnknownDeviceConnected(final String serial) {
        final String notificationTicker = context.getString(R.string.accessory_connected_title);
        final String notificationDesc = context.getString(R.string.untrusted_usb_accessory, serial);
        final Notification notification =
                generateNotification(USB_UNKNOWN_DEVICE_CONNECTED.id(), notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_UNKNOWN_DEVICE_CONNECTED.id(), notificationManager,
                        notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    // TODO: delete
    @Override
    public void testNotification(final String ticker, final String desc) {
        final Notification notification = generateNotification(9999, ticker, desc);
        final NotificationRunnable runnable =
                new NotificationRunnable(9999, notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    private Notification generateNotification(final int id, final String ticker,
                                              final String text,
                                              final NotificationCompat.Action... actions) {
        //notificationBuilder.setContentIntent(PendingIntent.getActivity(context,
        //        id, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.paranoidandroid_launcher)
                        .setContentTitle(appName)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setTicker(ticker)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .setContentText(text);

        for(final NotificationCompat.Action action : actions) notificationBuilder.addAction(action);

        return notificationBuilder.build();
    }

    private String getApplicationName() {
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        final int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() :
                context.getString(stringId);
    }
}
