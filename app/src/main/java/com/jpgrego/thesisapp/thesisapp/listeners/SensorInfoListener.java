package com.jpgrego.thesisapp.thesisapp.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jgrego on 20-09-2016.
 */
public final class SensorInfoListener implements SensorEventListener {

    private final Map<Sensor, float[]> sensorMap = new HashMap<>();

    public SensorInfoListener() {

    }

    public Map<Sensor, float[]> getSensorMap() {
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
