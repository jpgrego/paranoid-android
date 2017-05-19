package com.jpgrego.watchtower.listeners;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.jpgrego.watchtower.data.MyBluetoothDevice;
import com.jpgrego.watchtower.services.DataService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 13/01/17.
 */

public final class BluetoothInfoReceiver extends BroadcastReceiver implements BluetoothProfile.ServiceListener {

    private static final int BLUETOOTH_SCAN_DELAY_SECONDS = 1;
    private final Set<MyBluetoothDevice> bluetoothDeviceSet = new HashSet<>();
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String currentUUID = "";

    private final Runnable discoverRunnable = new Runnable() {

        @Override
        public void run() {
            final boolean deviceConnected =
                    bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH)
                            == BluetoothProfile.STATE_CONNECTED
                            || bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)
                            == BluetoothProfile.STATE_CONNECTED
                            || bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                            == BluetoothProfile.STATE_CONNECTED;
            final boolean discover = bluetoothAdapter.isEnabled()
                    && !bluetoothAdapter.isDiscovering() && !deviceConnected;


            if(!discover || !bluetoothAdapter.startDiscovery()) {
                DataService.SCHEDULED_EXECUTOR.schedule(
                        this, BLUETOOTH_SCAN_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        }
    };

    public BluetoothInfoReceiver(final Context context) {
        final IntentFilter bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(this, bluetoothIntentFilter);
        DataService.SCHEDULED_EXECUTOR.execute(discoverRunnable);

        bluetoothAdapter.getProfileProxy(context, this, BluetoothProfile.A2DP);
        bluetoothAdapter.getProfileProxy(context, this, BluetoothProfile.HEADSET);
        bluetoothAdapter.getProfileProxy(context, this, BluetoothProfile.HEALTH);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        switch(intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND:
                synchronized (bluetoothDeviceSet) {
                    final BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    final short rssi =
                            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) -1);
                    final MyBluetoothDevice myBTdevice =
                            MyBluetoothDevice.fromBluetoothDevice(device, rssi);
                    bluetoothDeviceSet.remove(myBTdevice);
                    bluetoothDeviceSet.add(myBTdevice);
                }
                break;
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                final BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final MyBluetoothDevice myDevice =
                        MyBluetoothDevice.fromBluetoothDevice(device, (short) -1);

                switch(state) {
                    case BluetoothAdapter.STATE_DISCONNECTED:
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        currentUUID = "";
                        synchronized (bluetoothDeviceSet) {
                            bluetoothDeviceSet.remove(myDevice);
                        }
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                    case BluetoothAdapter.STATE_CONNECTING:
                        currentUUID = myDevice.getAddress();
                        bluetoothAdapter.cancelDiscovery();
                        synchronized (bluetoothDeviceSet) {
                            bluetoothDeviceSet.clear();
                            bluetoothDeviceSet.add(myDevice);
                        }
                }

                break;
            }
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                synchronized (bluetoothDeviceSet) {
                    // iterating using iterator, so we can modify the set while iterating it
                    for (final Iterator<MyBluetoothDevice> it = bluetoothDeviceSet.iterator();
                         it.hasNext(); ) {
                        final MyBluetoothDevice device = it.next();
                        if (device.getVisibilityCounter().decrementAndGet() < 0) it.remove();
                    }
                }

                DataService.SCHEDULED_EXECUTOR.schedule(
                        discoverRunnable, BLUETOOTH_SCAN_DELAY_SECONDS, TimeUnit.SECONDS);

                break;
            }
            default:
                break;
        }

    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        for(BluetoothDevice device : proxy.getConnectedDevices()) {
            final MyBluetoothDevice myDevice =
                    MyBluetoothDevice.fromBluetoothDevice(device, (short) -1);
            currentUUID = myDevice.getAddress();
            synchronized (bluetoothDeviceSet) {
                bluetoothDeviceSet.add(myDevice);
            }
        }

    }

    @Override
    public void onServiceDisconnected(int profile) {
    }

    public ArrayList<MyBluetoothDevice> getBluetoothDevices() {
        synchronized (bluetoothDeviceSet) {
            return new ArrayList<>(bluetoothDeviceSet);
        }
    }

    public String getCurrentUUID() {
        return currentUUID;
    }
}
