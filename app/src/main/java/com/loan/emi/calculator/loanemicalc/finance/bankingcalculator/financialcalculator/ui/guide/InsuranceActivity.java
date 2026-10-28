package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class InsuranceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_insurance);
        setupToolbar("Insurance", true);
        TextView t = findViewById(R.id.tvInsuranceTitle);
        if (t != null) t.setText("Protect What Matters");
        findViewById(R.id.btnGetInsurance).setOnClickListener(v ->
                startActivity(new Intent(this, InsuranceOptionsActivity.class)));
    }
}
