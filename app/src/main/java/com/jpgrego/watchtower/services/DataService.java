package com.jpgrego.watchtower.services;

import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.AppTrafficData;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jpgrego on 13/12/16.
 */

public final class DataService extends Service {

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private static final int SERVICE_ID = 100;
    private static final int WRITE_DB_PERIOD_SECONDS = 10;
    private static final int UPDATE_LOCATION_PERIOD_SECONDS = 10;

    private final IBinder localBinder = new LocalBinder();

    private volatile LocationResponse currentLocation;
    private CellInfoListener cellInfoListener;
    private WifiInfoReceiver wifiInfoReceiver;
    private SensorInfoListener sensorInfoListener;
    private BluetoothInfoReceiver bluetoothInfoReceiver;
    private AppTrafficReceiver appTrafficReceiver;

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

    @Override
    public void onCreate() {
        startForeground(SERVICE_ID, generateNotification());
        cellInfoListener = new CellInfoListener(this);
        wifiInfoReceiver = new WifiInfoReceiver(this);
        sensorInfoListener = new SensorInfoListener(this);
        bluetoothInfoReceiver = new BluetoothInfoReceiver(this);
        appTrafficReceiver = new AppTrafficReceiver(this);
        new USBEventsReceiver(this);

        SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new WriteDBRunnable(),
                WRITE_DB_PERIOD_SECONDS, WRITE_DB_PERIOD_SECONDS, TimeUnit.SECONDS);
        SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new UpdateLocationRunnable(), 0,
                UPDATE_LOCATION_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private Notification generateNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.watchtower_notification);
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentText(getString(R.string.currently_monitoring));
        return notificationBuilder.build();
    }

    public LocationResponse getCurrentLocationResponse() {
        return currentLocation;
    }

    public ArrayList<Cell> getCellList() {
        return cellInfoListener.getSortedCellList();
    }

    public ArrayList<MySensor> getSensorList() {
        return sensorInfoListener.getSensorList();
    }

    public ArrayList<WifiAP> getWifiAPList() {
        return wifiInfoReceiver.getOrderedWifiAPList();
    }

    public String getCurrentlyConnectedWifiAP() {
        return wifiInfoReceiver.getCurrentWifiConnectionBSSID();
    }

    public ArrayList<MyBluetoothDevice> getBluetoothDeviceList() {
        return bluetoothInfoReceiver.getBluetoothDevices();
    }

    public String getCurrentlyConnectedBTDevice() {
        return bluetoothInfoReceiver.getCurrentUUID();
    }

    private void setCurrentLocationResponse(final LocationResponse location) {
        currentLocation = location;
    }

    public List<AppTrafficData> getAppTrafficDataList() {
        return appTrafficReceiver.getAppTrafficDataList();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public DataService getDataServiceInstance() {
            return DataService.this;
        }
    }

    private class UpdateLocationRunnable implements Runnable {
        @Override
        public void run() {
            final ArrayList<Cell> cellList = getCellList();
            final ArrayList<WifiAP> wifiAPList = getWifiAPList();
            final ArrayList<MyBluetoothDevice> bluetoothDeviceList = getBluetoothDeviceList();
            final LocationHelperData locationHelperData = new LocationHelperData(
                    cellInfoListener.getNetworkOperatorName(), cellInfoListener.getMcc(),
                    cellInfoListener.getMnc(), cellList, wifiAPList, bluetoothDeviceList);

            try {
                final Response<LocationResponse> resp =
                        mozillaLocationService.geolocate(locationHelperData).execute();
                setCurrentLocationResponse(resp.body());
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private class WriteDBRunnable implements Runnable {
        private final SQLiteDatabase db =
                new DatabaseHelper(DataService.this).getWritableDatabase();

        @Override
        public void run() {
            final ArrayList<Cell> cellList = cellInfoListener.getSortedCellList();
            final ArrayList<WifiAP> wifiAPList = wifiInfoReceiver.getOrderedWifiAPList();
            final ArrayList<MyBluetoothDevice> bluetoothDeviceList =
                    bluetoothInfoReceiver.getBluetoothDevices();

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

