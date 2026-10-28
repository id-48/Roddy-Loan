package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide;

import android.content.Intent;
import android.os.Bundle;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class InsuranceOptionsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_insurance_options);
        setupToolbar("Insurance Options", true);
        findViewById(R.id.cardLifeInsurance).setOnClickListener(v -> open("life_insurance"));
        findViewById(R.id.cardGeneralInsurance).setOnClickListener(v -> open("general_insurance"));
    }
    private void open(String type) {
        Intent i = new Intent(this, SelectionActivity.class);
        i.putExtra("type", type);
        startActivity(i);
    }
}
