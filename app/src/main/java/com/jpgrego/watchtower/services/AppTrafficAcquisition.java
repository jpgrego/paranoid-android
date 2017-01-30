package com.jpgrego.watchtower.services;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import com.jpgrego.watchtower.data.AppTrafficData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpgrego on 1/30/17.
 */

public class AppTrafficAcquisition {

    private static long totalTransmittedBytes = -1;
    private static long totalTransmittedPackets = -1;
    private static long totalReceivedBytes = -1;
    private static long totalReceivedPackets = -1;
    private static long mobileTransmittedBytes = -1;
    private static long mobileReceivedBytes = -1;

    // enforce noninstantiability
    private AppTrafficAcquisition() {
        throw new AssertionError();
    }

    public static List<AppTrafficData> getAppTrafficData(final PackageManager packageManager) {
        final List<AppTrafficData> appTrafficDataList = new ArrayList<>();
        totalTransmittedBytes = TrafficStats.getTotalTxBytes();
        totalTransmittedPackets = TrafficStats.getTotalTxPackets();
        totalReceivedBytes = TrafficStats.getTotalRxBytes();
        totalReceivedPackets = TrafficStats.getTotalRxPackets();
        mobileTransmittedBytes = TrafficStats.getMobileTxBytes();
        mobileReceivedBytes = TrafficStats.getMobileRxBytes();

        for(ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
            final AppTrafficData data = AppTrafficData.fromApplicationInfo(appInfo, packageManager);
            appTrafficDataList.add(data);
        }

        return appTrafficDataList;
    }

    public static long getTotalTransmittedBytes() {
        return totalTransmittedBytes != 0 ? totalTransmittedBytes : -1;
    }

    public static long getTotalTransmittedPackets() {
        return totalTransmittedPackets != 0 ? totalTransmittedPackets : -1;
    }

    public static long getTotalReceivedBytes() {
        return totalReceivedBytes != 0 ? totalReceivedBytes : -1;
    }

    public static long getTotalReceivedPackets() {
        return totalReceivedPackets != 0 ? totalReceivedPackets : -1;
    }

    public static long getMobileTransmittedBytes() {
        return mobileTransmittedBytes != 0 ? mobileTransmittedBytes : -1;
    }

    public static long getMobileReceivedBytes() {
        return mobileReceivedBytes != 0 ? mobileReceivedBytes : -1;
    }

}
