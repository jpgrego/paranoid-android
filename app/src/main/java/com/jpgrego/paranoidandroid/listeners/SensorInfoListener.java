package com.jpgrego.paranoidandroid.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.jpgrego.paranoidandroid.data.MySensor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by jpgrego on 20-09-2016.
 * https://link.springer.com/article/10.1186/s13678-016-0004-1
 */
public final class SensorInfoListener implements SensorEventListener {

    private static final int CSV_THRESHOLD = 23;
    private static final int CAV_THRESHOLD = 18;
    private static final float CCA_THRESHOLD = 65.5f;
    private static final int ACC_VALS_MAX_SIZE = 20;

    private static long timestamp = 0L;
    private final Set<MySensor> sensorSet = new HashSet<>();
    private LinkedList<float[]> accelerometerValues = new LinkedList<>();

    public SensorInfoListener(final Context context) {
        final SensorManager sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                100000000);

        /*
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)) {
            //sensorManager.registerListener(this, sensor, 3000000);
            //sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensor, 1000000);
        }
        */
    }

    public ArrayList<MySensor> getSensorList() {
        synchronized (sensorSet) {
            return new ArrayList<>(sensorSet);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final MySensor mySensor = new MySensor(event);

        //if(event.timestamp - timestamp >= 1000000000) {
        if(event.timestamp - timestamp >= 100000000) {
            timestamp = event.timestamp;

            addValuesToList(
                    new float[]{mySensor.getXAxis(), mySensor.getYAxis(), mySensor.getZAxis()});

            if(svDidItFall() && avDidItFall() && caDidItFall()) {
                Log.e("*********", "FALL");

                Log.e("*** ARRAY ***", "");
                for(final float[] vals : accelerometerValues) {
                    Log.e("vals", String.format("(%f;%f;%f)", vals[0], vals[1], vals[2]));
                }
                Log.e("***", "");
            }

            synchronized (sensorSet) {
                sensorSet.remove(mySensor);
                sensorSet.add(mySensor);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private boolean svDidItFall() {
        if(accelerometerValues.size() > 0) {
            final float[] values = accelerometerValues.getLast();
            final float accelerationSum =
                    Math.abs(values[0]) + Math.abs(values[1]) + Math.abs(values[2]);
            if(accelerationSum >= CSV_THRESHOLD) {
                Log.e("CSV", String.format("%f", accelerationSum));
                return true;
            }
        }
        return false;
    }

    private boolean avDidItFall() {
        final int listSize = accelerometerValues.size();
        if(listSize > 1) {
            for(int i = 0; i < listSize; i+= 2) {
                final float[] a = accelerometerValues.get(i);
                final float[] b = accelerometerValues.get(i+1);

                float scalarProduct = 0f;
                for (int j = 0; j < 3; ++j) {
                    scalarProduct += a[j] * b[j];
                }

                final double xcomp = Math.pow(Math.abs(a[0] - b[0]), 2);
                final double ycomp = Math.pow(Math.abs(a[1] - b[1]), 2);
                final double zcomp = Math.pow(Math.abs(a[2] - b[2]), 2);
                final double euclidianNorm = Math.sqrt(xcomp + ycomp + zcomp);

                final double result = Math.acos(scalarProduct / euclidianNorm) * (180.0f / Math.PI);

                if (result > CAV_THRESHOLD) {
                    Log.e("CAV", String.format("%f", result));
                    return true;
                }
            }
        }

        return false;
    }

    private boolean caDidItFall() {
        final int listSize = accelerometerValues.size();
        if(listSize >= ACC_VALS_MAX_SIZE) {
            final float[] avgFirstSecond = new float[3];
            final float[] avgLastSecond = new float[3];

            for(int i = 0; i < 10; ++i) {
                avgFirstSecond[0] += accelerometerValues.get(i)[0];
                avgFirstSecond[1] += accelerometerValues.get(i)[1];
                avgFirstSecond[2] += accelerometerValues.get(i)[2];
            }
            avgFirstSecond[0] /= 10;
            avgFirstSecond[1] /= 10;
            avgFirstSecond[2] /= 10;

            for(int i = 10; i < 20; ++i) {
                avgLastSecond[0] += accelerometerValues.get(i)[0];
                avgLastSecond[1] += accelerometerValues.get(i)[1];
                avgLastSecond[2] += accelerometerValues.get(i)[2];
            }
            avgLastSecond[0] /= 10;
            avgLastSecond[1] /= 10;
            avgLastSecond[2] /= 10;

            float scalarProduct = 0f;
            for(int i = 0; i < 3; ++i) {
                scalarProduct += avgFirstSecond[i] * avgLastSecond[i];
            }

            final double xcomp = Math.pow(Math.abs(avgFirstSecond[0] - avgLastSecond[0]), 2);
            final double ycomp = Math.pow(Math.abs(avgFirstSecond[1] - avgLastSecond[1]), 2);
            final double zcomp = Math.pow(Math.abs(avgFirstSecond[2] - avgLastSecond[2]), 2);

            final double euclidianNorm = Math.sqrt(xcomp + ycomp + zcomp);

            final double result = Math.acos(scalarProduct / euclidianNorm) * (180.0f / Math.PI);

            if(result > CCA_THRESHOLD) {
                Log.e("CCA", String.format("%f", result));
                return true;
            }
        }

        return false;
    }

    private void addValuesToList(final float[] values) {
        if(accelerometerValues.size() >= ACC_VALS_MAX_SIZE) {
            this.accelerometerValues.removeFirst();
        }

        this.accelerometerValues.add(values);
    }
}
