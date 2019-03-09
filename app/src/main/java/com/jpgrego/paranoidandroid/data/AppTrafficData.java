package com.jpgrego.paranoidandroid.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.jpgrego.paranoidandroid.utils.NetworkStatsUtils;
import com.jpgrego.paranoidandroid.utils.RxTxData;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficData implements Parcelable, Comparable<AppTrafficData> {

    private final String appName, appPackageName;
    private final int uid;
    private final long wifiTransmittedBytes, wifiReceivedBytes, cellularTransmittedBytes, cellularReceivedBytes;

    private volatile int hashCode;

    private AppTrafficData(final int uid, final String appName, final String appPackageName) {
        this.uid = uid;
        this.appName = appName;
        this.appPackageName = appPackageName;

        this.wifiTransmittedBytes = TrafficStats.getUidTxBytes(uid);
        this.wifiReceivedBytes = TrafficStats.getUidRxBytes(uid);
        this.cellularTransmittedBytes = TrafficStats.getUidTxPackets(uid);
        this.cellularReceivedBytes = TrafficStats.getUidRxPackets(uid);
    }

    private AppTrafficData(final int uid, final String appName, final String appPackageName,
                           final RxTxData wifiBytes, final RxTxData mobileBytes) {
        this.uid = uid;
        this.appName = appName;
        this.appPackageName = appPackageName;

        this.wifiTransmittedBytes = wifiBytes.txBytes;
        this.wifiReceivedBytes = wifiBytes.rxBytes;
        this.cellularTransmittedBytes = mobileBytes.txBytes;
        this.cellularReceivedBytes = mobileBytes.rxBytes;
    }

    private AppTrafficData(final Parcel in) {
        appName = in.readString();
        appPackageName = in.readString();
        uid = in.readInt();
        wifiTransmittedBytes = in.readLong();
        wifiReceivedBytes = in.readLong();
        cellularTransmittedBytes = in.readLong();
        cellularReceivedBytes = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(appPackageName);
        dest.writeInt(uid);
        dest.writeLong(wifiTransmittedBytes);
        dest.writeLong(wifiReceivedBytes);
        dest.writeLong(cellularTransmittedBytes);
        dest.writeLong(cellularReceivedBytes);
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

    public static AppTrafficData fromApplicationInfo(final ApplicationInfo appInfo,
                                                     final PackageManager pm,
                                                     final NetworkStatsUtils networkStatsUtils){
        final String name = appInfo.loadLabel(pm).toString();
        final int uid = appInfo.uid;

        final RxTxData wifiBytes = networkStatsUtils.getWifiBytes(uid);
        final RxTxData mobileBytes = networkStatsUtils.getMobileBytes(uid);

        return new AppTrafficData(appInfo.uid, name, appInfo.packageName, wifiBytes, mobileBytes);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public long getWifiTransmittedBytes() {
        return wifiTransmittedBytes;
    }

    public long getWifiReceivedBytes() {
        return wifiReceivedBytes;
    }

    public long getCellularTransmittedBytes() {
        return cellularTransmittedBytes;
    }

    public long getCellularReceivedBytes() {
        return cellularReceivedBytes;
    }

    public int getUid() {
        return uid;
    }

    public boolean hasNetworkActivity() {
        return wifiTransmittedBytes > 0 || wifiReceivedBytes > 0 || cellularTransmittedBytes > 0
                || cellularReceivedBytes > 0;
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
            result = 31 * result + uid;
            hashCode = result;
        }

        return result;
    }

    @Override
    public int compareTo(@NonNull AppTrafficData another) {
        if(this.equals(another)) return 0;
        else {
            final int diff = Long.compare(another.wifiTransmittedBytes + another.wifiReceivedBytes,
                    this.wifiTransmittedBytes + this.wifiReceivedBytes);
            return diff != 0 ? diff : 1;
        }
    }
}
