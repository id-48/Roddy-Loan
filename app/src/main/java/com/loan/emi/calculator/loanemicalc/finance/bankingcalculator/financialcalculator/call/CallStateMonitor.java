package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-process phone-state listener — works while app process is alive
 * (Samsung often delays/drops only-manifest PHONE_STATE delivery).
 */
public final class CallStateMonitor {
    private static final String TAG = "CallStateMonitor";
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private static TelephonyManager telephonyManager;
    private static Object callbackOrListener;

    private CallStateMonitor() {
    }

    public static void ensureStarted(Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        if (!CallOverlayHelper.isAfterCallFeatureOn(app)) {
            return;
        }
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        telephonyManager = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            STARTED.set(false);
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                registerApi31(app);
            } else {
                registerLegacy(app);
            }
            Log.i(TAG, "started");
        } catch (SecurityException se) {
            STARTED.set(false);
            Log.e(TAG, "permission denied starting monitor", se);
        } catch (Exception e) {
            STARTED.set(false);
            Log.e(TAG, "failed to start", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static void registerApi31(Context app) {
        CallStateCallback combined = new CallStateCallback(app);
        telephonyManager.registerTelephonyCallback(ContextCompat.getMainExecutor(app), combined);
        callbackOrListener = combined;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final class CallStateCallback extends TelephonyCallback
            implements TelephonyCallback.CallStateListener {
        private final Context app;

        CallStateCallback(Context app) {
            this.app = app;
        }

        @Override
        public void onCallStateChanged(int state) {
            String mapped = mapCallState(state);
            Log.i(TAG, "state=" + mapped);
            CallOverlayHelper.onPhoneStateChanged(app, mapped, null);
        }
    }

    @SuppressWarnings("deprecation")
    private static void registerLegacy(Context app) {
        PhoneStateListener listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                String mapped = mapCallState(state);
                Log.i(TAG, "state=" + mapped);
                // Ignore phoneNumber — never store or display (Private Number only).
                CallOverlayHelper.onPhoneStateChanged(app, mapped, null);
            }
        };
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        callbackOrListener = listener;
    }

    private static String mapCallState(int state) {
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            return TelephonyManager.EXTRA_STATE_RINGING;
        }
        if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            return TelephonyManager.EXTRA_STATE_OFFHOOK;
        }
        return TelephonyManager.EXTRA_STATE_IDLE;
    }
}
