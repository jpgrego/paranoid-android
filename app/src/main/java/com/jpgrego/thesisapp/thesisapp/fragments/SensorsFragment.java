package com.jpgrego.thesisapp.thesisapp.fragments;

import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jpgrego on 28/11/16.
 */

public final class SensorsFragment extends Fragment {

    private static final Handler SENSOR_UPDATE_HANDLER = new Handler();

    private TableLayout sensorsTable;
    private MainActivity mainActivity;

    private final Runnable sensorUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if(mainActivity != null && mainActivity.getSensorInfoListener() != null) {
                updateSensorsTable(mainActivity.getSensorInfoListener().getSensorMap());
            }
            SENSOR_UPDATE_HANDLER.postDelayed(this, 3000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View thisView = inflater.inflate(R.layout.fragment_sensors, container, false);
        sensorsTable = (TableLayout) thisView.findViewById(R.id.sensors_table);
        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity = (MainActivity) this.getContext();
        SENSOR_UPDATE_HANDLER.post(sensorUpdateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        SENSOR_UPDATE_HANDLER.removeCallbacks(sensorUpdateRunnable);
    }

    private void updateSensorsTable(Map<Sensor, float[]> sensorMap) {
        /*
         * This is done to avoid a NullPointerException being thrown by View.inflate, due to the
         * fact that getActivity() returns null in case the fragment isn't added to the activity,
         * which seems to happen occasionally
         */
        if (!isAdded()) {
            return;
        }

        final TableRow sensorTableTitleRow =
                (TableRow) View.inflate(getActivity(), R.layout.sensors_table_title_row, null);

        sensorsTable.removeAllViews();
        sensorsTable.addView(sensorTableTitleRow);

        for (Map.Entry<Sensor, float[]> sensorEntry : sensorMap.entrySet()) {
            final TableRow sensorsTableDataRow =
                    (TableRow) View.inflate(getActivity(), R.layout.sensors_table_data_row, null);
            final TextView sensorName = (TextView) sensorsTableDataRow.findViewById(R.id.name);
            final TextView sensorXAxisVal =
                    (TextView) sensorsTableDataRow.findViewById(R.id.x_axis);
            final TextView sensorYAxisVal =
                    (TextView) sensorsTableDataRow.findViewById(R.id.y_axis);
            final TextView sensorZAxisVal =
                    (TextView) sensorsTableDataRow.findViewById(R.id.z_axis);

            final String sensorType = getTypeFromInt(sensorEntry.getKey().getType());
            sensorName.setText(
                    sensorType.equals("Unknown") ? sensorEntry.getKey().getName() : sensorType);

            final float xVal = sensorEntry.getValue()[0], yVal = sensorEntry.getValue()[1],
                    zVal = sensorEntry.getValue()[2];
            sensorXAxisVal.setText(String.format(Locale.US, "%.3f", xVal));
            sensorYAxisVal.setText(String.format(Locale.US, "%.3f", yVal));
            sensorZAxisVal.setText(String.format(Locale.US, "%.3f", zVal));

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
