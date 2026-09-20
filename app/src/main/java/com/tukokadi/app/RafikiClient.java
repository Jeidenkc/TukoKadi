package com.tukokadi.app;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RafikiClient {

    public interface Listener {
        void onMessage(JSONObject msg);
        void onFailure();
    }

    // Server running in Termux on THIS phone. Later this becomes an online address.
    private static final String SERVER_URL = "ws://127.0.0.1:3001";

    private static final OkHttpClient http = new OkHttpClient();
    private static volatile WebSocket socket;
    private static volatile WebSocketListener activeListener;
    private static volatile Listener listener;
    private static volatile JSONObject lastGame;

    public static void setListener(Listener l) { listener = l; }

    public static JSONObject getLastGame() { return lastGame; }

    public static void connect(final JSONObject first, Listener l) {
        close();
        listener = l;
        Request req = new Request.Builder().url(SERVER_URL).build();
        WebSocketListener wl = new WebSocketListener() {
            @Override public void onOpen(WebSocket ws, Response r) {
                ws.send(first.toString());
            }
            @Override public void onMessage(WebSocket ws, String text) {
                if (this != activeListener) return;
                try {
                    JSONObject m = new JSONObject(text);
                    if ("game".equals(m.optString("type"))) lastGame = m;
                    Listener cur = listener;
                    if (cur != null) cur.onMessage(m);
                } catch (Exception ignored) { }
            }
            @Override public void onFailure(WebSocket ws, Throwable t, Response r) {
                if (this != activeListener) return;
                Listener cur = listener;
                if (cur != null) cur.onFailure();
            }
        };
        activeListener = wl;
        socket = http.newWebSocket(req, wl);
    }

    public static void send(JSONObject msg) {
        WebSocket s = socket;
        if (s != null) s.send(msg.toString());
    }

    public static void close() {
        activeListener = null;
        WebSocket s = socket;
        socket = null;
        lastGame = null;
        if (s != null) s.close(1000, null);
    }
}
