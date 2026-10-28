package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

import java.util.Locale;
import java.util.UUID;

/**
 * Contacts-app style Firebase Analytics + Crashlytics helper.
 */
public final class Analytics {

    public static final String PARAM_SCREEN = "screen";
    public static final String PARAM_ELEMENT = "element";
    public static final String PARAM_LIST_POSITION = "list_position";
    public static final String PARAM_DIALOG = "dialog";

    private static final int MAX_EVENT_NAME = 40;
    private static final int MAX_USER_PROPERTY_VALUE = 36;
    private static final String PREFS = "emi_analytics";
    private static final String KEY_USER_ID = "analytics_user_id";

    private static FirebaseAnalytics analytics;
    private static FirebaseCrashlytics crashlytics;
    private static String currentScreen = "";

    private Analytics() {
    }

    public static void init(Context context) {
        Context app = context.getApplicationContext();
        analytics = FirebaseAnalytics.getInstance(app);
        crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
        String userId = analyticsUserId(app);
        analytics.setUserId(userId);
        crashlytics.setUserId(userId);
    }

    public static String currentScreen() {
        return currentScreen;
    }

    public static void setCurrentScreen(@NonNull String screen) {
        currentScreen = screen;
    }

    public static void logScreen(@NonNull String screen, @NonNull String screenClass) {
        if (analytics == null) {
            return;
        }
        currentScreen = screen;
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SCREEN_NAME, screen);
        params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass);
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params);
        crashlytics.setCustomKey(PARAM_SCREEN, screen);
        crashlytics.log("screen_view " + screen);
    }

    public static void logEvent(@NonNull String name) {
        logEvent(name, null);
    }

    public static void logEvent(@NonNull String name, @Nullable Bundle params) {
        if (analytics == null) {
            return;
        }
        Bundle bundle = params == null ? new Bundle() : params;
        if (!bundle.containsKey(PARAM_SCREEN)) {
            bundle.putString(PARAM_SCREEN, currentScreen);
        }
        String event = eventName(name);
        analytics.logEvent(event, bundle);
        crashlytics.log(event);
    }

    public static void logClick(@NonNull String source, @NonNull String element) {
        logClick(source, element, -1);
    }

    public static void logClick(@NonNull String source, @NonNull String element, int listPosition) {
        Bundle params = new Bundle();
        params.putString(PARAM_SCREEN, currentScreen);
        params.putString(PARAM_ELEMENT, element);
        if (listPosition >= 0) {
            params.putLong(PARAM_LIST_POSITION, listPosition);
        }
        logEvent(source + "_" + element, params);
    }

    /** Optional: set a friendly click name on a view (otherwise resource id / class is used). */
    public static void setClickName(@NonNull View view, @NonNull String name) {
        view.setTag(R.id.analytics_name, name);
    }

    public static void setUserProperty(@NonNull String name, @Nullable String value) {
        if (analytics == null) {
            return;
        }
        if (value != null && value.length() > MAX_USER_PROPERTY_VALUE) {
            value = value.substring(0, MAX_USER_PROPERTY_VALUE);
        }
        analytics.setUserProperty(name, value);
        crashlytics.setCustomKey(name, value == null ? "" : value);
    }

    public static void recordException(@NonNull Throwable throwable) {
        if (crashlytics != null) {
            crashlytics.recordException(throwable);
        }
    }

    public static String screenName(@NonNull Class<?> type) {
        String name = type.getSimpleName();
        for (String suffix : new String[]{"Activity", "Fragment"}) {
            if (name.endsWith(suffix) && name.length() > suffix.length()) {
                name = name.substring(0, name.length() - suffix.length());
                break;
            }
        }
        return snakeCase(name);
    }

    public static String snakeCase(@NonNull String value) {
        StringBuilder builder = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c) && i > 0 && !Character.isUpperCase(value.charAt(i - 1))
                    && value.charAt(i - 1) != '_') {
                builder.append('_');
            }
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString();
    }

    private static String eventName(String raw) {
        String name = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_");
        name = name.replaceAll("^_+|_+$", "");
        if (name.isEmpty() || !Character.isLetter(name.charAt(0))) {
            name = "e_" + name;
        }
        if (name.startsWith("firebase_") || name.startsWith("google_") || name.startsWith("ga_")) {
            name = "app_" + name;
        }
        if (name.length() > MAX_EVENT_NAME) {
            name = name.substring(0, MAX_EVENT_NAME).replaceAll("_+$", "");
        }
        return name;
    }

    private static String analyticsUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_USER_ID, null);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = (androidId == null || androidId.isEmpty())
                ? UUID.randomUUID().toString()
                : androidId;
        prefs.edit().putString(KEY_USER_ID, id).apply();
        return id;
    }
}
