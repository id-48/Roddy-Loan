package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class IncomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_income);
        setupToolbar("Income", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Monthly Income");
        TextView symbol = findViewById(R.id.tvCurrencySymbol);
        if (symbol != null) symbol.setText(AppPrefs.getCurrencySymbol(this));
        SeekBar seek = findViewById(R.id.seekIncome);
        TextView value = findViewById(R.id.tvIncome);
        int min = 10000;
        int max = 500000;
        seek.setMax(max - min);
        int current = (int) LoanSession.monthlyIncome;
        if (current < min) current = 50000;
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
            LoanSession.monthlyIncome = progress;
            LoanSession.save(this);
            goNext(true);
        });
    }

    private void update(TextView value, int amount) {
        if (value == null) return;
        value.setText(MoneyFormat.amountOnly(amount));
    }
}
