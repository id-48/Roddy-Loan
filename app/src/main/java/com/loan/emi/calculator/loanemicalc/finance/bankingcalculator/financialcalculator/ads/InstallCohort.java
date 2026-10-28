package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public final class InstallCohort {
    private static final String TAG = "InstallCohort";
    private static final long REFERRER_TIMEOUT_MS = 4000L;

    public interface Callback {
        void onResolved(boolean marketing);
    }

    private InstallCohort() {
    }

    public static boolean isMarketing(Context context) {
        return AdsPrefs.isMarketingUser(context);
    }

    /**
     * Resolves organic vs marketing once via Play Install Referrer.
     * Meta / Facebook ads typically send utm_source=apps.facebook.com (or facebook / instagram).
     */
    public static void resolve(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        SharedPreferences prefs = AdsPrefs.prefs(app);
        if (prefs.getBoolean("audience_resolved", false)) {
            finish(callback, isMarketing(app));
            return;
        }

        AtomicBoolean done = new AtomicBoolean(false);
        Handler main = new Handler(Looper.getMainLooper());
        Runnable timeout = () -> {
            if (!done.compareAndSet(false, true)) {
                return;
            }
            Log.w(TAG, "referrer timeout → organic");
            applyAudience(app, false, "timeout");
            finish(callback, false);
        };
        main.postDelayed(timeout, REFERRER_TIMEOUT_MS);

        InstallReferrerClient client = InstallReferrerClient.newBuilder(app).build();
        try {
            client.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    String referrer = "";
                    try {
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            ReferrerDetails details = client.getInstallReferrer();
                            if (details != null && details.getInstallReferrer() != null) {
                                referrer = details.getInstallReferrer();
                            }
                        } else {
                            Log.w(TAG, "referrer responseCode=" + responseCode);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "referrer read failed: " + e.getMessage());
                    } finally {
                        try {
                            client.endConnection();
                        } catch (Exception ignored) {
                        }
                    }

                    if (!done.compareAndSet(false, true)) {
                        return;
                    }
                    main.removeCallbacks(timeout);
                    boolean marketing = isPaidReferrer(referrer);
                    Log.i(TAG, "referrer=\"" + referrer + "\" marketing=" + marketing);
                    applyAudience(app, marketing, referrer);
                    finish(callback, marketing);
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    // Timeout / retry path handles unresolved first launch.
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "referrer connect failed: " + e.getMessage());
            if (done.compareAndSet(false, true)) {
                main.removeCallbacks(timeout);
                applyAudience(app, false, "connect_failed");
                finish(callback, false);
            }
        }
    }

    public static void markMarketing(Context context, boolean marketing) {
        applyAudience(context.getApplicationContext(), marketing, "manual");
    }

    static boolean isPaidReferrer(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return false;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            decoded = raw;
        }
        String lower = decoded.toLowerCase(Locale.US);

        // Play organic / store browse
        if (lower.contains("utm_medium=organic")
                || lower.contains("utm_source=google-play")) {
            // Still marketing if a paid click id is also present
            if (!lower.contains("gclid=") && !lower.contains("fbclid=") && !lower.contains("gbraid=")
                    && !lower.contains("wbraid=")) {
                return false;
            }
        }

        if (lower.contains("gclid=")
                || lower.contains("gbraid=")
                || lower.contains("wbraid=")
                || lower.contains("fbclid=")) {
            return true;
        }

        String source = queryParam(decoded, "utm_source");
        String medium = queryParam(decoded, "utm_medium");
        if (!TextUtils.isEmpty(source)) {
            String s = source.toLowerCase(Locale.US);
            if (s.contains("facebook")
                    || s.contains("apps.facebook")
                    || s.equals("fb")
                    || s.contains("instagram")
                    || s.equals("ig")
                    || s.contains("meta")
                    || s.contains("googleads")
                    || s.contains("google_ads")
                    || (s.equals("google") && !TextUtils.isEmpty(medium)
                    && !"organic".equalsIgnoreCase(medium))) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(medium)) {
            String m = medium.toLowerCase(Locale.US);
            if (m.contains("cpc")
                    || m.contains("cpm")
                    || m.contains("paid")
                    || m.contains("ppc")
                    || m.contains("display")
                    || m.contains("affiliate")) {
                return true;
            }
        }

        // Meta / Google fragments without full UTM
        return lower.contains("apps.facebook.com")
                || lower.contains("facebook.com")
                || lower.contains("instagram.com")
                || lower.contains("utm_source=facebook")
                || lower.contains("utm_source=fb");
    }

    private static String queryParam(String referrer, String key) {
        try {
            // Install referrer is a query string, not a full URL
            Uri uri = Uri.parse("https://local/?" + referrer);
            return uri.getQueryParameter(key);
        } catch (Exception e) {
            return null;
        }
    }

    private static void applyAudience(Context context, boolean marketing, String reason) {
        AdsPrefs.setAudience(context, marketing ? "marketing" : "organic");
        AdsPrefs.prefs(context).edit()
                .putBoolean("audience_resolved", true)
                .putString("install_referrer", reason == null ? "" : reason)
                .apply();
        try {
            Analytics.setUserProperty("audience", marketing ? "marketing" : "organic");
        } catch (Exception ignored) {
        }
    }

    private static void finish(Callback callback, boolean marketing) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) {
                callback.onResolved(marketing);
            }
        });
    }
}
