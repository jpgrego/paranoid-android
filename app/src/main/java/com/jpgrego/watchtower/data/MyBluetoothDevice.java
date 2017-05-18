package com.jpgrego.watchtower.data;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jpgrego on 17/01/17.
 */

public final class MyBluetoothDevice implements Comparable<MyBluetoothDevice>, Parcelable {

    private final String name, address;
    private final int rssi, type;
    private final AtomicLong timestamp;
    private final AtomicInteger visibilityCounter;

    private MyBluetoothDevice(final BluetoothDevice bluetoothDevice, final short rssi) {
        this.name = bluetoothDevice.getName();
        this.address = bluetoothDevice.getAddress();
        this.type = bluetoothDevice.getType();
        this.rssi = rssi;
        this.timestamp = new AtomicLong(SystemClock.elapsedRealtime());
        this.visibilityCounter = new AtomicInteger(3);
    }

    private MyBluetoothDevice(final Parcel in) {
        name = in.readString();
        address = in.readString();
        type = in.readInt();
        rssi = in.readInt();
        timestamp = new AtomicLong(in.readLong());
        visibilityCounter = new AtomicInteger(in.readInt());
    }

    public static final Creator<MyBluetoothDevice> CREATOR = new Creator<MyBluetoothDevice>() {
        @Override
        public MyBluetoothDevice createFromParcel(Parcel in) {
            return new MyBluetoothDevice(in);
        }

        @Override
        public MyBluetoothDevice[] newArray(int size) {
            return new MyBluetoothDevice[size];
        }
    };

    public static MyBluetoothDevice fromBluetoothDevice(final BluetoothDevice bluetoothDevice,
                                                        final short rssi) {
        return new MyBluetoothDevice(bluetoothDevice, rssi);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MyBluetoothDevice)) return false;

        final MyBluetoothDevice myBluetoothDevice = (MyBluetoothDevice) o;

        if(address != null && address.equals(myBluetoothDevice.address)) {
            visibilityCounter.set(3);
            timestamp.set(SystemClock.elapsedRealtime());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public int compareTo(@NonNull final MyBluetoothDevice myBluetoothDevice) {
        return this.rssi - myBluetoothDevice.rssi;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getRssi() {
        return rssi;
    }

    public int getType() {
        return type;
    }

    public AtomicInteger getVisibilityCounter() {
        return visibilityCounter;
    }

    public long getTimeSinceLastSeen() {
        return SystemClock.elapsedRealtime() - this.timestamp.get();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeInt(type);
        parcel.writeInt(rssi);
        parcel.writeLong(timestamp.get());
        parcel.writeInt(visibilityCounter.get());
    }
}
