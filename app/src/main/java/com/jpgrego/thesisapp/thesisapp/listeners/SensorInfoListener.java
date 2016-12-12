package com.jpgrego.thesisapp.thesisapp.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.HashMap;

/**
 * Created by jgrego on 20-09-2016.
 */
public final class SensorInfoListener implements SensorEventListener {

    private final HashMap<Sensor, float[]> sensorMap = new HashMap<>();

    public SensorInfoListener() {

    }

    public HashMap<Sensor, float[]> getSensorMap() {
        return sensorMap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorMap.put(event.sensor, event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
