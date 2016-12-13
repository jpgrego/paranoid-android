package com.jpgrego.thesisapp.thesisapp.services;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import com.jpgrego.thesisapp.thesisapp.data.MySensor;
import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import com.jpgrego.thesisapp.thesisapp.listeners.CellInfoListener;
import com.jpgrego.thesisapp.thesisapp.listeners.SensorInfoListener;
import com.jpgrego.thesisapp.thesisapp.listeners.WifiInfoReceiver;
import com.jpgrego.thesisapp.thesisapp.utils.Constants;

import java.util.ArrayList;

/**
 * Created by jpgrego on 13/12/16.
 */

public final class DataService extends Service {

    private static final int SEND_INFO_PERIOD = 500;
    private static final Handler infoHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        final CellInfoListener cellInfoListener = new CellInfoListener(telephonyManager);
        final WifiInfoReceiver wifiInfoReceiver = new WifiInfoReceiver(wifiManager);
        final SensorInfoListener sensorInfoListener = new SensorInfoListener();

        telephonyManager.listen(cellInfoListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        registerReceiver(wifiInfoReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(wifiInfoReceiver,
                new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        for(Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            sensorManager.registerListener(sensorInfoListener, sensor, 3000000);
        }

        final Runnable cellInfoRunnable = new Runnable() {
            @Override
            public void run() {
                debug();
                sendCellInfo();
                sendWifiInfo();
                sendSensorInfo();
                infoHandler.postDelayed(this, SEND_INFO_PERIOD);
            }

            private void debug() {

            }

            private void sendCellInfo() {
                final Intent intent = new Intent(Constants.CELL_INTENT_FILTER_NAME);
                final ArrayList<Cell> cellList = cellInfoListener.getSortedCellList();
                intent.putExtra(Constants.CELL_INFO_LIST_INTENT_EXTRA_NAME, cellList);
                sendBroadcast(intent);
            }

            private void sendWifiInfo() {
                final Intent intent = new Intent(Constants.WIFI_INTENT_FILTER_NAME);
                final ArrayList<WifiAP> wifiAPList = wifiInfoReceiver.getOrderedWifiAPList();
                intent.putExtra(Constants.WIFI_INFO_LIST_INTENT_EXTRA_NAME, wifiAPList);
                intent.putExtra(Constants.WIFI_CURRENT_BSSID_INTENT_EXTRA_NAME,
                        wifiInfoReceiver.getCurrentWifiConnectionBSSID());
                sendBroadcast(intent);
            }

            private void sendSensorInfo() {
                final Intent intent = new Intent(Constants.SENSOR_INTENT_FILTER_NAME);
                final ArrayList<MySensor> sensorList = sensorInfoListener.getSensorList();
                intent.putExtra(Constants.SENSOR_INFO_LIST_INTENT_EXTRA_NAME, sensorList);
                sendBroadcast(intent);
            }
        };
        infoHandler.post(cellInfoRunnable);

        return super.onStartCommand(intent, flags, startId);
    }
}
