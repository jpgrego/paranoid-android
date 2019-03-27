package com.jpgrego.paranoidandroid.utils.notifications;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.services.DataService;
import com.jpgrego.paranoidandroid.services.NotificationActionHandler;

import static com.jpgrego.paranoidandroid.utils.notifications.ENotification.*;

public final class NotificationFactory implements IRadioNotificationFactory,
        IUSBNotificationFactory, IDebugNotificationFactory, IStickyNotificationFactory {

    private static NotificationFactory instance = null;

    private static final String WIFI_NOTIFICATION_CHANNEL_ID = ENotificationChannel.WIFI.name();
    private static final String USB_NOTIFICATION_CHANNEL_ID = ENotificationChannel.USB.name();
    private static final String DEBUG_NOTIFICATION_CHANNEL_ID = ENotificationChannel.DEBUG.name();
    private static final String STICKY_NOTIFICATION_CHANNEL_ID = ENotificationChannel.STICKY.name();

    private final NotificationManager notificationManager;
    private final String appName;
    private final Application context;

    private NotificationFactory(final Context context) {
        // guaranteed to be passed Application Context from getInstance() method
        this.context = (Application) context;

        this.appName = getApplicationName(context);
        this.notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.notificationManager != null) {
            for(final ENotificationChannel eChannel : ENotificationChannel.values()) {
                final NotificationChannel channel = new NotificationChannel(eChannel.name(),
                        eChannel.toString(), NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }

        // this is the intent responsible to trigger the consequence of clicking in the notification
        // in this case, open the first activity. revisit this to be more flexible later
        //resultIntent = new Intent(context, WifiCellsBTActivity.class);
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
    }

    public static synchronized NotificationFactory getInstance(final Context context) {
        if(instance == null) {
            instance = new NotificationFactory(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public Notification stickyNotification() {
        final String title = context.getString(R.string.currently_monitoring_title);
        final String message = context.getString(R.string.currently_monitoring_message);
        return generateNotification(STICKY_NOTIFICATION_CHANNEL_ID, title, message);
    }

    @Override
    public void wifiNewAPNotification(final String ssid, final String bssid) {
        final String notificationTicker = context.getString(R.string.new_ap_associated_title);
        final String notificationDesc =
                    context.getString(R.string.new_ap_associated_desc, ssid, bssid);


        final Intent trustButtonIntent = new Intent(context, NotificationActionHandler.class);
        trustButtonIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        trustButtonIntent.setAction(NotificationActionHandler.ACTION_ADD_TRUSTED_AP);
        trustButtonIntent.putExtra(NotificationActionHandler.EXTRA_NOT_ID,
                WIFI_NEW_AP.notificationId());

        trustButtonIntent.putExtra(NotificationActionHandler.EXTRA_BSSID, bssid);

        final PendingIntent notificationPendingIntent =
                PendingIntent.getService(context, 0, trustButtonIntent, 0);

        final NotificationCompat.Action trustAction =
                new NotificationCompat.Action(R.drawable.direction_arrow, "Trust this network",
                        notificationPendingIntent);

        final Notification notification = generateNotification(WIFI_NOTIFICATION_CHANNEL_ID,
                notificationTicker, notificationDesc, trustAction);

        final NotificationRunnable runnable =
                new NotificationRunnable(WIFI_NEW_AP.notificationId(), notificationManager,
                        notification);

        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void wifiSecurityChangedNotification(final String oldSec, final String newSec) {
        final String notificationTicker = context.getString(R.string.ap_changed_sec_title);
        final String notificationDesc = context.getString(R.string.ap_changed_sec_desc, oldSec,
                newSec);

        final Notification notification = generateNotification(WIFI_NOTIFICATION_CHANNEL_ID,
                notificationTicker, notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(WIFI_SEC_CHANGE.notificationId(), notificationManager,
                        notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void wifiSimilarAPNotification(final String ssid, final int similarCount) {
        final String notificationTicker = context.getString(R.string.ap_similar_title);
        final String notificationDesc;
        if(similarCount > 0) {
            notificationDesc = context.getString(R.string.ap_similar_desc_many, similarCount, ssid);
        } else notificationDesc = context.getString(R.string.ap_similar_desc_one, ssid);

        final Notification notification = generateNotification(WIFI_NOTIFICATION_CHANNEL_ID,
                notificationTicker, notificationDesc);

        final NotificationRunnable runnable =
                new NotificationRunnable(WIFI_SIMILAR_AP.notificationId(), notificationManager,
                        notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbTrustedDeviceConnectedNotification(final String vendorID,
                                                      final String productID) {
        final String notificationTicker = context.getString(R.string.device_connected_title);
        final String notificationDesc =
                context.getString(R.string.trusted_usb_device, vendorID, productID);
        final Notification notification =
                generateNotification(USB_NOTIFICATION_CHANNEL_ID, notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_TRUSTED_DEVICE_CONNECTED.notificationId(),
                        notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbTrustedDeviceConnectedNotification(final String serial) {
        final String notificationTicker = context.getString(R.string.device_connected_title);
        final String notificationDesc =
                context.getString(R.string.trusted_usb_accessory, serial);
        final Notification notification =
                generateNotification(USB_NOTIFICATION_CHANNEL_ID, notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_TRUSTED_DEVICE_CONNECTED.notificationId(),
                        notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbUnknownDeviceConnected(final String vendorID, final String productID) {
        final String notificationTicker = context.getString(R.string.device_connected_title);
        final String notificationDesc =
                context.getString(R.string.untrusted_usb_device, vendorID, productID);
        final Notification notification =
                generateNotification(USB_NOTIFICATION_CHANNEL_ID, notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_UNKNOWN_DEVICE_CONNECTED.notificationId(),
                        notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void usbUnknownDeviceConnected(final String serial) {
        final String notificationTicker = context.getString(R.string.accessory_connected_title);
        final String notificationDesc = context.getString(R.string.untrusted_usb_accessory, serial);
        final Notification notification =
                generateNotification(USB_NOTIFICATION_CHANNEL_ID, notificationTicker,
                        notificationDesc);
        final NotificationRunnable runnable =
                new NotificationRunnable(USB_UNKNOWN_DEVICE_CONNECTED.notificationId(),
                        notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    @Override
    public void testNotification(final String ticker, final String desc) {
        final Notification notification = generateNotification(DEBUG_NOTIFICATION_CHANNEL_ID,
                ticker, desc);

        final NotificationRunnable runnable =
                new NotificationRunnable(DEBUG.notificationId(), notificationManager, notification);
        DataService.SCHEDULED_EXECUTOR.execute(runnable);
    }

    /*
    private Notification generateNotification(final int id, final String ticker,
                                              final String text,
                                              final NotificationCompat.Action... actions) {
        //notificationBuilder.setContentIntent(PendingIntent.getActivity(context,
        //        notificationId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.paranoidandroid_launcher)
                        .setContentTitle(appName)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setTicker(ticker)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .setContentText(text);

        for(final NotificationCompat.Action action : actions) notificationBuilder.addAction(action);

        return notificationBuilder.build();
    }
    */

    private Notification generateNotification(final String channelId,
                                              final String ticker,
                                              final String text,
                                              final NotificationCompat.Action... actions) {

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.paranoidandroid_launcher)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setTicker(ticker)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .setContentText(text);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setContentTitle(ticker);
        } else notificationBuilder.setContentTitle(appName);

        for(final NotificationCompat.Action action : actions) notificationBuilder.addAction(action);

        return notificationBuilder.build();
    }

    private static String getApplicationName(final Context context) {
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        final int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() :
                context.getString(stringId);
    }
}
