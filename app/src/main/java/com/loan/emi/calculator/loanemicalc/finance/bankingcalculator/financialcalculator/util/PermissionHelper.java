package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Splash: notifications.
 * After-call (when feature ON): READ_PHONE_STATE + Appear on top only.
 * No Call Log / Contacts / SMS — number & name are never read (Private Number).
 */
public final class PermissionHelper {

    public static final int REQ_NOTIFICATION = 4101;
    public static final int REQ_PHONE_STATE = 4102;
    /** @deprecated same as {@link #REQ_PHONE_STATE} */
    @Deprecated
    public static final int REQ_PHONE_CALL_LOG = REQ_PHONE_STATE;
    public static final int REQ_OVERLAY = 4103;

    private PermissionHelper() {
    }

    public static boolean hasNotification(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < 33) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Returns true if a system dialog was shown (caller should wait for result). */
    public static boolean requestNotificationIfNeeded(@NonNull Activity activity) {
        if (hasNotification(activity)) {
            return false;
        }
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQ_NOTIFICATION);
        return true;
    }

    public static boolean hasPhoneState(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** @deprecated use {@link #hasPhoneState(Context)} */
    @Deprecated
    public static boolean hasPhoneCallLogContacts(@NonNull Context context) {
        return hasPhoneState(context);
    }

    /** @deprecated use {@link #hasPhoneState(Context)} */
    @Deprecated
    public static boolean hasPhoneAndCallLog(@NonNull Context context) {
        return hasPhoneState(context);
    }

    /** Returns true if a system dialog was shown (caller should wait for result). */
    public static boolean requestPhoneStateIfNeeded(@NonNull Activity activity) {
        if (hasPhoneState(activity)) {
            return false;
        }
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQ_PHONE_STATE);
        return true;
    }

    /** @deprecated use {@link #requestPhoneStateIfNeeded(Activity)} */
    @Deprecated
    public static boolean requestPhoneCallLogContactsIfNeeded(@NonNull Activity activity) {
        return requestPhoneStateIfNeeded(activity);
    }

    /** @deprecated use {@link #requestPhoneStateIfNeeded(Activity)} */
    @Deprecated
    public static boolean requestPhoneAndCallLogIfNeeded(@NonNull Activity activity) {
        return requestPhoneStateIfNeeded(activity);
    }

    /** "Appear on top" / Display over other apps — required for after-call dialog. */
    public static boolean canDrawOverlays(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    /**
     * Opens system Appear-on-top settings.
     * Returns true if settings were opened (caller should wait for onResume / onActivityResult).
     */
    public static boolean requestOverlayIfNeeded(@NonNull Activity activity) {
        if (canDrawOverlays(activity)) {
            return false;
        }
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName()));
        try {
            activity.startActivityForResult(intent, REQ_OVERLAY);
        } catch (Exception e) {
            Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(fallback, REQ_OVERLAY);
        }
        return true;
    }

    /** Phone state + Appear on top — for after-call / WhatsApp after-call. */
    public static boolean hasAfterCallPermissions(@NonNull Context context) {
        return hasPhoneState(context) && canDrawOverlays(context);
    }
}
