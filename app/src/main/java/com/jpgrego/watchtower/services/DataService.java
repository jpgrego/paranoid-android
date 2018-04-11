package com.jpgrego.watchtower.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.Cell;
import com.jpgrego.watchtower.data.MyBluetoothDevice;
import com.jpgrego.watchtower.data.MySensor;
import com.jpgrego.watchtower.data.WifiAP;
import com.jpgrego.watchtower.db.DatabaseContract;
import com.jpgrego.watchtower.db.DatabaseHelper;
import com.jpgrego.watchtower.listeners.AppTrafficReceiver;
import com.jpgrego.watchtower.listeners.BluetoothInfoReceiver;
import com.jpgrego.watchtower.listeners.CellInfoListener;
import com.jpgrego.watchtower.listeners.SensorInfoListener;
import com.jpgrego.watchtower.listeners.USBEventsReceiver;
import com.jpgrego.watchtower.listeners.WifiInfoReceiver;
import com.jpgrego.watchtower.utils.Constants;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jpgrego on 13/12/16.
 */

public final class DataService extends Service implements LocationListener {

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private static final int SERVICE_ID = 100;
    private static final int SEND_INFO_PERIOD_SECONDS = 1;
    private static final int WRITE_DB_PERIOD_SECONDS = 10;

    private CellInfoListener cellInfoListener;
    private WifiInfoReceiver wifiInfoReceiver;
    private SensorInfoListener sensorInfoListener;
    private BluetoothInfoReceiver bluetoothInfoReceiver;

    /*
    private final HttpLoggingInterceptor loggingInterceptor =
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    private final OkHttpClient.Builder clientBuilder =
            new OkHttpClient().newBuilder().addInterceptor(loggingInterceptor);
    */

