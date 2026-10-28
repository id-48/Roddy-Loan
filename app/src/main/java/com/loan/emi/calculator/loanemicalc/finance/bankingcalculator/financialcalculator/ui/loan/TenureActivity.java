package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

public class TenureActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_tenure);
        setupToolbar("Tenure", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Select Tenure");
        TextView symbol = findViewById(R.id.tvCurrencySymbol);
        if (symbol != null) symbol.setText(AppPrefs.getCurrencySymbol(this));
        SeekBar seek = findViewById(R.id.seekTenure);
        TextView value = findViewById(R.id.tvTenure);
        int min = 6;
        int max = 360;
        seek.setMax(max - min);
        int current = (int) LoanSession.tenureMonths;
        if (current < min) current = 24;
        if (current > max) current = max;
        seek.setProgress(current - min);
        update(value, current);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                update(value, progress + min);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            int progress = seek.getProgress() + min;
            LoanSession.tenureMonths = progress;
            LoanSession.save(this);
            goNext(true);
        });
    }

    private void update(TextView value, int amount) {
        if (value == null) return;
        value.setText(amount + " months");
    }
}
