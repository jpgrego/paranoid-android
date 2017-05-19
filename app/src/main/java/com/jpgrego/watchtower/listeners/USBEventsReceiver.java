package com.jpgrego.watchtower.listeners;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.jpgrego.watchtower.R;

import java.util.Map;

/**
 * Created by jpgrego on 4/28/17.
 */

public final class USBEventsReceiver extends BroadcastReceiver {

    private final SharedPreferences sharedPreferences;
    private final AlertDialog.Builder alertDialogBuilder;
    private final UsbManager usbManager;

    public USBEventsReceiver(final Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(this, intentFilter);
        context.registerReceiver(this, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        this.alertDialogBuilder = new AlertDialog.Builder(context);
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.sharedPreferences =
                context.getSharedPreferences("PERMITTED_DEVICES", Context.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //alertDialogBuilder.setMessage("Something happened!");
        //alertDialogBuilder.show();
        //final ParcelFileDescriptor accessoryDescriptor = usbManager.openAccessory(accessory);

        switch(intent.getAction()) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                final UsbDeviceConnection connection = usbManager.openDevice(device);
                connection.close();
                Log.i("", device.getDeviceName() + " (" + device.getVendorId() + ":" +
                        device.getProductId() + ") was connected.");
                alertDialogBuilder.setMessage(context.getString(R.string.device_connected,
                        device.getDeviceName(), device.getVendorId(), device.getProductId()));
                alertDialogBuilder.setPositiveButton(R.string.yes_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                usbManager.openDevice(device);
                                //sharedPreferences.edit().putBoolean(device.toString(), true).apply();
                            }
                        });
                alertDialogBuilder.setNegativeButton(R.string.no_button, null);
                alertDialogBuilder.show();
                break;
            }
            case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.i("", device.getDeviceName() + " (" + device.getVendorId() + ":" +
                        device.getProductId() + ") was disconnected.");
                alertDialogBuilder.setMessage(context.getString(R.string.device_disconnected,
                        device.getDeviceName(), device.getVendorId(), device.getProductId()));
                alertDialogBuilder.show();
                break;
            }
            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED: {
                final UsbAccessory accessory =
                        intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                Log.i("", "Accessory " + accessory.getModel() + " with serial number "
                        + accessory.getSerial() + " was connected.");
                alertDialogBuilder.setMessage(context.getString(R.string.accessory_connected,
                        accessory.getModel(), accessory.getSerial()));
                alertDialogBuilder.show();
                break;
            }
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED: {
                final UsbAccessory accessory =
                        intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                Log.i("", "Accessory " + accessory.getModel() + " with serial number "
                        + accessory.getSerial() + " was disconnected.");
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
                        usbManager.openDevice(device).close();
                        alertDialogBuilder.setMessage(context.getString(R.string.device_untrusted,
                                device.getDeviceName(), device.getVendorId(),
                                device.getProductId()));
                        alertDialogBuilder.show();
                    }
                }
                break;
            }
            default:
                break;
        }
    }
}
