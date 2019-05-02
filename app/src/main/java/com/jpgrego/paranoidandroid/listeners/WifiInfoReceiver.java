package com.jpgrego.paranoidandroid.listeners;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.data.WifiAP;
import com.jpgrego.paranoidandroid.db.DatabaseContract.WifiAPEntry;
import com.jpgrego.paranoidandroid.db.DatabaseHelper;
import com.jpgrego.paranoidandroid.services.DataService;
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
    private final ConnectivityManager connectivityManager;
    private final IRadioNotificationFactory notificationFactory;

    private String currentBSSID = "";

    public WifiInfoReceiver(final Context context) {
        this.db = new DatabaseHelper(context).getWritableDatabase();
        this.wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        this.notificationFactory = NotificationFactory.getInstance(context);

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

                // TODO: find a better way to do this
                // A bit hacky, but I couldn't find a better way to avoid processing the two
                // consecutive NETWORK_STATE_CHANGED actions that fired up when connecting to a WiFi
                // network. Since I'm only interested in the first event, when there still isn't an
                // active network info, I do this hack. This might not work very well in the future.
                if(connectivityManager.getActiveNetworkInfo() != null) return;

                if(networkInfo.isConnected()) {
                    //currentBSSID = intent.getStringExtra(WifiManager.EXTRA_BSSID);

                    final WifiInfo info = wifiManager.getConnectionInfo();
                    currentBSSID = info.getBSSID();
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
                                final String oldSec = storedAP.getLastSecurityLabel();
                                final String newSec = connectedAP.getSecurityLabel()
                                        .replace("-", context.getString(
                                                R.string.ap_changed_sec_no_sec));

                                if (oldSec != null && !oldSec.equals("") &&
                                        !oldSec.equalsIgnoreCase(newSec)) {
                                    notificationFactory
                                            .wifiSecurityChangedNotification(ssid, oldSec, newSec);
                                }

                                final ContentValues cv = new ContentValues();
                                cv.put(WifiAPEntry.LAST_SECURITY_COLUMN, newSec);

                                db.update(WifiAPEntry.TABLE_NAME, cv,
                                        WifiAPEntry.BSSID_COLUMN + "=?",
                                        new String[]{currentBSSID});
                            }

                            break;
                        }
                    }

                    final boolean wasConnectedBefore = wasConnectedBefore(ssid, currentBSSID);
                    final int sameSsidDifferentBssidCount =
                            sameSsidDifferentBssidStored(ssid, currentBSSID);
                    final boolean sameSsidDifferentBssid = sameSsidDifferentBssidCount > 0;

                    // TODO: investigate how to add to existing notification, instead of creating new one
                    if(!wasConnectedBefore) {
                        if(storedAP != null) { // if it's not null, it exists in DB
                            final ContentValues cv = new ContentValues();
                            cv.put(WifiAPEntry.CONNECTED_COLUMN, 1);

                            db.update(WifiAPEntry.TABLE_NAME, cv,
                                    WifiAPEntry.BSSID_COLUMN + "=?",
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
                        }

                        // TODO: consider changing the text of this notification ("unknown" is misleading)
                        notificationFactory.wifiNewAPNotification(ssid, currentBSSID);
                    } else if(storedAP != null && !storedAP.isTrusted()) {
                        notificationFactory.wifiUntrustedAPNotification(ssid, currentBSSID);
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

                            final String oldSec = getAPLastSecurity(tempAP.getBssid());
                            final String newSec = tempAP.getSecurityLabel().replace("-",
                                    context.getString(R.string.ap_changed_sec_no_sec));

                            if (oldSec != null && !oldSec.equals("") &&
                                    !oldSec.equalsIgnoreCase(newSec)) {
                                notificationFactory
                                        .wifiSecurityChangedNotification(tempAP.getSsid(), oldSec,
                                                newSec);
                            }

                            final ContentValues cv = new ContentValues();
                            cv.put(WifiAPEntry.LAST_SECURITY_COLUMN, newSec);
                            db.update(WifiAPEntry.TABLE_NAME, cv,
                                    WifiAPEntry.BSSID_COLUMN + "=?",
                                    new String[]{tempAP.getBssid()});

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
                WifiAPEntry.BSSID_COLUMN + "=?", new String[]{bssid},
                null, null, null)) {
            // we only expect one result, BSSID is unique in our DB wifi table
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                return WifiAP.fromCursor(cursor);
            } else return null;
        }
    }

    private boolean wasConnectedBefore(final String ssid, final String bssid) {
        try (final Cursor cursor = db.query(WifiAPEntry.TABLE_NAME,
                new String[]{WifiAPEntry.SSID_COLUMN},
                WifiAPEntry.SSID_COLUMN + "=? and " +
                        WifiAPEntry.BSSID_COLUMN + "=? and " +
                        WifiAPEntry.CONNECTED_COLUMN + "=?",
                new String[]{ssid, bssid, "1"},
                null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    private int sameSsidDifferentBssidStored(final String ssid, final String bssid) {
        try (final Cursor cursor = db.query(WifiAPEntry.TABLE_NAME,
                new String[]{WifiAPEntry.SSID_COLUMN},
                WifiAPEntry.SSID_COLUMN + "=? and " +
                        WifiAPEntry.BSSID_COLUMN + "!=?",
                new String[]{ssid, bssid}, null, null, null)) {
            return cursor.getCount();
        }
    }

    private String getAPLastSecurity(final String bssid) {
        try (final Cursor cursor = db.query(WifiAPEntry.TABLE_NAME,
                new String[]{WifiAPEntry.LAST_SECURITY_COLUMN},
                WifiAPEntry.BSSID_COLUMN + "=?", new String[]{bssid}, null,
                null, null)) {
            if(cursor.moveToFirst()) {
                return cursor.getString(0);
            } else {
                return null;
            }
        }
    }
}

