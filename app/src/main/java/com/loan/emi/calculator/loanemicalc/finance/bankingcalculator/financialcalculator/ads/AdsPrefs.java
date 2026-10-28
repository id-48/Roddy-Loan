package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.content.Context;
import android.content.SharedPreferences;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher.LauncherRoleHelper;

import java.util.ArrayList;
import java.util.List;

public final class AdsPrefs {
    private static final String PREFS = "emi_ads_prefs";

    private AdsPrefs() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isMarketingUser(Context context) {
        return "marketing".equals(prefs(context).getString("audience", "organic"));
    }

    public static void setAudience(Context context, String audience) {
        prefs(context).edit().putString("audience",
                "marketing".equals(audience) ? "marketing" : "organic").apply();
    }

    public static boolean isAfterCallEnabled(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null && config.afterCallON != null) {
            return config.afterCallON;
        }
        return prefs(context).getBoolean("afterCallON", false);
    }

    public static boolean isWhatsAppAfterCallEnabled(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null && config.whatsappAfterCall != null) {
            return config.whatsappAfterCall;
        }
        return prefs(context).getBoolean("whatsappAfterCall", false);
    }

    /**
     * Show frequency for Call Ended (phone + WhatsApp shared), 0–100.
     * 100 = every call, 50 = every 2nd, 25 = every 4th.
     */
    public static int afterCallShowPercent(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null && config.afterCallShowPercent != null) {
            return Math.max(0, Math.min(100, config.afterCallShowPercent));
        }
        return Math.max(0, Math.min(100, prefs(context).getInt("afterCallShowPercent", 100)));
    }

    /**
     * Counts phone + WhatsApp call-end events and returns whether to show the screen
     * based on {@link #afterCallShowPercent(Context)}.
     */
    public static boolean consumeAfterCallShowSlot(Context context) {
        int percent = afterCallShowPercent(context);
        if (percent <= 0) {
            return false;
        }
        if (percent >= 100) {
            return true;
        }
        int interval = Math.max(1, Math.round(100f / percent));
        SharedPreferences p = prefs(context);
        int count = p.getInt("after_call_event_count", 0) + 1;
        p.edit().putInt("after_call_event_count", count).apply();
        boolean show = count % interval == 0;
        android.util.Log.d("AfterCallFreq",
                "event=" + count + " percent=" + percent + " interval=" + interval + " show=" + show);
        return show;
    }

    /** Marketing Loan Tracker switch (admin loanTrackerON). Organic never shows tracker. */
    public static boolean isLoanTrackerEnabled(Context context) {
        if (!isMarketingUser(context)) {
            return false;
        }
        AdsConfig config = AdsManager.get().config();
        if (config != null && config.loanTrackerON != null) {
            return config.loanTrackerON;
        }
        return prefs(context).getBoolean("loanTrackerON", false);
    }

    public static boolean isLauncherEnabled(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null && config.launcherON != null) {
            return config.launcherON;
        }
        return prefs(context).getBoolean("launcherON", false);
    }

    public static int launcherInterval(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null) {
            return config.resolvedLauncherInterval();
        }
        return Math.max(0, prefs(context).getInt("launcher_interval", 1));
    }

    public static boolean isTestMode(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null) {
            return config.mainTest || (config.testMode != null && config.testMode);
        }
        return true;
    }

    public static void cacheFlags(Context context, AdsConfig config) {
        if (config == null) {
            return;
        }
        List<Integer> order = config.resolvedOnboardingOrder();
        StringBuilder orderCsv = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            if (i > 0) {
                orderCsv.append(',');
            }
            orderCsv.append(order.get(i));
        }
        boolean wasLauncher = prefs(context).getBoolean("launcherON", false);
        boolean nowLauncher = config.launcherON != null && config.launcherON;
        int showPct = config.afterCallShowPercent == null
                ? 100
                : Math.max(0, Math.min(100, config.afterCallShowPercent));
        prefs(context).edit()
                .putBoolean("afterCallON", config.afterCallON != null && config.afterCallON)
                .putBoolean("whatsappAfterCall", config.whatsappAfterCall != null && config.whatsappAfterCall)
                .putInt("afterCallShowPercent", showPct)
                .putBoolean("loanTrackerON", config.loanTrackerON != null && config.loanTrackerON)
                .putBoolean("launcherON", nowLauncher)
                .putInt("launcher_interval", config.resolvedLauncherInterval())
                .putBoolean("updateInprogress", config.updateInprogress)
                .putBoolean("isSplashFullscreen", config.isSplashFullscreen)
                .putInt("remoteVersionCode", config.versionCode)
                .putString("applink", config.applink == null ? "" : config.applink)
                .putString("onboardingScreenOrder", orderCsv.toString())
                .apply();
        if (config.fbAppId != null && !config.fbAppId.trim().isEmpty()) {
            FacebookPrefs.save(context, config.fbAppId, config.fbClientToken);
        }
        LauncherRoleHelper.syncLauncherComponent(context, nowLauncher);
        if (nowLauncher && !wasLauncher) {
            LauncherRoleHelper.resetAsked(context);
        }
    }

    public static List<Integer> onboardingScreenOrder(Context context) {
        AdsConfig config = AdsManager.get().config();
        if (config != null) {
            List<Integer> fromConfig = config.resolvedOnboardingOrder();
            if (!fromConfig.isEmpty()) {
                return fromConfig;
            }
        }
        String csv = prefs(context).getString("onboardingScreenOrder", "");
        List<Integer> out = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) {
            return out;
        }
        for (String part : csv.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            try {
                out.add(Integer.parseInt(p));
            } catch (Exception ignored) {
            }
        }
        return out;
    }
}
