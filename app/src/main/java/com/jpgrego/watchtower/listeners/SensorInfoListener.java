package com.jpgrego.watchtower.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import com.jpgrego.watchtower.data.MySensor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jgrego on 20-09-2016.
 */
public final class SensorInfoListener implements SensorEventListener {

    private final Set<MySensor> sensorSet = new HashSet<>();

    public SensorInfoListener() {

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
