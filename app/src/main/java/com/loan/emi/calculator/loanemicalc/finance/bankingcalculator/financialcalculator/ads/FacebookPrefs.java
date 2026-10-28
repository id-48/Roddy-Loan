package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.content.Context;
import android.content.SharedPreferences;

public final class FacebookPrefs {
    private static final String PREF_NAME = "facebook_prefs";
    private static final String KEY_FB_APP_ID = "fb_app_id";
    private static final String KEY_FB_CLIENT_TOKEN = "fb_client_token";

    private FacebookPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void save(Context context, String appId, String clientToken) {
        if (appId == null || appId.trim().isEmpty()) {
            return;
        }
        prefs(context).edit()
                .putString(KEY_FB_APP_ID, appId.trim())
                .putString(KEY_FB_CLIENT_TOKEN, clientToken == null ? "" : clientToken.trim())
                .apply();
    }

    public static String getAppId(Context context) {
        return prefs(context).getString(KEY_FB_APP_ID, "");
    }

    public static String getClientToken(Context context) {
        return prefs(context).getString(KEY_FB_CLIENT_TOKEN, "");
    }

    public static boolean hasCredentials(Context context) {
        String appId = getAppId(context);
        return appId != null && !appId.trim().isEmpty();
    }
}
