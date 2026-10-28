package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

public class RiskAssessmentActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_risk_assessment);
        setupToolbar("Risk Assessment", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Risk Assessment");
        TextView risk = findViewById(R.id.tvRiskLevel);
        String level = LoanSession.monthlyIncome > LoanSession.monthlyExpense * 2 ? "Low Risk" : "Medium Risk";
        if (risk != null) risk.setText(level);
        TextView sub = findViewById(R.id.tvSubtext);
        if (sub != null) sub.setText("Based on your income and expenses");
        findViewById(R.id.btnNext).setOnClickListener(v -> goNext(true));
    }
}
