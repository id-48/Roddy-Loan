package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanTrackerProgress;

public class TrackingDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_tracking_detail);
        LoanSession.load(this);
        int step = getIntent().getIntExtra("step_index", 1);
        boolean rejected = getIntent().getBooleanExtra("rejected", false);
        LoanTrackerProgress progress = LoanTrackerProgress.fromSubmission(LoanSession.submissionTime);
        if (progress.rejected && step == 4) {
            rejected = true;
        }

        TextView title = findViewById(R.id.tvTitle);
        if (title != null) {
            title.setText(rejected ? "Loan Rejected" : ("Step " + step + " Tracking"));
        }
        TextView sub = findViewById(R.id.tvSubTitle);
        if (sub != null) {
            sub.setText(rejected ? LoanTrackerProgress.REJECT_REASON : "Detailed processing log");
        }
        LinearLayout log = findViewById(R.id.logContainer);
        if (log != null) {
            String[] lines;
            if (rejected) {
                lines = new String[]{
                        "Final review completed",
                        "Eligibility criteria checked",
                        LoanTrackerProgress.REJECT_REASON,
                        "Application closed"
                };
            } else if (step == 1) {
                lines = new String[]{
                        "Application submitted",
                        "Profile validated",
                        "Request queued for processing"
                };
            } else if (step == 2) {
                lines = new String[]{
                        "Documents under review",
                        "KYC verification in progress",
                        "Waiting for underwriting"
                };
            } else if (step == 3) {
                lines = new String[]{
                        "Underwriting complete",
                        "Risk score computed",
                        "Awaiting final decision"
                };
            } else {
                lines = new String[]{
                        "Request queued",
                        "Profile validated",
                        "Eligibility score computed",
                        "Waiting for next stage"
                };
            }
            for (String line : lines) {
                TextView tv = new TextView(this);
                tv.setText("• " + line);
                tv.setPadding(16, 16, 16, 16);
                log.addView(tv);
            }
        }
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }
}
