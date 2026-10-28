package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

/** Asks user to set this app as default home when admin launcherON is true. */
public final class LauncherRoleHelper {

    public static final int REQUEST_HOME_ROLE = 4821;
    private static final String TAG = "LauncherRole";
    private static final String PREF = "launcher_role_pref";
    private static final String KEY_ASKED = "home_role_asked";

    /** True when we opened Home Settings (no onActivityResult). */
    private static boolean waitingHomeSettings;

    private LauncherRoleHelper() {
    }

    public static boolean isWaitingHomeSettings() {
        return waitingHomeSettings;
    }

    public static boolean isDefaultHome(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolve = context.getPackageManager()
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolve == null || resolve.activityInfo == null) {
            return false;
        }
        return context.getPackageName().equals(resolve.activityInfo.packageName);
    }

    public static void syncLauncherComponent(Context context, boolean enabled) {
        if (context == null) {
            return;
        }
        try {
            PackageManager pm = context.getPackageManager();
            ComponentName component = launcherComponent(context);
            int state = enabled
                    ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            pm.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP);
            Log.d(TAG, "launcher_component_enabled=" + enabled);
        } catch (Throwable t) {
            Log.w(TAG, "launcher_component_sync_failed " + t.getMessage());
        }
    }

    /**
     * After notification + ads config: ask set-as-default once if launcherON.
     *
     * @return true if system UI was started (wait for result / resume)
     */
    public static boolean requestDefaultHomeAfterNotification(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (!LauncherHome.isEnabled(activity)) {
            syncLauncherComponent(activity, false);
            return false;
        }
        syncLauncherComponent(activity, true);
        if (isDefaultHome(activity)) {
            markAsked(activity);
            return false;
        }
        if (wasAsked(activity)) {
            return false;
        }
        markAsked(activity);
        return openHomeChooser(activity);
    }

    private static boolean openHomeChooser(Activity activity) {
        waitingHomeSettings = false;
        LauncherGate.onHomeRoleUiStarted();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = activity.getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    LauncherGate.onHomeRoleUiFinished();
                    return false;
                }
                try {
                    Intent roleIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
                    activity.startActivityForResult(roleIntent, REQUEST_HOME_ROLE);
                    return true;
                } catch (Exception e) {
                    Log.w(TAG, "role request failed: " + e.getMessage());
                }
            }
        }
        try {
            waitingHomeSettings = true;
            activity.startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
            return true;
        } catch (Exception e) {
            waitingHomeSettings = false;
            LauncherGate.onHomeRoleUiFinished();
            return false;
        }
    }

    public static void onHomeRoleResult(Activity activity) {
        waitingHomeSettings = false;
        LauncherGate.onHomeRoleUiFinished();
        Log.d(TAG, "home_role_done isDefault=" + isDefaultHome(activity));
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static boolean wasAsked(Context context) {
        return prefs(context).getBoolean(KEY_ASKED, false);
    }

    private static void markAsked(Context context) {
        prefs(context).edit().putBoolean(KEY_ASKED, true).apply();
    }

    public static void resetAsked(Context context) {
        prefs(context).edit().putBoolean(KEY_ASKED, false).apply();
    }

    public static ComponentName launcherComponent(Context context) {
        return new ComponentName(context, LauncherActivity.class);
    }
}
