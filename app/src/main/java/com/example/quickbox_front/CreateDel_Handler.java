package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CreateDel_Handler extends AppCompatActivity {
    private WebSocket webSocket;
    Button back;
    ImageButton qr_scan;
    EditText receiveID;
    EditText note;
    CheckBox economy;
    CheckBox standard;
    CheckBox fast;
    Button create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_delivery);
        EdgeToEdge.enable(this);
        back = findViewById(R.id.backD);
        qr_scan = findViewById(R.id.qrScan);
        receiveID = findViewById(R.id.receivID);
        note = findViewById(R.id.note);
        economy = findViewById(R.id.economy);
        standard = findViewById(R.id.standard);
        fast = findViewById(R.id.fast);
        create = findViewById(R.id.createD);
        ProgressBar progressBar = findViewById(R.id.progressBarUp);

        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/create_delivery")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket connection established
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection established (Create Delivery)");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Receiving messages
                super.onMessage(webSocket, text);
                Log.d("WebSocket", "Received message: " + text);

                // Handle received message
                JSONObject jsonObject;
                String from;
                String sent_time;
                String delivery_time;
                String delivery_type;
                String status;
                try {
                    jsonObject = new JSONObject(text);
                    if (jsonObject.has("detail")) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            create.setVisibility(View.VISIBLE);
                            AlertDialog alert = new AlertDialog.Builder(CreateDel_Handler.this)
                                    .setTitle("Error")
                                    .setMessage(getString(R.string.invalid))
                                    .setPositiveButton("OK", null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                            Objects.requireNonNull(alert.getWindow()).setBackgroundDrawableResource(R.drawable.map_round);
                        });
                    } else {
                        from = jsonObject.getString("from");
                        sent_time = jsonObject.getString("sent_time");
                        delivery_time = jsonObject.getString("delivery_time");
                        delivery_type = jsonObject.getString("delivery_type");
                        status = jsonObject.getString("status");

                        if (!from.isEmpty() && from.equals(id) && !sent_time.isEmpty() && !delivery_time.isEmpty()
                                && !delivery_type.isEmpty() && !status.isEmpty()) {
                            Log.d("WebSocket", "Delivery created successfully");
                            runOnUiThread(() -> {
                                webSocket.close(1000, "Closing the connection");
                                progressBar.setVisibility(View.INVISIBLE);
                                finish();
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // WebSocket is about to close
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // WebSocket connection closed
                super.onClosed(webSocket, code, reason);
                Log.d("WebSocket", "Connection is closed (Create Delivery)");
            }
        });


        back.setOnClickListener(v -> {
            webSocket.close(1000, "Closing the connection");
            finish();
        });

        qr_scan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CreateDel_Handler.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(CreateDel_Handler.this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
            } else {
                // Permission is granted
                startScanning();
            }
        });


        economy.setOnClickListener(v -> {
            if (economy.isChecked()) {
                standard.setChecked(false);
                fast.setChecked(false);
            }
        });

        standard.setOnClickListener(v -> {
            if (standard.isChecked()) {
                economy.setChecked(false);
                fast.setChecked(false);
            }
        });

        fast.setOnClickListener(v -> {
            if (fast.isChecked()) {
                economy.setChecked(false);
                standard.setChecked(false);
            }
        });

        create.setOnClickListener(v -> {
            create.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            JSONObject jsonObject = new JSONObject();
            if (receiveID.getText().toString().isEmpty() || !economy.isChecked() && !standard.isChecked()
            && !fast.isChecked()){
                progressBar.setVisibility(View.GONE);
                create.setVisibility(View.VISIBLE);
                AlertDialog alert = new AlertDialog.Builder(CreateDel_Handler.this)
                        .setTitle("Error")
                        .setMessage(getString(R.string.fields))
                        .setPositiveButton("OK", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                Objects.requireNonNull(alert.getWindow()).setBackgroundDrawableResource(R.drawable.round_corners);
            }
            else {
                try {
                    jsonObject.put("receiveID", receiveID.getText().toString());
                    jsonObject.put("fromID", id);
                    if (economy.isChecked()) {
                        jsonObject.put("deliveryType", "economy");
                    } else if (standard.isChecked()) {
                        jsonObject.put("deliveryType", "standard");
                    } else if (fast.isChecked()) {
                        jsonObject.put("deliveryType", "fast");
                    }
                    jsonObject.put("note", note.getText().toString());
                    webSocket.send(jsonObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                receiveID.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void startScanning() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan QR Code");
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                startScanning();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
