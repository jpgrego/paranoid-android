package com.jpgrego.thesisapp.thesisapp.services;
import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import com.jpgrego.thesisapp.thesisapp.data.MySensor;
import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import com.jpgrego.thesisapp.thesisapp.db.DatabaseContract;
import com.jpgrego.thesisapp.thesisapp.db.DatabaseHelper;
import com.jpgrego.thesisapp.thesisapp.listeners.CellInfoListener;
import com.jpgrego.thesisapp.thesisapp.listeners.SensorInfoListener;
import com.jpgrego.thesisapp.thesisapp.listeners.WifiInfoReceiver;
import com.jpgrego.thesisapp.thesisapp.utils.Constants;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jpgrego on 13/12/16.
 */

public final class DataService extends Service {


    private static final int SEND_INFO_PERIOD = 3000;
    private static final Handler INFO_HANDLER = new Handler();
    private final MozillaLocationService mozillaLocationService = new Retrofit.Builder()
            .baseUrl(MozillaLocationService.MOZILLA_LOCATION_SERVICE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MozillaLocationService.class);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(1337, generateNotification());
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
        final DatabaseHelper dbHelper = new DatabaseHelper(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        telephonyManager.listen(cellInfoListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        registerReceiver(wifiInfoReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(wifiInfoReceiver,
                new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            sensorManager.registerListener(sensorInfoListener, sensor, 3000000);
        }

        final Runnable sendInfoRunnable = new Runnable() {
            @Override
            public void run() {
                final ArrayList<Cell> cellList = cellInfoListener.getSortedCellList();
                final ArrayList<WifiAP> wifiAPList = wifiInfoReceiver.getOrderedWifiAPList();
                final ArrayList<MySensor> sensorList = sensorInfoListener.getSensorList();


                mozillaLocationService.geolocate().enqueue(new LocationCallback());

                sendCellInfo(cellList);
                sendWifiInfo(wifiAPList);
                sendSensorInfo(sensorList);
                writeCellInfoToDB(cellList);
                writeWifiAPInfoToDB(wifiAPList);
                INFO_HANDLER.postDelayed(this, SEND_INFO_PERIOD);
            }

            private void sendCellInfo(final ArrayList<Cell> cellList) {
                final Intent intent = new Intent(Constants.CELL_INTENT_FILTER_NAME);
                intent.putExtra(Constants.CELL_INFO_LIST_INTENT_EXTRA_NAME, cellList);
                sendBroadcast(intent);
            }

            private void sendWifiInfo(final ArrayList<WifiAP> wifiAPList) {
                final Intent intent = new Intent(Constants.WIFI_INTENT_FILTER_NAME);
                intent.putExtra(Constants.WIFI_INFO_LIST_INTENT_EXTRA_NAME, wifiAPList);
                intent.putExtra(Constants.WIFI_CURRENT_BSSID_INTENT_EXTRA_NAME,
                        wifiInfoReceiver.getCurrentWifiConnectionBSSID());
                sendBroadcast(intent);
            }

            private void sendSensorInfo(final ArrayList<MySensor> sensorList) {
                final Intent intent = new Intent(Constants.SENSOR_INTENT_FILTER_NAME);
                intent.putExtra(Constants.SENSOR_INFO_LIST_INTENT_EXTRA_NAME, sensorList);
                sendBroadcast(intent);
            }

            private void writeCellInfoToDB(final ArrayList<Cell> cellList) {
                final ContentValues values = new ContentValues();

                for (Cell cell : cellList) {
                    if (cell.cid == -1) {
                        continue;
                    }
                    values.put(DatabaseContract.CellEntry.CID_COLUMN, cell.cid);
                    values.put(DatabaseContract.CellEntry.MCC_COLUMN, cell.mcc);
                    values.put(DatabaseContract.CellEntry.MNC_COLUMN, cell.mnc);
                    values.put(DatabaseContract.CellEntry.LAC_COLUMN, cell.lac);
                    values.put(DatabaseContract.CellEntry.PSC_COLUMN, cell.psc);
                }
                if (values.size() > 0) {
                    db.insertWithOnConflict(DatabaseContract.CellEntry.TABLE_NAME, null, values,
                            SQLiteDatabase.CONFLICT_IGNORE);
                }
            }

            private void writeWifiAPInfoToDB(final ArrayList<WifiAP> wifiAPList) {
                final ContentValues values = new ContentValues();

                for (WifiAP wifiAP : wifiAPList) {
                    values.put(DatabaseContract.WifiAPEntry.BSSID_COLUMN, wifiAP.bssid);
                    values.put(DatabaseContract.WifiAPEntry.SSID_COLUMN, wifiAP.ssid);
                    values.put(DatabaseContract.WifiAPEntry.CHANNEL_COLUMN, wifiAP.channel);
                    values.put(DatabaseContract.WifiAPEntry.SECURITY, wifiAP.securityLabel);
                }
                if (values.size() > 0) {
                    db.insertWithOnConflict(DatabaseContract.WifiAPEntry.TABLE_NAME, null, values,
                            SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        };
        INFO_HANDLER.post(sendInfoRunnable);
        //super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private Notification generateNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.watchtower_notification);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentText("Monitoring!");
        return notificationBuilder.build();
    }

    private class LocationCallback implements Callback<MozillaLocationResponse> {
        @Override
        public void onResponse(Call<MozillaLocationResponse> call,
                               Response<MozillaLocationResponse> response) {
            final MozillaLocationResponse resp = response.body();
            System.out.println("success!");
        }

        @Override
        public void onFailure(Call<MozillaLocationResponse> call, Throwable t) {

        }
    }
}

