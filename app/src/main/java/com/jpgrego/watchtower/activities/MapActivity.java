package com.jpgrego.watchtower.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;

import com.jpgrego.watchtower.R;
import com.jpgrego.watchtower.services.DataService;
import com.jpgrego.watchtower.services.LocationResponse;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Created by jpgrego on 28/11/16.
 */

public final class MapActivity extends BaseActivity {

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
                if(mapView.getMapCenter().equals(point)) return;
                mapController.setCenter(point);
                mapController.setZoom(getZoomLevel(accuracy));

                final List<Overlay> overlays = mapView.getOverlays();

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
    }

    private double getZoomLevel(final float radius) {
        final float scale = radius / 400;
        return (17 - Math.log(scale) / Math.log(2));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.map);
        mapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                final GeoPoint defaultPoint = new GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
                mapController.setCenter(defaultPoint);
                mapController.setZoom(DEFAULT_ZOOM_LEVEL);
            }
        });
        if(mapView != null) {
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

            mapController = mapView.getController();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
