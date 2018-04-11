package com.jpgrego.watchtower.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.data.MySensor;
import com.jpgrego.watchtower.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jpgrego on 28/11/16.
 */

public final class SensorsActivity extends BaseActivity {

    private final BroadcastReceiver sensorInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<MySensor> sensorList = intent.getParcelableArrayListExtra(
                    Constants.SENSOR_INFO_LIST_INTENT_EXTRA_NAME);
            updateSensorsTable(sensorList);
        }
    };

    private TableLayout sensorsTable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        sensorsTable = (TableLayout) findViewById(R.id.sensors_table);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(sensorInfoReceiver, new IntentFilter(Constants.SENSOR_INTENT_FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(sensorInfoReceiver);
    }

    //TODO: implement this
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void updateSensorsTable(List<MySensor> sensorList) {

        final TableRow sensorTableTitleRow =
                (TableRow) View.inflate(this, R.layout.sensors_table_title_row, null);

        sensorsTable.removeAllViews();
        sensorsTable.addView(sensorTableTitleRow);

        for (MySensor sensor : sensorList) {
            final TableRow sensorsTableDataRow =
                    (TableRow) View.inflate(this, R.layout.sensors_table_data_row,
                            null);
            final TextView sensorName = (TextView) sensorsTableDataRow.findViewById(R.id.name);
            final TextView sensorXAxisVal =
                    (TextView) sensorsTableDataRow.findViewById(R.id.x_axis);
            final TextView sensorYAxisVal =
                    (TextView) sensorsTableDataRow.findViewById(R.id.y_axis);
            final TextView sensorZAxisVal =
                    (TextView) sensorsTableDataRow.findViewById(R.id.z_axis);

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

}
