package com.jpgrego.watchtower.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

import com.jpgrego.watchtower.services.AppTrafficAcquisition;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficData {

    private final String appName;
    private final int iconID;
    private final long transmittedBytes, receivedBytes, transmittedPackages, receivedPackages;
    private final int uid, mobileTransmittedPercentage, mobileReceivedPercentage,
            totalTransmittedPercentage, totalReceivedPercentage;

    private AppTrafficData(final int uid, final int iconID, final String appName) {
        this.uid = uid;
        this.appName = appName;
        this.iconID = iconID;
        this.transmittedBytes = TrafficStats.getUidTxBytes(uid);
        this.receivedBytes = TrafficStats.getUidRxBytes(uid);
        this.transmittedPackages = TrafficStats.getUidTxPackets(uid);
        this.receivedPackages = TrafficStats.getUidRxPackets(uid);
        this.mobileTransmittedPercentage =
                (int) (this.transmittedBytes / AppTrafficAcquisition.getMobileTransmittedBytes());
        this.mobileReceivedPercentage =
                (int) (this.receivedBytes / AppTrafficAcquisition.getMobileReceivedBytes());
        this.totalTransmittedPercentage =
                (int) (this.transmittedBytes / AppTrafficAcquisition.getTotalTransmittedBytes());
        this.totalReceivedPercentage =
                (int) (this.receivedBytes / AppTrafficAcquisition.getTotalReceivedBytes());
    }

    public static AppTrafficData fromApplicationInfo(final ApplicationInfo appInfo,
                                                     final PackageManager pm) {
        final String name = appInfo.loadLabel(pm).toString();
        return new AppTrafficData(appInfo.uid, appInfo.icon, name);
    }

    public String getAppName() {
        return appName;
    }

    public int getIconID() {
        return iconID;
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

    public int getMobileTransmittedPercentage() {
        return mobileTransmittedPercentage;
    }

    public int getMobileReceivedPercentage() {
        return mobileReceivedPercentage;
    }

    public int getTotalTransmittedPercentage() {
        return totalTransmittedPercentage;
    }

    public int getTotalReceivedPercentage() {
        return totalReceivedPercentage;
    }
}
