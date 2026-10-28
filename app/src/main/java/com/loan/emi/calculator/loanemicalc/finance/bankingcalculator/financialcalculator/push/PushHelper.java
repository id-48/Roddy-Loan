package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.push;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsConfig;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.DeviceTracker;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;

/**
 * FCM bootstrap: notification channel, topic {@link #FCM_TOPIC}, token → server.
 * Firebase Console / admin can target topic {@code all_users} or a device token.
 */
public final class PushHelper {
    public static final String FCM_TOPIC = "all_users";
    public static final String CHANNEL_ID = "fcm_push";

    private static final String TAG = "PushHelper";
    private static volatile boolean initialized;

    private PushHelper() {
    }

    public static void init(Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        FcmNotifyHelper.ensureChannel(app);
        if (initialized) {
            return;
        }
        initialized = true;
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d(TAG, "FCM token ready len=" + (token == null ? 0 : token.length()));
                    if (!TextUtils.isEmpty(token)) {
                        AdsPrefs.prefs(app).edit().putString("fcm_token", token).apply();
                        registerToken(app, token);
                    }
                });
        subscribeTopic();
    }

    public static void onNewToken(Context context, String token) {
        if (context == null || TextUtils.isEmpty(token)) {
            return;
        }
        Context app = context.getApplicationContext();
        FcmNotifyHelper.ensureChannel(app);
        AdsPrefs.prefs(app).edit().putString("fcm_token", token).apply();
        subscribeTopic();
        registerToken(app, token);
    }

    private static void subscribeTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to topic " + FCM_TOPIC);
                    } else {
                        Log.w(TAG, "Topic subscribe failed", task.getException());
                    }
                });
    }

    public static void registerToken(Context context, String token) {
        if (context == null || TextUtils.isEmpty(token)) {
            return;
        }
        Context app = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String base = AdsConfig.DEFAULT_API_BASE();
                String endpoint = AdsConfig.resolveApiPath(base, "api/register_fcm.php");
                String audience = AdsPrefs.isMarketingUser(app) ? "marketing" : "organic";
                String url = endpoint
                        + (endpoint.contains("?") ? "&" : "?")
                        + "device_id=" + URLEncoder.encode(DeviceTracker.deviceId(app), "UTF-8")
                        + "&fcm_token=" + URLEncoder.encode(token, "UTF-8")
                        + "&audience=" + audience;
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.setRequestMethod("GET");
                int code = connection.getResponseCode();
                Log.d(TAG, "register_fcm code=" + code);
                connection.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "register_fcm failed: " + e.getMessage());
            }
        });
    }
}
