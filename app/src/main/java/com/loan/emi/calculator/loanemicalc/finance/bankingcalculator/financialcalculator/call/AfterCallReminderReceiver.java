package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AfterCallReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        Toast.makeText(context, "Call reminder", Toast.LENGTH_LONG).show();
    }
}
