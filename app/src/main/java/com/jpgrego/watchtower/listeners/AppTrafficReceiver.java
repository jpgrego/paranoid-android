package com.jpgrego.watchtower.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import com.jpgrego.watchtower.data.AppTrafficData;
import com.jpgrego.watchtower.utils.Constants;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficReceiver extends BroadcastReceiver {

    private static final int UPDATE_PERIOD = 60000; // 10 seconds
    private static final Handler UPDATE_HANDLER = new Handler();
    private final PackageManager packageManager;
    private final ArrayList<AppTrafficData> appTrafficDataList = new ArrayList<>();
    private long totalTransmittedBytes = 0;
    private long totalReceivedBytes = 0;

    public AppTrafficReceiver(final PackageManager packageManager) {
        this.packageManager = packageManager;
        UPDATE_HANDLER.post(new UpdateRunnable());
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
            UPDATE_HANDLER.postDelayed(this, UPDATE_PERIOD);
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
