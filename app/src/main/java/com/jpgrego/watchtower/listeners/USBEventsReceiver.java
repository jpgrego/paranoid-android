package com.jpgrego.watchtower.listeners;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.activities.MainActivity;

import java.util.Map;

/**
 * Created by jpgrego on 4/28/17.
 */

//TODO: detect connection to computer
//TODO: save on db instead of sharedpreferences
public final class USBEventsReceiver extends BroadcastReceiver {

    private static final int USB_NOTIFICATION_ID = 1000;
    private final SharedPreferences sharedPreferences;
    private final UsbManager usbManager;
    private final Context context;
    private final NotificationCompat.Builder unknownNotificationBuilder;

    public USBEventsReceiver(final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(this, intentFilter);
        context.registerReceiver(this, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.sharedPreferences =
                context.getSharedPreferences("PERMITTED_DEVICES", Context.MODE_PRIVATE);
        this.context = context;
        this.unknownNotificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.usb)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                final String vendorID = toHexString(device.getVendorId());
                final String productID = toHexString(device.getProductId());
                Log.i("", device.toString() + " (" + vendorID + ":" + productID
                        + ") was connected.");
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(R.string.device_connected_title);
                alertDialogBuilder.setMessage(context.getString(R.string.device_connected, vendorID,
                        productID));
                alertDialogBuilder.setPositiveButton(R.string.yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //usbManager.openDevice(device);
                                //sharedPreferences.edit().putBoolean(device.toString(), true).apply();
                            }
                        });
                alertDialogBuilder.setNegativeButton(R.string.no_button, null);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.show();

                unknownConnectedNotification(vendorID, productID);
                break;
            }
            case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                final String vendorID = toHexString(device.getVendorId());
                final String productID = toHexString(device.getProductId());
                Log.i("", device.getDeviceName() + " (" + vendorID + ":" +
                        productID + ") was disconnected.");
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(R.string.device_disconnected_title);
                alertDialogBuilder.setMessage(context.getString(R.string.device_disconnected,
                        vendorID, productID));
                alertDialogBuilder.show();
                break;
            }
            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED: {
                final UsbAccessory accessory =
                        intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                Log.i("", "Accessory " + accessory.getModel() + " with serial number "
                        + accessory.getSerial() + " was connected.");
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(R.string.accessory_connected_title);
                alertDialogBuilder.setMessage(context.getString(R.string.accessory_connected,
                        accessory.getModel(), accessory.getSerial()));
                alertDialogBuilder.show();

                unknownConnectedNotification(accessory.getSerial());
                break;
            }
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED: {
                final UsbAccessory accessory =
                        intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                Log.i("", "Accessory " + accessory.getModel() + " with serial number "
                        + accessory.getSerial() + " was disconnected.");
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(R.string.accessory_disconnected_title);
                alertDialogBuilder.setMessage(context.getString(R.string.accessory_disconnected,
                        accessory.getModel(), accessory.getSerial()));
                alertDialogBuilder.show();
                break;
            }
            case "android.hardware.usb.action.USB_STATE": {
                final Map<String, UsbDevice> connectedDevices = usbManager.getDeviceList();

                for (Map.Entry<String, UsbDevice> entry : connectedDevices.entrySet()) {
                    final UsbDevice device = entry.getValue();
                    if (!sharedPreferences.contains(device.toString())) {
                        final AlertDialog.Builder alertDialogBuilder =
                                new AlertDialog.Builder(context);
                        alertDialogBuilder.setTitle(R.string.warning_title);
                        alertDialogBuilder.setMessage(context.getString(R.string.device_untrusted,
                                Integer.toHexString(device.getVendorId()),
                                Integer.toHexString(device.getProductId())));
                        alertDialogBuilder.show();
                    }
                }

                final UsbAccessory[] accessoriesArray = usbManager.getAccessoryList();

                if(accessoriesArray != null) {
                    for (UsbAccessory accessory : accessoriesArray) {
                        if (!sharedPreferences.contains(accessory.getSerial())) {
                            final AlertDialog.Builder alertDialogBuilder =
                                    new AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle(R.string.warning_title);
                            alertDialogBuilder.setMessage(context.getString(
                                    R.string.accessory_untrusted, accessory.getModel(),
                                    accessory.getSerial()));
                            alertDialogBuilder.show();
                        }
                    }
                }

                break;
            }
            default:
                break;
        }
    }

    private void unknownConnectedNotification(final String vendorID, final String productID) {
        final Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        final String notificationDesc = context.getString(R.string.untrusted_usb_device, vendorID,
                productID);
        final String notificationTicker = context.getString(R.string.device_connected_title);

        unknownNotificationBuilder.setContentIntent(PendingIntent.getActivity(context,
                USB_NOTIFICATION_ID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        unknownNotificationBuilder.setTicker(notificationTicker);
        unknownNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(notificationDesc));
        unknownNotificationBuilder.setContentText(notificationDesc);

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(USB_NOTIFICATION_ID, unknownNotificationBuilder.build());
    }

    private void unknownConnectedNotification(final String serial) {
        final Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        final String notificationDesc = context.getString(R.string.untrusted_usb_accessory, serial);
        final String notificationTicker = context.getString(R.string.accessory_connected_title);

        unknownNotificationBuilder.setContentIntent(PendingIntent.getActivity(context,
                USB_NOTIFICATION_ID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        unknownNotificationBuilder.setTicker(notificationTicker);
        unknownNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(notificationDesc));
        unknownNotificationBuilder.setContentText(notificationDesc);

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(USB_NOTIFICATION_ID, unknownNotificationBuilder.build());
    }

    private String toHexString(final int num) {
        return Integer.toHexString(0x10000 | num).substring(1).toUpperCase();
    }
}
