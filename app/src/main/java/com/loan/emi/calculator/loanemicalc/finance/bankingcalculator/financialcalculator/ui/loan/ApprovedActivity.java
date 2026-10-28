package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class ApprovedActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_approved);
        setupToolbar("Submitted", false);
        LoanSession.applicationComplete = true;
        if (LoanSession.submissionTime <= 0L) {
            LoanSession.submissionTime = System.currentTimeMillis();
        }
        LoanSession.lastNotifiedTrackerStep = 0;
        LoanSession.save(this);
        AppPrefs.setOnboardingDone(this, true);
        set(R.id.tvHeadline, "Application Submitted");
        set(R.id.tvSubheadline, "Reference: PL" + (System.currentTimeMillis() % 1000000));
        set(R.id.tvApprovedAmount, MoneyFormat.format(this, LoanSession.loanAmount));
        set(R.id.tvBody, "Your EMI estimate has been saved. Track status from Home.");
        findViewById(R.id.btnNext).setOnClickListener(v -> goNext(true));
    }
    private void set(int id, String t) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(t);
    }
}
