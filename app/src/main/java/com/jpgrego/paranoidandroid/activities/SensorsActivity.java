package com.jpgrego.paranoidandroid.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.data.MySensor;
import com.jpgrego.paranoidandroid.services.DataService;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by jpgrego on 28/11/16.
 */

public final class SensorsActivity extends BaseActivity {

    private static final int UPDATE_PERIOD_SECONDS = 1;
    private volatile ScheduledFuture<?> scheduledUpdates = null;
    private TableLayout sensorsTable;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            final DataService dataService =
                    ((DataService.LocalBinder) iBinder).getDataServiceInstance();

            scheduledUpdates = DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    new UpdateDataRunnable(dataService),
                    0 ,
                    UPDATE_PERIOD_SECONDS,
                    TimeUnit.SECONDS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // do nothing
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.sensors_activity_title);
        setContentView(R.layout.activity_sensors);
        sensorsTable =  findViewById(R.id.sensors_table);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Intent serviceIntent = new Intent(this, DataService.class);
        bindService(serviceIntent, serviceConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(scheduledUpdates != null) scheduledUpdates.cancel(true);
        unbindService(serviceConnection);
    }

    private void updateSensorsTable(List<MySensor> sensorList) {

        if(sensorList == null) return;

        final TableRow sensorTableTitleRow =
                (TableRow) View.inflate(this, R.layout.sensors_table_title_row, null);

        sensorsTable.removeAllViews();
        sensorsTable.addView(sensorTableTitleRow);

        for (MySensor sensor : sensorList) {
            final TableRow sensorsTableDataRow =
                    (TableRow) View.inflate(this, R.layout.sensors_table_data_row,
                            null);
            final TextView sensorName =  sensorsTableDataRow.findViewById(R.id.name);
            final TextView sensorXAxisVal =
                    sensorsTableDataRow.findViewById(R.id.x_axis);
            final TextView sensorYAxisVal =
                    sensorsTableDataRow.findViewById(R.id.y_axis);
            final TextView sensorZAxisVal =
                    sensorsTableDataRow.findViewById(R.id.z_axis);

            final String sensorType = getTypeFromInt(sensor.getSensorType());
            sensorName.setText(
                    sensorType.equals("Unknown") ? sensor.getSensorName() : sensorType);

            sensorXAxisVal.setText(String.format(Locale.US, "%.3f", sensor.getXAxis()));
            sensorYAxisVal.setText(String.format(Locale.US, "%.3f", sensor.getYAxis()));
            sensorZAxisVal.setText(String.format(Locale.US, "%.3f", sensor.getZAxis()));

            sensorsTable.addView(sensorsTableDataRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    @SuppressWarnings("deprecation")
    private static String getTypeFromInt(final int type) {
        switch(type) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Relative Humidity";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_ORIENTATION:
                return "Orientation";
            case Sensor.TYPE_TEMPERATURE:
                return "Device temperature";
            default:
                return "Unknown";
        }
    }

    private final class UpdateDataRunnable implements Runnable {

        private final DataService service;

        private UpdateDataRunnable(final DataService service) {
            this.service = service;
        }

        @Override
        public void run() {
            SensorsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateSensorsTable(service.getSensorList());
                }
            });
        }
    }

}
