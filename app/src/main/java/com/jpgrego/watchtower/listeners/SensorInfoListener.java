package com.jpgrego.watchtower.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.jpgrego.watchtower.data.MySensor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jgrego on 20-09-2016.
 */
public final class SensorInfoListener implements SensorEventListener {

    private final Set<MySensor> sensorSet = new HashSet<>();

    public SensorInfoListener(final Context context) {
        final SensorManager sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            //sensorManager.registerListener(this, sensor, 3000000);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public ArrayList<MySensor> getSensorList() {
        synchronized (sensorSet) {
            return new ArrayList<>(sensorSet);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (sensorSet) {
            final MySensor mySensor = new MySensor(event);
            sensorSet.remove(mySensor);
            sensorSet.add(mySensor);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
