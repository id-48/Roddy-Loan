package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class AiAnalysisActivity extends BaseActivity {
    private static final long MIN_WAIT_MS = 5000L;
    private final String[] statuses = {
            "Analyzing profile...",
            "Checking credit signals...",
            "Matching lenders...",
            "Preparing offer..."
    };
    private int idx;
    private boolean movedOn;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_ai_analysis);
        setupToolbar("AI Analysis", true);
        setupProgressHeader("Smart analysis in progress");
        setText(R.id.tvHeadline, "Smart Analysis");
        setText(R.id.tvSubtext, "Please wait while we analyse your application");
        startedAt = System.currentTimeMillis();

        View btn = findViewById(R.id.btnContinue);
        if (btn != null) {
            btn.setVisibility(View.GONE);
            btn.setOnClickListener(v -> proceed());
        }

        TextView status = findViewById(R.id.tvStatus);
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (idx < statuses.length) {
                    if (status != null) {
                        status.setText(statuses[idx]);
                    }
                    idx++;
                    handler.postDelayed(this, 1000);
                } else {
                    if (status != null) {
                        status.setText("Analysis complete");
                    }
                    setText(R.id.tvSubtext, "Tap Continue to proceed");
                    showAndEnable(R.id.btnContinue);
                    scheduleProceed();
                }
            }
        };
        handler.post(tick);
    }

    private void scheduleProceed() {
        long elapsed = System.currentTimeMillis() - startedAt;
        long delay = Math.max(0L, MIN_WAIT_MS - elapsed);
        handler.postDelayed(this::proceed, delay);
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
