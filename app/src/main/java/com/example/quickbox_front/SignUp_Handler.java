package com.example.quickbox_front;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignUp_Handler extends AppCompatActivity {
    private JSONObject receivedMessage;
    double latitude;
    double longitude;
    WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        loadLocale();
        setContentView(R.layout.signup);

        EditText name = findViewById(R.id.name);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.passw);
        CardView map = findViewById(R.id.mapS);
        Button back = findViewById(R.id.backB);
        Button create = findViewById(R.id.create);
        ProgressBar progressBar = findViewById(R.id.progressBarUp);
        ImageView noConnection = findViewById(R.id.no_connection);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/signup")
                .build();

        SharedPreferences sharedPreferencesConnection = getSharedPreferences("Connection", MODE_PRIVATE);

        if (sharedPreferencesConnection.getBoolean("connection", false)) {
            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    // WebSocket connection established
                    super.onOpen(webSocket, response);
                    Log.d("WebSocket", "Connection established (Sign Up)");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.d("WebSocket", "Received message: " + text);

                    String resultId;
                    String resultName;
                    String resultEmail;
                    String resultLongitude;
                    String resultLatitude;
                    try {
                        receivedMessage = new JSONObject(text);
                        if (receivedMessage.has("detail")) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                create.setVisibility(View.VISIBLE);
                                TextView error = findViewById(R.id.error);
                                error.setText(getString(R.string.errorsignup));
                            });
                        } else {
                            resultId = receivedMessage.getString("id");
                            resultName = receivedMessage.getString("name");
                            resultEmail = receivedMessage.getString("email");
                            resultLongitude = receivedMessage.getString("longitude");
                            resultLatitude = receivedMessage.getString("latitude");
                            Bitmap qrCode = generateQRCode(resultId);
                            // Check received message
                            if (!resultId.isEmpty() && email.getText().toString().contains("@") && email.getText().toString().contains(".")
                                    && resultEmail.equals(email.getText().toString())) {
                                // If credentials are correct, start Home_Handler activity
                                runOnUiThread(() -> {
                                    sharedPreferences.edit().clear().apply();
                                    progressBar.setVisibility(View.GONE);
                                    Intent returnIntent = new Intent();
                                    setResult(RESULT_OK, returnIntent);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("isUserLoggedIn", true);
                                    editor.putString("id", resultId);
                                    editor.putString("name", resultName);
                                    editor.putString("email", resultEmail);
                                    editor.putFloat("longitude", Float.parseFloat(resultLongitude));
                                    editor.putFloat("latitude", Float.parseFloat(resultLatitude));
                                    editor.apply();
                                    JSONObject json = new JSONObject();
                                    try {
                                        json.put("id", resultId);
                                        json.put("qr_code", convertBitmapToBase64String(Objects.requireNonNull(qrCode)));
                                        Log.d("WebSocket", "Sending QR code: " + json);
                                        webSocket.send(json.toString());
                                    } catch (Exception e) {
                                        Log.e("WebSocket", "Error: " + e.getMessage());
                                    }
                                    finish();
                                    Intent intent = new Intent(SignUp_Handler.this, Home_Handler.class);
                                    intent.putExtra("id", resultId);
                                    intent.putExtra("email", resultEmail);
                                    intent.putExtra("longitude", Float.parseFloat(resultLongitude));
                                    intent.putExtra("latitude", Float.parseFloat(resultLatitude));
                                    webSocket.close(1000, "Closing the connection");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
                                });
                            } else if ((!email.getText().toString().contains("@") || !email.getText().toString().contains("."))
                                    && !resultId.isEmpty() && resultEmail.equals(email.getText().toString())) {
                                progressBar.setVisibility(View.GONE);
                                create.setVisibility(View.VISIBLE);
                                email.setError(getString(R.string.wrongfromat));
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.d("WebSocket", "Connection closed (Sign Up)");
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e("WebSocket", "Connection failed: " + t.getMessage());
                }
            });
        } else {
            ConnectionThread connectionThread = new ConnectionThread();
            Thread thread = connectionThread.getThread(sharedPreferencesConnection, noConnection);
            thread.start();
        }
        email.setOnFocusChangeListener((v, hasFocus) -> {
            if(!email.getText().toString().contains("@") || !email.getText().toString().contains(".")){
                email.setError(getString(R.string.wrongfromat));
            }
        });

        back.setOnClickListener(v -> {
            if (sharedPreferencesConnection.getBoolean("connection", false)){
                webSocket.close(1000, "Closing the connection");
            }
            finish();
            overridePendingTransition(R.anim.enter_animation_back, R.anim.exit_animation_back);
        });

        map.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp_Handler.this, MapSignUp_Handler.class);
            startActivityForResult(intent, 1); // Use requestCode 1
        });

        create.setOnClickListener(v -> {
            create.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            JSONObject json = new JSONObject();
            try {
                json.put("id", "");
                json.put("name", name.getText().toString());
                json.put("email", email.getText().toString());
                json.put("password", password.getText().toString());
                json.put("longitude", longitude);
                json.put("latitude", latitude);
                json.put("qr_code", "");
                webSocket.send(json.toString());
            } catch (Exception e) {
                Log.e("WebSocket", "Error: " + e.getMessage());
            }
        });
    }
    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }
    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }
    public String convertBitmapToBase64String(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private Bitmap generateQRCode(String email) {
        try {
            String qrCodeData = email;
            String charset = "UTF-8";


            HashMap<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(qrCodeData.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, 350, 350, hintMap);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (Exception e) {
            Log.e("WebSocket", "Error generating QR code: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { // Check if the result is from the map activity
            if (resultCode == RESULT_OK) { // Check if the result is OK
                if (data != null) {
                    latitude = data.getDoubleExtra("latitude", 0.0); // Get the latitude value
                    longitude = data.getDoubleExtra("longitude", 0.0); // Get the longitude value
                }
            }
        }
    }

}
