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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class Map_Handler extends AppCompatActivity implements MapListener, GpsStatus.Listener{
    Button back, up, down;
    View hiddenPanel;
    LinearLayout layout;
    ScrollView scrollView;
    TextView noDel;
    Float longitude, latitude;
    String idGet;
    private MapView mMap;
    private Polygon currentCircle;
    WebSocketManager webSocketManager = WebSocketManager.getInstance();
    private IMapController controller;
    GeoPoint defaultLocation;
    private MyLocationNewOverlay mMyLocationOverlay;
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        EdgeToEdge.enable(this);

        hiddenPanel = findViewById(R.id.hidden_panel);
        layout = findViewById(R.id.linearLayoutDel);
        scrollView = findViewById(R.id.scrollDel);
        noDel = findViewById(R.id.noDel);
        back = findViewById(R.id.backD);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);

        Log.d("WebSocket", webSocketManager.getFailed().toString());

        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences sharedPreferencesDeliveries = getSharedPreferences("Deliveries", MODE_PRIVATE);
        SharedPreferences sharedPreferencesLocation = getSharedPreferences("Location", MODE_PRIVATE);

        idGet = sharedPreferences.getString("id", "");
        longitude = sharedPreferences.getFloat("longitude", 0);
        latitude = sharedPreferences.getFloat("latitude", 0);


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


        defaultLocation = new GeoPoint(latitude, longitude);
        controller.setCenter(defaultLocation);
        controller.setZoom(16);
        Drawable icon = getResources().getDrawable(R.drawable.location);
        icon.setBounds(0, 0, icon.getIntrinsicWidth() / 2, icon.getIntrinsicHeight() / 2);

        Marker startMarker = new Marker(mMap);
        startMarker.setPosition(defaultLocation);
        startMarker.setIcon(icon);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMap.getOverlays().add(startMarker);

        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.addMapListener(this);

        if (!sharedPreferencesDeliveries.getAll().isEmpty()) {
            layout.removeAllViews();
            layout.invalidate();
            for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                    Integer id = jsonObject.getInt("id");
                    String delivery_time = jsonObject.getString("delivery_time");
                    String status = jsonObject.getString("status");
                    if (status.equals("Nearby")) {
                        if (!isPanelShown()) {
                            Button button = new Button(Map_Handler.this);
                            runOnUiThread(() -> {
                                button.setText("ID: " + id + "\n" + "Delivery time: " + delivery_time + "\n" + "Status: " + status);
                                button.setOnClickListener(v -> {
                                    double latitude = 0;
                                    double longitude = 0;
                                    for (int j = 0; j < sharedPreferencesLocation.getAll().size(); j++) {
                                        try {
                                            JSONObject location = new JSONObject(sharedPreferencesLocation.getString("idLocation" + j, ""));
                                            Log.d("WebSocket", location.toString());
                                            if (location.getInt("id") == id) {
                                                latitude = location.getDouble("latitude");
                                                longitude = location.getDouble("longitude");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Log.e("WebSocket", "latitude: " + latitude + " longitude: " + longitude);
                                    GeoPoint locationFromArray = new GeoPoint(latitude, longitude);
                                    double radius = Math.abs(defaultLocation.distanceToAsDouble(locationFromArray));
                                    Log.e("WebSocket", "radius: " + radius);
                                    // If there is a current circle, remove it
                                    if (currentCircle != null) {
                                        mMap.getOverlays().remove(currentCircle);
                                    }

                                    double newLatitude = defaultLocation.getLatitude() - 0.008; // Adjust this value as needed
                                    GeoPoint newCenter = new GeoPoint(newLatitude, defaultLocation.getLongitude());
                                    controller.animateTo(newCenter);
                                    controller.setZoom(16);
                                    new Handler().postDelayed(() -> {
                                        layout.setVisibility(View.VISIBLE);
                                        up.setVisibility(View.GONE);
                                    }, 170);
                                    // Create a new circle and add it to the map
                                    currentCircle = new Polygon();
                                    currentCircle.setPoints(Polygon.pointsAsCircle(defaultLocation, radius));
                                    currentCircle.setFillColor(getColor(R.color.blueMap));
                                    currentCircle.setStrokeWidth(0);
                                    mMap.getOverlays().remove(currentCircle);
                                    mMap.getOverlays().remove(startMarker);

                                    mMap.getOverlays().add(currentCircle);
                                    mMap.getOverlays().add(startMarker);

                                    mMap.invalidate();
                                });
                                layout.addView(button);
                                scrollView.invalidate();
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            noDel.setVisibility(View.VISIBLE);
        }


        if (!webSocketManager.getFailed()) {
            final Handler handler = new Handler();
            final Runnable updateUI = new Runnable() {
                @Override
                public void run() {
                    if (webSocketManager.getWebSocket() != null) {
                        if (!webSocketManager.getDeliveries().isEmpty()) {
                            sharedPreferencesLocation.edit().clear().apply();
                            layout.removeAllViews();
                            layout.invalidate();
                            for (int i = 0; i < webSocketManager.getDeliveries().size(); i++) {
                                try {
                                    JSONObject jsonObject = webSocketManager.getDeliveries().get(i);
                                    int id = jsonObject.getInt("id");
                                    String delivery_time = jsonObject.getString("delivery_time");
                                    String status = jsonObject.getString("status");
                                    float latitude = (float) jsonObject.getDouble("latitude");
                                    float longitude = (float) jsonObject.getDouble("longitude");
                                    sharedPreferencesLocation.edit().putString("idLocation" + i, jsonObject.toString()).apply();
                                    Log.d("WebSocket", "id: " + id +  " status: " + status + " latitude: " + latitude + " longitude: " + longitude);
                                    if (status.equals("Nearby")) {
                                        Button button = new Button(Map_Handler.this);
                                        runOnUiThread(() -> {
                                            button.setText("ID: " + id + "\n" + "Delivery time: " + delivery_time + "\n" + "Status: " + status);
                                            button.setOnClickListener(v -> {
                                                Log.d("WebSocket", "latitude: " + latitude + " longitude: " + longitude);
                                                Log.d("WebSocket", "defaultLocation: " + defaultLocation);
                                                GeoPoint locationFromArray = new GeoPoint(latitude, longitude);
                                                double radius = Math.abs(defaultLocation.distanceToAsDouble(locationFromArray));

                                                // If there is a current circle, remove it
                                                if (currentCircle != null) {
                                                    mMap.getOverlays().remove(currentCircle);
                                                }

                                                double newLatitude = defaultLocation.getLatitude() - 0.008; // Adjust this value as needed
                                                GeoPoint newCenter = new GeoPoint(newLatitude, defaultLocation.getLongitude());
                                                controller.animateTo(newCenter);
                                                controller.setZoom(16);
                                                new Handler().postDelayed(() -> {
                                                    layout.setVisibility(View.VISIBLE);
                                                    up.setVisibility(View.GONE);
                                                }, 170);

                                                currentCircle = new Polygon();
                                                currentCircle.setPoints(Polygon.pointsAsCircle(defaultLocation, radius));
                                                currentCircle.setFillColor(getColor(R.color.blueMap));
                                                currentCircle.setStrokeWidth(0);
                                                mMap.getOverlays().remove(currentCircle);
                                                mMap.getOverlays().remove(startMarker);

                                                mMap.getOverlays().add(currentCircle);
                                                mMap.getOverlays().add(startMarker);

                                                mMap.invalidate();
                                            });
                                            layout.addView(button);
                                            scrollView.invalidate();
                                        });

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            noDel.setVisibility(View.VISIBLE);
                        }
                    }
                    handler.postDelayed(this, 2000);
                }
            };
            handler.post(updateUI);
        }
        back.setOnClickListener(v -> {
            finish();
        });
    }

    public void slideUpDown(final View view) {
        if (!isPanelShown()) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
            bottomUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    double newLatitude = defaultLocation.getLatitude() - 0.008; // Adjust this value as needed
                    GeoPoint newCenter = new GeoPoint(newLatitude, defaultLocation.getLongitude());
                    controller.animateTo(newCenter);
                    controller.setZoom(16);
                    new Handler().postDelayed(() -> {
                        layout.setVisibility(View.VISIBLE);
                        up.setVisibility(View.GONE);
                    }, 170);
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
        } else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
            bottomDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    GeoPoint newCenter = new GeoPoint(defaultLocation.getLatitude(), defaultLocation.getLongitude());
                    controller.animateTo(newCenter);
                    controller.setZoom(16);
                    new Handler().postDelayed(() -> {
                        layout.setVisibility(View.GONE);
                        up.setVisibility(View.VISIBLE);
                    }, 260);
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
