package com.jpgrego.watchtower.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.jpgrego.watchtower.data.AppTrafficData;
import com.jpgrego.watchtower.services.DataService;
import com.jpgrego.watchtower.utils.Constants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficReceiver extends BroadcastReceiver {

    private static final int UPDATE_PERIOD_SECONDS = 10;

    private final PackageManager packageManager;
    private final ArrayList<AppTrafficData> appTrafficDataList = new ArrayList<>();

    private long totalTransmittedBytes = 0;
    private long totalReceivedBytes = 0;

    public AppTrafficReceiver(final Context context) {
        this.packageManager = context.getPackageManager();
        DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new UpdateRunnable(), 0,
                UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS);

        context.registerReceiver(this,
                new IntentFilter(Constants.APP_TRAFFIC_REQUEST_INTENT_FILTER_NAME));
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

            Collections.sort(appTrafficDataList);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent myIntent = new Intent(Constants.APP_TRAFFIC_RESPONSE_INTENT_FILTER_NAME);
        myIntent.putExtra(Constants.APP_TRAFFIC_LIST_INTENT_EXTRA_NAME,
                appTrafficDataList);
        context.sendBroadcast(myIntent);
    }
}
