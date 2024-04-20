package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
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
    ImageView noConnection;
    private MapView mMap;
    private Polygon currentCircle;
    WebSocketManager webSocketManager = WebSocketManager.getInstance();
    private IMapController controller;
    GeoPoint defaultLocation;
    private MyLocationNewOverlay mMyLocationOverlay;
    Handler handler = new Handler();
    SharedPreferences sharedPreferencesMap;

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
        noConnection = findViewById(R.id.no_connection);

        Log.d("WebSocket", webSocketManager.getFailed().toString());

        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences sharedPreferencesDeliveries = getSharedPreferences("Deliveries", MODE_PRIVATE);
        sharedPreferencesMap = getSharedPreferences("Map", MODE_PRIVATE);
        SharedPreferences sharedPreferencesConnection = getSharedPreferences("Connection", MODE_PRIVATE);

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
        if (!sharedPreferencesConnection.getBoolean("connection", false)) {
            for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                    int id = jsonObject.getInt("id");
                    String delivery_time = jsonObject.getString("delivery_time");
                    String status = jsonObject.getString("status");
                    if (status.equals("Nearby")) {
                        if (!isPanelShown()) {
                            // Retrieve the delivery's location using the id
                            JSONObject location = new JSONObject(sharedPreferencesMap.getString("idLocation" + id, ""));
                            double latitude = location.getDouble("latitude");
                            double longitude = location.getDouble("longitude");
                            ContextThemeWrapper newContext = new ContextThemeWrapper(Map_Handler.this, R.style.materialButtonStyle);
                            Button button = layout.findViewWithTag("button" + id);
                            if (button == null) {
                                button = new Button(newContext);
                                button.setTag("button" + id);
                                button.setBackgroundResource(R.drawable.round_corners);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,  // Width
                                        LinearLayout.LayoutParams.WRAP_CONTENT   // Height
                                );
                                params.setMargins(0, 0, 0, 30);
                                button.setLayoutParams(params);
                                layout.addView(button);
                            }
                            button.setVisibility(View.VISIBLE);
                            Button finalButton1 = button;
                            runOnUiThread(() -> {
                                finalButton1.setText("ID: " + id + "\n" + "Delivery time: " + delivery_time + "\n" + "Status: " + status);
                                finalButton1.setOnClickListener(v -> {
                                    Log.e("WebSocket", "latitude: " + latitude + " longitude: " + longitude);
                                    GeoPoint locationFromArray = new GeoPoint(latitude, longitude);
                                    double radius = Math.abs(defaultLocation.distanceToAsDouble(locationFromArray));
                                    Log.e("WebSocket", "radius: " + radius);
                                    // If there is a current circle, remove it
                                    if (currentCircle != null) {
                                        mMap.getOverlays().remove(currentCircle);
                                    }
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
                                layout.addView(finalButton1);
                                scrollView.invalidate();
                            });
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (sharedPreferencesConnection.getBoolean("connection", false)) {
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    Log.d("WebSocket", "Connection successful(Map)");
                    Log.d("WebSocket", sharedPreferencesMap.getAll().toString());
                    runOnUiThread(() -> {
                        for (int i = 0; i < sharedPreferencesDeliveries.getAll().size(); i++) {
                            try {
                                JSONObject jsonObject = new JSONObject(sharedPreferencesDeliveries.getString("idHome" + i, ""));
                                int id = jsonObject.getInt("id");
                                String delivery_time = jsonObject.getString("delivery_time");
                                String status = jsonObject.getString("status");
                                if (status.equals("Nearby")) {
                                    if (!isPanelShown()) {
                                        // Retrieve the delivery's location using the id
                                        ContextThemeWrapper newContext = new ContextThemeWrapper(Map_Handler.this, R.style.materialButtonStyle);
                                        Button button = layout.findViewWithTag("button" + id);
                                        if (button == null) {
                                            button = new Button(newContext);
                                            button.setTag("button" + id);
                                            button.setBackgroundResource(R.drawable.round_corners);
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,  // Width
                                                    LinearLayout.LayoutParams.WRAP_CONTENT   // Height
                                            );
                                            button.setLayoutParams(params);
                                            layout.addView(button);
                                        }
                                        button.setVisibility(View.VISIBLE);
                                        Button finalButton = button;
                                        finalButton.setText("ID: " + id + "\n" + "Delivery time: " + delivery_time + "\n" + "Status: " + status);
                                        finalButton.setOnClickListener(v -> {
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        JSONObject location = new JSONObject(sharedPreferencesMap.getString("idLocation" + id, ""));
                                                        double latitudeC = location.getDouble("latitude");
                                                        double longitudeC = location.getDouble("longitude");
                                                        Log.e("WebSocket", "latitude: " + latitudeC + " longitude: " + longitudeC);
                                                        GeoPoint locationFromArray = new GeoPoint(latitudeC, longitude);
                                                        double radius = Math.abs(defaultLocation.distanceToAsDouble(locationFromArray));
                                                        Log.e("WebSocket", "radius: " + radius);
                                                        // If there is a current circle, remove it
                                                        if (currentCircle != null) {
                                                            mMap.getOverlays().remove(currentCircle);
                                                        }
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
                                                        handler.postDelayed(this, 1000);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            };
                                            handler.post(runnable);
                                        });
                                        layout.addView(finalButton);
                                        scrollView.invalidate();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnableCode);
        }else {
            ConnectionThread connectionThread = new ConnectionThread();
            Thread thread = connectionThread.getThread(sharedPreferencesConnection, noConnection);
            thread.start();
        }
        back.setOnClickListener(v -> {
            finish();
            handler.removeCallbacksAndMessages(null);
            overridePendingTransition(R.anim.enter_animation_back, R.anim.exit_animation_back);
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
                        if (sharedPreferencesMap.getAll().isEmpty()) {
                            noDel.setVisibility(View.VISIBLE);
                            layout.removeAllViews();
                        } else {
                            noDel.setVisibility(View.GONE);
                        }
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
                        noDel.setVisibility(View.GONE);
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
