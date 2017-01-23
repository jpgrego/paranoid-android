package com.jpgrego.thesisapp.thesisapp.services;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jpgrego on 05/01/17.
 */

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class LocationResponse implements Parcelable {
    private final Location location;
    private final float accuracy;

    private LocationResponse(final Location location, final float accuracy) {
        this.location = location;
        this.accuracy = accuracy;
    }

    static LocationResponse fromAndroidLocation(
            final android.location.Location location) {
        final Location myLoc = new Location(location.getLatitude(), location.getLongitude());
        final float accuracy = location.getAccuracy();
        return new LocationResponse(myLoc, accuracy);
    }

    private LocationResponse(Parcel in) {
        location = in.readParcelable(Location.class.getClassLoader());
        accuracy = in.readFloat();
    }

    public static final Creator<LocationResponse> CREATOR =
            new Creator<LocationResponse>() {
        @Override
        public LocationResponse createFromParcel(Parcel in) {
            return new LocationResponse(in);
        }

        @Override
        public LocationResponse[] newArray(int size) {
            return new LocationResponse[size];
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

    public double getLatitude() {
        return location.lat;
    }

    public double getLongitude() {
        return location.lng;
    }

    public float getAccuracy() {
        return accuracy;
    }

    private static final class Location implements Parcelable {
        private final double lat;
        private final double lng;

        Location(final double lat, final double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        private Location(final Parcel in) {
            lat = in.readDouble();
            lng = in.readDouble();
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
            dest.writeDouble(lat);
            dest.writeDouble(lng);
        }
    }
}
