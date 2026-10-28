package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.home.HomeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.splash.SplashActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LauncherHome {

    public interface AppsCallback {
        void onAppsLoaded(List<AppEntry> apps);
    }

    public static final class AppEntry {
        public final String packageName;
        public final String label;
        public final Drawable icon;
        public final Intent launch;

        public AppEntry(String packageName, String label, Drawable icon, Intent launch) {
            this.packageName = packageName == null ? "" : packageName;
            this.label = label == null ? "" : label;
            this.icon = icon;
            this.launch = launch;
        }
    }

    private static final ExecutorService LOAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static List<AppEntry> cachedApps;
    private static long cachedAt;
    private static final long CACHE_MS = 30_000L;

    private LauncherHome() {
    }

    public static boolean isEnabled(Context context) {
        return AdsPrefs.isLauncherEnabled(context);
    }

    public static Intent mainAppIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent splashIntent(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent resumeAppIntent(Context context) {
        if (AppPrefs.isOnboardingDone(context)) {
            return mainAppIntent(context);
        }
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return launch;
        }
        return splashIntent(context);
    }

    public static void invalidateCache() {
        cachedApps = null;
        cachedAt = 0L;
    }

    /** Background load — never call {@link #loadApps} on the UI thread. */
    public static void loadAppsAsync(Context context, boolean forceRefresh, AppsCallback callback) {
        if (callback == null || context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        if (!forceRefresh && cachedApps != null
                && System.currentTimeMillis() - cachedAt < CACHE_MS) {
            callback.onAppsLoaded(cachedApps);
            return;
        }
        LOAD_EXECUTOR.execute(() -> {
            List<AppEntry> apps = loadApps(app);
            cachedApps = apps;
            cachedAt = System.currentTimeMillis();
            MAIN.post(() -> callback.onAppsLoaded(apps));
        });
    }

    public static List<AppEntry> loadApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved;
        try {
            resolved = pm.queryIntentActivities(main, 0);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        if (resolved == null || resolved.isEmpty()) {
            return Collections.emptyList();
        }
        String self = context.getPackageName();
        List<AppEntry> out = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        for (ResolveInfo info : resolved) {
            if (info == null || info.activityInfo == null) {
                continue;
            }
            String packageName = info.activityInfo.packageName;
            if (packageName == null || packageName.isEmpty() || !seen.add(packageName)) {
                continue;
            }
            Intent launch;
            if (packageName.equals(self)) {
                launch = mainAppIntent(context);
            } else {
                // Build launch intent without extra PM round-trip per package
                launch = new Intent(Intent.ACTION_MAIN);
                launch.addCategory(Intent.CATEGORY_LAUNCHER);
                launch.setClassName(packageName, info.activityInfo.name);
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            CharSequence labelCs;
            try {
                labelCs = info.loadLabel(pm);
            } catch (Exception e) {
                labelCs = packageName;
            }
            String label = labelCs == null ? packageName : labelCs.toString();
            Drawable icon;
            try {
                icon = info.loadIcon(pm);
            } catch (Exception e) {
                icon = null;
            }
            out.add(new AppEntry(packageName, label, icon, launch));
        }
        if (!seen.contains(self)) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(self, 0);
                CharSequence labelCs = pm.getApplicationLabel(info);
                Drawable icon = pm.getApplicationIcon(info);
                out.add(new AppEntry(self,
                        labelCs == null ? self : labelCs.toString(),
                        icon,
                        mainAppIntent(context)));
            } catch (Exception ignored) {
            }
        }
        Collections.sort(out, Comparator.comparing(a -> a.label, String.CASE_INSENSITIVE_ORDER));
        return out;
    }
}
