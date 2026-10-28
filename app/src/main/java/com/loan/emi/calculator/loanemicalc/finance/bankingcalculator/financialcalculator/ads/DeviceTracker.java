package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public final class DeviceTracker {
    private static final String TAG = "YagnikLoan2";

    private DeviceTracker() {
    }

    public static String deviceId(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return id == null ? "" : id;
    }

    public static void trackOpen(Context context) {
        Context app = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> trackOpenSync(app));
    }

    private static void trackOpenSync(Context app) {
        try {
            String base = AdsConfig.DEFAULT_API_BASE();
            String endpoint = AdsConfig.resolveApiPath(base, "api/app_open.php");
            String audience = AdsPrefs.isMarketingUser(app) ? "marketing" : "organic";
            String url = endpoint
                    + (endpoint.contains("?") ? "&" : "?")
                    + "device_id=" + java.net.URLEncoder.encode(deviceId(app), "UTF-8")
                    + "&audience=" + audience;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            Log.d(TAG, "app_open track code=" + code + " audience=" + audience);
            connection.disconnect();
        } catch (Exception e) {
            Log.w(TAG, "app_open track failed: " + e.getMessage());
        }
    }

    public static void pollNotification(Context context) {
        Context app = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Register device first so server can skip pushes sent before this install
                trackOpenSync(app);

                String base = AdsConfig.DEFAULT_API_BASE();
                String endpoint = AdsConfig.resolveApiPath(base, "api/pending_notification.php");
                String url = endpoint
                        + (endpoint.contains("?") ? "&" : "?")
                        + "device_id=" + java.net.URLEncoder.encode(deviceId(app), "UTF-8");
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    connection.disconnect();
                    return;
                }
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream(),
                                java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                connection.disconnect();
                String body = sb.toString();
                if (TextUtils.isEmpty(body)
                        || body.contains("\"notification\":null")
                        || !body.contains("\"title\"")) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(() -> AdminNotifyHelper.showFromJson(app, body));
            } catch (Exception e) {
                Log.w(TAG, "notify poll failed: " + e.getMessage());
            }
        });
    }
}
