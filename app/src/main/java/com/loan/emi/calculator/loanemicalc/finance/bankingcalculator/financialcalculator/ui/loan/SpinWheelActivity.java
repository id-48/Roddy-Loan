package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

import java.util.Random;

public class SpinWheelActivity extends BaseActivity {
    private boolean spun;
    private float currentRotation;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_spin_wheel);
        setupProgressHeader("Spin for a loan offer");
        setText(R.id.tvHeadline, "Spin & Win");
        setText(R.id.tvSubtext, "Win loan offer up to 1 Lac, 3 Lac or 5 Lac");

        SpinWheelView wheel = findViewById(R.id.wheelSpinner);
        TextView result = findViewById(R.id.tvPrizeResult);
        findViewById(R.id.btnSpin).setOnClickListener(v -> {
            if (spun) {
                goNext(true);
                return;
            }
            spun = true;
            v.setEnabled(false);
            int targetIndex = new Random().nextInt(SpinWheelView.PRIZES.length);
            float sweep = 360f / SpinWheelView.PRIZES.length;
            // land near center of target slice under top pointer
            float targetAngle = 360f - (targetIndex * sweep + sweep / 2f);
            float extra = 720f + ((targetAngle - currentRotation) % 360f + 360f) % 360f;
            currentRotation += extra;
            if (wheel != null) {
                wheel.animate()
                        .rotation(currentRotation)
                        .setDuration(2800)
                        .setInterpolator(new DecelerateInterpolator())
                        .withEndAction(() -> {
                            String prize = SpinWheelView.PRIZES[targetIndex];
                            if (result != null) {
                                result.setText("You won: " + prize);
                            }
                            applyPrize(prize);
                            setButtonTextNext();
                            handler.postDelayed(() -> goNext(true), 5000);
                        })
                        .start();
            }
        });
    }

    private void applyPrize(String prize) {
        double amount = 100000;
        if (prize.contains("5")) {
            amount = 500000;
        } else if (prize.contains("3")) {
            amount = 300000;
        }
        LoanSession.loanAmount = Math.max(LoanSession.loanAmount, amount);
        LoanSession.spinApplied = true;
        LoanSession.spinDiscountPercent = 0;
        LoanSession.save(this);
    }

    private void setButtonTextNext() {
        android.view.View btn = findViewById(R.id.btnSpin);
        if (btn instanceof TextView) {
            ((TextView) btn).setText("Continue");
            btn.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
