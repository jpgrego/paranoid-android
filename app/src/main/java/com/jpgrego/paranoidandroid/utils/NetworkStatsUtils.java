package com.jpgrego.paranoidandroid.utils;

import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.net.ConnectivityManager;

// this class is only used with Android versions superior or equal to Android N
@SuppressLint("NewApi")
public final class NetworkStatsUtils {

    private final NetworkStatsManager networkStatsManager;
    private final String subscriberId;

    public NetworkStatsUtils(final NetworkStatsManager networkStatsManager,
                             final String subscriberId) {
        this.networkStatsManager = networkStatsManager;
        this.subscriberId = subscriberId;
    }

    public RxTxData getWifiBytes(final int uid) {

        final NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI, "", 0,
                System.currentTimeMillis(), uid);

        final NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        long rxBytes = 0L;
        long txBytes = 0L;

        while(networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            rxBytes += bucket.getRxBytes();
            txBytes += bucket.getTxBytes();
        }

        networkStats.close();
        return new RxTxData(rxBytes, txBytes);
    }

    public RxTxData getMobileBytes(final int uid) {

        final NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE, subscriberId, 0,
                System.currentTimeMillis(), uid);

        final NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        long rxBytes = 0L;
        long txBytes = 0L;

        while(networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            rxBytes += bucket.getRxBytes();
            txBytes += bucket.getTxBytes();
        }

        networkStats.close();
        return new RxTxData(rxBytes, txBytes);
    }
}
