package com.tukokadi.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class Api {
    public static final String BASE = "http://127.0.0.1:3000";

    public interface Callback {
        void done(boolean ok, JSONObject json, String error);
    }

    public static String token(Context c) {
        return c.getSharedPreferences("tuko_session", Context.MODE_PRIVATE).getString("token", null);
    }

    public static void saveSession(Context c, String token, String phone) {
        c.getSharedPreferences("tuko_session", Context.MODE_PRIVATE).edit()
            .putString("token", token).putString("phone", phone).apply();
    }

    public static void clearSession(Context c) {
        c.getSharedPreferences("tuko_session", Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static void post(String path, JSONObject body, String token, Callback cb) {
        call("POST", path, body, token, cb);
    }

    public static void get(String path, String token, Callback cb) {
        call("GET", path, null, token, cb);
    }

    private static void call(final String method, final String path, final JSONObject body,
                             final String token, final Callback cb) {
        new Thread(() -> {
            boolean ok = false;
            JSONObject json = null;
            String err = null;
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(BASE + path).openConnection();
                con.setRequestMethod(method);
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                con.setRequestProperty("Content-Type", "application/json");
                if (token != null) con.setRequestProperty("Authorization", "Bearer " + token);
                if (body != null) {
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    os.write(body.toString().getBytes("UTF-8"));
                    os.close();
                }
                int code = con.getResponseCode();
                InputStream is = code >= 400 ? con.getErrorStream() : con.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while (is != null && (n = is.read(buf)) > 0) bos.write(buf, 0, n);
                json = new JSONObject(bos.toString("UTF-8"));
                ok = code < 400;
                if (!ok) err = json.optString("error", "Server error");
            } catch (Exception e) {
                err = "Cannot reach server: " + e;
            }
            final boolean fOk = ok;
            final JSONObject fJson = json;
            final String fErr = err;
            new Handler(Looper.getMainLooper()).post(() -> cb.done(fOk, fJson, fErr));
        }).start();
    }
}
