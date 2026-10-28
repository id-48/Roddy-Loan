package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AdsConfigLoader {
    private static final String TAG = "AdsConfigLoader";
    private static final String TAG_JSON = "AdsJsonRES";
    private static final String PREFS = "emi_ads_prefs";
    private static final String KEY_CACHE = "ads_config_json";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    /** Logcat line limit ~4k; chunk long JSON so full response is visible. */
    private static final int LOG_CHUNK = 3500;

    public interface Callback {
        void onLoaded(AdsConfig config);
    }

    private AdsConfigLoader() {
    }

    public static void load(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        InstallCohort.resolve(app, marketing -> EXECUTOR.execute(() -> {
            AdsConfig config = resolve(app, marketing);
            AdsPrefs.cacheFlags(app, config);
            MAIN.post(() -> {
                FacebookSdkHelper.applyFromConfig(app, config);
                AdsManager.get().initialize(app, config);
                if (callback != null) {
                    callback.onLoaded(config);
                }
            });
        }));
    }

    private static AdsConfig resolve(Context context, boolean marketing) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String asset = readAsset(context, "ads_config.json");
        AdsConfig fromAsset = AdsConfig.parse(asset, marketing);
        // Base URL only from native (C++); never from assets/prefs plaintext
        String remoteUrl = AdsConfig.resolveConfigEndpoint(AdsConfig.DEFAULT_API_BASE());
        if (!TextUtils.isEmpty(remoteUrl)) {
            String withAudience = remoteUrl
                    + (remoteUrl.contains("?") ? "&" : "?")
                    + "audience=" + (marketing ? "marketing" : "organic");
            Log.d(TAG, "fetching remote ads config url=" + withAudience);
            String remote = download(withAudience);
            if (!TextUtils.isEmpty(remote)) {
                logAdsJson("api", remote);
                prefs.edit().putString(KEY_CACHE, remote).apply();
                return AdsConfig.parse(remote, marketing);
            }
        }
        String cached = prefs.getString(KEY_CACHE, null);
        if (!TextUtils.isEmpty(cached)) {
            logAdsJson("cache", cached);
            return AdsConfig.parse(cached, marketing);
        }
        if (!TextUtils.isEmpty(asset)) {
            logAdsJson("asset", asset);
        }
        return fromAsset;
    }

    private static void logAdsJson(String source, String json) {
        if (TextUtils.isEmpty(json)) {
            Log.i(TAG_JSON, source + " empty");
            return;
        }
        Log.i(TAG_JSON, source + " len=" + json.length());
        if (json.length() <= LOG_CHUNK) {
            Log.i(TAG_JSON, json);
            return;
        }
        int parts = (json.length() + LOG_CHUNK - 1) / LOG_CHUNK;
        for (int i = 0; i < parts; i++) {
            int start = i * LOG_CHUNK;
            int end = Math.min(json.length(), start + LOG_CHUNK);
            Log.i(TAG_JSON, "[" + (i + 1) + "/" + parts + "] " + json.substring(start, end));
        }
    }

    private static String readAsset(Context context, String name) {
        try (InputStream in = context.getAssets().open(name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            Log.w(TAG, "asset missing: " + name);
            return "";
        }
    }

    private static String download(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (Exception e) {
            Log.w(TAG, "remote ads config failed: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }
}
