package com.jpgrego.watchtower.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.services.LocationResponse;
import com.jpgrego.watchtower.utils.Constants;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.List;
import java.util.Locale;


/**
 * Created by jpgrego on 28/11/16.
 */

public final class MapActivity extends BaseActivity {

    private static final int RADIUS_BORDER_COLOR =
            Color.argb(70, 72, 133, 237);
    private static final int RADIUS_FILL_COLOR = Color.argb(30, 72, 133, 237);
    private static Marker currentLocationMarker;
    private static Polygon currentLocationRadius;
    private MapView mapView;
    private IMapController mapController;

    private final BroadcastReceiver locationInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final LocationResponse location =
                    intent.getParcelableExtra(Constants.MAP_LOCATION_EXTRA_NAME);
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            final float accuracy = location.getAccuracy();
            final GeoPoint point = new GeoPoint(latitude, longitude);

            if(mapController != null) {
                final List<Overlay> overlays = mapView.getOverlays();

                if(currentLocationMarker != null) {
                    currentLocationMarker.closeInfoWindow();
                    overlays.remove(currentLocationMarker);
                }

                if(currentLocationRadius != null) {
                    overlays.remove(currentLocationRadius);
                }

                currentLocationMarker = new Marker(mapView);
                //noinspection deprecation
                currentLocationMarker.setIcon(getResources()
                        .getDrawable(R.drawable.marker_default_focused_base));
                currentLocationMarker.setPosition(point);
                currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                currentLocationMarker.setTitle(
                        String.format(Locale.US, "%f\n%f", latitude,
                                longitude));
                currentLocationMarker.setSubDescription(
                        String.format(Locale.US, "+-%fm", accuracy));
                currentLocationMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble,
                        mapView));
                currentLocationMarker.showInfoWindow();
                overlays.add(currentLocationMarker);

                currentLocationRadius = new Polygon();
                currentLocationRadius.setPoints(Polygon.pointsAsCircle(point, accuracy));
                currentLocationRadius.setStrokeColor(RADIUS_BORDER_COLOR);
                currentLocationRadius.setFillColor(RADIUS_FILL_COLOR);
                overlays.add(currentLocationRadius);

                mapController.setCenter(point);
                mapController.setZoom(getZoomLevel(accuracy));
            }
        }

        private int getZoomLevel(final float radius) {
            final float scale = radius / 500;
            return (int) (17 - Math.log(scale) / Math.log(2));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.map);
        if(mapView != null) {
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

            // default view point
            mapController = mapView.getController();
            final GeoPoint defaultPoint = new GeoPoint(48.8583, 2.2944);
            mapController.setZoom(6);
            mapController.setCenter(defaultPoint);

            // add compass
            final CompassOverlay compassOverlay = new CompassOverlay(this,
                    new InternalCompassOrientationProvider(this), mapView);
            compassOverlay.enableCompass();
            mapView.getOverlays().add(compassOverlay);

            // add mapView scale
            final ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
            scaleBarOverlay.setCentred(true);
            final DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            scaleBarOverlay.setScaleBarOffset(metrics.widthPixels / 2, 10);
            mapView.getOverlays().add(scaleBarOverlay);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(locationInfoReceiver, new IntentFilter(Constants.MAP_INTENT_FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(locationInfoReceiver);
    }

    //TODO: implement this
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
