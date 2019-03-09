package com.jpgrego.paranoidandroid.utils;

import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.net.ConnectivityManager;

// this class is only used with Android versions superior or equal to Android N
@SuppressLint("NewApi")
public final class NetworkStatsUtils {

    private static final long DAYS_IN_MS = 2592000000L;
    private final NetworkStatsManager networkStatsManager;
    private final String[] subscriberIds;

    public NetworkStatsUtils(final NetworkStatsManager networkStatsManager,
                             final String[] subscriberIds) {
        this.networkStatsManager = networkStatsManager;
        this.subscriberIds = subscriberIds;
    }

    public RxTxData getWifiBytes(final int uid) {

        final long currentTime = System.currentTimeMillis();
        final NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI, "", currentTime - DAYS_IN_MS,
                currentTime, uid);

        final NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        long rxBytes = 0L;
        long txBytes = 0L;

        while(networkStats.hasNextBucket()) {
            if(!networkStats.getNextBucket(bucket)) continue;
            rxBytes += bucket.getRxBytes();
            txBytes += bucket.getTxBytes();
        }

        networkStats.close();
        return new RxTxData(rxBytes, txBytes);
    }

    public RxTxData getMobileBytes(final int uid) {

        long rxBytes = 0L;
        long txBytes = 0L;
        for(final String subscriberId : subscriberIds) {
            final long currentTime = System.currentTimeMillis();
            final NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE, subscriberId,
                    currentTime - DAYS_IN_MS,
                    currentTime, uid);

            final NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                if (!networkStats.getNextBucket(bucket)) continue;
                rxBytes += bucket.getRxBytes();
                txBytes += bucket.getTxBytes();
            }

            networkStats.close();
        }
        return new RxTxData(rxBytes, txBytes);
    }
}
