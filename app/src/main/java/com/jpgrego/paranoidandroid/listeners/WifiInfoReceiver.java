package com.jpgrego.paranoidandroid.listeners;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.jpgrego.paranoidandroid.data.WifiAP;
import com.jpgrego.paranoidandroid.db.DatabaseContract.WifiAPEntry;
import com.jpgrego.paranoidandroid.db.DatabaseHelper;
import com.jpgrego.paranoidandroid.services.DataService;
import com.jpgrego.paranoidandroid.utils.notifications.IDebugNotificationFactory;
import com.jpgrego.paranoidandroid.utils.notifications.IRadioNotificationFactory;
import com.jpgrego.paranoidandroid.utils.notifications.NotificationFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 24-08-2016.
 */
public final class WifiInfoReceiver extends BroadcastReceiver {

    private static final int WIFI_SCAN_DELAY_SECONDS = 10;

    private final SQLiteDatabase db;
    private final Set<WifiAP> wifiAPSet = new TreeSet<>();
    private final WifiManager wifiManager;
    private final IRadioNotificationFactory notificationFactory;
    private final IDebugNotificationFactory debugNotificationFactory;

    private String currentBSSID = "";

    public WifiInfoReceiver(final Context context) {
        this.db = new DatabaseHelper(context).getWritableDatabase();
        this.wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        this.notificationFactory = new NotificationFactory(context);
        this.debugNotificationFactory = new NotificationFactory(context);

        if(wifiManager == null) return;

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

        if(action == null) return;

        switch(action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                final NetworkInfo networkInfo;

                networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    currentBSSID = intent.getStringExtra(WifiManager.EXTRA_BSSID);

                    final WifiInfo info = wifiManager.getConnectionInfo();
                    final String ssid = info.getSSID()
                            .replaceAll("\"", "");

                    final WifiAP storedAP = getAPFromDB(currentBSSID);

                    // assuming that, in order to connect, there must be scan results available...
                    // this is the only way I know of to get the security type of the network
                    WifiAP connectedAP = null;
                    for(final ScanResult scanResult : wifiManager.getScanResults()) {
                        if(scanResult.BSSID.equalsIgnoreCase(currentBSSID)) {
                            connectedAP = WifiAP.fromScanResult(scanResult);

                            if(storedAP != null) {
                                final String oldSec = storedAP.getSecurityLabel();
                                final String newSec = connectedAP.getSecurityLabel();

                                if (!oldSec.equalsIgnoreCase(newSec)) {
                                    notificationFactory
                                            .wifiSecurityChangedNotification(oldSec, newSec);
                                }
                            }

                            break;
                        }
                    }

                    final boolean wasConnectedBefore = wasConnectedBefore(currentBSSID);
                    final int sameSsidDifferentBssidCount =
                            sameSsidDifferentBssidStored(ssid, currentBSSID);
                    final boolean sameSsidDifferentBssid = sameSsidDifferentBssidCount > 0;

                    // TODO: investigate how to add to existing notification, instead of creating new one
                    // TODO: work out "decision" notifications
                    if(!wasConnectedBefore) {
                        if(storedAP != null) { // if it's not null, it exists in DB
                            final ContentValues cv = new ContentValues();
                            cv.put(WifiAPEntry.CONNECTED_COLUMN, 1);

                            db.update(WifiAPEntry.TABLE_NAME, cv, "bssid=?",
                                    new String[]{currentBSSID});
                        } else if (connectedAP != null) {
                            final ContentValues cv = new ContentValues();
                            cv.put(WifiAPEntry.BSSID_COLUMN, currentBSSID);
                            cv.put(WifiAPEntry.SSID_COLUMN, ssid);
                            cv.put(WifiAPEntry.CHANNEL_COLUMN, connectedAP.getChannel());
                            cv.put(WifiAPEntry.SECURITY_COLUMN, connectedAP.getSecurityLabel());
                            cv.put(WifiAPEntry.CONNECTED_COLUMN, 1);

                            db.insertWithOnConflict(WifiAPEntry.TABLE_NAME,
                                    null, cv, SQLiteDatabase.CONFLICT_REPLACE);

                            // TODO: delete
                            debugNotificationFactory.testNotification("reached", "yup!");
                        }

                        // TODO: consider changing the text of this notification ("unknown" is misleading)
                        notificationFactory.wifiNewAPNotification(ssid, currentBSSID);
                    }

                    if(sameSsidDifferentBssid) {
                        notificationFactory.wifiSimilarAPNotification(ssid,
                                sameSsidDifferentBssidCount);
                    }

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
            //Collections.sort(wifiAPList, Collections.reverseOrder());
            return wifiAPList;
        }
    }

    public String getCurrentWifiConnectionBSSID() {
        return currentBSSID;
    }

    private WifiAP getAPFromDB(final String bssid) {
        try (final Cursor cursor = db.query(WifiAPEntry.TABLE_NAME, null,
                "bssid=?", new String[]{bssid},
                null, null, null)) {
            // we only expect one result, BSSID is unique in our DB wifi table
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                return WifiAP.fromCursor(cursor);
            } else return null;
        }
    }

    private boolean wasConnectedBefore(final String bssid) {
        try (final Cursor cursor = db.query(WifiAPEntry.TABLE_NAME,
                new String[]{WifiAPEntry.SSID_COLUMN},
                "bssid=? and connected=?", new String[]{bssid, "1"},
                null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    private int sameSsidDifferentBssidStored(final String ssid, final String bssid) {
        try (final Cursor cursor = db.query(WifiAPEntry.TABLE_NAME,
                new String[]{WifiAPEntry.SSID_COLUMN}, "ssid=? and bssid!=?",
                new String[]{ssid, bssid}, null, null, null)) {
            return cursor.getCount();
        }
    }
}

