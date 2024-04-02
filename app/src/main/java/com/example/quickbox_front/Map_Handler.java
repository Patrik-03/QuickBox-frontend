package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class Map_Handler extends AppCompatActivity implements MapListener, GpsStatus.Listener{
    Button back, up, down;
    View hiddenPanel;
    LinearLayout layout;
    ScrollView scrollView;

    Float longitude, latitude;

    private MapView mMap;
    private IMapController controller;
    private MyLocationNewOverlay mMyLocationOverlay;


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        EdgeToEdge.enable(this);

        hiddenPanel = findViewById(R.id.hidden_panel);
        layout = findViewById(R.id.linearLayoutDel);
        scrollView = findViewById(R.id.scrollDel);
        back = findViewById(R.id.backD);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);

        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);

        longitude = sharedPreferences.getFloat("longitude", 0);
        latitude = sharedPreferences.getFloat("latitude", 0);

        back.setOnClickListener(v -> finish());



        Configuration.getInstance().load(this, getSharedPreferences("OSMConfig", MODE_PRIVATE));

        mMap = findViewById(R.id.osmmap);
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

        Log.d("WebSocket", longitude + " " + latitude);

        GeoPoint defaultLocation = new GeoPoint(latitude, longitude);
        controller.setCenter(defaultLocation);
        controller.setZoom(16);
        Drawable icon = getResources().getDrawable(R.drawable.location);
        icon.setBounds(0, 0, icon.getIntrinsicWidth() / 2, icon.getIntrinsicHeight() / 2);

        Marker startMarker = new Marker(mMap);
        startMarker.setPosition(defaultLocation);
        startMarker.setIcon(icon); // Set custom icon
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMap.getOverlays().add(startMarker);

        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.addMapListener(this);
    }

    public void slideUpDown(final View view) {
        if (!isPanelShown()) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);

            bottomUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    GeoPoint currentCenter = (GeoPoint) mMap.getMapCenter();
                    double newLatitude = currentCenter.getLatitude() - 0.008; // Adjust this value as needed
                    GeoPoint newCenter = new GeoPoint(newLatitude, currentCenter.getLongitude());
                    controller.animateTo(newCenter);
                    new Handler().postDelayed(() -> up.setVisibility(View.GONE), 170);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);
        }
        else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);

            bottomDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    GeoPoint currentCenter = (GeoPoint) mMap.getMapCenter();
                    double newLatitude = currentCenter.getLatitude() + 0.008; // Adjust this value as needed
                    GeoPoint newCenter = new GeoPoint(newLatitude, currentCenter.getLongitude());
                    controller.animateTo(newCenter);
                    new Handler().postDelayed(() -> up.setVisibility(View.VISIBLE), 260);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.GONE);
        }
    }
    private boolean isPanelShown() {
        return hiddenPanel.getVisibility() == View.VISIBLE;
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
