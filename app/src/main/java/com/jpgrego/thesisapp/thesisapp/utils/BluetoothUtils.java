package com.jpgrego.thesisapp.thesisapp.utils;

import android.bluetooth.BluetoothDevice;

/**
 * Created by jgrego on 05-09-2016.
 */
public final class BluetoothUtils {

    // enforce noninstantiability
    private BluetoothUtils() { throw new AssertionError(); }

    public static String getTypeStringFromInt(final int type) {
        switch(type) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                return "Classic";
            case BluetoothDevice.DEVICE_TYPE_LE:
                return "BLE";
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                return "Dual";
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                return "Unknown";
            default:
                return "Unknown";
        }
    }
}
