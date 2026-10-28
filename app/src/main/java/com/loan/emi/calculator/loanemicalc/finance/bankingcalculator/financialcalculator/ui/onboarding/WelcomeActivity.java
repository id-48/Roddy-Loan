package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class WelcomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Only skip if onboarding already finished — never jump Home mid organic/marketing order
        if (AppPrefs.isOnboardingDone(this)) {
            markStepAndFinishToHome();
            return;
        }
        setContentView(R.layout.activity_pl_welcome);
        setupProgressHeader("Welcome — start your application");
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Get Instant Loan Offer");
        TextView s = findViewById(R.id.tvSubtext);
        if (s != null) s.setText("Check your EMI eligibility in minutes");
        TextView amount = findViewById(R.id.tvTeaserAmount);
        if (amount != null) amount.setText(MoneyFormat.format(this, 500000));
        TextView rate = findViewById(R.id.tvTeaserRate);
        if (rate != null) rate.setText("10.5% p.a.");
        TextView emi = findViewById(R.id.tvTeaserEmi);
        if (emi != null) emi.setText("EMI from " + MoneyFormat.format(this, 8500));
        findViewById(R.id.btnStart).setOnClickListener(v -> goNext(true));
    }
}
