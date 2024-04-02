package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MapSignUp_Handler extends AppCompatActivity implements MapListener, GpsStatus.Listener{
    Button back, confirm;
    private MapView mMap;
    Drawable icon;
    private IMapController controller;
    private MyLocationNewOverlay mMyLocationOverlay;
    private Marker chosenMarker;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapsignup);
        EdgeToEdge.enable(this);
        back = findViewById(R.id.backD);
        confirm = findViewById(R.id.confirm);

        back.setOnClickListener(v -> finish());

        Configuration.getInstance().load(this, getSharedPreferences("OSMConfig", MODE_PRIVATE));

        mMap = findViewById(R.id.osmmapSignUp);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);
        mMap.getLocalVisibleRect(new Rect());
        mMap.setBuiltInZoomControls(false);

        mMyLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMap);
        controller = mMap.getController();

        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
        mMyLocationOverlay.setDrawAccuracyEnabled(true);
        mMyLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            controller.setCenter(mMyLocationOverlay.getMyLocation());
            controller.animateTo(mMyLocationOverlay.getMyLocation());
        }));

        GeoPoint defaultLocation = new GeoPoint(48.1530988, 17.0715788);
        controller.setCenter(defaultLocation);
        controller.setZoom(9.0);

        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.addMapListener(this);

        icon = getResources().getDrawable(R.drawable.location);

        mMap.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                // Update chosen marker position and icon
                if (chosenMarker != null) {
                    chosenMarker.setPosition(p);
                    chosenMarker.setIcon(icon);
                } else {
                    chosenMarker = new Marker(mMap);
                    chosenMarker.setPosition(p);
                    chosenMarker.setIcon(icon);
                    chosenMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mMap.getOverlays().add(chosenMarker);
                }

                BigDecimal roundedLatitude = BigDecimal.valueOf(p.getLatitude()).setScale(7, RoundingMode.HALF_UP);
                BigDecimal roundedLongitude = BigDecimal.valueOf(p.getLongitude()).setScale(7, RoundingMode.HALF_UP);

                confirm.setOnClickListener(v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude", roundedLatitude.doubleValue()); // Pass rounded latitude value
                    returnIntent.putExtra("longitude", roundedLongitude.doubleValue()); // Pass rounded longitude value
                    setResult(RESULT_OK, returnIntent);
                    finish();
                });
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        Log.e("TAG", "onScroll latitude: " + event.getSource().getMapCenter().getLatitude());
        Log.e("TAG", "onScroll longitude: " + event.getSource().getMapCenter().getLongitude());
        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        Log.e("TAG", "onZoom zoom level: " + event.getZoomLevel() + " source: " + event.getSource());
        return false;
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}

