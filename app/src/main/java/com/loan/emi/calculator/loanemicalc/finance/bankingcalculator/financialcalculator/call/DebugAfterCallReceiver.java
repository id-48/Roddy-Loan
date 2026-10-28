package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Debug-only: adb shell am broadcast -a com.loan.emi.calculator.DEBUG_AFTER_CALL \
 *   -n …/.call.DebugAfterCallReceiver
 */
public class DebugAfterCallReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.loan.emi.calculator.DEBUG_AFTER_CALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null
                || (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            return;
        }
        if (intent == null || !ACTION.equals(intent.getAction())) {
            return;
        }
        Log.i("DebugAfterCall", "forcing after-call dialog (Private Number)");
        CallOverlayHelper.showAfterCall(
                context.getApplicationContext(),
                CallOverlayHelper.TYPE_OUTGOING,
                12L,
                System.currentTimeMillis(),
                false);
    }
}
