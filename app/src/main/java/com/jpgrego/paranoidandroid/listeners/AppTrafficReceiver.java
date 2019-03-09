package com.jpgrego.paranoidandroid.listeners;

import android.annotation.SuppressLint;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jpgrego.paranoidandroid.data.AppTrafficData;
import com.jpgrego.paranoidandroid.services.DataService;
import com.jpgrego.paranoidandroid.utils.NetworkStatsUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 1/30/17.
 */

public final class AppTrafficReceiver {

    private static final int UPDATE_PERIOD_SECONDS = 60;

    private final Context context;
    private final PackageManager packageManager;
    private final ArrayList<AppTrafficData> appTrafficDataList = new ArrayList<>();

    private long totalTransmittedBytes = 0;
    private long totalReceivedBytes = 0;

    public AppTrafficReceiver(final Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();

        final boolean useTrafficStats = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;

        DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new UpdateRunnable(useTrafficStats),
                0, UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS);
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

        private final boolean useTrafficStats;
        private final String[] subscriberIds;

        // already checked in the beginning of the app
        @SuppressLint("MissingPermission")
        private UpdateRunnable(final boolean useTrafficStats) {
            this.useTrafficStats = useTrafficStats;

            if(useTrafficStats) subscriberIds = new String[]{""};
            else {

                final TelephonyManager telephonyManager =
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    final SubscriptionManager subscriptionManager = (SubscriptionManager)
                            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

                    if(subscriptionManager == null) {
                        this.subscriberIds = new String[]{""};
                        return;
                    }

                    final List<SubscriptionInfo> subscriptionInfos =
                            subscriptionManager.getActiveSubscriptionInfoList();

                    final int[] subscriptionIds = new int[subscriptionInfos.size()];

                    int i = 0;
                    for(final SubscriptionInfo info : subscriptionInfos) {
                        subscriptionIds[i++] = info.getSubscriptionId();

                    }

                    subscriberIds = new String[subscriptionIds.length];
                    i = 0;
                    for (final int subscriptionId : subscriptionIds) {
                        try {
                            final Class cls = Class.forName("android.telephony.TelephonyManager");
                            final Method method = cls.getMethod("getSubscriberId", int.class);
                            final Object obj = method.invoke(telephonyManager, subscriptionId);

                            subscriberIds[i++] = (String) obj;
                        } catch (final Exception ex) {
                            // do nothing
                        }
                    }
                } else {
                    this.subscriberIds = new String[]{
                            telephonyManager != null ? telephonyManager.getSubscriberId() : ""};
                }
            }
        }

        @SuppressLint("NewApi")
        @Override
        public void run() {

            final ArrayList<AppTrafficData> temp = new ArrayList<>();
            int tempTotalTransmittedBytes = 0;
            int tempTotalReceivedBytes = 0;

            for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {

                final AppTrafficData data;

                if(useTrafficStats) {
                    data = AppTrafficData.fromApplicationInfo(appInfo, packageManager);
                } else {
                    final NetworkStatsManager networkStatsManager =
                            (NetworkStatsManager)
                                    context.getSystemService(Context.NETWORK_STATS_SERVICE);
                    if(networkStatsManager != null) {
                        data = AppTrafficData.fromApplicationInfo(appInfo, packageManager,
                                new NetworkStatsUtils(networkStatsManager, subscriberIds));
                    } else {
                        Log.e(this.getClass().getSimpleName(),
                                "Couldn't obtain NetworkStatsManager");
                        return;
                    }
                }

                if (data.hasNetworkActivity()) {
                    tempTotalTransmittedBytes += data.getWifiTransmittedBytes();
                    tempTotalReceivedBytes += data.getWifiReceivedBytes();
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
