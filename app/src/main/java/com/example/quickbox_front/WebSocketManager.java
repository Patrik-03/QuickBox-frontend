package com.example.quickbox_front;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;



public class WebSocketManager {
    private static WebSocketManager instance = null;
    public Boolean failed = false;
    private WebSocket webSocket;

    private WebSocketManager() {

    }

    public static WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void startWebSocket(String idGet, SharedPreferences sharedPreferencesMap) {
        if (webSocket != null) {
            webSocket.close(1000, "Closing old connection");
            webSocket = null;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IPServer.IP + ":8000/ws/deliveries_map")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("WebSocket", "Connection opened (Map)");
                super.onOpen(webSocket, response);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", idGet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webSocket.send(jsonObject.toString());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.d("WebSocket", "Message received MAP: " + text);
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("items")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("items");
                        Log.d("WebSocket", jsonArray.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject delivery = jsonArray.getJSONObject(i);
                            int id = delivery.getInt("id");
                            String status = delivery.getString("status");
                            sharedPreferencesMap.edit()
                                    .putString("idLocation" + id, String.valueOf(delivery)) // Use id instead of i
                                    .apply();
                        }
                    }
                    else if (jsonObject.has("type")) {
                        String type = jsonObject.getString("type");
                        if (type.equals("delete")) {
                            sharedPreferencesMap.edit().clear().apply();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d("WebSocket", "Connection closed (Map)");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
                Log.e("WebSocket", "Connection failed2", t);
                failed = true;
            }
        });
    }

    public void stopWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void sendMsg(String msg) {
        webSocket.send(msg);
    }
}

