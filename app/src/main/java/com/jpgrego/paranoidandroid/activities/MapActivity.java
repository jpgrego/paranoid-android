package com.jpgrego.paranoidandroid.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;

import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.services.DataService;
import com.jpgrego.paranoidandroid.services.LocationResponse;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Created by jpgrego on 28/11/16.
 */

public final class MapActivity extends BaseActivity {

    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    private static final String ZOOM_KEY = "zoom";
    private static final String ACCURACY_KEY = "accuracy";
    private static final double DEFAULT_LATITUDE = 48.8583;
    private static final double DEFAULT_LONGITUDE = 2.2944;
    private static final double DEFAULT_ZOOM_LEVEL = 6.0;
    private static final int UPDATE_PERIOD_SECONDS = 1;

    private static final int RADIUS_BORDER_COLOR =
            Color.argb(70, 72, 133, 237);
    private static final int RADIUS_FILL_COLOR = Color.argb(30, 72, 133, 237);
    private Marker currentLocationMarker;
    private Polygon currentLocationRadius;

    private final Object lockObj = new Object();

    private MapView mapView;
    private IMapController mapController;
    private volatile ScheduledFuture<?> scheduledUpdates = null;

    private static double oldLatitude = 0;
    private static double oldLongitude = 0;


    private final class UpdateLocationRunnable implements Runnable {

        private final DataService service;

        private UpdateLocationRunnable(final DataService service) {
            this.service = service;
        }

        @Override
        public void run() {
            final LocationResponse location = service.getCurrentLocationResponse();

            if (location != null) {
                MapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPointAndZoom(location);
                        oldLatitude = location.getLatitude();
                        oldLongitude = location.getLongitude();
                    }
                });
            }
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            final DataService dataService =
                    ((DataService.LocalBinder) iBinder).getDataServiceInstance();

            scheduledUpdates = DataService.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    new UpdateLocationRunnable(dataService),
                    0 ,
                    UPDATE_PERIOD_SECONDS,
                    TimeUnit.SECONDS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // do nothing
        }

    };

    private void setPointAndZoom(final LocationResponse locationResponse) {

        if(locationResponse == null) return;

        final double latitude = locationResponse.getLatitude();
        final double longitude = locationResponse.getLongitude();
        final float accuracy = locationResponse.getAccuracy();
        final GeoPoint point = new GeoPoint(latitude, longitude);

        if(mapController != null) {
            synchronized (lockObj) {

                final double truncatedNewLatitude = Math.floor(latitude * 1000) / 1000;
                final double truncatedNewLongitude = Math.floor(longitude * 1000) / 1000;
                final double truncatedOldLatitude = Math.floor(oldLatitude * 1000) / 1000;
                final double truncatedOldLongitude = Math.floor(oldLongitude * 1000) / 1000;

                if(truncatedOldLatitude == truncatedNewLatitude
                        && truncatedOldLongitude == truncatedNewLongitude) return;

                mapController.setZoom(Math.min(getZoomLevel(accuracy), 19L));
                mapController.setCenter(point);

                markPointOnMap(latitude, longitude, accuracy);
            }
        }

        final IGeoPoint currentCenter = mapView.getMapCenter();
        final double currentZoomLevel = mapView.getZoomLevelDouble();

        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                this.getClass().getSimpleName(), MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LATITUDE_KEY, Double.doubleToRawLongBits(currentCenter.getLatitude()));
        editor.putLong(LONGITUDE_KEY, Double.doubleToRawLongBits(currentCenter.getLongitude()));
        editor.putLong(ZOOM_KEY, Double.doubleToLongBits(currentZoomLevel));
        editor.putFloat(ACCURACY_KEY, accuracy);
        editor.apply();
    }

    private void markPointOnMap(final double latitude, final double longitude,
                                final float accuracy) {

        final GeoPoint point = new GeoPoint(latitude, longitude);
        final List<Overlay> overlays = mapView.getOverlays();

        synchronized (lockObj) {
            if (currentLocationMarker != null) {
                currentLocationMarker.closeInfoWindow();
                overlays.remove(currentLocationMarker);
            }

            if (currentLocationRadius != null) overlays.remove(currentLocationRadius);

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
        }
    }

    private double getZoomLevel(final float radius) {
        final float scale = radius / 400;
        return (17 - Math.log(scale) / Math.log(2));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final IGeoPoint currentCenter = mapView.getMapCenter();
        final double currentZoomLevel = mapView.getZoomLevelDouble();

        outState.putDouble(LATITUDE_KEY, currentCenter.getLatitude());
        outState.putDouble(LONGITUDE_KEY, currentCenter.getLongitude());
        outState.putDouble(ZOOM_KEY, currentZoomLevel);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.location_activity_title);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));

        mapView =  findViewById(R.id.map);
        mapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {

                final SharedPreferences localSP = getSharedPreferences(
                        MapActivity.this.getClass().getSimpleName(), MODE_PRIVATE);

                final boolean containsSavedState = localSP.contains(LATITUDE_KEY)
                        && localSP.contains(LONGITUDE_KEY) && localSP.contains(ZOOM_KEY);

                if(savedInstanceState != null
                        && savedInstanceState.containsKey(LATITUDE_KEY)
                        && savedInstanceState.containsKey(LONGITUDE_KEY)
                        && savedInstanceState.containsKey(ZOOM_KEY)) {
                    final double latitude = savedInstanceState.getDouble(LATITUDE_KEY);
                    final double longitude = savedInstanceState.getDouble(LONGITUDE_KEY);
                    final double zoom = savedInstanceState.getDouble(ZOOM_KEY);
                    final float accuracy = savedInstanceState.getFloat(ACCURACY_KEY);
                    final GeoPoint lastPoint = new GeoPoint(latitude, longitude);

                    mapController.setZoom(zoom);
                    mapController.setCenter(lastPoint);
                    markPointOnMap(latitude, longitude, accuracy);
                } else if(containsSavedState) {
                    final double latitude = Double.longBitsToDouble(
                            localSP.getLong(LATITUDE_KEY, 0L));
                    final double longitude = Double.longBitsToDouble(
                            localSP.getLong(LONGITUDE_KEY, 0L));
                    final double zoom = Double.longBitsToDouble(localSP.getLong(ZOOM_KEY, 0L));
                    final float accuracy = localSP.getFloat(ACCURACY_KEY, 0f);

                    final IGeoPoint lastCenter = new GeoPoint(latitude, longitude);
                    mapController.setZoom(zoom);
                    mapController.setCenter(lastCenter);
                    markPointOnMap(latitude, longitude, accuracy);
                } else {
                    final GeoPoint defaultPoint = new GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
                    mapController.setZoom(DEFAULT_ZOOM_LEVEL);
                    mapController.setCenter(defaultPoint);
                }
            }
        });
        if(mapView != null) {
            mapView.setClickable(true);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

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

            // experimental
            // TODO: attempt to implement this
            //MyLocationNewOverlay myLocation = new MyLocationNewOverlay()


            mapController = mapView.getController();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));

        final Intent serviceIntent = new Intent(this, DataService.class);
        bindService(serviceIntent, serviceConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(locationInfoReceiver);
        if(scheduledUpdates != null) scheduledUpdates.cancel(true);
        unbindService(serviceConnection);
        //mapView.getTileProvider().clearTileCache();
        //System.gc();
    }
}
