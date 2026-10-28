package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.PermissionHelper;

/**
 * After-call overlay. Never reads call number/name (no Call Log / Contacts).
 * UI always shows Private Number.
 */
public final class CallOverlayHelper {
    private static final String TAG = "CallOverlayHelper";
    private static final String CHANNEL_ID = "after_call_fs";
    private static final int NOTIF_ID = 7101;
    private static final long AFTER_CALL_COOLDOWN_MS = 2500L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Local call-type ints (avoid needing CallLog permission for constants only). */
    public static final int TYPE_INCOMING = 1;
    public static final int TYPE_OUTGOING = 2;
    public static final int TYPE_MISSED = 3;

    private static volatile boolean wasRinging;
    private static volatile boolean wasOffhook;
    private static volatile long callStartMs;
    private static volatile long ringStartMs;
    private static volatile long lastAfterCallShownMs;
    private static PowerManager.WakeLock overlayWakeLock;

    private CallOverlayHelper() {
    }

    public static boolean isAfterCallFeatureOn(Context context) {
        return AdsPrefs.isAfterCallEnabled(context) || AdsPrefs.isWhatsAppAfterCallEnabled(context);
    }

    public static boolean hasPhonePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasOverlayPermission(Context context) {
        return PermissionHelper.canDrawOverlays(context);
    }

    public static void onPhoneStateChanged(Context context, String state, String number) {
        if (TextUtils.isEmpty(state) || context == null) {
            return;
        }
        if (!hasPhonePermission(context)) {
            Log.w(TAG, "skip phone state: READ_PHONE_STATE missing");
            return;
        }
        // Intentionally ignore number — never store or display it (Private Number only).
        Log.i(TAG, "phoneState=" + state);
        if (android.telephony.TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            wasRinging = true;
            wasOffhook = false;
            long now = System.currentTimeMillis();
            ringStartMs = now;
            callStartMs = now;
        } else if (android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            wasOffhook = true;
            if (callStartMs <= 0L) {
                callStartMs = System.currentTimeMillis();
            }
        } else if (android.telephony.TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            long now = System.currentTimeMillis();
            boolean hadCall = wasRinging || wasOffhook;
            boolean rang = wasRinging;
            boolean answered = wasOffhook;
            long start = callStartMs > 0L ? callStartMs : ringStartMs;
            wasRinging = false;
            wasOffhook = false;
            callStartMs = 0L;
            ringStartMs = 0L;
            if (!hadCall) {
                return;
            }
            if (!AdsPrefs.isAfterCallEnabled(context)) {
                Log.w(TAG, "skip IDLE: afterCallON false");
                return;
            }
            int type;
            long duration;
            if (!rang) {
                type = TYPE_OUTGOING;
                duration = Math.max(0L, (now - (start > 0L ? start : now)) / 1000L);
            } else if (answered) {
                type = TYPE_INCOMING;
                duration = Math.max(0L, (now - (start > 0L ? start : now)) / 1000L);
            } else {
                type = TYPE_MISSED;
                duration = 0L;
            }
            showAfterCall(context, type, duration, start > 0L ? start : now, false);
        }
    }

    public static void showAfterCall(Context context, int callType,
                                     long durationSeconds, long callTimeMs, boolean fromWhatsApp) {
        if (context == null) {
            return;
        }
        boolean allowed = fromWhatsApp
                ? AdsPrefs.isWhatsAppAfterCallEnabled(context)
                : AdsPrefs.isAfterCallEnabled(context);
        if (!allowed) {
            Log.w(TAG, "skip after-call: feature OFF");
            return;
        }
        if (!fromWhatsApp && !hasPhonePermission(context)) {
            Log.w(TAG, "skip after-call: phone permission missing");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAfterCallShownMs < AFTER_CALL_COOLDOWN_MS) {
            Log.w(TAG, "skip after-call: cooldown");
            return;
        }
        // Organic/marketing % — phone + WhatsApp share one counter (after cooldown)
        if (!AdsPrefs.consumeAfterCallShowSlot(context)) {
            Log.w(TAG, "skip after-call: show percent gate");
            return;
        }
        lastAfterCallShownMs = now;

        Context app = context.getApplicationContext();
        acquireOverlayWakeLock(app);
        AdsManager.get().suppressResumeAppOpenBriefly();

        // Never pass number or name — AfterCallActivity always shows Private Number.
        Intent intent = AfterCallActivity.createIntent(
                app, callType, durationSeconds, callTimeMs, fromWhatsApp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        MAIN.post(() -> {
            boolean started = false;
            if (hasOverlayPermission(app) || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                try {
                    app.startActivity(intent);
                    started = true;
                    Log.i(TAG, "AfterCallActivity startActivity ok");
                } catch (Exception e) {
                    Log.e(TAG, "startActivity failed", e);
                }
            } else {
                Log.w(TAG, "Appear on top missing — using full-screen notification");
            }
            postFullScreenNotification(app, intent, fromWhatsApp);
            if (!started) {
                Log.i(TAG, "relying on full-screen notification to open dialog");
            }
        });
    }

    private static void postFullScreenNotification(Context app, Intent openIntent,
                                                   boolean fromWhatsApp) {
        try {
            ensureChannel(app);
            PendingIntent pi = PendingIntent.getActivity(
                    app,
                    NOTIF_ID,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(app, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(fromWhatsApp ? "WhatsApp Call Ended" : "Call Ended")
                    .setContentText("Tap for quick actions")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setFullScreenIntent(pi, true);
            NotificationManager nm =
                    (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify(NOTIF_ID, builder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "full-screen notification failed", e);
        }
    }

    private static void ensureChannel(Context app) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm =
                (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "After call", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Opens Call Ended dialog");
        ch.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }

    public static void configureLockScreen(Activity activity) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true);
            activity.setTurnScreenOn(true);
            KeyguardManager keyguardManager = activity.getSystemService(KeyguardManager.class);
            if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                keyguardManager.requestDismissKeyguard(activity, null);
            }
        }
        try {
            NotificationManager nm =
                    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIF_ID);
            }
        } catch (Exception ignored) {
        }
    }

    public static void releaseOverlayResources() {
        releaseOverlayWakeLock();
    }

    private static void acquireOverlayWakeLock(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager == null) {
                return;
            }
            if (overlayWakeLock != null && overlayWakeLock.isHeld()) {
                return;
            }
            overlayWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "PersonalEMI:AfterCall");
            overlayWakeLock.acquire(15_000L);
        } catch (Exception ignored) {
        }
    }

    private static void releaseOverlayWakeLock() {
        try {
            if (overlayWakeLock != null && overlayWakeLock.isHeld()) {
                overlayWakeLock.release();
            }
        } catch (Exception ignored) {
        }
        overlayWakeLock = null;
    }
}
