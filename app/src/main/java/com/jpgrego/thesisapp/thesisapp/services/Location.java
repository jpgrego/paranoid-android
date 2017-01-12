package com.jpgrego.thesisapp.thesisapp.services;

/**
 * Created by jpgrego on 05/01/17.
 */

@SuppressWarnings({"FieldCanBeLocal", "unused"})
final class MozillaLocationResponse {
    private final Location location;
    private final float accuracy;

    MozillaLocationResponse(final Location location, final float accuracy) {
        this.location = location;
        this.accuracy = accuracy;
    }

    private static final class Location {
        private final float lat;
        private final float lng;

        Location(final float lat, final float lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
}
