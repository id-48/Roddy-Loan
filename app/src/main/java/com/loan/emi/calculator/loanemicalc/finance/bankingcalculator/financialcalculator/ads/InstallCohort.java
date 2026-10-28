package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

public final class InstallCohort {
    public interface Callback {
        void onResolved(boolean marketing);
    }

    private InstallCohort() {
    }

    public static boolean isMarketing(Context context) {
        return AdsPrefs.isMarketingUser(context);
    }

    /** Resolves organic vs marketing. Default organic until referrer/API says otherwise. */
    public static void resolve(Context context, Callback callback) {
        SharedPreferences prefs = AdsPrefs.prefs(context);
        if (!prefs.contains("audience_resolved")) {
            AdsPrefs.setAudience(context, "organic");
            prefs.edit().putBoolean("audience_resolved", true).apply();
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) {
                callback.onResolved(isMarketing(context));
            }
        });
    }

    public static void markMarketing(Context context, boolean marketing) {
        AdsPrefs.setAudience(context, marketing ? "marketing" : "organic");
        AdsPrefs.prefs(context).edit().putBoolean("audience_resolved", true).apply();
    }
}
