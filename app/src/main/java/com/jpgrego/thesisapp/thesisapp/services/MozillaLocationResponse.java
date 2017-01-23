package com.jpgrego.thesisapp.thesisapp.services;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jpgrego on 05/01/17.
 */

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class MozillaLocationResponse implements Parcelable {
    private final Location location;
    private final float accuracy;

    MozillaLocationResponse(final Location location, final float accuracy) {
        this.location = location;
        this.accuracy = accuracy;
    }

    private MozillaLocationResponse(Parcel in) {
        location = in.readParcelable(Location.class.getClassLoader());
        accuracy = in.readFloat();
    }

    public static final Creator<MozillaLocationResponse> CREATOR =
            new Creator<MozillaLocationResponse>() {
        @Override
        public MozillaLocationResponse createFromParcel(Parcel in) {
            return new MozillaLocationResponse(in);
        }

        @Override
        public MozillaLocationResponse[] newArray(int size) {
            return new MozillaLocationResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(location, flags);
        dest.writeFloat(accuracy);
    }

    public float getLatitude() {
        return location.lat;
    }

    public float getLongitude() {
        return location.lng;
    }

    public float getAccuracy() {
        return accuracy;
    }

    private static final class Location implements Parcelable {
        final float lat;
        private final float lng;

        Location(final float lat, final float lng) {
            this.lat = lat;
            this.lng = lng;
        }

        private Location(final Parcel in) {
            lat = in.readFloat();
            lng = in.readFloat();
        }

        public static final Creator<Location> CREATOR = new Creator<Location>() {
            @Override
            public Location createFromParcel(Parcel in) {
                return new Location(in);
            }

            @Override
            public Location[] newArray(int size) {
                return new Location[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(lat);
            dest.writeFloat(lng);
        }
    }
}
