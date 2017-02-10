package com.jpgrego.watchtower.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficData implements Parcelable, Comparable<AppTrafficData> {

    private final String appName, appPackageName;
    private final int uid;
    private final long transmittedBytes, receivedBytes, transmittedPackages, receivedPackages;

    private volatile int hashCode;

    private AppTrafficData(final int uid, final String appName, final String appPackageName) {
        this.uid = uid;
        this.appName = appName;
        this.appPackageName = appPackageName;
        this.transmittedBytes = TrafficStats.getUidTxBytes(uid);
        this.receivedBytes = TrafficStats.getUidRxBytes(uid);
        this.transmittedPackages = TrafficStats.getUidTxPackets(uid);
        this.receivedPackages = TrafficStats.getUidRxPackets(uid);
    }

    private AppTrafficData(final Parcel in) {
        appName = in.readString();
        appPackageName = in.readString();
        uid = in.readInt();
        transmittedBytes = in.readLong();
        receivedBytes = in.readLong();
        transmittedPackages = in.readLong();
        receivedPackages = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(appPackageName);
        dest.writeInt(uid);
        dest.writeLong(transmittedBytes);
        dest.writeLong(receivedBytes);
        dest.writeLong(transmittedPackages);
        dest.writeLong(receivedPackages);
    }

    public static final Creator<AppTrafficData> CREATOR = new Creator<AppTrafficData>() {
        @Override
        public AppTrafficData createFromParcel(Parcel in) {
            return new AppTrafficData(in);
        }

        @Override
        public AppTrafficData[] newArray(int size) {
            return new AppTrafficData[size];
        }
    };

    public static AppTrafficData fromApplicationInfo(final ApplicationInfo appInfo,
                                                     final PackageManager pm) {
        final String name = appInfo.loadLabel(pm).toString();
        return new AppTrafficData(appInfo.uid, name, appInfo.packageName);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public long getTransmittedBytes() {
        return transmittedBytes;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public long getTransmittedPackages() {
        return transmittedPackages;
    }

    public long getReceivedPackages() {
        return receivedPackages;
    }

    public int getUid() {
        return uid;
    }

    public boolean hasNetworkActivity() {
        return transmittedBytes > 0 || receivedBytes > 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof AppTrafficData)) return false;

        final AppTrafficData appTrafficData = (AppTrafficData) o;

        return appTrafficData.uid == this.uid;
    }

    @Override
    public int hashCode() {
        int result = hashCode;

        if(result == 0) {
            result = 17;
            result *= 31 + uid;
            hashCode = result;
        }

        return result;
    }

    @Override
    public int compareTo(@NonNull AppTrafficData another) {
        return Long.compare(another.transmittedBytes + another.receivedBytes,
                this.transmittedBytes + this.receivedBytes);
    }
}
