package com.jpgrego.watchtower.listeners;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.db.DatabaseContract;
import com.jpgrego.watchtower.db.DatabaseHelper;
import com.jpgrego.watchtower.utils.GeneralUtils;

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
    public void onReceive(final Context context, Intent intent) {
        switch(intent.getAction()) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                final String vendorID = GeneralUtils.toHexString(device.getVendorId());
                final String productID = GeneralUtils.toHexString(device.getProductId());
                Log.i("", device.toString() + " (" + vendorID + ":" + productID
                        + ") was connected.");

                final SQLiteDatabase readableDB = new DatabaseHelper(context).getReadableDatabase();
                final String[] columns = new String[] {
                        DatabaseContract.TrustedUSBDeviceEntry.VENDORID_COLUMN,
                        DatabaseContract.TrustedUSBDeviceEntry.PRODUCTID_COLUMN
                };
                final Cursor results = readableDB.query(
                        DatabaseContract.TrustedUSBDeviceEntry.TABLE_NAME, columns, null, null,
                        null, null, null);

                if(results.moveToFirst()) {
                    do {
                        final String resultVendorID = results.getString(
                                results.getColumnIndex(
                                        DatabaseContract.TrustedUSBDeviceEntry.VENDORID_COLUMN));
                        final String resultProductID = results.getString(
                                results.getColumnIndex(
                                        DatabaseContract.TrustedUSBDeviceEntry.PRODUCTID_COLUMN));

                        if(resultVendorID.equalsIgnoreCase(vendorID)
                                && resultProductID.equalsIgnoreCase(productID)) {
                            results.close();
                            readableDB.close();
                            trustedConnectedNotification(vendorID, productID);
                            return;
                        }
                    } while(results.moveToNext());
                }

                results.close();
                readableDB.close();

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(R.string.device_connected_title);
                alertDialogBuilder.setMessage(context.getString(R.string.device_connected, vendorID,
                        productID));
                alertDialogBuilder.setPositiveButton(R.string.yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ContentValues values = new ContentValues();
                                values.put(DatabaseContract.TrustedUSBDeviceEntry.VENDORID_COLUMN,
                                        vendorID);
                                values.put(DatabaseContract.TrustedUSBDeviceEntry.PRODUCTID_COLUMN,
                                        productID);
                                final SQLiteDatabase db = new DatabaseHelper(context)
                                        .getWritableDatabase();
                                db.insertWithOnConflict(
                                        DatabaseContract.TrustedUSBDeviceEntry.TABLE_NAME, null,
                                        values, SQLiteDatabase.CONFLICT_IGNORE);
                                db.close();
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
                final String vendorID = GeneralUtils.toHexString(device.getVendorId());
                final String productID = GeneralUtils.toHexString(device.getProductId());
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

                final SQLiteDatabase readableDB = new DatabaseHelper(context).getReadableDatabase();
                final String[] columns = new String[] {
                        DatabaseContract.TrustedAccessoryDeviceEntry.SERIAL_COLUMN
                };
                final Cursor results = readableDB.query(
                        DatabaseContract.TrustedAccessoryDeviceEntry.TABLE_NAME, columns, null,
                        null, null, null, null);

                if(results.moveToFirst()) {
                    do {
                        final String resultSerial = results.getString(
                                results.getColumnIndex(
                                        DatabaseContract.TrustedAccessoryDeviceEntry.SERIAL_COLUMN)
                        );

                        if(resultSerial.equalsIgnoreCase(accessory.getSerial())) {
                            results.close();
                            readableDB.close();
                            trustedConnectedNotification(accessory.getSerial());
                            return;
                        }
                    } while(results.moveToNext());
                }

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

    private void trustedConnectedNotification(final String vendorID, final String productID) {
        final String notificationDesc =
                context.getString(R.string.trusted_usb_device, vendorID, productID);
        final String notificationTicker = context.getString(R.string.device_connected_title);
        GeneralUtils.generateBigTextNotification(context, unknownNotificationBuilder,
                USB_NOTIFICATION_ID, notificationTicker, notificationDesc);
    }

    private void trustedConnectedNotification(final String serial) {
        final String notificationDesc =
                context.getString(R.string.trusted_usb_accessory, serial);
        final String notificationTicker = context.getString(R.string.device_connected_title);
        GeneralUtils.generateBigTextNotification(context, unknownNotificationBuilder,
                USB_NOTIFICATION_ID, notificationTicker, notificationDesc);
    }

    private void unknownConnectedNotification(final String vendorID, final String productID) {
        final String notificationDesc =
                context.getString(R.string.untrusted_usb_device, vendorID, productID);
        final String notificationTicker = context.getString(R.string.device_connected_title);
        GeneralUtils.generateBigTextNotification(context, unknownNotificationBuilder,
                USB_NOTIFICATION_ID, notificationTicker, notificationDesc);
    }

    private void unknownConnectedNotification(final String serial) {
        final String notificationDesc = context.getString(R.string.untrusted_usb_accessory, serial);
        final String notificationTicker = context.getString(R.string.accessory_connected_title);
        GeneralUtils.generateBigTextNotification(context, unknownNotificationBuilder,
                USB_NOTIFICATION_ID, notificationTicker, notificationDesc);
    }
}
