package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            return;
        }
        if (!CallOverlayHelper.isAfterCallFeatureOn(context)) {
            return;
        }
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        // Never read EXTRA_INCOMING_NUMBER — Private Number only, no Call Log permission.
        final PendingResult pendingResult = goAsync();
        try {
            CallOverlayHelper.onPhoneStateChanged(context.getApplicationContext(), state, null);
        } finally {
            pendingResult.finish();
        }
    }
}
