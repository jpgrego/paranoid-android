package com.jpgrego.watchtower.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.jpgrego.watchtower.data.WifiAP;
import com.jpgrego.watchtower.services.DataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 24-08-2016.
 */
public final class WifiInfoReceiver extends BroadcastReceiver {

    private static final int WIFI_SCAN_DELAY_SECONDS = 10;

    private final Set<WifiAP> wifiAPSet = new HashSet<>();
    private final WifiManager wifiManager;

    private String currentBSSID = "";

    public WifiInfoReceiver(final Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                wifiManager.startScan();
            }
        }, 0, WIFI_SCAN_DELAY_SECONDS, TimeUnit.SECONDS);

        final IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        context.registerReceiver(this, wifiIntentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action;

        action = intent.getAction();

        switch(action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                final NetworkInfo networkInfo;

                networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    currentBSSID = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                } else {
                    currentBSSID = "";
                }

                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                final List<ScanResult> wifiScanList;

                wifiScanList = wifiManager.getScanResults();

                if (wifiScanList != null) {
                    synchronized (wifiAPSet) {
                        for (Iterator<WifiAP> it = wifiAPSet.iterator(); it.hasNext(); ) {
                            final WifiAP tempAP;

                            tempAP = it.next();

                            if(tempAP.getVisibilityCounter().decrementAndGet() <= 0) {
                                it.remove();
                            }
                        }

                        for (ScanResult scanResult : wifiScanList) {
                            final WifiAP tempAP;

                            tempAP = WifiAP.fromScanResult(scanResult);
                            wifiAPSet.remove(tempAP);
                            wifiAPSet.add(tempAP);
                        }
                    }
                }
                break;

            default:
                // not supposed to get here
                throw new IllegalArgumentException();
        }

    }

    public ArrayList<WifiAP> getOrderedWifiAPList() {
        final ArrayList<WifiAP> wifiAPList;

        synchronized (wifiAPSet) {
            wifiAPList = new ArrayList<>(wifiAPSet);
            Collections.sort(wifiAPList, Collections.reverseOrder());
            return wifiAPList;
        }
    }

    public String getCurrentWifiConnectionBSSID() {
        return currentBSSID;
    }
}
