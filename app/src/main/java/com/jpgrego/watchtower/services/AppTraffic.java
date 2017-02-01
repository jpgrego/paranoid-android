package com.jpgrego.watchtower.services;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.jpgrego.watchtower.data.AppTrafficData;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jpgrego on 1/30/17.
 */

final class AppTraffic {

    private static final int UPDATE_PERIOD = 10000; // 10 seconds
    private static final Handler UPDATE_HANDLER = new Handler();
    private final ArrayList<AppTrafficData> appTrafficDataList = new ArrayList<>();
    private final PackageManager packageManager;
    private long totalTransmittedBytes = 0;
    private long totalReceivedBytes = 0;

    AppTraffic(final PackageManager packageManager) {
        this.packageManager = packageManager;
        UPDATE_HANDLER.post(new UpdateRunnable());
    }

    long getTotalTransmittedBytes() {
        synchronized (appTrafficDataList) {
            return totalTransmittedBytes;
        }
    }

    long getTotalReceivedBytes() {
        synchronized (appTrafficDataList) {
            return totalReceivedBytes;
        }
    }

    ArrayList<AppTrafficData> getAppTrafficDataList() {
        synchronized (appTrafficDataList) {
            Collections.sort(appTrafficDataList);
            return appTrafficDataList;
        }
    }

    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {
            synchronized (appTrafficDataList) {
                totalTransmittedBytes = 0;
                totalReceivedBytes = 0;
                appTrafficDataList.clear();
                for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
                    final AppTrafficData data =
                            AppTrafficData.fromApplicationInfo(appInfo, packageManager);
                    if (data.hasNetworkActivity()) {
                        totalTransmittedBytes += data.getTransmittedBytes();
                        totalReceivedBytes += data.getReceivedBytes();
                        appTrafficDataList.add(data);
                    }
                }
            }
            UPDATE_HANDLER.postDelayed(this, UPDATE_PERIOD);
        }
    }
}
