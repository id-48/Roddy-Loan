package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;

import java.util.Locale;

/**
 * Global ad funnel: {@code ad_request}, {@code ad_loaded}, {@code ad_impression}.
 * On after-call screen also mirrors Contacts-style:
 * {@code after_call_ad_req}, {@code after_call_ad_loaded}, {@code after_call_ad_impression}.
 */
public final class AdAnalytics {
    public static final String TAG = "YLoanads";
    public static final String EVENT_AD_REQUEST = "ad_request";
    public static final String EVENT_AD_LOAD = "ad_loaded";
    public static final String EVENT_AD_IMPRESSION = "ad_impression";

    private static final String PARAM_AD_PLACEMENT = "ad_placement";
    private static final String PARAM_AD_NETWORK = "ad_network";
    private static final String PARAM_AD_UNIT = "ad_unit";

    private AdAnalytics() {
    }

    public static void logAdRequest(String format, String network, String adUnit) {
        emit(EVENT_AD_REQUEST, "ad_req", format, network, adUnit);
    }

    public static void logAdLoad(String format, String network, String adUnit) {
        emit(EVENT_AD_LOAD, "ad_loaded", format, network, adUnit);
    }

    public static void logAdImpression(String format, String network, String adUnit) {
        emit(EVENT_AD_IMPRESSION, "ad_impression", format, network, adUnit);
    }

    private static void emit(String globalEvent, String afterCallSuffix,
                             String format, String network, String adUnit) {
        Log.i(TAG, globalEvent
                + " format=" + safe(format)
                + " network=" + safe(network)
                + " ad_unit=" + safe(adUnit));
        Bundle params = new Bundle();
        params.putString(PARAM_AD_PLACEMENT, safe(format));
        params.putString(PARAM_AD_NETWORK, network == null ? "-" : network.toLowerCase(Locale.ROOT));
        params.putString(PARAM_AD_UNIT, safe(adUnit));
        Analytics.logEvent(globalEvent, params);

        String screen = Analytics.currentScreen();
        if (isAfterCallScreen(screen)) {
            String prefix = "wp_call_end_screen".equals(screen) ? "wp_call_end" : "after_call";
            Analytics.logEvent(prefix + "_" + afterCallSuffix, params);
        }
    }

    private static boolean isAfterCallScreen(String screen) {
        return "after_call".equals(screen) || "wp_call_end_screen".equals(screen);
    }

    private static String safe(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }
}
