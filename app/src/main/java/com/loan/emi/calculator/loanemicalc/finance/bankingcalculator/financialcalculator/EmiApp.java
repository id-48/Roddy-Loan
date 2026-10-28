package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator;

import android.app.Application;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.FacebookPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.FacebookSdkHelper;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.ScreenTracker;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call.CallStateMonitor;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.push.PushHelper;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.LocaleHelper;

public class EmiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocaleHelper.applyStored(this);
        Analytics.init(this);
        Analytics.setUserProperty("audience",
                AdsPrefs.isMarketingUser(this) ? "marketing" : "organic");
        ScreenTracker.register(this);
        AdsManager.get().warmUp(this);
        PushHelper.init(this);
        if (FacebookPrefs.hasCredentials(this)) {
            FacebookSdkHelper.initialize(this);
        }
        CallStateMonitor.ensureStarted(this);
    }
}
