package com.jpgrego.watchtower.listeners;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.jpgrego.watchtower.data.MyBluetoothDevice;
import com.jpgrego.watchtower.services.DataService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jpgrego on 13/01/17.
 */

public final class BluetoothInfoReceiver extends BroadcastReceiver {

    private static final int BLUETOOTH_SCAN_DELAY_SECONDS = 3;
    private final Set<MyBluetoothDevice> bluetoothDeviceSet = new HashSet<>();
    private final AtomicBoolean isDeviceConnected = new AtomicBoolean(false);

    public BluetoothInfoReceiver(final Context context) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                final boolean discover =
                        bluetoothAdapter != null
                        && bluetoothAdapter.isEnabled()
                        && !bluetoothAdapter.isDiscovering()
                        && isDeviceConnected.get();

                if(discover) {
                    bluetoothAdapter.startDiscovery();
                }
            }
        }, 0, BLUETOOTH_SCAN_DELAY_SECONDS, TimeUnit.SECONDS);

        final IntentFilter bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        context.registerReceiver(this, bluetoothIntentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        switch(intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                synchronized (bluetoothDeviceSet) {
                    final BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    final short rssi =
                            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    bluetoothDeviceSet.add(MyBluetoothDevice.fromBluetoothDevice(device, rssi));
                }
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                isDeviceConnected.set(true);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                isDeviceConnected.set(false);
                break;
        }

    }

    public ArrayList<MyBluetoothDevice> getBluetoothDevices() {
        synchronized (bluetoothDeviceSet) {
            return new ArrayList<>(bluetoothDeviceSet);
        }
    }
}
