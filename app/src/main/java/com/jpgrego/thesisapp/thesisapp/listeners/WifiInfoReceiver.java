package com.jpgrego.thesisapp.thesisapp.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jpgrego on 24-08-2016.
 */
public class WifiInfoReceiver extends BroadcastReceiver {

    private static final int WIFI_SCAN_DELAY = 3000;


    private final Set<WifiAP> wifiAPSet = new HashSet<>();
    private final WifiManager wifiManager;
    private String currentBSSID = "";

    public WifiInfoReceiver(final WifiManager wifiManager) {
        final Handler wifiScanHandler;

        wifiScanHandler = new Handler();
        this.wifiManager = wifiManager;

        new Runnable() {
            @Override
            public void run() {
                wifiManager.startScan();
                wifiScanHandler.postDelayed(this, WIFI_SCAN_DELAY);
            }
        }.run();
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
                            if (tempAP.getVisibilityCounter() > 0) {
                                tempAP.decrementVisibilityCounter();
                            } else if (tempAP.getVisibilityCounter() <= 0) {
                                it.remove();
                            }
                        }

                        for (ScanResult scanResult : wifiScanList) {
                            final WifiAP tempAP;

                            tempAP = new WifiAP(scanResult);
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

    public List<WifiAP> getOrderedWifiAPList() {
        final List<WifiAP> wifiAPList;

        synchronized (wifiAPSet) {
            wifiAPList = new ArrayList<>(wifiAPSet);
            Collections.sort(wifiAPList, Collections.reverseOrder());
            return Collections.unmodifiableList(wifiAPList);
        }
    }

    public String getCurrentWifiConnectionBSSID() {
        return currentBSSID;
    }
}
