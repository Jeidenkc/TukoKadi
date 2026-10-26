package com.tukokadi.app;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

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

    private static final String SERVER_URL = "wss://rafiki-server-cir7.onrender.com";

    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build();
    private static volatile WebSocket socket;
    private static volatile WebSocketListener activeListener;
    private static volatile Listener listener;
    private static volatile Listener socialListener;
    private static volatile JSONObject lastGame;
    private static volatile int lastBalance = -1;
    private static volatile String token;

    public static void setListener(Listener l) { listener = l; }

    // Friends messages (hello, friends, invite, inviteSent) go only to this listener,
    // never to the game screens.
    public static void setSocialListener(Listener l) { socialListener = l; }

    public static JSONObject getLastGame() { return lastGame; }

    public static int getLastBalance() { return lastBalance; }

    public static void setToken(String t) { token = t; }

    private static boolean isSocial(String type) {
        return "hello".equals(type) || "friends".equals(type)
                || "invite".equals(type) || "inviteSent".equals(type)
                || "inviteReply".equals(type);
    }

    public static void connect(final JSONObject first, Listener l) {
        close();
        listener = l;
        String t = token;
        try {
            if (t != null && !t.isEmpty() && !first.has("token")) first.put("token", t);
        } catch (Exception ignored) { }
        Request req = new Request.Builder().url(SERVER_URL).build();
        WebSocketListener wl = new WebSocketListener() {
            @Override public void onOpen(WebSocket ws, Response r) {
                ws.send(first.toString());
                try {
                    String tk = token;
                    if (tk != null && !tk.isEmpty() && !"hello".equals(first.optString("type"))) {
                        JSONObject h = new JSONObject();
                        h.put("type", "hello");
                        h.put("token", tk);
                        ws.send(h.toString());
                    }
                } catch (Exception ignored) { }
            }
            @Override public void onMessage(WebSocket ws, String text) {
                if (this != activeListener) return;
                try {
                    JSONObject m = new JSONObject(text);
                    if (isSocial(m.optString("type"))) {
                        Listener s = socialListener;
                        if (s != null) s.onMessage(m);
                        return;
                    }
                    if ("game".equals(m.optString("type"))) lastGame = m;
                    if (m.has("balance") && !m.isNull("balance")) {
                        lastBalance = m.optInt("balance", lastBalance);
                    }
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
        if (s == null) return;
        try {
            String t = token;
            if (t != null && !t.isEmpty() && !msg.has("token")) msg.put("token", t);
        } catch (Exception ignored) { }
        s.send(msg.toString());
    }

    public static void close() {
        activeListener = null;
        WebSocket s = socket;
        socket = null;
        lastGame = null;
        lastBalance = -1;
        if (s != null) s.close(1000, null);
    }
}
