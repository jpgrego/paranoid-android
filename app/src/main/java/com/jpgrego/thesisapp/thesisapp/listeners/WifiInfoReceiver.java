package com.jpgrego.thesisapp.thesisapp.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

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

    private final Set<WifiAP> wifiAPSet = new HashSet<>();
    private final WifiManager wifiManager;

    public WifiInfoReceiver(final WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final List<ScanResult> wifiScanList;
        WifiAP tempAP;

        wifiScanList = wifiManager.getScanResults();


        if (wifiScanList != null) {
            synchronized (wifiAPSet) {
                for (Iterator<WifiAP> it = wifiAPSet.iterator(); it.hasNext(); ) {
                    tempAP = it.next();
                    if (tempAP.getVisibilityCounter() > 0) {
                        tempAP.decrementVisibilityCounter();
                    } else if (tempAP.getVisibilityCounter() <= 0) {
                        it.remove();
                    }
                }

                for (ScanResult scanResult : wifiScanList) {
                    tempAP = new WifiAP(scanResult);
                    wifiAPSet.remove(tempAP);
                    wifiAPSet.add(tempAP);
                }
            }
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
}