    private final MozillaLocationService mozillaLocationService = new Retrofit.Builder()
            .baseUrl(MozillaLocationService.MOZILLA_LOCATION_SERVICE_URL)
            //.client(clientBuilder.build())
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
        startForeground(SERVICE_ID, generateNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cellInfoListener = new CellInfoListener(this);
        wifiInfoReceiver = new WifiInfoReceiver(this);
        sensorInfoListener = new SensorInfoListener(this);
        bluetoothInfoReceiver = new BluetoothInfoReceiver(this);
        new AppTrafficReceiver(this);
        new USBEventsReceiver(this);

        final LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new SendInfoRunnable(), 0,
                SEND_INFO_PERIOD_SECONDS, TimeUnit.SECONDS);
        SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new WriteDBRunnable(),
                WRITE_DB_PERIOD_SECONDS, WRITE_DB_PERIOD_SECONDS, TimeUnit.SECONDS);
        //super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private Notification generateNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.watchtower_notification);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentText(getString(R.string.currently_monitoring));
        return notificationBuilder.build();
    }

    @Override
    public void onLocationChanged(Location location) {
        // do nothing
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        // do nothing
    }

    @Override
    public void onProviderDisabled(String provider) {
        // do nothing
    }

    private class LocationCallback implements Callback<LocationResponse> {
        @Override
        public void onResponse(Call<LocationResponse> call,
                               Response<LocationResponse> response) {
            final LocationResponse resp = response.body();
            final Intent intent = new Intent(Constants.MAP_INTENT_FILTER_NAME);
            intent.putExtra(Constants.MAP_LOCATION_EXTRA_NAME, resp);
            sendBroadcast(intent);
        }

        @Override
        public void onFailure(Call<LocationResponse> call, Throwable t) {

        }
    }

    private class SendInfoRunnable implements Runnable {
        @Override
        public void run() {
            final ArrayList<Cell> cellList = cellInfoListener.getSortedCellList();
            final ArrayList<WifiAP> wifiAPList = wifiInfoReceiver.getOrderedWifiAPList();
            final ArrayList<MySensor> sensorList = sensorInfoListener.getSensorList();
            final ArrayList<MyBluetoothDevice> bluetoothDeviceList =
                    bluetoothInfoReceiver.getBluetoothDevices();

            sendCellInfo(cellList);
            sendWifiInfo(wifiAPList);
            sendBluetoothInfo(bluetoothDeviceList);
            sendSensorInfo(sensorList);
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

        private void sendBluetoothInfo(final ArrayList<MyBluetoothDevice> list) {
            final Intent intent = new Intent(Constants.BLUETOOTH_INTENT_FILTER_NAME);
            intent.putExtra(Constants.BLUETOOTH_INFO_LIST_INTENT_EXTRA_NAME, list);
            intent.putExtra(Constants.BLUETOOTH_CURRENT_UUID_INTENT_EXTRA_NAME,
                    bluetoothInfoReceiver.getCurrentUUID());
            sendBroadcast(intent);
        }

        private void sendSensorInfo(final ArrayList<MySensor> sensorList) {
            final Intent intent = new Intent(Constants.SENSOR_INTENT_FILTER_NAME);
            intent.putExtra(Constants.SENSOR_INFO_LIST_INTENT_EXTRA_NAME, sensorList);
            sendBroadcast(intent);
        }
    }

    private class WriteDBRunnable implements Runnable {
        private final SQLiteDatabase db =
                new DatabaseHelper(DataService.this).getWritableDatabase();

        @Override
        public void run() {
            final Location location;
            if (ActivityCompat.checkSelfPermission(DataService.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                final LocationManager locationManager =
                        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                location =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                location = null;
            }

            final ArrayList<Cell> cellList = cellInfoListener.getSortedCellList();
            final ArrayList<WifiAP> wifiAPList = wifiInfoReceiver.getOrderedWifiAPList();
            final ArrayList<MyBluetoothDevice> bluetoothDeviceList =
                    bluetoothInfoReceiver.getBluetoothDevices();

            if (location != null) {
                final LocationResponse locationResponse =
                        LocationResponse.fromAndroidLocation(location);
                final Intent intent = new Intent(Constants.MAP_INTENT_FILTER_NAME);
                intent.putExtra(Constants.MAP_LOCATION_EXTRA_NAME, locationResponse);
                sendBroadcast(intent);
            } else {
                final LocationHelperData locationHelperData = new LocationHelperData(
                        cellInfoListener.getNetworkOperatorName(), cellInfoListener.getMcc(),
                        cellInfoListener.getMnc(), cellList, wifiAPList, bluetoothDeviceList);
                mozillaLocationService.geolocate(locationHelperData)
                        .enqueue(new LocationCallback());
            }

            writeCellInfoToDB(cellList);
            writeWifiAPInfoToDB(wifiAPList);
            writeBluetoothInfoToDB(bluetoothDeviceList);
        }

        private void writeCellInfoToDB(final ArrayList<Cell> cellList) {
            final ContentValues values = new ContentValues();

            for (Cell cell : cellList) {
                if (cell.getCid() == -1) {
                    continue;
                }
                values.put(DatabaseContract.CellEntry.CID_COLUMN, cell.getCid());
                values.put(DatabaseContract.CellEntry.MCC_COLUMN, cell.getMcc());
                values.put(DatabaseContract.CellEntry.MNC_COLUMN, cell.getMnc());
                values.put(DatabaseContract.CellEntry.LAC_COLUMN, cell.getLac());
                values.put(DatabaseContract.CellEntry.PSC_COLUMN, cell.getPsc());
            }
            if (values.size() > 0) {
                db.insertWithOnConflict(DatabaseContract.CellEntry.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE);
            }
        }

        private void writeWifiAPInfoToDB(final ArrayList<WifiAP> wifiAPList) {
            final ContentValues values = new ContentValues();

            for (WifiAP wifiAP : wifiAPList) {
                values.put(DatabaseContract.WifiAPEntry.BSSID_COLUMN, wifiAP.getBssid());
                values.put(DatabaseContract.WifiAPEntry.SSID_COLUMN, wifiAP.getSsid());
                values.put(DatabaseContract.WifiAPEntry.CHANNEL_COLUMN, wifiAP.getChannel());
                values.put(DatabaseContract.WifiAPEntry.SECURITY, wifiAP.getSecurityLabel());
            }
            if (values.size() > 0) {
                db.insertWithOnConflict(DatabaseContract.WifiAPEntry.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE);
            }
        }

        private void writeBluetoothInfoToDB(
                final ArrayList<MyBluetoothDevice> bluetoothDeviceList) {
            final ContentValues values = new ContentValues();

            for(MyBluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                values.put(DatabaseContract.BluetoothEntry.NAME_COLUMN,
                        bluetoothDevice.getName());
                values.put(DatabaseContract.BluetoothEntry.ADDRESS_COLUMN,
                        bluetoothDevice.getAddress());
                values.put(DatabaseContract.BluetoothEntry.TYPE_COLUMN,
                        bluetoothDevice.getType());
            }

            if(values.size() > 0) {
                db.insertWithOnConflict(DatabaseContract.BluetoothEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }

    }
}

