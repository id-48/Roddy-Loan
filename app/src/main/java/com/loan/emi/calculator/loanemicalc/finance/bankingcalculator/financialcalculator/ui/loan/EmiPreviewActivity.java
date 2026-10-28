package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.EmiMath;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class EmiPreviewActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_emi_preview);
        setupToolbar("EMI Preview", true);
        setupProgressHeader();
        if (LoanSession.monthlyEmi <= 0) {
            LoanSession.monthlyEmi = EmiMath.monthlyEmi(LoanSession.loanAmount, LoanSession.interestRate, LoanSession.tenureMonths);
            LoanSession.totalPayable = EmiMath.totalPayable(LoanSession.monthlyEmi, LoanSession.tenureMonths);
        }
        set(R.id.tvHeadline, "Your EMI Estimate");
        set(R.id.tvEmi, MoneyFormat.format(this, LoanSession.monthlyEmi));
        set(R.id.tvPrincipal, MoneyFormat.format(this, LoanSession.loanAmount));
        set(R.id.tvInterest, String.format("%.1f%%", LoanSession.interestRate));
        set(R.id.tvTotal, MoneyFormat.format(this, LoanSession.totalPayable));
        findViewById(R.id.btnApply).setOnClickListener(v -> {
            LoanSession.save(this);
            goNext(true);
        });
    }
    private void set(int id, String t) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(t);
    }
}
