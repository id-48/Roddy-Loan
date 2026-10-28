package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.EmiMath;

public class InterestRateActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_interest_rate);
        setupToolbar("Interest Rate", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Your Interest Rate");
        double rate = LoanSession.interestRate;
        if (LoanSession.spinApplied && LoanSession.spinDiscountPercent > 0) {
            rate = Math.max(7.5, rate - LoanSession.spinDiscountPercent);
            LoanSession.interestRate = rate;
        }
        TextView tvRate = findViewById(R.id.tvRate);
        if (tvRate != null) tvRate.setText(String.format("%.1f%%", rate));
        TextView oldR = findViewById(R.id.tvOldRate);
        if (oldR != null) oldR.setText("12.5%");
        TextView newR = findViewById(R.id.tvNewRate);
        if (newR != null) newR.setText(String.format("%.1f%%", rate));
        findViewById(R.id.btnCalculate).setOnClickListener(v -> {
            LoanSession.monthlyEmi = EmiMath.monthlyEmi(LoanSession.loanAmount, LoanSession.interestRate, LoanSession.tenureMonths);
            LoanSession.totalPayable = EmiMath.totalPayable(LoanSession.monthlyEmi, LoanSession.tenureMonths);
            LoanSession.save(this);
            goNext(true);
        });
    }
}
