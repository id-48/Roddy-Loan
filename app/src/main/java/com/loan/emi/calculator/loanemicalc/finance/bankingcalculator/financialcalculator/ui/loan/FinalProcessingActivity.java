package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class FinalProcessingActivity extends BaseActivity {
    private static final long MIN_WAIT_MS = 5000L;
    private boolean movedOn;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_final_processing);
        setupToolbar("Processing", true);
        setupProgressHeader("Final processing");
        setText(R.id.tvHeadline, "Final Processing");
        setText(R.id.tvSubtext, "We are preparing your application summary...");

        View btn = findViewById(R.id.btnContinue);
        if (btn != null) {
            btn.setVisibility(View.GONE);
            btn.setOnClickListener(v -> proceed());
        }

        handler.postDelayed(() -> {
            setText(R.id.tvSubtext, "Ready — continue to risk assessment");
            showAndEnable(R.id.btnContinue);
            proceed();
        }, MIN_WAIT_MS);
    }

    private void proceed() {
        if (movedOn || isFinishing()) {
            return;
        }
        movedOn = true;
        handler.removeCallbacksAndMessages(null);
        goNext(true);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
