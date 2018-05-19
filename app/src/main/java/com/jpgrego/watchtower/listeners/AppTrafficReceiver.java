package com.jpgrego.watchtower.listeners;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.jpgrego.watchtower.activities.AppTrafficActivity;
import com.jpgrego.watchtower.data.AppTrafficData;
import com.jpgrego.watchtower.services.DataService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficReceiver {

    private static final int UPDATE_PERIOD_SECONDS = 60;

    private final PackageManager packageManager;
    private final ArrayList<AppTrafficData> appTrafficDataList = new ArrayList<>();

    private long totalTransmittedBytes = 0;
    private long totalReceivedBytes = 0;

    public AppTrafficReceiver(final Context context) {
        this.packageManager = context.getPackageManager();
        DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new UpdateRunnable(), 0,
                UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    public long getTotalTransmittedBytes() {
        synchronized (appTrafficDataList) {
            return totalTransmittedBytes;
        }
    }

    public long getTotalReceivedBytes() {
        synchronized (appTrafficDataList) {
            return totalReceivedBytes;
        }
    }

    public List<AppTrafficData> getAppTrafficDataList() {
        synchronized (appTrafficDataList) {
            return appTrafficDataList;
        }
    }

    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {

            final ArrayList<AppTrafficData> temp = new ArrayList<>();
            int tempTotalTransmittedBytes = 0;
            int tempTotalReceivedBytes = 0;

            for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
                final AppTrafficData data =
                        AppTrafficData.fromApplicationInfo(appInfo, packageManager);
                if (data.hasNetworkActivity()) {
                    tempTotalTransmittedBytes += data.getTransmittedBytes();
                    tempTotalReceivedBytes += data.getReceivedBytes();
                    temp.add(data);
                }
            }

            if(tempTotalTransmittedBytes != totalTransmittedBytes ||
                    tempTotalReceivedBytes != totalReceivedBytes) {
                totalTransmittedBytes = tempTotalTransmittedBytes;
                totalReceivedBytes = tempTotalReceivedBytes;

                Collections.sort(temp);

                synchronized (appTrafficDataList) {
                    appTrafficDataList.clear();
                    appTrafficDataList.addAll(temp);
                }
            }
        }
    }

}
