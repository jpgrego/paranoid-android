package com.jpgrego.thesisapp.thesisapp.services;

/**
 * Created by jpgrego on 05/01/17.
 */

final class MozillaLocationResponse {
    private final Location location;
    private final int accuracy;

    MozillaLocationResponse(final Location location, final int accuracy) {
        this.location = location;
        this.accuracy = accuracy;
    }

    private final class Location {
        private final float lat;
        private final float lon;

        Location(final float lat, final float lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
