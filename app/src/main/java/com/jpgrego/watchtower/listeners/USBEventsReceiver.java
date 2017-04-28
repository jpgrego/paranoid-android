package com.jpgrego.watchtower.listeners;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

/**
 * Created by jpgrego on 4/28/17.
 */

public class USBEventsReceiver extends BroadcastReceiver {

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
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //alertDialogBuilder.setMessage("Something happened!");
        //alertDialogBuilder.show();
        switch(intent.getAction()) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                alertDialogBuilder.setMessage("Device attached!");
                alertDialogBuilder.show();
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                final UsbDeviceConnection connection = usbManager.openDevice(device);
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                alertDialogBuilder.setMessage("Device detached!");
                alertDialogBuilder.show();
                break;
            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                alertDialogBuilder.setMessage("Accessory attached!");
                alertDialogBuilder.show();
                break;
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                alertDialogBuilder.setMessage("Accessory detached!");
                alertDialogBuilder.show();
                break;
            case "android.hardware.usb.action.USB_STATE":
                if(intent.getExtras().getBoolean("connected")) {
                    alertDialogBuilder.setMessage("Something was connected!");
                } else alertDialogBuilder.setMessage("Something was disconnected!");
                alertDialogBuilder.show();
                break;
            default:
                break;
        }
    }
}
