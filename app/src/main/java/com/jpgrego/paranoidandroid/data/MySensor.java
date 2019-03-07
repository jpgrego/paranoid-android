package com.jpgrego.paranoidandroid.data;

import android.hardware.SensorEvent;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by jpgrego on 13/12/16.
 */

public final class MySensor implements Parcelable {

    private volatile int hashCode;

    private final String sensorName;
    private final int sensorType;
    private final float xAxis, yAxis, zAxis;

    public MySensor(final SensorEvent sensorEvent) {
        this.sensorName = sensorEvent.sensor.getName();
        this.sensorType = sensorEvent.sensor.getType();
        this.xAxis = sensorEvent.values[0];

        // TODO: rethink this
        if(sensorEvent.values.length > 1) {
            this.yAxis = sensorEvent.values[1];
            this.zAxis = sensorEvent.values[2];
        } else {
            this.yAxis = 0;
            this.zAxis = 0;
        }
    }

    private MySensor(Parcel in) {
        sensorName = in.readString();
        sensorType = in.readInt();
        xAxis = in.readFloat();
        yAxis = in.readFloat();
        zAxis = in.readFloat();
    }

    public static final Creator<MySensor> CREATOR = new Creator<MySensor>() {
        @Override
        public MySensor createFromParcel(Parcel in) {
            return new MySensor(in);
        }

        @Override
        public MySensor[] newArray(int size) {
            return new MySensor[size];
        }
    };

    public String getSensorName() {
        return sensorName;
    }

    public int getSensorType() {
        return sensorType;
    }

    public float getXAxis() {
        return xAxis;
    }

    public float getYAxis() {
        return yAxis;
    }

    public float getZAxis() {
        return zAxis;
    }

    @Override
    public String toString() {
        return sensorName + " (" +
                String.format(Locale.US, "%.3f", xAxis) + ", " +
                String.format(Locale.US, "%.3f", yAxis) + ", " +
                String.format(Locale.US, "%.3f", zAxis) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MySensor)) {
            return false;
        }

        final MySensor mySensor = (MySensor) o;
        return mySensor.sensorType == this.sensorType &&
                mySensor.sensorName.equals(this.sensorName);
    }

    @Override
    public int hashCode() {
        int result = hashCode;

        if(result == 0) {
            result = 17;
            result += sensorName.hashCode();
            result = 31 * result + sensorType;
            hashCode = result;
        }

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sensorName);
        parcel.writeInt(sensorType);
        parcel.writeFloat(xAxis);
        parcel.writeFloat(yAxis);
        parcel.writeFloat(zAxis);
    }
}
